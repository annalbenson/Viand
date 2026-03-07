package com.annabenson.viand.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import com.annabenson.viand.R;
import com.annabenson.viand.activities.RecipeDetailActivity;
import com.annabenson.viand.models.Recipe;

import java.util.List;

public class RecipeFavoriteAdapter extends RecyclerView.Adapter<RecipeFavoriteAdapter.FavoriteViewHolder> {

    public static final String[] MEAL_TYPES = {"Breakfast", "Lunch", "Dinner", "Dessert", "Snack", "Other"};

    private static final String RATING_LIKED    = "liked";
    private static final String RATING_NEUTRAL  = "neutral";
    private static final String RATING_DISLIKED = "disliked";

    private static final float ALPHA_SELECTED   = 1.0f;
    private static final float ALPHA_UNSELECTED = 0.3f;
    private static final float ALPHA_UNRATED    = 0.6f;

    public interface OnDeleteListener {
        void onDelete(Recipe recipe, int position);
    }

    public interface OnRatingListener {
        void onRating(Recipe recipe, String rating);
    }

    public interface OnMealTypeListener {
        void onMealTypeChanged(Recipe recipe, String mealType);
    }

    private final Context context;
    private final List<Recipe> favorites;
    private final OnDeleteListener deleteListener;
    private final OnRatingListener ratingListener;
    private final OnMealTypeListener mealTypeListener;

    public RecipeFavoriteAdapter(Context context, List<Recipe> favorites,
                                 OnDeleteListener deleteListener,
                                 OnRatingListener ratingListener,
                                 OnMealTypeListener mealTypeListener) {
        this.context = context;
        this.favorites = favorites;
        this.deleteListener = deleteListener;
        this.ratingListener = ratingListener;
        this.mealTypeListener = mealTypeListener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_recipe_favorite_card, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Recipe recipe = favorites.get(position);
        holder.title.setText(recipe.getTitle());

        // Meal type label — tap to change
        String mt = recipe.getMealType();
        holder.mealTypeLabel.setText(mt != null ? mt + " ▾" : "Other ▾");
        holder.mealTypeLabel.setOnClickListener(v -> showMealTypePopup(v, recipe, holder));

        Glide.with(context)
                .load(recipe.getImage())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailActivity.class);
            intent.putExtra("RECIPE_ID", recipe.getId());
            intent.putExtra("RECIPE_TITLE", recipe.getTitle());
            context.startActivity(intent);
        });

        applyRatingColors(holder, recipe.getRating());

        holder.btnThumbsUp.setOnClickListener(v -> {
            recipe.setRating(RATING_LIKED);
            applyRatingColors(holder, RATING_LIKED);
            if (ratingListener != null) ratingListener.onRating(recipe, RATING_LIKED);
        });
        holder.btnNeutral.setOnClickListener(v -> {
            recipe.setRating(RATING_NEUTRAL);
            applyRatingColors(holder, RATING_NEUTRAL);
            if (ratingListener != null) ratingListener.onRating(recipe, RATING_NEUTRAL);
        });
        holder.btnThumbsDown.setOnClickListener(v -> {
            recipe.setRating(RATING_DISLIKED);
            applyRatingColors(holder, RATING_DISLIKED);
            if (ratingListener != null) ratingListener.onRating(recipe, RATING_DISLIKED);
        });

        holder.deleteButton.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                deleteListener.onDelete(recipe, adapterPosition);
            }
        });
    }

    private void showMealTypePopup(View anchor, Recipe recipe, FavoriteViewHolder holder) {
        PopupMenu popup = new PopupMenu(context, anchor);
        for (int i = 0; i < MEAL_TYPES.length; i++) {
            popup.getMenu().add(0, i, i, MEAL_TYPES[i]);
        }
        popup.setOnMenuItemClickListener(item -> {
            String selected = MEAL_TYPES[item.getItemId()];
            recipe.setMealType(selected);
            holder.mealTypeLabel.setText(selected + " ▾");
            if (mealTypeListener != null) mealTypeListener.onMealTypeChanged(recipe, selected);
            return true;
        });
        popup.show();
    }

    private void applyRatingColors(FavoriteViewHolder holder, String rating) {
        if (rating == null || rating.isEmpty()) {
            holder.btnThumbsUp.setAlpha(ALPHA_UNRATED);
            holder.btnNeutral.setAlpha(ALPHA_UNRATED);
            holder.btnThumbsDown.setAlpha(ALPHA_UNRATED);
        } else {
            holder.btnThumbsUp.setAlpha(
                    RATING_LIKED.equals(rating)    ? ALPHA_SELECTED : ALPHA_UNSELECTED);
            holder.btnNeutral.setAlpha(
                    RATING_NEUTRAL.equals(rating)  ? ALPHA_SELECTED : ALPHA_UNSELECTED);
            holder.btnThumbsDown.setAlpha(
                    RATING_DISLIKED.equals(rating) ? ALPHA_SELECTED : ALPHA_UNSELECTED);
        }
    }

    @Override
    public int getItemCount() { return favorites.size(); }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView mealTypeLabel;
        TextView title;
        ImageButton btnThumbsUp;
        ImageButton btnNeutral;
        ImageButton btnThumbsDown;
        Button deleteButton;

        FavoriteViewHolder(View itemView) {
            super(itemView);
            image         = itemView.findViewById(R.id.favCardImage);
            mealTypeLabel = itemView.findViewById(R.id.favCardMealType);
            title         = itemView.findViewById(R.id.favCardTitle);
            btnThumbsUp   = itemView.findViewById(R.id.btnThumbsUp);
            btnNeutral    = itemView.findViewById(R.id.btnNeutral);
            btnThumbsDown = itemView.findViewById(R.id.btnThumbsDown);
            deleteButton  = itemView.findViewById(R.id.favCardDeleteButton);
        }
    }
}
