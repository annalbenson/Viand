package com.annabenson.viand.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.annabenson.viand.R;
import com.annabenson.viand.activities.RecipeDetailActivity;
import com.annabenson.viand.models.ChatMessage;
import com.annabenson.viand.models.Recipe;
import com.annabenson.viand.models.RecommendationSet;

import java.util.List;
import java.util.Random;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_AI = 1;
    private static final int VIEW_TYPE_RECIPE_LIST = 2;
    private static final int VIEW_TYPE_RECOMMENDATION = 3;
    private static final int VIEW_TYPE_PREFERENCE_PROMPT = 4;

    private static final String[] VIVIAN_INTRO_LINES = {
        "Here's what I'm thinking for you today!",
        "Based on what you love, how about these?",
        "I've got some ideas..."
    };

    public interface PreferenceResponseListener {
        void onPreferenceResponse(String topic, int points);
    }

    private final List<ChatMessage> messages;
    private final PreferenceResponseListener preferenceResponseListener;

    public ChatAdapter(List<ChatMessage> messages, PreferenceResponseListener listener) {
        this.messages = messages;
        this.preferenceResponseListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        switch (messages.get(position).getType()) {
            case USER:              return VIEW_TYPE_USER;
            case RECIPE_LIST:       return VIEW_TYPE_RECIPE_LIST;
            case RECOMMENDATION:    return VIEW_TYPE_RECOMMENDATION;
            case PREFERENCE_PROMPT: return VIEW_TYPE_PREFERENCE_PROMPT;
            default:                return VIEW_TYPE_AI;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_USER:
                return new UserViewHolder(inflater.inflate(R.layout.item_message_user, parent, false));
            case VIEW_TYPE_RECIPE_LIST:
                return new RecipeListViewHolder(inflater.inflate(R.layout.item_message_recipe_list, parent, false));
            case VIEW_TYPE_RECOMMENDATION:
                return new RecommendationViewHolder(inflater.inflate(R.layout.item_message_recommendation, parent, false));
            case VIEW_TYPE_PREFERENCE_PROMPT:
                return new PreferencePromptViewHolder(inflater.inflate(R.layout.item_message_preference_prompt, parent, false));
            default:
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
        } else if (holder instanceof RecommendationViewHolder) {
            ((RecommendationViewHolder) holder).bind(message.getRecommendationSet());
        } else if (holder instanceof PreferencePromptViewHolder) {
            ((PreferencePromptViewHolder) holder).bind(message.getText(), message.getPromptTopic());
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

    static class RecommendationViewHolder extends RecyclerView.ViewHolder {
        TextView vivianIntroText;
        View slotGoTo;
        View slotSimilar;
        View slotAdventurous;

        RecommendationViewHolder(View v) {
            super(v);
            vivianIntroText = v.findViewById(R.id.vivianIntroText);
            slotGoTo = v.findViewById(R.id.slotGoTo);
            slotSimilar = v.findViewById(R.id.slotSimilar);
            slotAdventurous = v.findViewById(R.id.slotAdventurous);
        }

        void bind(RecommendationSet set) {
            vivianIntroText.setText(VIVIAN_INTRO_LINES[new Random().nextInt(VIVIAN_INTRO_LINES.length)]);

            bindSlot(slotGoTo, "Your Recent Go-To:", set.getGoToRecipe());
            bindSlot(slotSimilar, "Try Something Similar!", set.getSimilarRecipe());
            bindSlot(slotAdventurous, "Feeling Bold?", set.getAdventurousRecipe());
        }

        private void bindSlot(View slot, String label, Recipe recipe) {
            ((TextView) slot.findViewById(R.id.slotLabel)).setText(label);
            if (recipe == null) {
                ((TextView) slot.findViewById(R.id.slotTitle)).setText("No results yet");
                slot.findViewById(R.id.slotChevron).setVisibility(View.GONE);
                slot.setClickable(false);
            } else {
                ((TextView) slot.findViewById(R.id.slotTitle)).setText(recipe.getTitle());
                slot.findViewById(R.id.slotChevron).setVisibility(View.VISIBLE);
                slot.setClickable(true);
                Context context = itemView.getContext();
                slot.setOnClickListener(v -> {
                    Intent intent = new Intent(context, RecipeDetailActivity.class);
                    intent.putExtra("RECIPE_ID", recipe.getId());
                    intent.putExtra("RECIPE_TITLE", recipe.getTitle());
                    intent.putExtra("FROM_RECOMMENDATION", true);
                    context.startActivity(intent);
                });
            }
        }
    }

    class PreferencePromptViewHolder extends RecyclerView.ViewHolder {
        TextView promptText;
        LinearLayout promptButtonsContainer;
        Button btnOften;
        Button btnSometimes;
        Button btnRarely;
        Button btnNever;
        TextView promptResponseText;

        PreferencePromptViewHolder(View v) {
            super(v);
            promptText = v.findViewById(R.id.promptText);
            promptButtonsContainer = v.findViewById(R.id.promptButtonsContainer);
            btnOften = v.findViewById(R.id.btnOften);
            btnSometimes = v.findViewById(R.id.btnSometimes);
            btnRarely = v.findViewById(R.id.btnRarely);
            btnNever = v.findViewById(R.id.btnNever);
            promptResponseText = v.findViewById(R.id.promptResponseText);
        }

        void bind(String text, String topic) {
            promptText.setText(text);
            promptButtonsContainer.setVisibility(View.VISIBLE);
            promptResponseText.setVisibility(View.GONE);

            View.OnClickListener listener = v -> {
                int points = 0;
                if (v == btnOften) points = 3;
                else if (v == btnSometimes) points = 1;
                else if (v == btnRarely) points = -1;
                else if (v == btnNever) points = -3;

                if (preferenceResponseListener != null) {
                    preferenceResponseListener.onPreferenceResponse(topic, points);
                }
                promptButtonsContainer.setVisibility(View.GONE);
                promptResponseText.setVisibility(View.VISIBLE);
            };

            btnOften.setOnClickListener(listener);
            btnSometimes.setOnClickListener(listener);
            btnRarely.setOnClickListener(listener);
            btnNever.setOnClickListener(listener);
        }
    }
}
