package com.annabenson.viand.network;

import com.annabenson.viand.models.RecipeDetail;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SpoonacularService {

    @GET("recipes/complexSearch")
    Call<RecipeSearchResponse> searchRecipes(
            @Query("query") String query,
            @Query("number") int number,
            @Query("apiKey") String apiKey
    );

    @GET("recipes/{id}/information")
    Call<RecipeDetail> getRecipeDetail(
            @Path("id") int id,
            @Query("apiKey") String apiKey
    );
}
