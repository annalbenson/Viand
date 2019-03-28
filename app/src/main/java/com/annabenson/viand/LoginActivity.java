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
import android.widget.Toast;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private LoginActivity loginActivity = this;

    private Button createButton; Button loginButton;
    private TextView email; TextView password;

    private String [] dialogOptions = {"User","Shopper","Store"};

    private Intent intent;

    private DatabaseHandler databaseHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* views */
        createButton = findViewById(R.id.createButtonID);
        email = findViewById(R.id.emailID);
        password = findViewById(R.id.passwordID);
        loginButton = findViewById(R.id.loginID);

        /* database */
        databaseHandler = new DatabaseHandler(this);

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
                UserAccount userAccount = databaseHandler.loadUserAccount(inputEmail,inputPassword);
                if( userAccount != null){
                    intent = new Intent(loginActivity,MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("UserAccount", userAccount);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(view.getContext(), "Invalid login",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public void accountCreationDialog(){
        /* called by createButton on click listener */

        intent = new Intent(loginActivity,AccountCreationActivity.class);
        //Bundle bundle = new Bundle();
        //bundle.putSerializable("Database", databaseHandler);
        //intent.putExtras(bundle);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Account Type");
        final String [] options = {"User","Shopper","Store"};

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String choice = options[i];
                intent.putExtra("AccountType",choice);
                startActivity(intent);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }


}
