package com.annabenson.viand;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class StoreViewHolder extends RecyclerView.ViewHolder{

    public TextView name;

    public StoreViewHolder(View view){
        super(view);

        name = view.findViewById(R.id.nameID);
    }
}
