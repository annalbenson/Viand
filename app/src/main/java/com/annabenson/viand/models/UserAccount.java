package com.annabenson.viand.models;

public class UserAccount extends Account {

    private int id;
    private String name;
    private String dietaryPreferences; // comma-separated e.g. "Gluten Free,Vegan"

    public UserAccount(int id, String email, String password, String name, String dietaryPreferences) {
        super(email, password);
        this.id = id;
        this.name = name;
        this.dietaryPreferences = dietaryPreferences;
    }

    public int getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDietaryPreferences() { return dietaryPreferences; }
    public void setDietaryPreferences(String dietaryPreferences) {
        this.dietaryPreferences = dietaryPreferences;
    }
}
