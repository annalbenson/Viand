package com.annabenson.viand;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class RecipeViewHolder extends RecyclerView.ViewHolder {

    public ImageView recipeImage;
    public TextView recipeTitle;
    public TextView recipeSummary;

    public RecipeViewHolder(View itemView) {
        super(itemView);
        recipeImage = itemView.findViewById(R.id.recipeCardImage);
        recipeTitle = itemView.findViewById(R.id.recipeCardTitle);
        recipeSummary = itemView.findViewById(R.id.recipeCardSummary);
    }
}
