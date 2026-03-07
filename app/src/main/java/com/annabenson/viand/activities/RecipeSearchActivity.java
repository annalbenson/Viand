package com.annabenson.viand.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annabenson.viand.BuildConfig;
import com.annabenson.viand.adapters.CustomRecipeAdapter;
import com.annabenson.viand.adapters.RecipeAdapter;
import com.annabenson.viand.adapters.RecipeFavoriteAdapter;
import com.annabenson.viand.data.DatabaseHandler;
import com.annabenson.viand.models.CustomRecipe;
import com.annabenson.viand.models.Recipe;
import com.annabenson.viand.network.RecipeSearchResponse;
import com.annabenson.viand.network.RetrofitClient;
import com.annabenson.viand.network.SpoonacularService;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeSearchActivity extends AppCompatActivity {

    private TextView greetingText;
    private Button signOutButton;
    private EditText searchQueryInput;
    private Button searchButton;
    private Button pantryButton;
    private RecyclerView searchResultsRecyclerView;
    private TextView searchResultsLabel;
    private RecyclerView favoritesRecyclerView;
    private RecyclerView customRecipesRecyclerView;

    private RecipeAdapter recipeAdapter;
    private RecipeFavoriteAdapter favoriteAdapter;
    private CustomRecipeAdapter customRecipeAdapter;
    private List<Recipe> searchResults = new ArrayList<>();
    private List<Recipe> favorites = new ArrayList<>();
    private List<CustomRecipe> customRecipes = new ArrayList<>();

    private SpoonacularService spoonacularService;
    private DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.annabenson.viand.R.layout.activity_recipe_search);

        greetingText = findViewById(com.annabenson.viand.R.id.greetingText);
        signOutButton = findViewById(com.annabenson.viand.R.id.signOutButton);
        searchQueryInput = findViewById(com.annabenson.viand.R.id.searchQueryInput);
        searchButton = findViewById(com.annabenson.viand.R.id.searchButton);
        pantryButton = findViewById(com.annabenson.viand.R.id.pantryButton);

        // Greeting — prefer intent extra, fall back to SharedPreferences
        String name = getIntent().getStringExtra("USER_NAME");
        if (name == null || name.isEmpty()) {
            name = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                    .getString(LoginActivity.KEY_USER_NAME, "");
        }
        greetingText.setText("Hello, " + (name.isEmpty() ? "there" : name) + "!");

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                        .edit().clear().apply();
                Intent intent = new Intent(RecipeSearchActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        searchResultsRecyclerView = findViewById(com.annabenson.viand.R.id.searchResultsRecyclerView);
        searchResultsLabel = findViewById(com.annabenson.viand.R.id.searchResultsLabel);
        favoritesRecyclerView = findViewById(com.annabenson.viand.R.id.favoritesRecyclerView);
        customRecipesRecyclerView = findViewById(com.annabenson.viand.R.id.customRecipesRecyclerView);

        databaseHandler = new DatabaseHandler(this);
        spoonacularService = RetrofitClient.getInstance().create(SpoonacularService.class);

        setupSearchResultsRecyclerView();
        setupFavoritesRecyclerView();
        setupCustomRecipesRecyclerView();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });

        pantryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RecipeSearchActivity.this, PantryActivity.class));
            }
        });

        searchQueryInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
        loadCustomRecipes();
    }

    private void setupSearchResultsRecyclerView() {
        recipeAdapter = new RecipeAdapter(this, searchResults);
        searchResultsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        searchResultsRecyclerView.setAdapter(recipeAdapter);
    }

    private void setupFavoritesRecyclerView() {
        favoriteAdapter = new RecipeFavoriteAdapter(this, favorites,
                new RecipeFavoriteAdapter.OnDeleteListener() {
                    @Override
                    public void onDelete(Recipe recipe, int position) {
                        databaseHandler.deleteFavorite(recipe.getId());
                        favorites.remove(position);
                        favoriteAdapter.notifyItemRemoved(position);
                    }
                });
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoritesRecyclerView.setAdapter(favoriteAdapter);
        favoritesRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setupCustomRecipesRecyclerView() {
        customRecipeAdapter = new CustomRecipeAdapter(this, customRecipes);
        customRecipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        customRecipesRecyclerView.setAdapter(customRecipeAdapter);
        customRecipesRecyclerView.setNestedScrollingEnabled(false);
    }

    private void loadFavorites() {
        List<Recipe> loaded = databaseHandler.loadFavorites();
        favorites.clear();
        favorites.addAll(loaded);
        favoriteAdapter.notifyDataSetChanged();
    }

    private void loadCustomRecipes() {
        List<CustomRecipe> loaded = databaseHandler.loadCustomRecipes();
        customRecipes.clear();
        customRecipes.addAll(loaded);
        customRecipeAdapter.notifyDataSetChanged();
    }

    private void performSearch() {
        String query = searchQueryInput.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Enter a search term", Toast.LENGTH_SHORT).show();
            return;
        }

        spoonacularService.searchRecipes(query, 10, BuildConfig.SPOONACULAR_KEY)
                .enqueue(new Callback<RecipeSearchResponse>() {
                    @Override
                    public void onResponse(Call<RecipeSearchResponse> call,
                                           Response<RecipeSearchResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Recipe> results = response.body().getResults();
                            searchResults.clear();
                            if (results != null) {
                                searchResults.addAll(results);
                            }
                            recipeAdapter.notifyDataSetChanged();
                            searchResultsLabel.setVisibility(View.VISIBLE);
                            searchResultsRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(RecipeSearchActivity.this,
                                    "Search failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RecipeSearchResponse> call, Throwable t) {
                        Toast.makeText(RecipeSearchActivity.this,
                                "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
