package com.annabenson.viand;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button createButton; Button loginButton;
    private TextView email; TextView password;

    private String [] dialogOptions = {"User","Shopper","Store"};

    private Intent intent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        createButton = findViewById(R.id.createButtonID);
        email = findViewById(R.id.emailID);
        password = findViewById(R.id.passwordID);
        loginButton = findViewById(R.id.loginID);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: create account button pressed");
                accountCreationDialog();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: login button pressed");
                String inputEmail = email.getText().toString();
                String inputPassword = password.getText().toString();

            }
        });

    }

    public void accountCreationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Account Type");
        builder.setMessage("Select the type of account you would like to create");
        builder.setSingleChoiceItems(dialogOptions, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        intent.putExtra("type","user");
                        break;
                    case 1:
                        intent.putExtra("type","shopper");
                        break;
                    case 2:
                        intent.putExtra("type","store");
                        break;

                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }
}
