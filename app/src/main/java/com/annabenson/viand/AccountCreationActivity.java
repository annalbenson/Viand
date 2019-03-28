package com.annabenson.viand;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class AccountCreationActivity extends AppCompatActivity {

    private static final String TAG = "AccountCreationActivity";
    private AccountCreationActivity accountCreationActivity = this;

    Intent intent;
    DatabaseHandler databaseHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* check what kind of account */

        intent = this.getIntent();

        //Bundle bundle = intent.getExtras();
        //databaseHandler = (DatabaseHandler) bundle.getSerializable("Database");

        String accountType = intent.getStringExtra("AccountType");
        if(accountType.equals("User")){
            Log.d(TAG, "onCreate: User");
            setContentView(R.layout.activity_user_account_creation);

            final EditText firstName = findViewById(R.id.firstNameID);
            final EditText lastName = findViewById(R.id.lastNameID);
            final EditText phoneNumber = findViewById(R.id.phoneNumberID);
            final EditText email = findViewById(R.id.emailID);
            final EditText password = findViewById(R.id.passwordID);
            final Button button = findViewById(R.id.button);

            /*
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /* save to database
                    UserAccount userAccount = new UserAccount(email.getText().toString(),
                            password.getText().toString(),
                            phoneNumber.getText().toString(),
                            new ArrayList<Order>(),
                            firstName.getText().toString(),
                            lastName.getText().toString()
                            );
                    databaseHandler.addUserAccount(userAccount);
                    /* pass UserAccount object to MainActivity //
                    intent = new Intent(accountCreationActivity,MainActivity.class);
                }
            });
        */

        }
        else if(accountType.equals("Shopper")){
            Log.d(TAG, "onCreate: Shopper");
            setContentView(R.layout.activity_shopper_account_creation);
        }
        else if(accountType.equals("Store")){
            Log.d(TAG, "onCreate: Store");
            //setContentView(R.layout.activity_store_account_creation);
        }


        /*


         */


    }
}
