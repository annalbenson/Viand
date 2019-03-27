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

    private static final String TAG = "MainActivity";
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

        createButton = findViewById(R.id.createButtonID);
        email = findViewById(R.id.emailID);
        password = findViewById(R.id.passwordID);
        loginButton = findViewById(R.id.loginID);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: create account button pressed");
                intent = new Intent(loginActivity,AccountCreationActivity.class);
                accountCreationDialog();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: login button pressed");
                String inputEmail = email.getText().toString();
                String inputPassword = password.getText().toString();
                UserAccount userAccount = databaseHandler.loadAccount(inputEmail,inputPassword);
                if( userAccount != null){
                    intent.putExtra(UserAccount);
                    intent = new Intent(loginActivity,MainActivity.class);
                }
                if (databaseHandler.inDatabase(inputEmail,inputPassword)){
                    /* TODO : load Account Object from database and pass to Main Activity */




                }
                else{
                    Toast.makeText(view.getContext(), "Invalid login",Toast.LENGTH_SHORT).show();
                }
            }
        });

        databaseHandler = new DatabaseHandler(this);

    }

    public void accountCreationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Account Type");

        final String [] options = {"User","Shopper","Store"};

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                intent.putExtra("AccountType",options[i]);
                startActivity(intent);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }


}
