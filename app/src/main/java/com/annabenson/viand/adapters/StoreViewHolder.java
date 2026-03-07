package com.annabenson.viand.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.annabenson.viand.R;

public class StoreViewHolder extends RecyclerView.ViewHolder{

    public TextView name;

    public StoreViewHolder(View view){
        super(view);

        name = view.findViewById(R.id.nameID);
    }
}
