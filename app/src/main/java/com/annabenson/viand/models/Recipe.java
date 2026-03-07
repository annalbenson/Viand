package com.annabenson.viand.models;

import com.google.gson.annotations.SerializedName;

public class Recipe {

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("image")
    private String image;

    // Not persisted via Gson; set manually when loading from SQLite
    private String rating;   // null = unrated, "liked", "neutral", "disliked"
    private String mealType; // "Breakfast","Lunch","Dinner","Dessert","Snack","Other"

    // Manual constructor for loading from SQLite favorites
    public Recipe(int id, String title, String image) {
        this.id = id;
        this.title = title;
        this.image = image;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getImage() { return image; }
    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
}
