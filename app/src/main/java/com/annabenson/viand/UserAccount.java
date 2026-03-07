package com.annabenson.viand;

public class UserAccount extends Account {

    private String name;
    private String dietaryPreferences; // comma-separated e.g. "Gluten Free,Vegan"

    public UserAccount(String email, String password, String name, String dietaryPreferences) {
        super(email, password);
        this.name = name;
        this.dietaryPreferences = dietaryPreferences;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDietaryPreferences() { return dietaryPreferences; }
    public void setDietaryPreferences(String dietaryPreferences) {
        this.dietaryPreferences = dietaryPreferences;
    }
}
