package com.annabenson.viand.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

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

    private ImageView detailImage;
    private TextView detailTitle;
    private LinearLayout ingredientsContainer;
    private TextView instructionsText;
    private Button saveToFavoritesButton;
    private Button createMyVersionButton;

    private SpoonacularService spoonacularService;
    private DatabaseHandler databaseHandler;

    private int recipeId;
    private RecipeDetail currentDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.annabenson.viand.R.layout.activity_recipe_detail);

        detailImage = findViewById(com.annabenson.viand.R.id.detailImage);
        detailTitle = findViewById(com.annabenson.viand.R.id.detailTitle);
        ingredientsContainer = findViewById(com.annabenson.viand.R.id.ingredientsContainer);
        instructionsText = findViewById(com.annabenson.viand.R.id.instructionsText);
        saveToFavoritesButton = findViewById(com.annabenson.viand.R.id.saveToFavoritesButton);
        createMyVersionButton = findViewById(com.annabenson.viand.R.id.createMyVersionButton);

        ImageButton backButton = findViewById(com.annabenson.viand.R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        databaseHandler = new DatabaseHandler(this);
        spoonacularService = RetrofitClient.getInstance().create(SpoonacularService.class);

        recipeId = getIntent().getIntExtra("RECIPE_ID", -1);
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

        saveToFavoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDetail != null) {
                    databaseHandler.addFavorite(
                            currentDetail.getId(),
                            currentDetail.getTitle(),
                            currentDetail.getImage()
                    );
                    Toast.makeText(RecipeDetailActivity.this,
                            "Saved to favorites!", Toast.LENGTH_SHORT).show();
                    saveToFavoritesButton.setEnabled(false);
                    saveToFavoritesButton.setText("Saved");
                }
            }
        });

        createMyVersionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDetail != null) {
                    Intent intent = new Intent(RecipeDetailActivity.this, CustomRecipeActivity.class);
                    intent.putExtra("CUSTOM_TITLE", currentDetail.getTitle());
                    intent.putExtra("CUSTOM_INGREDIENTS", buildIngredientsText(currentDetail));
                    intent.putExtra("CUSTOM_INSTRUCTIONS", buildInstructionsText(currentDetail));
                    startActivity(intent);
                }
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

    private void populateUI(RecipeDetail detail) {
        detailTitle.setText(detail.getTitle());

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

    @SuppressWarnings("deprecation")
    private String stripHtml(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(html).toString();
        }
    }
}
