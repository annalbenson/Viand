package com.annabenson.viand.models;

import java.util.List;

public class ChatMessage {

    public enum Type { USER, AI, LOADING, RECIPE_LIST, RECOMMENDATION, PREFERENCE_PROMPT }

    private final Type type;
    private String text;
    private List<Recipe> recipes;
    private RecommendationSet recommendationSet;
    private String promptTopic;

    public ChatMessage(Type type, String text) {
        this.type = type;
        this.text = text;
    }

    public ChatMessage(List<Recipe> recipes) {
        this.type = Type.RECIPE_LIST;
        this.recipes = recipes;
    }

    public ChatMessage(RecommendationSet set) {
        this.type = Type.RECOMMENDATION;
        this.recommendationSet = set;
    }

    public ChatMessage(Type type, String text, String promptTopic) {
        this.type = type;
        this.text = text;
        this.promptTopic = promptTopic;
    }

    public Type getType() { return type; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public List<Recipe> getRecipes() { return recipes; }
    public RecommendationSet getRecommendationSet() { return recommendationSet; }
    public String getPromptTopic() { return promptTopic; }
}
