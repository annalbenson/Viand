package com.annabenson.viand.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.annabenson.viand.R;
import com.annabenson.viand.activities.CustomRecipeActivity;
import com.annabenson.viand.models.CustomRecipe;

import java.util.List;

public class CustomRecipeAdapter extends RecyclerView.Adapter<CustomRecipeAdapter.ViewHolder> {

    private final Context context;
    private final List<CustomRecipe> recipes;

    public CustomRecipeAdapter(Context context, List<CustomRecipe> recipes) {
        this.context = context;
        this.recipes = recipes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_custom_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomRecipe recipe = recipes.get(position);
        holder.title.setText(recipe.getTitle());

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CustomRecipeActivity.class);
                intent.putExtra("CUSTOM_RECIPE_ID", recipe.getId());
                intent.putExtra("CUSTOM_TITLE", recipe.getTitle());
                intent.putExtra("CUSTOM_INGREDIENTS", recipe.getIngredients());
                intent.putExtra("CUSTOM_INSTRUCTIONS", recipe.getInstructions());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() { return recipes.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        Button editButton;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.customRecipeTitle);
            editButton = itemView.findViewById(R.id.editCustomRecipeButton);
        }
    }
}
