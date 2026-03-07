package com.annabenson.viand.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import com.annabenson.viand.BuildConfig;
import com.annabenson.viand.data.DatabaseHandler;
import com.annabenson.viand.models.Ingredient;
import com.annabenson.viand.models.RecipeDetail;
import com.annabenson.viand.network.RetrofitClient;
import com.annabenson.viand.network.SpoonacularService;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeDetailActivity extends AppCompatActivity {

    private static final String TAG = "RecipeDetailActivity";

    private ImageView detailImage;
    private TextView detailTitle;
    private ChipGroup tagsChipGroup;
    private LinearLayout ingredientsContainer;
    private TextView instructionsText;
    private Button saveToFavoritesButton;
    private Button createMyVersionButton;

    private SpoonacularService spoonacularService;
    private DatabaseHandler databaseHandler;

    private int recipeId;
    private RecipeDetail currentDetail;
    private boolean fromRecommendation;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.annabenson.viand.R.layout.activity_recipe_detail);

        detailImage = findViewById(com.annabenson.viand.R.id.detailImage);
        detailTitle = findViewById(com.annabenson.viand.R.id.detailTitle);
        tagsChipGroup = findViewById(com.annabenson.viand.R.id.tagsChipGroup);
        ingredientsContainer = findViewById(com.annabenson.viand.R.id.ingredientsContainer);
        instructionsText = findViewById(com.annabenson.viand.R.id.instructionsText);
        saveToFavoritesButton = findViewById(com.annabenson.viand.R.id.saveToFavoritesButton);
        createMyVersionButton = findViewById(com.annabenson.viand.R.id.createMyVersionButton);

        ImageButton backButton = findViewById(com.annabenson.viand.R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        databaseHandler = new DatabaseHandler(this);
        spoonacularService = RetrofitClient.getInstance().create(SpoonacularService.class);
        userEmail = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                .getString(LoginActivity.KEY_EMAIL, "");

        recipeId = getIntent().getIntExtra("RECIPE_ID", -1);
        fromRecommendation = getIntent().getBooleanExtra("FROM_RECOMMENDATION", false);

        String recipeTitle = getIntent().getStringExtra("RECIPE_TITLE");
        if (recipeTitle != null) {
            detailTitle.setText(recipeTitle);
        }

        if (recipeId != -1) {
            fetchRecipeDetail(recipeId);
        } else {
            Toast.makeText(this, "Invalid recipe", Toast.LENGTH_SHORT).show();
            finish();
        }

        saveToFavoritesButton.setOnClickListener(v -> {
            if (currentDetail != null) {
                String mealType = detectMealType(currentDetail.getDishTypes());
                databaseHandler.addFavorite(
                        currentDetail.getId(),
                        currentDetail.getTitle(),
                        currentDetail.getImage(),
                        mealType
                );

                // Award +1 per cuisine, +0.5 per ingredient (top 5)
                awardTastePointsOnSave(currentDetail);

                Toast.makeText(RecipeDetailActivity.this,
                        "Saved to favorites!", Toast.LENGTH_SHORT).show();
                saveToFavoritesButton.setEnabled(false);
                saveToFavoritesButton.setText("Saved");
            }
        });

        createMyVersionButton.setOnClickListener(v -> {
            if (currentDetail != null) {
                Intent intent = new Intent(RecipeDetailActivity.this, CustomRecipeActivity.class);
                intent.putExtra("CUSTOM_TITLE", currentDetail.getTitle());
                intent.putExtra("CUSTOM_INGREDIENTS", buildIngredientsText(currentDetail));
                intent.putExtra("CUSTOM_INSTRUCTIONS", buildInstructionsText(currentDetail));
                startActivity(intent);
            }
        });
    }

    private void fetchRecipeDetail(int id) {
        spoonacularService.getRecipeDetail(id, BuildConfig.SPOONACULAR_KEY)
                .enqueue(new Callback<RecipeDetail>() {
                    @Override
                    public void onResponse(Call<RecipeDetail> call,
                                           Response<RecipeDetail> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            currentDetail = response.body();
                            populateUI(currentDetail);

                            // Award taste points for opening from recommendation
                            if (fromRecommendation && currentDetail.getCuisines() != null) {
                                for (String cuisine : currentDetail.getCuisines()) {
                                    Log.d(TAG, "Recommendation open: +0.5 for " + cuisine);
                                    databaseHandler.upsertTasteScore(userEmail, cuisine, "cuisine", 0.5f);
                                }
                            }
                        } else {
                            Toast.makeText(RecipeDetailActivity.this,
                                    "Failed to load recipe: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RecipeDetail> call, Throwable t) {
                        Toast.makeText(RecipeDetailActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void awardTastePointsOnSave(RecipeDetail detail) {
        // +1 per cuisine
        if (detail.getCuisines() != null) {
            for (String cuisine : detail.getCuisines()) {
                Log.d(TAG, "Save: +1 for cuisine " + cuisine);
                databaseHandler.upsertTasteScore(userEmail, cuisine, "cuisine", 1f);
            }
        }
        // +0.5 per ingredient (top 5)
        List<Ingredient> ingredients = detail.getExtendedIngredients();
        if (ingredients != null) {
            int limit = Math.min(5, ingredients.size());
            for (int i = 0; i < limit; i++) {
                String ingredientName = ingredients.get(i).getName();
                Log.d(TAG, "Save: +0.5 for ingredient " + ingredientName);
                databaseHandler.upsertTasteScore(userEmail, ingredientName, "ingredient", 0.5f);
            }
        }
    }

    private void populateUI(RecipeDetail detail) {
        detailTitle.setText(detail.getTitle());
        populateTags(detail);

        Glide.with(this)
                .load(detail.getImage())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(detailImage);

        ingredientsContainer.removeAllViews();
        List<Ingredient> ingredients = detail.getExtendedIngredients();
        if (ingredients != null) {
            for (Ingredient ingredient : ingredients) {
                TextView tv = new TextView(this);
                String amount = ingredient.getAmount() % 1 == 0
                        ? String.valueOf((int) ingredient.getAmount())
                        : String.valueOf(ingredient.getAmount());
                tv.setText("• " + amount + " " + ingredient.getUnit() + " " + ingredient.getName());
                tv.setTextSize(14);
                tv.setPadding(0, 4, 0, 4);
                ingredientsContainer.addView(tv);
            }
        }

        String instructionsTxt = buildInstructionsText(detail);
        instructionsText.setText(instructionsTxt.isEmpty() ? "No instructions available." : instructionsTxt);
    }

    private String buildIngredientsText(RecipeDetail detail) {
        StringBuilder sb = new StringBuilder();
        if (detail.getExtendedIngredients() != null) {
            for (Ingredient ingredient : detail.getExtendedIngredients()) {
                String amount = ingredient.getAmount() % 1 == 0
                        ? String.valueOf((int) ingredient.getAmount())
                        : String.valueOf(ingredient.getAmount());
                sb.append(amount).append(" ").append(ingredient.getUnit())
                  .append(" ").append(ingredient.getName()).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private String buildInstructionsText(RecipeDetail detail) {
        StringBuilder sb = new StringBuilder();
        List<RecipeDetail.AnalyzedInstruction> instructions = detail.getAnalyzedInstructions();
        if (instructions != null) {
            for (RecipeDetail.AnalyzedInstruction instruction : instructions) {
                if (instruction.getSteps() != null) {
                    for (RecipeDetail.Step step : instruction.getSteps()) {
                        sb.append(step.getNumber()).append(". ")
                          .append(step.getStep()).append("\n\n");
                    }
                }
            }
        }
        if (sb.length() == 0 && detail.getSummary() != null) {
            return stripHtml(detail.getSummary());
        }
        return sb.toString().trim();
    }

    private void populateTags(RecipeDetail detail) {
        tagsChipGroup.removeAllViews();
        // Collect: cuisines → dishTypes → diets (in that order, deduped)
        java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<>();
        if (detail.getCuisines() != null) {
            for (String s : detail.getCuisines()) seen.add(formatTag(s));
        }
        if (detail.getDishTypes() != null) {
            for (String s : detail.getDishTypes()) seen.add(formatTag(s));
        }
        if (detail.getDiets() != null) {
            for (String s : detail.getDiets()) seen.add(formatTag(s));
        }
        for (String tag : seen) {
            if (tag.isEmpty()) continue;
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setClickable(false);
            chip.setFocusable(false);
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            tagsChipGroup.addView(chip);
        }
    }

    private static String formatTag(String raw) {
        if (raw == null) return "";
        switch (raw.toLowerCase().trim()) {
            case "gluten free":           return "gf";
            case "dairy free":            return "df";
            case "lacto ovo vegetarian":  return "vegetarian";
            case "vegan":                 return "vegan";
            case "vegetarian":            return "vegetarian";
            case "ketogenic":             return "keto";
            case "paleolithic":           return "paleo";
            case "primal":                return "primal";
            case "whole 30":
            case "whole30":               return "whole30";
            case "fodmap friendly":
            case "low fodmap":            return "fodmap";
            case "main course":
            case "main dish":             return "main";
            default:                      return raw.toLowerCase().trim();
        }
    }

    private static String detectMealType(List<String> dishTypes) {
        if (dishTypes == null || dishTypes.isEmpty()) return "Other";
        for (String dt : dishTypes) {
            String lower = dt.toLowerCase();
            if (lower.contains("breakfast") || lower.contains("brunch")) return "Breakfast";
            if (lower.contains("lunch"))                                  return "Lunch";
            if (lower.contains("dinner") || lower.equals("main course")
                    || lower.equals("main dish"))                         return "Dinner";
            if (lower.contains("dessert"))                                return "Dessert";
            if (lower.contains("snack") || lower.contains("appetizer")
                    || lower.contains("side"))                            return "Snack";
        }
        return "Other";
    }

    @SuppressWarnings("deprecation")
    private String stripHtml(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(html).toString();
        }
    }
}
