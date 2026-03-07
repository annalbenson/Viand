package com.annabenson.viand.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.annabenson.viand.R;
import com.annabenson.viand.activities.RecipeDetailActivity;
import com.annabenson.viand.models.ChatMessage;
import com.annabenson.viand.models.Recipe;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_AI = 1;
    private static final int VIEW_TYPE_RECIPE_LIST = 2;

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        switch (messages.get(position).getType()) {
            case USER:    return VIEW_TYPE_USER;
            case RECIPE_LIST: return VIEW_TYPE_RECIPE_LIST;
            default:      return VIEW_TYPE_AI;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            return new UserViewHolder(inflater.inflate(R.layout.item_message_user, parent, false));
        } else if (viewType == VIEW_TYPE_RECIPE_LIST) {
            return new RecipeListViewHolder(inflater.inflate(R.layout.item_message_recipe_list, parent, false));
        } else {
            return new AiViewHolder(inflater.inflate(R.layout.item_message_ai, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).text.setText(message.getText());
        } else if (holder instanceof RecipeListViewHolder) {
            ((RecipeListViewHolder) holder).bind(message.getRecipes());
        } else {
            String display = message.getType() == ChatMessage.Type.LOADING
                    ? "Thinking…" : message.getText();
            ((AiViewHolder) holder).text.setText(display);
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        UserViewHolder(View v) {
            super(v);
            text = v.findViewById(R.id.messageText);
        }
    }

    static class AiViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        AiViewHolder(View v) {
            super(v);
            text = v.findViewById(R.id.messageText);
        }
    }

    static class RecipeListViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;

        RecipeListViewHolder(View v) {
            super(v);
            container = v.findViewById(R.id.recipeListContainer);
        }

        void bind(List<Recipe> recipes) {
            container.removeAllViews();
            Context context = itemView.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            for (Recipe recipe : recipes) {
                View row = inflater.inflate(R.layout.item_recipe_suggestion, container, false);
                ((TextView) row.findViewById(R.id.suggestionTitle)).setText(recipe.getTitle());
                row.setOnClickListener(v -> {
                    Intent intent = new Intent(context, RecipeDetailActivity.class);
                    intent.putExtra("RECIPE_ID", recipe.getId());
                    intent.putExtra("RECIPE_TITLE", recipe.getTitle());
                    context.startActivity(intent);
                });
                container.addView(row);
            }
        }
    }
}
