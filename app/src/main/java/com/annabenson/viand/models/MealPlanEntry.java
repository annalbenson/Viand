package com.annabenson.viand.models;

public class MealPlanEntry {
    public int id;
    public int recipeId;
    public String recipeTitle;
    public String recipeImage;
    public String dayOfWeek;       // null = Bucket
    public String mealType;
    public String ingredientsJson;
    public long weekStart;
}
