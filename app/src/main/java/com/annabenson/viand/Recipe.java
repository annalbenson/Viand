package com.annabenson.viand;

import com.google.gson.annotations.SerializedName;

public class Recipe {

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("image")
    private String image;

    // Manual constructor for loading from SQLite favorites
    public Recipe(int id, String title, String image) {
        this.id = id;
        this.title = title;
        this.image = image;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getImage() { return image; }
}
