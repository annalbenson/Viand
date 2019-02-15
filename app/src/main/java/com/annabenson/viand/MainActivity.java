package com.annabenson.viand;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.location.*;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MainActivity mainActivity = this;


    private RecyclerView recyclerView;
    private TextView locationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);



    }
}
