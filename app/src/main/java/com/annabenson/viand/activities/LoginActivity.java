package com.annabenson.viand.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;

import com.annabenson.viand.data.DatabaseHandler;
import com.annabenson.viand.models.UserAccount;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    static final String PREFS_NAME     = "ViandPrefs";
    static final String KEY_REMEMBER   = "remember_me";
    static final String KEY_EMAIL      = "saved_email";
    static final String KEY_PASSWORD   = "saved_password";
    static final String KEY_USER_NAME  = "user_name";

    private LoginActivity loginActivity = this;

    private TextInputEditText email;
    private TextInputEditText password;
    private CheckBox rememberMe;
    private Button loginButton;
    private Button createButton;

    private DatabaseHandler databaseHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Auto-login if credentials are saved
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_REMEMBER, false)) {
            String savedEmail    = prefs.getString(KEY_EMAIL, "");
            String savedPassword = prefs.getString(KEY_PASSWORD, "");
            DatabaseHandler db   = new DatabaseHandler(this);
            UserAccount account  = db.loadUserAccount(savedEmail, savedPassword);
            if (account != null) {
                Log.d(TAG, "Auto-login successful for " + savedEmail);
                launchSearchScreen(account.getName());
                return;
            }
            // Saved credentials no longer valid — clear them
            prefs.edit().clear().apply();
        }

        setContentView(com.annabenson.viand.R.layout.activity_login);

        email        = findViewById(com.annabenson.viand.R.id.emailID);
        password     = findViewById(com.annabenson.viand.R.id.passwordID);
        rememberMe   = findViewById(com.annabenson.viand.R.id.rememberMeCheckbox);
        loginButton  = findViewById(com.annabenson.viand.R.id.loginID);
        createButton = findViewById(com.annabenson.viand.R.id.createButtonID);

        databaseHandler = new DatabaseHandler(this);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: create account button pressed");
                Intent intent = new Intent(loginActivity, AccountCreationActivity.class);
                intent.putExtra("AccountType", "User");
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: login button pressed");
                String inputEmail    = email.getText().toString().trim();
                String inputPassword = password.getText().toString();
                UserAccount account  = databaseHandler.loadUserAccount(inputEmail, inputPassword);
                if (account != null) {
                    if (rememberMe.isChecked()) {
                        prefs.edit()
                                .putBoolean(KEY_REMEMBER, true)
                                .putString(KEY_EMAIL, inputEmail)
                                .putString(KEY_PASSWORD, inputPassword)
                                .putString(KEY_USER_NAME, account.getName())
                                .apply();
                    }
                    launchSearchScreen(account.getName());
                } else {
                    Toast.makeText(view.getContext(), "Invalid login", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void launchSearchScreen(String name) {
        Intent intent = new Intent(this, RecipeSearchActivity.class);
        intent.putExtra("USER_NAME", name);
        startActivity(intent);
        finish();
    }
}
