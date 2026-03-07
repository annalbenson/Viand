package com.annabenson.viand;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class DatabaseHandler extends SQLiteOpenHelper implements Serializable {

    private static final String TAG = "DatabaseHandler";
    private static final int DATABASE_VERSION = 4; // change schema --> increment version
    private static final String DATABASE_NAME   = "ViandDatabase";

    /* accounts table */

    private static final String TABLE_ACCOUNTS = "UserAccountsTable";
    private static final String EMAIL = "Email";
    private static final String PASSWORD = "Password";
    private static final String NAME = "Name";
    private static final String DIETARY_PREFS = "DietaryPreferences";


    /* favorites table */

    private static final String TABLE_FAVORITES = "FavoritesTable";
    private static final String RECIPE_ID = "RecipeId";
    private static final String RECIPE_TITLE = "Title";
    private static final String RECIPE_IMAGE_URL = "ImageUrl";

    /* table creation */
    private static final String SQL_CREATE_USER_ACCOUNTS_TABLE =
        "CREATE TABLE " + TABLE_ACCOUNTS + " (" +
                EMAIL + " TEXT not null unique," +
                PASSWORD + " TEXT not null," +
                NAME + " TEXT not null," +
                DIETARY_PREFS + " TEXT)"
            ;

    private static final String SQL_CREATE_FAVORITES_TABLE =
        "CREATE TABLE IF NOT EXISTS " + TABLE_FAVORITES + " (" +
                RECIPE_ID + " INTEGER not null unique," +
                RECIPE_TITLE + " TEXT not null," +
                RECIPE_IMAGE_URL + " TEXT)"
            ;

    /* custom recipes table */

    private static final String TABLE_CUSTOM = "CustomRecipesTable";
    private static final String CUSTOM_ID = "Id";
    private static final String CUSTOM_TITLE = "Title";
    private static final String CUSTOM_INGREDIENTS = "Ingredients";
    private static final String CUSTOM_INSTRUCTIONS = "Instructions";

    private static final String SQL_CREATE_CUSTOM_TABLE =
        "CREATE TABLE IF NOT EXISTS " + TABLE_CUSTOM + " (" +
                CUSTOM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CUSTOM_TITLE + " TEXT not null," +
                CUSTOM_INGREDIENTS + " TEXT," +
                CUSTOM_INSTRUCTIONS + " TEXT)"
            ;

    private SQLiteDatabase database;

    public DatabaseHandler(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        database = getWritableDatabase();
        Log.d(TAG, "DatabaseHandler: Created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // only called if database DNE
        Log.d(TAG, "onCreate: Making new database");
        // create user accounts table
        db.execSQL(SQL_CREATE_USER_ACCOUNTS_TABLE);
        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
        db.execSQL(SQL_CREATE_CUSTOM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(SQL_CREATE_FAVORITES_TABLE);
        }
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
            db.execSQL(SQL_CREATE_USER_ACCOUNTS_TABLE);
        }
        if (oldVersion < 4) {
            db.execSQL(SQL_CREATE_CUSTOM_TABLE);
        }
    }


    public void addUserAccount(UserAccount account) {
        Log.d(TAG, "addAccount: ");
        ContentValues values = new ContentValues();
        values.put(EMAIL, account.getEmail());
        values.put(PASSWORD, account.getPassword());
        values.put(NAME, account.getName());
        values.put(DIETARY_PREFS, account.getDietaryPreferences());
        database.insertWithOnConflict(TABLE_ACCOUNTS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public UserAccount loadUserAccount(String email, String password) {
        String select = "SELECT * FROM " + TABLE_ACCOUNTS +
                " WHERE " + EMAIL + "=? AND " + PASSWORD + "=?";
        Cursor cursor = database.rawQuery(select, new String[]{email, password});
        if (cursor != null && cursor.getCount() == 1) {
            cursor.moveToFirst();
            String savedEmail = cursor.getString(0);
            String savedPass = cursor.getString(1);
            String savedName = cursor.getString(2);
            String savedPrefs = cursor.getString(3);
            cursor.close();
            return new UserAccount(savedEmail, savedPass, savedName, savedPrefs);
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public void addFavorite(int id, String title, String imageUrl) {
        ContentValues values = new ContentValues();
        values.put(RECIPE_ID, id);
        values.put(RECIPE_TITLE, title);
        values.put(RECIPE_IMAGE_URL, imageUrl);
        database.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public List<Recipe> loadFavorites() {
        List<Recipe> favorites = new ArrayList<>();
        String select = "SELECT * FROM " + TABLE_FAVORITES;
        Cursor cursor = database.rawQuery(select, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String imageUrl = cursor.getString(2);
                favorites.add(new Recipe(id, title, imageUrl));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return favorites;
    }

    public void deleteFavorite(int recipeId) {
        database.delete(TABLE_FAVORITES, RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)});
    }

    public void addCustomRecipe(CustomRecipe recipe) {
        ContentValues values = new ContentValues();
        values.put(CUSTOM_TITLE, recipe.getTitle());
        values.put(CUSTOM_INGREDIENTS, recipe.getIngredients());
        values.put(CUSTOM_INSTRUCTIONS, recipe.getInstructions());
        database.insert(TABLE_CUSTOM, null, values);
    }

    public List<CustomRecipe> loadCustomRecipes() {
        List<CustomRecipe> recipes = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_CUSTOM, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String ingredients = cursor.getString(2);
                String instructions = cursor.getString(3);
                recipes.add(new CustomRecipe(id, title, ingredients, instructions));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return recipes;
    }

    public void updateCustomRecipe(CustomRecipe recipe) {
        ContentValues values = new ContentValues();
        values.put(CUSTOM_TITLE, recipe.getTitle());
        values.put(CUSTOM_INGREDIENTS, recipe.getIngredients());
        values.put(CUSTOM_INSTRUCTIONS, recipe.getInstructions());
        database.update(TABLE_CUSTOM, values, CUSTOM_ID + "=?",
                new String[]{String.valueOf(recipe.getId())});
    }

    public void deleteCustomRecipe(int id) {
        database.delete(TABLE_CUSTOM, CUSTOM_ID + "=?",
                new String[]{String.valueOf(id)});
    }

}
