package com.annabenson.viand;

import com.google.gson.annotations.SerializedName;

public class Ingredient {

    @SerializedName("name")
    private String name;

    @SerializedName("amount")
    private double amount;

    @SerializedName("unit")
    private String unit;

    public String getName() { return name; }
    public double getAmount() { return amount; }
    public String getUnit() { return unit; }
}
