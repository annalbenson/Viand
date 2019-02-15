package com.annabenson.viand;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.location.*;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
                            implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "MainActivity";
    private MainActivity mainActivity = this;

    // views
    private RecyclerView recyclerView;
    private TextView locationView;

    // vars
    private StoreAdapter storeAdapter;
    private List<Store> storeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);
        storeAdapter = new StoreAdapter(storeList,this);

        recyclerView.setAdapter(storeAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        for(int i = 0; i < 5; i++){
                storeList.add(new Store ("Name" + i));
        }

        storeAdapter.notifyDataSetChanged();


    }

    /* START OF RECYCLERVIEW METHODS */

    public void setStoreList(Object[] results){

        /*
        if(results == null){
            locationView.setText("No Data For Location");
            storeList.clear();
        }
        else{
            */
            locationView.setText(results[0].toString());
            storeList.clear();
            ArrayList<Store> offList = (ArrayList<Store>) results[1];
            for(int i = 0; i < offList.size(); i++){
                storeList.add( offList.get(i));
            }

        //}
        storeAdapter.notifyDataSetChanged();

    }

    /* END OF RECYCLERVIEW METHODS*/


    /* START OF CLICK METHODS */

    @Override
    public void onClick(View v){
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();


        /*
        Intent intent = new Intent(MainActivity.this, OfficialActivity.class);
        // get official
        int pos = recyclerView.getChildLayoutPosition(v);
        Store s = storeList.get(pos);
        // Add extra w/ heading
        intent.putExtra("header", locationView.getText().toString() );
        // Add extra w/ Official object
        Bundle bundle = new Bundle();
        bundle.putSerializable("official", o);
        intent.putExtras(bundle); // Extra"s" because passing a bundle

        // start the activity
        startActivity(intent);
        */

    }
    @Override
    public boolean onLongClick(View v){
        //Toast.makeText(this, "Long Clicked", Toast.LENGTH_SHORT).show();
        int pos = recyclerView.getChildLayoutPosition(v);
        onClick(v);

        return false;
    }

    /* END OF CLICK METHODS */



}
