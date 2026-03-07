package com.annabenson.viand;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class AccountCreationActivity extends AppCompatActivity {

    private DatabaseHandler databaseHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account_creation);

        databaseHandler = new DatabaseHandler(this);

        final TextInputEditText name = findViewById(R.id.nameID);
        final TextInputEditText email = findViewById(R.id.emailID);
        final TextInputEditText password = findViewById(R.id.passwordID);

        final CheckBox checkGlutenFree = findViewById(R.id.checkGlutenFree);
        final CheckBox checkVegetarian = findViewById(R.id.checkVegetarian);
        final CheckBox checkVegan = findViewById(R.id.checkVegan);
        final CheckBox checkKosher = findViewById(R.id.checkKosher);
        final CheckBox checkHalal = findViewById(R.id.checkHalal);
        final CheckBox checkDairyFree = findViewById(R.id.checkDairyFree);
        final CheckBox checkNutFree = findViewById(R.id.checkNutFree);

        final Button createButton = findViewById(R.id.button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameInput = name.getText().toString().trim();
                String emailInput = email.getText().toString().trim();
                String passwordInput = password.getText().toString();

                if (nameInput.isEmpty() || emailInput.isEmpty() || passwordInput.isEmpty()) {
                    Toast.makeText(AccountCreationActivity.this,
                            "Please fill in name, email, and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<String> prefs = new ArrayList<>();
                if (checkGlutenFree.isChecked()) prefs.add("Gluten Free");
                if (checkVegetarian.isChecked()) prefs.add("Vegetarian");
                if (checkVegan.isChecked()) prefs.add("Vegan");
                if (checkKosher.isChecked()) prefs.add("Kosher");
                if (checkHalal.isChecked()) prefs.add("Halal");
                if (checkDairyFree.isChecked()) prefs.add("Dairy Free");
                if (checkNutFree.isChecked()) prefs.add("Nut Free");

                String dietaryPrefs = String.join(",", prefs);

                UserAccount newAccount = new UserAccount(emailInput, passwordInput, nameInput, dietaryPrefs);
                databaseHandler.addUserAccount(newAccount);

                Toast.makeText(AccountCreationActivity.this,
                        "Account created!", Toast.LENGTH_SHORT).show();
                // Store name so the greeting persists on next auto-login
                getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                        .edit()
                        .putString(LoginActivity.KEY_USER_NAME, nameInput)
                        .apply();
                Intent intent = new Intent(AccountCreationActivity.this, RecipeSearchActivity.class);
                intent.putExtra("USER_NAME", nameInput);
                startActivity(intent);
                finish();
            }
        });
    }
}
