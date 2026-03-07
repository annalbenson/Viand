package com.annabenson.viand.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annabenson.viand.BuildConfig;
import com.annabenson.viand.adapters.ChatAdapter;
import com.annabenson.viand.data.DatabaseHandler;
import com.annabenson.viand.engine.TasteEngine;
import com.annabenson.viand.models.ChatMessage;
import com.annabenson.viand.models.MealLogEntry;
import com.annabenson.viand.models.Recipe;
import com.annabenson.viand.models.RecommendationSet;
import com.annabenson.viand.models.TasteTag;
import com.annabenson.viand.network.GeminiClient;
import com.annabenson.viand.network.GeminiRequest;
import com.annabenson.viand.network.GeminiResponse;
import com.annabenson.viand.network.GeminiService;
import com.annabenson.viand.network.RecipeSearchResponse;
import com.annabenson.viand.network.RetrofitClient;
import com.annabenson.viand.network.SpoonacularService;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VivianActivity extends AppCompatActivity
        implements ChatAdapter.PreferenceResponseListener {

    // Set to true to use Spoonacular test responses instead of Gemini
    private static final boolean TEST_MODE = BuildConfig.DEBUG;

    private static final String SYSTEM_PROMPT =
            "You are Vivian, a friendly cooking assistant. " +
            "When the user tells you what ingredients they have, suggest practical recipes they can make. " +
            "Give each suggestion a name, a one-sentence description, and 2-3 key steps. " +
            "If the user asks follow-up questions about a recipe, give more detail. " +
            "If they're missing a key ingredient, suggest a simple substitution. " +
            "Keep responses warm, concise, and focused on cooking.";

    private RecyclerView chatRecyclerView;
    private TextInputEditText messageInput;
    private Button sendButton;

    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messages = new ArrayList<>();
    private final List<GeminiRequest.Content> conversationHistory = new ArrayList<>();

    private GeminiService geminiService;
    private SpoonacularService spoonacularService;
    private DatabaseHandler databaseHandler;
    private boolean isWaiting = false;
    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.annabenson.viand.R.layout.activity_vivian);

        Toolbar toolbar = findViewById(com.annabenson.viand.R.id.vivianToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        chatRecyclerView = findViewById(com.annabenson.viand.R.id.chatRecyclerView);
        messageInput = findViewById(com.annabenson.viand.R.id.messageInput);
        sendButton = findViewById(com.annabenson.viand.R.id.sendButton);

        geminiService = GeminiClient.getInstance().create(GeminiService.class);
        spoonacularService = RetrofitClient.getInstance().create(SpoonacularService.class);
        databaseHandler = new DatabaseHandler(this);
        userId = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                .getInt(LoginActivity.KEY_USER_ID, -1);

        chatAdapter = new ChatAdapter(messages, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        String opening = TEST_MODE
                ? "Hi, I'm Vivian! Tell me what you're in the mood for, or ask me to recommend something!"
                : "Hi, I'm Vivian! Tell me what ingredients you have in your pantry or fridge and I'll suggest some recipes you can make.";
        addAiMessage(opening);

        sendButton.setOnClickListener(v -> sendMessage());

        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    // ── PreferenceResponseListener ─────────────────────────────────────────────

    @Override
    public void onPreferenceResponse(String topic, int points) {
        Log.d("Vivian", "Preference response: topic=" + topic + ", points=" + points);
        String cuisineName = mapTopicToCuisine(topic);
        if (cuisineName != null) {
            databaseHandler.upsertTasteScore(userId, cuisineName, "cuisine", points);
        } else {
            databaseHandler.upsertTasteScore(userId, topic, "ingredient", points);
        }
    }

    private String mapTopicToCuisine(String topic) {
        switch (topic) {
            case "Italian food":  return "Italian";
            case "Japanese food": return "Japanese";
            case "Mexican food":  return "Mexican";
            case "Thai food":     return "Thai";
            case "Indian food":   return "Indian";
            case "Chinese food":  return "Chinese";
            default:              return null;
        }
    }

    // ── Message Sending ────────────────────────────────────────────────────────

    private void sendMessage() {
        if (isWaiting) return;

        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) return;

        messageInput.setText("");

        messages.add(new ChatMessage(ChatMessage.Type.USER, text));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();

        // Keyword detection for meal history queries
        String lower = text.toLowerCase();
        if (lower.contains("what did i eat") || lower.contains("what did i make") ||
                lower.contains("what did i cook") || lower.contains("what have i been making") ||
                lower.contains("what i ate") || lower.contains("what i've made") ||
                lower.contains("meal history") || lower.contains("my meals") ||
                lower.contains("what i've been eating") || lower.contains("what i cooked")) {
            handleMealHistoryQuery(lower);
            return;
        }

        // Keyword detection for ingredient-based suggestions
        if ((lower.contains("suggest") || lower.contains("what can i") ||
                lower.contains("recipe") || lower.contains("something") ||
                lower.contains("cook") || lower.contains("make")) &&
                (lower.contains(" with ") || lower.contains("using "))) {
            String ingredient = extractIngredient(lower);
            if (ingredient != null) {
                handleIngredientSuggestion(ingredient);
                return;
            }
        }

        // Keyword detection for recommendation requests
        if (lower.contains("what sounds good") || lower.contains("what should i make") ||
                lower.contains("what's for") || lower.contains("recommend") ||
                lower.contains("i'm hungry") || lower.contains("help me decide")) {
            handleRecommendationRequest();
            return;
        }

        // Guard: only pass food-related messages to the AI
        if (!isFoodRelated(lower)) {
            addAiMessage("I'm just a cooking assistant, so I can only help with recipes, " +
                    "ingredients, and meal planning. What are you in the mood to cook?");
            return;
        }

        ChatMessage loadingMsg = new ChatMessage(ChatMessage.Type.LOADING, "");
        messages.add(loadingMsg);
        int loadingIndex = messages.size() - 1;
        chatAdapter.notifyItemInserted(loadingIndex);
        scrollToBottom();

        setWaiting(true);

        if (TEST_MODE) {
            sendTestModeRequest(text, loadingIndex);
        } else {
            sendGeminiRequest(text, loadingIndex);
        }
    }

    private boolean isFoodRelated(String lower) {
        String[] foodTerms = {
            // actions
            "cook", "bake", "fry", "roast", "boil", "grill", "saute", "sauté", "steam",
            "simmer", "chop", "marinate", "season", "blend", "stir", "broil", "poach",
            // meal concepts
            "recipe", "meal", "dish", "food", "eat", "ate", "hungry", "dinner", "lunch",
            "breakfast", "brunch", "snack", "dessert", "appetizer", "cuisine", "ingredient",
            "leftovers", "pantry", "fridge", "freezer", "portion", "serving",
            // common ingredients
            "chicken", "beef", "pork", "fish", "shrimp", "salmon", "turkey", "lamb",
            "pasta", "rice", "bread", "noodle", "egg", "tofu", "bean", "lentil",
            "vegetable", "veggie", "fruit", "salad", "soup", "sauce", "stew", "curry",
            "cheese", "milk", "butter", "cream", "flour", "sugar", "honey",
            "garlic", "onion", "tomato", "potato", "pepper", "herb", "spice",
            // taste / diet
            "spicy", "sweet", "savory", "salty", "healthy", "vegan", "vegetarian",
            "gluten", "keto", "dairy", "calorie", "nutrition", "diet", "flavor", "taste"
        };
        for (String term : foodTerms) {
            if (lower.contains(term)) return true;
        }
        return false;
    }

    // ── Recommendation Flow ────────────────────────────────────────────────────

    private void handleRecommendationRequest() {
        // Show loading indicator
        ChatMessage loadingMsg = new ChatMessage(ChatMessage.Type.LOADING, "");
        messages.add(loadingMsg);
        final int loadingIndex = messages.size() - 1;
        chatAdapter.notifyItemInserted(loadingIndex);
        scrollToBottom();

        setWaiting(true);

        // Derive dietary filters from the user's account preferences
        String prefsStr = databaseHandler.getDietaryPreferences(userId);
        String[] filters = deriveDietaryFilters(prefsStr);
        final String diet = filters[0];
        final String intolerances = filters[1];

        // Pick goTo recipe from saved favorites (random)
        List<Recipe> favorites = databaseHandler.loadFavorites(userId);
        final Recipe goToRecipe = favorites.isEmpty()
                ? null : favorites.get(new Random().nextInt(favorites.size()));

        // Determine cuisines from taste profile
        List<TasteTag> profile = databaseHandler.loadCuisineProfile(userId);
        String topCuisine = TasteEngine.getTopCuisine(profile);

        final String similarCuisine;
        final String adventurousCuisine;
        if (topCuisine == null) {
            similarCuisine = "Italian";
            adventurousCuisine = "Thai";
        } else {
            List<String> similar = TasteEngine.getSimilarCuisines(topCuisine);
            List<String> adventurous = TasteEngine.getAdventurousCuisines(topCuisine);
            similarCuisine = similar.isEmpty() ? "Italian" : similar.get(0);
            adventurousCuisine = adventurous.isEmpty() ? "Thai" : adventurous.get(0);
        }

        // Two parallel Spoonacular calls; use counter to know when both finish
        final Recipe[] similarRecipe = {null};
        final Recipe[] adventurousRecipe = {null};
        final int[] pending = {2};

        Runnable onBothDone = () -> {
            setWaiting(false);
            messages.remove(loadingIndex);
            chatAdapter.notifyItemRemoved(loadingIndex);

            RecommendationSet set = new RecommendationSet(goToRecipe, similarRecipe[0], adventurousRecipe[0]);
            messages.add(new ChatMessage(set));
            chatAdapter.notifyItemInserted(messages.size() - 1);
            scrollToBottom();

            // 40% chance to inject a preference prompt
            if (Math.random() < 0.4) {
                String topic = TasteEngine.getNextPromptTopic(databaseHandler, userId);
                if (topic != null) {
                    databaseHandler.upsertPromptLog(userId, topic,
                            System.currentTimeMillis() / 1000);
                    messages.add(new ChatMessage(ChatMessage.Type.PREFERENCE_PROMPT,
                            "Do you like " + topic + "?", topic));
                    chatAdapter.notifyItemInserted(messages.size() - 1);
                    scrollToBottom();
                }
            }
        };

        spoonacularService.searchRecipesFiltered(similarCuisine, 3, similarCuisine, true,
                diet, intolerances, BuildConfig.SPOONACULAR_KEY)
                .enqueue(new Callback<RecipeSearchResponse>() {
                    @Override
                    public void onResponse(Call<RecipeSearchResponse> call,
                                           Response<RecipeSearchResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Recipe> results = response.body().getResults();
                            if (results != null && !results.isEmpty()) {
                                similarRecipe[0] = results.get(0);
                            }
                        }
                        if (--pending[0] == 0) onBothDone.run();
                    }

                    @Override
                    public void onFailure(Call<RecipeSearchResponse> call, Throwable t) {
                        if (--pending[0] == 0) onBothDone.run();
                    }
                });

        spoonacularService.searchRecipesFiltered(adventurousCuisine, 3, adventurousCuisine, true,
                diet, intolerances, BuildConfig.SPOONACULAR_KEY)
                .enqueue(new Callback<RecipeSearchResponse>() {
                    @Override
                    public void onResponse(Call<RecipeSearchResponse> call,
                                           Response<RecipeSearchResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Recipe> results = response.body().getResults();
                            if (results != null && !results.isEmpty()) {
                                adventurousRecipe[0] = results.get(0);
                            }
                        }
                        if (--pending[0] == 0) onBothDone.run();
                    }

                    @Override
                    public void onFailure(Call<RecipeSearchResponse> call, Throwable t) {
                        if (--pending[0] == 0) onBothDone.run();
                    }
                });
    }

    // Maps dietary preference strings to Spoonacular diet + intolerances params.
    // Returns [diet, intolerances] — either may be null if not applicable.
    private String[] deriveDietaryFilters(String prefsStr) {
        if (prefsStr == null || prefsStr.isEmpty()) return new String[]{null, null};
        Set<String> prefs = new HashSet<>(Arrays.asList(prefsStr.split(",")));

        // Spoonacular's diet param: pick strictest applicable
        String diet = null;
        if (prefs.contains("Vegan")) diet = "vegan";
        else if (prefs.contains("Vegetarian")) diet = "vegetarian";

        // Spoonacular's intolerances param: comma-separated
        List<String> intolerances = new ArrayList<>();
        if (prefs.contains("Gluten Free")) intolerances.add("gluten");
        if (prefs.contains("Dairy Free"))  intolerances.add("dairy");
        if (prefs.contains("Nut Free"))    { intolerances.add("peanut"); intolerances.add("tree nut"); }

        return new String[]{diet, intolerances.isEmpty() ? null : String.join(",", intolerances)};
    }

    // ── Test / Gemini Mode ─────────────────────────────────────────────────────

    private static final String[][] TEST_QUERIES = {
        // Breakfast
        { "avocado toast", "fluffy pancakes", "breakfast burrito", "overnight oats", "eggs benedict",
          "french toast", "acai bowl", "breakfast sandwich", "shakshuka", "granola parfait",
          "huevos rancheros", "smoothie bowl", "waffles", "crepes", "breakfast frittata",
          "bagel with lox", "chia pudding", "dutch baby pancake", "breakfast hash", "omelette",
          "biscuits and gravy", "banana bread", "muffins", "porridge", "breakfast quesadilla" },
        // Lunch
        { "chicken caesar salad", "grilled cheese sandwich", "tomato soup", "BLT wrap", "quinoa bowl",
          "greek salad", "tuna melt", "lentil soup", "caprese panini", "falafel wrap",
          "nicoise salad", "pho", "ramen", "chicken noodle soup", "veggie burger",
          "banh mi", "cobb salad", "minestrone soup", "fish tacos", "grain bowl",
          "turkey club sandwich", "gazpacho", "pad thai", "miso soup with tofu", "lobster bisque" },
        // Dinner
        { "spaghetti bolognese", "chicken stir fry", "beef tacos", "salmon with vegetables", "chicken tikka masala",
          "shrimp fried rice", "beef stew", "lamb chops", "mushroom risotto", "pork tenderloin",
          "vegetable curry", "chicken parmesan", "beef burgers", "lobster thermidor", "duck confit",
          "pasta carbonara", "chicken fajitas", "grilled halibut", "stuffed peppers", "lemon herb roast chicken",
          "beef bulgogi", "mussels in white wine", "eggplant parmesan", "short ribs", "paella" },
        // Dessert
        { "chocolate chip cookies", "cheesecake", "brownies", "apple pie", "tiramisu",
          "creme brulee", "lemon tart", "chocolate mousse", "red velvet cake", "baklava",
          "panna cotta", "churros", "bread pudding", "macarons", "peach cobbler",
          "mochi ice cream", "strawberry shortcake", "chocolate lava cake", "tres leches cake", "key lime pie",
          "beignets", "cannoli", "profiteroles", "affogato", "banana foster" },
        // Snack
        { "hummus and pita", "guacamole", "deviled eggs", "bruschetta", "caprese salad",
          "stuffed mushrooms", "spinach artichoke dip", "cheese board", "spring rolls", "nachos",
          "edamame", "antipasto platter", "baked brie", "tzatziki with vegetables", "potato skins",
          "chicken wings", "shrimp cocktail", "pigs in a blanket", "prosciutto wrapped melon", "quesadillas",
          "buffalo cauliflower", "loaded sweet potato fries", "smoked salmon crostini", "mini sliders", "wonton cups" },
    };

    private String pickTestQuery(String userText) {
        String lower = userText.toLowerCase();
        int category;
        if (lower.contains("breakfast") || lower.contains("morning"))      category = 0;
        else if (lower.contains("lunch"))                                  category = 1;
        else if (lower.contains("dinner") || lower.contains("supper"))     category = 2;
        else if (lower.contains("dessert") || lower.contains("sweet"))     category = 3;
        else if (lower.contains("snack"))                                  category = 4;
        else category = new Random().nextInt(TEST_QUERIES.length);
        String[] pool = TEST_QUERIES[category];
        return pool[new Random().nextInt(pool.length)];
    }

    private void sendTestModeRequest(String userText, int loadingIndex) {
        String query = pickTestQuery(userText);
        spoonacularService.searchRecipes(query, 5, BuildConfig.SPOONACULAR_KEY)
                .enqueue(new Callback<RecipeSearchResponse>() {
                    @Override
                    public void onResponse(Call<RecipeSearchResponse> call,
                                           Response<RecipeSearchResponse> response) {
                        setWaiting(false);
                        messages.remove(loadingIndex);
                        chatAdapter.notifyItemRemoved(loadingIndex);

                        if (response.isSuccessful() && response.body() != null) {
                            List<Recipe> results = response.body().getResults();
                            if (results == null || results.isEmpty()) {
                                addAiMessage("Test mode: Spoonacular returned no results.");
                                return;
                            }
                            messages.add(new ChatMessage(results));
                            chatAdapter.notifyItemInserted(messages.size() - 1);
                            scrollToBottom();
                        } else {
                            addAiMessage("Test mode: Spoonacular error " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<RecipeSearchResponse> call, Throwable t) {
                        setWaiting(false);
                        messages.remove(loadingIndex);
                        chatAdapter.notifyItemRemoved(loadingIndex);
                        addAiMessage("Test mode: Network error: " + t.getMessage());
                    }
                });
    }

    private void sendGeminiRequest(String text, int loadingIndex) {
        conversationHistory.add(new GeminiRequest.Content("user",
                Collections.singletonList(new GeminiRequest.Part(text))));

        GeminiRequest request = new GeminiRequest(SYSTEM_PROMPT,
                new ArrayList<>(conversationHistory));

        geminiService.generateContent(BuildConfig.GEMINI_KEY, request)
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(Call<GeminiResponse> call,
                                           Response<GeminiResponse> response) {
                        setWaiting(false);
                        messages.remove(loadingIndex);
                        chatAdapter.notifyItemRemoved(loadingIndex);

                        if (response.isSuccessful() && response.body() != null) {
                            String reply = response.body().getText();
                            if (reply == null || reply.isEmpty()) reply = "Sorry, I couldn't generate a response.";
                            addAiMessage(reply);
                            conversationHistory.add(new GeminiRequest.Content("model",
                                    Collections.singletonList(new GeminiRequest.Part(reply))));
                        } else {
                            String errorDetail = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorDetail = response.errorBody().string();
                                }
                            } catch (IOException e) {
                                errorDetail = e.getMessage();
                            }
                            Log.e("Vivian", "API error " + response.code() + ": " + errorDetail);
                            addAiMessage("Error " + response.code() + ": " + errorDetail);
                        }
                    }

                    @Override
                    public void onFailure(Call<GeminiResponse> call, Throwable t) {
                        setWaiting(false);
                        messages.remove(loadingIndex);
                        chatAdapter.notifyItemRemoved(loadingIndex);
                        addAiMessage("Network error: " + t.getMessage());
                    }
                });
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void setWaiting(boolean waiting) {
        isWaiting = waiting;
        sendButton.setEnabled(!waiting);
    }

    private void addAiMessage(String text) {
        messages.add(new ChatMessage(ChatMessage.Type.AI, text));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        chatRecyclerView.post(() ->
                chatRecyclerView.smoothScrollToPosition(messages.size() - 1));
    }

    // ── Ingredient Suggestions ─────────────────────────────────────────────────

    private String extractIngredient(String lower) {
        int idx = lower.indexOf(" with ");
        if (idx >= 0) {
            String after = lower.substring(idx + 6).trim();
            return after.isEmpty() ? null : after;
        }
        idx = lower.indexOf("using ");
        if (idx >= 0) {
            String after = lower.substring(idx + 6).trim();
            return after.isEmpty() ? null : after;
        }
        return null;
    }

    private void handleIngredientSuggestion(String ingredient) {
        ChatMessage loadingMsg = new ChatMessage(ChatMessage.Type.LOADING, "");
        messages.add(loadingMsg);
        final int loadingIndex = messages.size() - 1;
        chatAdapter.notifyItemInserted(loadingIndex);
        scrollToBottom();

        setWaiting(true);

        spoonacularService.searchRecipes(ingredient, 5, BuildConfig.SPOONACULAR_KEY)
                .enqueue(new Callback<RecipeSearchResponse>() {
                    @Override
                    public void onResponse(Call<RecipeSearchResponse> call,
                                           Response<RecipeSearchResponse> response) {
                        setWaiting(false);
                        messages.remove(loadingIndex);
                        chatAdapter.notifyItemRemoved(loadingIndex);

                        if (response.isSuccessful() && response.body() != null) {
                            List<Recipe> results = response.body().getResults();
                            if (results != null && !results.isEmpty()) {
                                messages.add(new ChatMessage(results));
                                chatAdapter.notifyItemInserted(messages.size() - 1);
                                scrollToBottom();
                            } else {
                                addAiMessage("I couldn't find any recipes with \"" + ingredient +
                                        "\". Try a different ingredient?");
                            }
                        } else {
                            addAiMessage("Couldn't reach the recipe search right now. Try again in a moment!");
                        }
                    }

                    @Override
                    public void onFailure(Call<RecipeSearchResponse> call, Throwable t) {
                        setWaiting(false);
                        messages.remove(loadingIndex);
                        chatAdapter.notifyItemRemoved(loadingIndex);
                        addAiMessage("Network error: " + t.getMessage());
                    }
                });
    }

    // ── Meal History ───────────────────────────────────────────────────────────

    private void handleMealHistoryQuery(String lower) {
        int daysBack = lower.contains("last month") ? 30 : 7;
        boolean likedOnly = lower.contains("liked") || lower.contains("loved") || lower.contains("enjoyed");

        List<MealLogEntry> meals = databaseHandler.loadRecentMeals(userId, daysBack);

        if (likedOnly) {
            List<MealLogEntry> filtered = new ArrayList<>();
            for (MealLogEntry entry : meals) {
                if (entry.rating != null && !entry.rating.equalsIgnoreCase("disliked")) {
                    filtered.add(entry);
                }
            }
            meals = filtered;
        }

        addAiMessage(formatMealHistoryResponse(meals));
    }

    private String formatMealHistoryResponse(List<MealLogEntry> meals) {
        if (meals.isEmpty()) {
            return "I don't see any meals logged yet! Next time you cook a recipe, " +
                    "tap \"I Made This!\" on the recipe page and I'll remember it for you.";
        }

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("EEE, MMM d", java.util.Locale.getDefault());
        StringBuilder sb = new StringBuilder("Here's what you've been cooking:\n\n");

        for (MealLogEntry entry : meals) {
            sb.append("• ").append(entry.recipeTitle);
            if (entry.mealType != null && !entry.mealType.equals("Other")) {
                sb.append(" (").append(entry.mealType).append(")");
            }
            if (entry.rating != null) {
                if (entry.rating.equalsIgnoreCase("disliked")) {
                    sb.append(" ✗");
                } else {
                    sb.append(" ★");
                }
            }
            String dateStr = sdf.format(new java.util.Date(entry.madeAt * 1000));
            sb.append(" — ").append(dateStr).append("\n");
        }

        if (meals.size() >= 3) {
            sb.append("\nLooks like you've been busy in the kitchen! Keep it up!");
        }

        return sb.toString().trim();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
