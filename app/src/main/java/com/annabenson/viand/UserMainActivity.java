package com.annabenson.viand;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class UserMainActivity extends AppCompatActivity {

    private static final String TAG = "UserMainActivity";
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_user_main);
        Log.d(TAG, "onCreate: ");
    }
}
