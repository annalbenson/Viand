package com.annabenson.viand.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.annabenson.viand.R;
import com.annabenson.viand.data.DatabaseHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccountSettingsActivity extends AppCompatActivity {

    private DatabaseHandler databaseHandler;
    private int userId;

    private CheckBox checkGlutenFree, checkVegetarian, checkVegan,
            checkKosher, checkHalal, checkDairyFree, checkNutFree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        Toolbar toolbar = findViewById(R.id.accountSettingsToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        databaseHandler = new DatabaseHandler(this);
        userId = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                .getInt(LoginActivity.KEY_USER_ID, -1);

        checkGlutenFree  = findViewById(R.id.checkGlutenFree);
        checkVegetarian  = findViewById(R.id.checkVegetarian);
        checkVegan       = findViewById(R.id.checkVegan);
        checkKosher      = findViewById(R.id.checkKosher);
        checkHalal       = findViewById(R.id.checkHalal);
        checkDairyFree   = findViewById(R.id.checkDairyFree);
        checkNutFree     = findViewById(R.id.checkNutFree);

        // Pre-check boxes based on saved preferences
        String savedPrefs = databaseHandler.getDietaryPreferences(userId);
        Set<String> current = new HashSet<>();
        if (savedPrefs != null && !savedPrefs.isEmpty()) {
            current.addAll(Arrays.asList(savedPrefs.split(",")));
        }
        checkGlutenFree.setChecked(current.contains("Gluten Free"));
        checkVegetarian.setChecked(current.contains("Vegetarian"));
        checkVegan.setChecked(current.contains("Vegan"));
        checkKosher.setChecked(current.contains("Kosher"));
        checkHalal.setChecked(current.contains("Halal"));
        checkDairyFree.setChecked(current.contains("Dairy Free"));
        checkNutFree.setChecked(current.contains("Nut Free"));

        Button saveButton = findViewById(R.id.saveSettingsButton);
        saveButton.setOnClickListener(v -> {
            List<String> prefs = new ArrayList<>();
            if (checkGlutenFree.isChecked()) prefs.add("Gluten Free");
            if (checkVegetarian.isChecked())  prefs.add("Vegetarian");
            if (checkVegan.isChecked())        prefs.add("Vegan");
            if (checkKosher.isChecked())       prefs.add("Kosher");
            if (checkHalal.isChecked())        prefs.add("Halal");
            if (checkDairyFree.isChecked())    prefs.add("Dairy Free");
            if (checkNutFree.isChecked())      prefs.add("Nut Free");

            databaseHandler.updateDietaryPreferences(userId, String.join(",", prefs));
            Toast.makeText(this, "Preferences saved!", Toast.LENGTH_SHORT).show();
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
