package com.annabenson.viand.models;

public class RecommendationSet {

    private final Recipe goToRecipe;
    private final Recipe similarRecipe;
    private final Recipe adventurousRecipe;

    public RecommendationSet(Recipe goToRecipe, Recipe similarRecipe, Recipe adventurousRecipe) {
        this.goToRecipe = goToRecipe;
        this.similarRecipe = similarRecipe;
        this.adventurousRecipe = adventurousRecipe;
    }

    public Recipe getGoToRecipe() { return goToRecipe; }
    public Recipe getSimilarRecipe() { return similarRecipe; }
    public Recipe getAdventurousRecipe() { return adventurousRecipe; }
}
