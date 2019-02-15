package com.annabenson.viand;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreViewHolder> {

    public static final String TAG = "StoreAdapter";
    private MainActivity mainActivity;

    private List<Store> storeList;

    public StoreAdapter(List<Store> storeList, MainActivity mainActivity){
        this.storeList = storeList;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Making New");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.store_list_row,parent,false);
        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);
        return new StoreViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Store store = storeList.get(position);
        holder.name.setText(store.getName());
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }
}
