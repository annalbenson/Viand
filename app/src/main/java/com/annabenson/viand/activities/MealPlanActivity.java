package com.annabenson.viand.activities;

import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.annabenson.viand.R;
import com.annabenson.viand.data.DatabaseHandler;
import com.annabenson.viand.models.Ingredient;
import com.annabenson.viand.models.MealPlanEntry;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MealPlanActivity extends AppCompatActivity {

    private DatabaseHandler databaseHandler;
    private int userId;
    private long weekStart;

    private LinearLayout containerMonday, containerTuesday, containerWednesday,
            containerThursday, containerFriday, containerSaturday, containerSunday,
            containerBucket;
    private LinearLayout groceryContainer, groceryItemsContainer;
    private NestedScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plan);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Meal Plan");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        databaseHandler = new DatabaseHandler(this);
        userId = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                .getInt(LoginActivity.KEY_USER_ID, -1);
        weekStart = RecipeDetailActivity.getCurrentWeekStart();

        containerMonday    = findViewById(R.id.containerMonday);
        containerTuesday   = findViewById(R.id.containerTuesday);
        containerWednesday = findViewById(R.id.containerWednesday);
        containerThursday  = findViewById(R.id.containerThursday);
        containerFriday    = findViewById(R.id.containerFriday);
        containerSaturday  = findViewById(R.id.containerSaturday);
        containerSunday    = findViewById(R.id.containerSunday);
        containerBucket    = findViewById(R.id.containerBucket);
        groceryContainer       = findViewById(R.id.groceryContainer);
        groceryItemsContainer  = findViewById(R.id.groceryItemsContainer);
        scrollView             = findViewById(R.id.scrollView);

        Button buildGroceryListButton = findViewById(R.id.buildGroceryListButton);
        buildGroceryListButton.setOnClickListener(v -> buildGroceryList());
    }

    @Override
    protected void onResume() {
        super.onResume();
        buildPlanView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void buildPlanView() {
        List<MealPlanEntry> entries = databaseHandler.loadWeekMealPlan(userId, weekStart);
        groceryContainer.setVisibility(View.GONE);

        Map<String, List<MealPlanEntry>> byDay = new LinkedHashMap<>();
        byDay.put("Monday", new ArrayList<>());
        byDay.put("Tuesday", new ArrayList<>());
        byDay.put("Wednesday", new ArrayList<>());
        byDay.put("Thursday", new ArrayList<>());
        byDay.put("Friday", new ArrayList<>());
        byDay.put("Saturday", new ArrayList<>());
        byDay.put("Sunday", new ArrayList<>());
        byDay.put(null, new ArrayList<>());   // Bucket

        for (MealPlanEntry entry : entries) {
            String key = entry.dayOfWeek; // null for Bucket
            if (byDay.containsKey(key)) {
                byDay.get(key).add(entry);
            } else {
                byDay.get(null).add(entry);
            }
        }

        populateDayContainer(containerMonday,    byDay.get("Monday"));
        populateDayContainer(containerTuesday,   byDay.get("Tuesday"));
        populateDayContainer(containerWednesday, byDay.get("Wednesday"));
        populateDayContainer(containerThursday,  byDay.get("Thursday"));
        populateDayContainer(containerFriday,    byDay.get("Friday"));
        populateDayContainer(containerSaturday,  byDay.get("Saturday"));
        populateDayContainer(containerSunday,    byDay.get("Sunday"));
        populateDayContainer(containerBucket,    byDay.get(null));
    }

    private void populateDayContainer(LinearLayout container, List<MealPlanEntry> entries) {
        container.removeAllViews();
        if (entries == null || entries.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Nothing planned");
            empty.setTextSize(13f);
            empty.setPadding(0, 2, 0, 2);
            container.addView(empty);
            return;
        }
        for (MealPlanEntry entry : entries) {
            addEntryRow(container, entry);
        }
    }

    private void addEntryRow(LinearLayout container, MealPlanEntry entry) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        int vPad = dp(4);
        row.setPadding(0, vPad, 0, vPad);

        // Thumbnail
        ImageView thumb = new ImageView(this);
        int size = dp(32);
        LinearLayout.LayoutParams thumbParams = new LinearLayout.LayoutParams(size, size);
        thumbParams.setMarginEnd(dp(8));
        thumb.setLayoutParams(thumbParams);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this)
                .load(entry.recipeImage)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(thumb);
        row.addView(thumb);

        // Title + meal type
        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textCol.setLayoutParams(textParams);

        TextView titleTv = new TextView(this);
        titleTv.setText(entry.recipeTitle);
        titleTv.setTextSize(14f);
        textCol.addView(titleTv);

        if (entry.mealType != null && !entry.mealType.isEmpty()) {
            TextView chipTv = new TextView(this);
            chipTv.setText(entry.mealType);
            chipTv.setTextSize(11f);
            chipTv.setAlpha(0.6f);
            textCol.addView(chipTv);
        }
        row.addView(textCol);

        // Delete button
        Button deleteBtn = new Button(this);
        deleteBtn.setText("×");
        deleteBtn.setTextSize(16f);
        LinearLayout.LayoutParams delParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        deleteBtn.setLayoutParams(delParams);
        deleteBtn.setOnClickListener(v -> {
            databaseHandler.removeMealPlanEntry(entry.id);
            buildPlanView();
        });
        row.addView(deleteBtn);

        container.addView(row);
    }

    private void buildGroceryList() {
        List<MealPlanEntry> entries = databaseHandler.loadWeekMealPlan(userId, weekStart);
        if (entries.isEmpty()) {
            Toast.makeText(this, "No meals planned this week.", Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();
        Type ingredientListType = new TypeToken<List<Ingredient>>() {}.getType();

        // ingredient name (lowercase) → list of "amount unit (RecipeTitle)"
        Map<String, List<String>> grouped = new LinkedHashMap<>();

        for (MealPlanEntry entry : entries) {
            if (entry.ingredientsJson == null || entry.ingredientsJson.isEmpty()) continue;
            List<Ingredient> ingredients = gson.fromJson(entry.ingredientsJson, ingredientListType);
            if (ingredients == null) continue;
            for (Ingredient ing : ingredients) {
                String key = ing.getName() != null ? ing.getName().toLowerCase().trim() : "unknown";
                String detail = formatAmount(ing.getAmount()) + " " + nullToEmpty(ing.getUnit())
                        + " (" + entry.recipeTitle + ")";
                if (!grouped.containsKey(key)) {
                    grouped.put(key, new ArrayList<>());
                }
                grouped.get(key).add(detail.trim());
            }
        }

        // Sort alphabetically
        List<String> sortedKeys = new ArrayList<>(grouped.keySet());
        Collections.sort(sortedKeys);

        groceryItemsContainer.removeAllViews();
        for (String key : sortedKeys) {
            String line = "• " + capitalize(key) + ": " + String.join(", ", grouped.get(key));
            TextView tv = new TextView(this);
            tv.setText(line);
            tv.setTextSize(14f);
            tv.setPadding(0, dp(3), 0, dp(3));
            groceryItemsContainer.addView(tv);
        }

        groceryContainer.setVisibility(View.VISIBLE);
        scrollView.post(() -> scrollView.smoothScrollTo(0, groceryContainer.getTop()));
    }

    private static String formatAmount(double amount) {
        return amount % 1 == 0 ? String.valueOf((int) amount) : String.valueOf(amount);
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private int dp(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
