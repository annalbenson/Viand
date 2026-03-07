package com.annabenson.viand;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class RecipeFavoriteAdapter extends RecyclerView.Adapter<RecipeFavoriteAdapter.FavoriteViewHolder> {

    public interface OnDeleteListener {
        void onDelete(Recipe recipe, int position);
    }

    private final Context context;
    private final List<Recipe> favorites;
    private final OnDeleteListener deleteListener;

    public RecipeFavoriteAdapter(Context context, List<Recipe> favorites, OnDeleteListener deleteListener) {
        this.context = context;
        this.favorites = favorites;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Recipe recipe = favorites.get(position);
        holder.title.setText(recipe.getTitle());

        Glide.with(context)
                .load(recipe.getImage())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(holder.thumbnail);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RecipeDetailActivity.class);
                intent.putExtra("RECIPE_ID", recipe.getId());
                intent.putExtra("RECIPE_TITLE", recipe.getTitle());
                context.startActivity(intent);
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_ID) {
                    deleteListener.onDelete(recipe, adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title;
        Button deleteButton;

        FavoriteViewHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.favoriteThumbnail);
            title = itemView.findViewById(R.id.favoriteTitle);
            deleteButton = itemView.findViewById(R.id.favoriteDeleteButton);
        }
    }
}
