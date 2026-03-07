package com.annabenson.viand;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RecipeDetail {

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("image")
    private String image;

    @SerializedName("summary")
    private String summary;

    @SerializedName("extendedIngredients")
    private List<Ingredient> extendedIngredients;

    @SerializedName("analyzedInstructions")
    private List<AnalyzedInstruction> analyzedInstructions;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getImage() { return image; }
    public String getSummary() { return summary; }
    public List<Ingredient> getExtendedIngredients() { return extendedIngredients; }
    public List<AnalyzedInstruction> getAnalyzedInstructions() { return analyzedInstructions; }

    public static class AnalyzedInstruction {
        @SerializedName("steps")
        private List<Step> steps;

        public List<Step> getSteps() { return steps; }
    }

    public static class Step {
        @SerializedName("number")
        private int number;

        @SerializedName("step")
        private String step;

        public int getNumber() { return number; }
        public String getStep() { return step; }
    }
}
