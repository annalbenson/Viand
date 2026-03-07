package com.annabenson.viand.network;

import com.annabenson.viand.models.Recipe;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RecipeSearchResponse {

    @SerializedName("results")
    private List<Recipe> results;

    public List<Recipe> getResults() { return results; }
}
