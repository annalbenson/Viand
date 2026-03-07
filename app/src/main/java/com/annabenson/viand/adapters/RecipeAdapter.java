package com.annabenson.viand.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import com.annabenson.viand.R;
import com.annabenson.viand.activities.RecipeDetailActivity;
import com.annabenson.viand.models.Recipe;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeViewHolder> {

    private final Context context;
    private final List<Recipe> recipes;

    public RecipeAdapter(Context context, List<Recipe> recipes) {
        this.context = context;
        this.recipes = recipes;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe_card, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.recipeTitle.setText(recipe.getTitle());

        Glide.with(context)
                .load(recipe.getImage())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(holder.recipeImage);

        // Summary field not available in search results; hide it
        holder.recipeSummary.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RecipeDetailActivity.class);
                intent.putExtra("RECIPE_ID", recipe.getId());
                intent.putExtra("RECIPE_TITLE", recipe.getTitle());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }
}
