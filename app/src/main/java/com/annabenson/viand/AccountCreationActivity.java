package com.annabenson.viand;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class AccountCreationActivity extends AppCompatActivity {

    private static final String TAG = "AccountCreationActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* check what kind of account */

        Intent intent = this.getIntent();
        String accountType = intent.getStringExtra("accountType");
        if(accountType.equals("user")){
            setContentView(R.layout.activity_user_account_creation);

            /* setup views */


        }
        else if(accountType.equals("shopper")){
            //setContentView(R.layout.activity_shopper_account_creation);
        }
        else if(accountType.equals("store")){
            //setContentView(R.layout.activity_store_account_creation);
        }


        /*


         */


    }
}
