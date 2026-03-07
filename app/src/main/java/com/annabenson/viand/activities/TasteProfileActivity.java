package com.annabenson.viand.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.annabenson.viand.R;
import com.annabenson.viand.data.DatabaseHandler;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TasteProfileActivity extends AppCompatActivity {

    static class Ingredient {
        final String emoji;
        final String name;
        Ingredient(String emoji, String name) { this.emoji = emoji; this.name = name; }
    }

    static final List<Ingredient> INGREDIENTS = Arrays.asList(
            new Ingredient("🧄", "Garlic"),
            new Ingredient("🍗", "Chicken"),
            new Ingredient("🥩", "Beef"),
            new Ingredient("🐷", "Pork"),
            new Ingredient("🐟", "Fish"),
            new Ingredient("🍤", "Shrimp"),
            new Ingredient("🍄", "Mushrooms"),
            new Ingredient("🍝", "Pasta"),
            new Ingredient("🍚", "Rice"),
            new Ingredient("🥑", "Avocado"),
            new Ingredient("🥚", "Eggs"),
            new Ingredient("🧀", "Cheese"),
            new Ingredient("🍅", "Tomatoes"),
            new Ingredient("🧅", "Onion"),
            new Ingredient("🍋", "Lemon"),
            new Ingredient("🧈", "Butter"),
            new Ingredient("🌶️", "Spicy food"),
            new Ingredient("🥓", "Bacon"),
            new Ingredient("🫚", "Olive oil"),
            new Ingredient("🥦", "Broccoli"),
            new Ingredient("🍠", "Sweet potato"),
            new Ingredient("🫘", "Beans"),
            new Ingredient("🥜", "Nuts"),
            new Ingredient("🥥", "Coconut"),
            new Ingredient("🍫", "Chocolate")
    );

    private int currentIndex = 0;
    private Map<String, Float> existingScores;
    private DatabaseHandler databaseHandler;
    private int userId;

    private TextView tvProgress;
    private TextView tvEmoji;
    private TextView tvIngredientName;
    private CardView cardIngredient;
    private GridLayout buttonGrid;
    private MaterialButton btnLove;
    private MaterialButton btnLike;
    private MaterialButton btnNeutral;
    private MaterialButton btnDislike;
    private TextView tvComplete;
    private MaterialButton btnRetake;
    private MaterialButton btnCuisines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taste_profile);

        Toolbar toolbar = findViewById(R.id.tasteProfileToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Taste Profile");
        }

        tvProgress = findViewById(R.id.tvProgress);
        tvEmoji = findViewById(R.id.tvEmoji);
        tvIngredientName = findViewById(R.id.tvIngredientName);
        cardIngredient = findViewById(R.id.cardIngredient);
        buttonGrid = findViewById(R.id.buttonGrid);
        btnLove = findViewById(R.id.btnLove);
        btnLike = findViewById(R.id.btnLike);
        btnNeutral = findViewById(R.id.btnNeutral);
        btnDislike = findViewById(R.id.btnDislike);
        tvComplete = findViewById(R.id.tvComplete);
        btnRetake = findViewById(R.id.btnRetake);
        btnCuisines = findViewById(R.id.btnCuisines);

        databaseHandler = new DatabaseHandler(this);
        userId = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                .getInt(LoginActivity.KEY_USER_ID, -1);

        existingScores = databaseHandler.loadIngredientProfile(userId);

        btnLove.setOnClickListener(v -> onRatingSelected(10f));
        btnLike.setOnClickListener(v -> onRatingSelected(7f));
        btnNeutral.setOnClickListener(v -> onRatingSelected(4f));
        btnDislike.setOnClickListener(v -> onRatingSelected(1f));

        btnRetake.setOnClickListener(v -> {
            currentIndex = 0;
            showQuizViews();
            showIngredient(0);
        });

        btnCuisines.setOnClickListener(v ->
                startActivity(new Intent(this, CuisinePreferencesActivity.class)));

        int startIndex = -1;
        for (int i = 0; i < INGREDIENTS.size(); i++) {
            if (!existingScores.containsKey(INGREDIENTS.get(i).name)) {
                startIndex = i;
                break;
            }
        }
        if (startIndex == -1) {
            currentIndex = INGREDIENTS.size();
            showCompletion();
        } else {
            currentIndex = startIndex;
            showIngredient(startIndex);
        }
    }

    private void showIngredient(int index) {
        tvProgress.setText((index + 1) + " of " + INGREDIENTS.size());
        tvEmoji.setText(INGREDIENTS.get(index).emoji);
        tvIngredientName.setText(INGREDIENTS.get(index).name);
        resetButtonStyles();
        Float existing = existingScores.get(INGREDIENTS.get(index).name);
        if (existing != null) highlightButton(existing);
    }

    private void onRatingSelected(float score) {
        String name = INGREDIENTS.get(currentIndex).name;
        databaseHandler.setTasteScore(userId, name, "ingredient", score);
        existingScores.put(name, score);
        currentIndex++;
        if (currentIndex < INGREDIENTS.size()) {
            showIngredient(currentIndex);
        } else {
            showCompletion();
        }
    }

    private void resetButtonStyles() {
        for (MaterialButton btn : new MaterialButton[]{btnLove, btnLike, btnNeutral, btnDislike}) {
            btn.setBackgroundColor(Color.TRANSPARENT);
            btn.setTextColor(getResources().getColor(com.google.android.material.R.color.design_default_color_primary, getTheme()));
        }
    }

    private void highlightButton(float score) {
        MaterialButton target;
        if (score >= 9f) target = btnLove;
        else if (score >= 6f) target = btnLike;
        else if (score >= 3f) target = btnNeutral;
        else target = btnDislike;

        target.setBackgroundColor(getResources().getColor(com.google.android.material.R.color.design_default_color_primary, getTheme()));
        target.setTextColor(Color.WHITE);
    }

    private void showCompletion() {
        cardIngredient.setVisibility(View.GONE);
        buttonGrid.setVisibility(View.GONE);
        tvProgress.setVisibility(View.GONE);
        tvComplete.setVisibility(View.VISIBLE);
        btnRetake.setVisibility(View.VISIBLE);
        btnCuisines.setVisibility(View.VISIBLE);
    }

    private void showQuizViews() {
        cardIngredient.setVisibility(View.VISIBLE);
        buttonGrid.setVisibility(View.VISIBLE);
        tvProgress.setVisibility(View.VISIBLE);
        tvComplete.setVisibility(View.GONE);
        btnRetake.setVisibility(View.GONE);
        btnCuisines.setVisibility(View.GONE);
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
