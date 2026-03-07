package com.annabenson.viand.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.annabenson.viand.adapters.StoreAdapter;
import com.annabenson.viand.models.Store;

import java.util.ArrayList;
import java.util.List;

public class UserMainActivity extends AppCompatActivity {

    private static final String TAG = "UserMainActivity";
    private UserMainActivity mainActivity = this;

    // views
    private RecyclerView recyclerView;
    private TextView locationView;

    // vars
    private StoreAdapter storeAdapter;
    private List<Store> storeList = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.annabenson.viand.R.layout.activity_user_main);
        Log.d(TAG, "onCreate: ");
    }
}
