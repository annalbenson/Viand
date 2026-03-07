package com.annabenson.viand.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;

import com.annabenson.viand.data.DatabaseHandler;
import com.annabenson.viand.models.CustomRecipe;

public class CustomRecipeActivity extends AppCompatActivity {

    private TextInputEditText titleField;
    private TextInputEditText ingredientsField;
    private TextInputEditText instructionsField;
    private Button saveButton;
    private Button deleteButton;

    private DatabaseHandler databaseHandler;
    private int existingId = -1; // -1 means new recipe

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.annabenson.viand.R.layout.activity_custom_recipe);

        Toolbar toolbar = findViewById(com.annabenson.viand.R.id.customRecipeToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        titleField = findViewById(com.annabenson.viand.R.id.customTitle);
        ingredientsField = findViewById(com.annabenson.viand.R.id.customIngredients);
        instructionsField = findViewById(com.annabenson.viand.R.id.customInstructions);
        saveButton = findViewById(com.annabenson.viand.R.id.saveCustomRecipeButton);
        deleteButton = findViewById(com.annabenson.viand.R.id.deleteCustomRecipeButton);

        databaseHandler = new DatabaseHandler(this);

        // Pre-populate from intent (either from Spoonacular detail or editing saved recipe)
        existingId = getIntent().getIntExtra("CUSTOM_RECIPE_ID", -1);
        String preTitle = getIntent().getStringExtra("CUSTOM_TITLE");
        String preIngredients = getIntent().getStringExtra("CUSTOM_INGREDIENTS");
        String preInstructions = getIntent().getStringExtra("CUSTOM_INSTRUCTIONS");

        if (preTitle != null) titleField.setText(preTitle);
        if (preIngredients != null) ingredientsField.setText(preIngredients);
        if (preInstructions != null) instructionsField.setText(preInstructions);

        if (existingId != -1) {
            deleteButton.setVisibility(View.VISIBLE);
        }

        saveButton.setOnClickListener(v -> {
            String title = titleField.getText().toString().trim();
            String ingredients = ingredientsField.getText().toString().trim();
            String instructions = instructionsField.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(CustomRecipeActivity.this,
                        "Please enter a recipe title", Toast.LENGTH_SHORT).show();
                return;
            }

            if (existingId == -1) {
                databaseHandler.addCustomRecipe(new CustomRecipe(title, ingredients, instructions));
                Toast.makeText(CustomRecipeActivity.this,
                        "Recipe saved!", Toast.LENGTH_SHORT).show();
            } else {
                databaseHandler.updateCustomRecipe(
                        new CustomRecipe(existingId, title, ingredients, instructions));
                Toast.makeText(CustomRecipeActivity.this,
                        "Recipe updated!", Toast.LENGTH_SHORT).show();
            }
            finish();
        });

        deleteButton.setOnClickListener(v -> {
            databaseHandler.deleteCustomRecipe(existingId);
            Toast.makeText(CustomRecipeActivity.this,
                    "Recipe deleted", Toast.LENGTH_SHORT).show();
            finish();
        });
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
