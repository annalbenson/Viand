package com.annabenson.viand.models;

public class CustomRecipe {

    private int id;
    private String title;
    private String ingredients; // one per line: "2 cups flour"
    private String instructions;

    // For loading from DB (id known)
    public CustomRecipe(int id, String title, String ingredients, String instructions) {
        this.id = id;
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    // For creating new (id assigned by DB)
    public CustomRecipe(String title, String ingredients, String instructions) {
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getIngredients() { return ingredients; }
    public String getInstructions() { return instructions; }

    public void setTitle(String title) { this.title = title; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}
