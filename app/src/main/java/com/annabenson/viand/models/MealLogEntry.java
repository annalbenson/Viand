package com.annabenson.viand.models;

public class MealLogEntry {
    public int id;
    public int recipeId;
    public String recipeTitle;
    public String recipeImage;
    public String mealType;
    public String rating;   // from FavoritesTable join — null if not saved as favorite
    public long madeAt;     // Unix epoch seconds
}
