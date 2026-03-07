package com.annabenson.viand.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annabenson.viand.BuildConfig;
import com.annabenson.viand.adapters.CustomRecipeAdapter;
import com.annabenson.viand.adapters.RecipeAdapter;
import com.annabenson.viand.adapters.RecipeFavoriteAdapter;
import com.annabenson.viand.data.DatabaseHandler;
import com.annabenson.viand.models.CustomRecipe;
import com.annabenson.viand.models.Recipe;
import com.annabenson.viand.network.RecipeSearchResponse;
import com.annabenson.viand.network.RetrofitClient;
import com.annabenson.viand.network.SpoonacularService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private TextView greetingText;
    private Button signOutButton;
    private Button accountSettingsButton;
    private Button tasteProfileButton;
    private EditText searchQueryInput;
    private Button searchButton;
    private Button vivianButton;
    private Button mealPlanButton;
    private RecyclerView searchResultsRecyclerView;
    private TextView searchResultsLabel;
    private LinearLayout favoritesContainer;
    private RecyclerView customRecipesRecyclerView;

    private RecipeAdapter recipeAdapter;
    private CustomRecipeAdapter customRecipeAdapter;
    private List<Recipe> searchResults = new ArrayList<>();
    private List<Recipe> favorites = new ArrayList<>();
    private List<CustomRecipe> customRecipes = new ArrayList<>();

    private SpoonacularService spoonacularService;
    private DatabaseHandler databaseHandler;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.annabenson.viand.R.layout.activity_home);

        greetingText          = findViewById(com.annabenson.viand.R.id.greetingText);
        signOutButton         = findViewById(com.annabenson.viand.R.id.signOutButton);
        accountSettingsButton = findViewById(com.annabenson.viand.R.id.accountSettingsButton);
        tasteProfileButton    = findViewById(com.annabenson.viand.R.id.tasteProfileButton);
        searchQueryInput     = findViewById(com.annabenson.viand.R.id.searchQueryInput);
        searchButton         = findViewById(com.annabenson.viand.R.id.searchButton);
        vivianButton         = findViewById(com.annabenson.viand.R.id.vivianButton);
        mealPlanButton       = findViewById(com.annabenson.viand.R.id.mealPlanButton);
        searchResultsRecyclerView = findViewById(com.annabenson.viand.R.id.searchResultsRecyclerView);
        searchResultsLabel   = findViewById(com.annabenson.viand.R.id.searchResultsLabel);
        favoritesContainer   = findViewById(com.annabenson.viand.R.id.favoritesContainer);
        customRecipesRecyclerView = findViewById(com.annabenson.viand.R.id.customRecipesRecyclerView);

        String name = getIntent().getStringExtra("USER_NAME");
        if (name == null || name.isEmpty()) {
            name = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                    .getString(LoginActivity.KEY_USER_NAME, "");
        }
        greetingText.setText("Hello, " + (name.isEmpty() ? "there" : name) + "!");

        databaseHandler = new DatabaseHandler(this);
        spoonacularService = RetrofitClient.getInstance().create(SpoonacularService.class);
        currentUserId = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                .getInt(LoginActivity.KEY_USER_ID, -1);

        signOutButton.setOnClickListener(v -> {
            getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        accountSettingsButton.setOnClickListener(v ->
                startActivity(new Intent(this, AccountSettingsActivity.class)));

        tasteProfileButton.setOnClickListener(v ->
                startActivity(new Intent(this, TasteProfileActivity.class)));

        vivianButton.setOnClickListener(v ->
                startActivity(new Intent(this, VivianActivity.class)));

        mealPlanButton.setOnClickListener(v ->
                startActivity(new Intent(this, MealPlanActivity.class)));

        searchButton.setOnClickListener(v -> performSearch());

        searchQueryInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { performSearch(); return true; }
            return false;
        });

        setupSearchResultsRecyclerView();
        setupCustomRecipesRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseHandler.seedTestDataIfNeeded(currentUserId);
        fetchSeedDataIfNeeded();
        loadFavorites();
        loadCustomRecipes();
    }

    // In DEBUG builds, fetches real Spoonacular data for any seeded (negative-ID)
    // favorites and updates the DB. Runs once per user via a SharedPrefs flag.
    private void fetchSeedDataIfNeeded() {
        if (!BuildConfig.DEBUG) return;
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        String flagKey = "seed_fetched_" + currentUserId;
        if (prefs.getBoolean(flagKey, false)) return;

        List<Recipe> seeded = new ArrayList<>();
        for (Recipe r : databaseHandler.loadFavorites(currentUserId)) {
            if (r.getId() < 0) seeded.add(r);
        }
        if (seeded.isEmpty()) {
            prefs.edit().putBoolean(flagKey, true).apply();
            return;
        }

        int[] remaining = {seeded.size()};
        boolean[] anyFailed = {false};
        for (Recipe seed : seeded) {
            int fakeId = seed.getId();
            spoonacularService.searchRecipes(seed.getTitle(), 1, BuildConfig.SPOONACULAR_KEY)
                    .enqueue(new Callback<RecipeSearchResponse>() {
                        @Override
                        public void onResponse(Call<RecipeSearchResponse> call,
                                               Response<RecipeSearchResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Recipe> results = response.body().getResults();
                                if (results != null && !results.isEmpty()) {
                                    Recipe real = results.get(0);
                                    databaseHandler.updateSeedRecipe(currentUserId, fakeId,
                                            real.getId(), real.getTitle(), real.getImage());
                                }
                            } else {
                                anyFailed[0] = true;
                            }
                            if (--remaining[0] == 0) {
                                if (!anyFailed[0]) prefs.edit().putBoolean(flagKey, true).apply();
                                loadFavorites();
                            }
                        }

                        @Override
                        public void onFailure(Call<RecipeSearchResponse> call, Throwable t) {
                            anyFailed[0] = true;
                            if (--remaining[0] == 0) loadFavorites();
                        }
                    });
        }
    }

    // ── Search results ────────────────────────────────────────────────────────

    private void setupSearchResultsRecyclerView() {
        recipeAdapter = new RecipeAdapter(this, searchResults);
        searchResultsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        searchResultsRecyclerView.setAdapter(recipeAdapter);
    }

    // ── Favorites — grouped by meal type ─────────────────────────────────────

    private void loadFavorites() {
        favorites.clear();
        favorites.addAll(databaseHandler.loadFavorites(currentUserId));
        buildFavoritesSections();
    }

    private void buildFavoritesSections() {
        favoritesContainer.removeAllViews();

        // Group into ordered buckets
        Map<String, List<Recipe>> grouped = new LinkedHashMap<>();
        for (String type : RecipeFavoriteAdapter.MEAL_TYPES) {
            grouped.put(type, new ArrayList<>());
        }
        for (Recipe r : favorites) {
            String mt = r.getMealType();
            if (mt == null || !grouped.containsKey(mt)) mt = "Other";
            grouped.get(mt).add(r);
        }

        boolean anySections = false;
        for (String type : RecipeFavoriteAdapter.MEAL_TYPES) {
            List<Recipe> group = grouped.get(type);
            if (group == null || group.isEmpty()) continue;
            anySections = true;

            // Section sub-header
            TextView header = new TextView(this);
            header.setText(type);
            header.setTypeface(null, Typeface.BOLD);
            header.setTextSize(14f);
            int hPad = dp(12);
            header.setPadding(hPad, dp(10), hPad, dp(2));
            favoritesContainer.addView(header);

            // Horizontal RecyclerView for this group
            RecyclerView rv = new RecyclerView(this);
            LinearLayout.LayoutParams rvParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(260));
            rv.setLayoutParams(rvParams);
            rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rv.setPadding(dp(8), 0, dp(8), 0);
            rv.setClipToPadding(false);

            final List<Recipe> groupList = group; // effectively final for lambda
            RecipeFavoriteAdapter adapter = new RecipeFavoriteAdapter(
                    this,
                    groupList,
                    (recipe, position) -> {
                        databaseHandler.deleteFavorite(currentUserId, recipe.getId());
                        loadFavorites(); // rebuild all sections
                    },
                    (recipe, rating) -> databaseHandler.updateFavoriteRating(currentUserId, recipe.getId(), rating),
                    (recipe, mealType) -> {
                        databaseHandler.updateFavoriteMealType(currentUserId, recipe.getId(), mealType);
                        loadFavorites(); // move card to the correct section
                    }
            );
            rv.setAdapter(adapter);
            favoritesContainer.addView(rv);
        }

        if (!anySections) {
            TextView empty = new TextView(this);
            empty.setText("No saved favorites yet.");
            empty.setTextSize(13f);
            int p = dp(12);
            empty.setPadding(p, p, p, p);
            favoritesContainer.addView(empty);
        }
    }

    // ── My Recipes ────────────────────────────────────────────────────────────

    private void setupCustomRecipesRecyclerView() {
        customRecipeAdapter = new CustomRecipeAdapter(this, customRecipes);
        customRecipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        customRecipesRecyclerView.setAdapter(customRecipeAdapter);
        customRecipesRecyclerView.setNestedScrollingEnabled(false);
    }

    private void loadCustomRecipes() {
        List<CustomRecipe> loaded = databaseHandler.loadCustomRecipes(currentUserId);
        customRecipes.clear();
        customRecipes.addAll(loaded);
        customRecipeAdapter.notifyDataSetChanged();
    }

    // ── Search ────────────────────────────────────────────────────────────────

    private void performSearch() {
        String query = searchQueryInput.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Enter a search term", Toast.LENGTH_SHORT).show();
            return;
        }
        spoonacularService.searchRecipes(query, 10, BuildConfig.SPOONACULAR_KEY)
                .enqueue(new Callback<RecipeSearchResponse>() {
                    @Override
                    public void onResponse(Call<RecipeSearchResponse> call,
                                           Response<RecipeSearchResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Recipe> results = response.body().getResults();
                            searchResults.clear();
                            if (results != null) searchResults.addAll(results);
                            recipeAdapter.notifyDataSetChanged();
                            searchResultsLabel.setVisibility(View.VISIBLE);
                            searchResultsRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(HomeActivity.this,
                                    "Search failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<RecipeSearchResponse> call, Throwable t) {
                        Toast.makeText(HomeActivity.this,
                                "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Util ──────────────────────────────────────────────────────────────────

    private int dp(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
