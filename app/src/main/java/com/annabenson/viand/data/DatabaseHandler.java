package com.annabenson.viand.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.annabenson.viand.models.CustomRecipe;
import com.annabenson.viand.models.Recipe;
import com.annabenson.viand.models.TasteTag;
import com.annabenson.viand.models.UserAccount;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class DatabaseHandler extends SQLiteOpenHelper implements Serializable {

    private static final String TAG = "DatabaseHandler";
    private static final int DATABASE_VERSION = 7; // change schema --> increment version
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
    private static final String RECIPE_RATING = "Rating";
    private static final String RECIPE_MEAL_TYPE = "MealType";

    /* custom recipes table */

    private static final String TABLE_CUSTOM = "CustomRecipesTable";
    private static final String CUSTOM_ID = "Id";
    private static final String CUSTOM_TITLE = "Title";
    private static final String CUSTOM_INGREDIENTS = "Ingredients";
    private static final String CUSTOM_INSTRUCTIONS = "Instructions";

    /* taste profile table */

    private static final String TABLE_TASTE_PROFILE = "TasteProfileTable";
    private static final String TASTE_EMAIL = "UserEmail";
    private static final String TASTE_TAG = "Tag";
    private static final String TASTE_TAG_TYPE = "TagType";
    private static final String TASTE_SCORE = "Score";

    /* preference prompt log table */

    private static final String TABLE_PROMPT_LOG = "PreferencePromptLogTable";
    private static final String PROMPT_EMAIL = "UserEmail";
    private static final String PROMPT_TOPIC = "Topic";
    private static final String PROMPT_LAST_ASKED = "LastAskedAt";

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

    private static final String SQL_CREATE_CUSTOM_TABLE =
        "CREATE TABLE IF NOT EXISTS " + TABLE_CUSTOM + " (" +
                CUSTOM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CUSTOM_TITLE + " TEXT not null," +
                CUSTOM_INGREDIENTS + " TEXT," +
                CUSTOM_INSTRUCTIONS + " TEXT)"
            ;

    private static final String SQL_CREATE_TASTE_PROFILE_TABLE =
        "CREATE TABLE IF NOT EXISTS " + TABLE_TASTE_PROFILE + " (" +
                TASTE_EMAIL + " TEXT NOT NULL," +
                TASTE_TAG + " TEXT NOT NULL," +
                TASTE_TAG_TYPE + " TEXT NOT NULL," +
                TASTE_SCORE + " REAL NOT NULL DEFAULT 0," +
                "PRIMARY KEY (" + TASTE_EMAIL + ", " + TASTE_TAG + ", " + TASTE_TAG_TYPE + "))"
            ;

    private static final String SQL_CREATE_PREFERENCE_PROMPT_LOG_TABLE =
        "CREATE TABLE IF NOT EXISTS " + TABLE_PROMPT_LOG + " (" +
                "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                PROMPT_EMAIL + " TEXT NOT NULL," +
                PROMPT_TOPIC + " TEXT NOT NULL," +
                PROMPT_LAST_ASKED + " INTEGER NOT NULL," +
                "UNIQUE(" + PROMPT_EMAIL + ", " + PROMPT_TOPIC + "))"
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
        db.execSQL(SQL_CREATE_USER_ACCOUNTS_TABLE);
        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
        db.execSQL(SQL_CREATE_CUSTOM_TABLE);
        db.execSQL(SQL_CREATE_TASTE_PROFILE_TABLE);
        db.execSQL(SQL_CREATE_PREFERENCE_PROMPT_LOG_TABLE);
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
        if (oldVersion < 5) {
            db.execSQL(SQL_CREATE_TASTE_PROFILE_TABLE);
            db.execSQL(SQL_CREATE_PREFERENCE_PROMPT_LOG_TABLE);
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE " + TABLE_FAVORITES + " ADD COLUMN " + RECIPE_RATING + " TEXT");
        }
        if (oldVersion < 7) {
            db.execSQL("ALTER TABLE " + TABLE_FAVORITES + " ADD COLUMN " + RECIPE_MEAL_TYPE + " TEXT");
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

    public void addFavorite(int id, String title, String imageUrl, String mealType) {
        ContentValues values = new ContentValues();
        values.put(RECIPE_ID, id);
        values.put(RECIPE_TITLE, title);
        values.put(RECIPE_IMAGE_URL, imageUrl);
        values.put(RECIPE_MEAL_TYPE, mealType);
        database.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public List<Recipe> loadFavorites() {
        List<Recipe> favorites = new ArrayList<>();
        String select = "SELECT " + RECIPE_ID + ", " + RECIPE_TITLE + ", " +
                RECIPE_IMAGE_URL + ", " + RECIPE_RATING + ", " + RECIPE_MEAL_TYPE +
                " FROM " + TABLE_FAVORITES;
        Cursor cursor = database.rawQuery(select, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String imageUrl = cursor.getString(2);
                String rating = cursor.getString(3);   // may be null
                String mealType = cursor.getString(4); // may be null
                Recipe recipe = new Recipe(id, title, imageUrl);
                recipe.setRating(rating);
                recipe.setMealType(mealType != null ? mealType : "Other");
                favorites.add(recipe);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return favorites;
    }

    public void updateFavoriteRating(int recipeId, String rating) {
        ContentValues values = new ContentValues();
        values.put(RECIPE_RATING, rating);
        database.update(TABLE_FAVORITES, values, RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)});
    }

    public void updateFavoriteMealType(int recipeId, String mealType) {
        ContentValues values = new ContentValues();
        values.put(RECIPE_MEAL_TYPE, mealType);
        database.update(TABLE_FAVORITES, values, RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)});
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

    // ── Taste Profile Methods ──────────────────────────────────────────────────

    public void upsertTasteScore(String email, String tag, String tagType, float delta) {
        String select = "SELECT " + TASTE_SCORE + " FROM " + TABLE_TASTE_PROFILE +
                " WHERE " + TASTE_EMAIL + "=? AND " + TASTE_TAG + "=? AND " + TASTE_TAG_TYPE + "=?";
        Cursor cursor = database.rawQuery(select, new String[]{email, tag, tagType});
        if (cursor != null && cursor.moveToFirst()) {
            float current = cursor.getFloat(0);
            cursor.close();
            ContentValues values = new ContentValues();
            values.put(TASTE_SCORE, current + delta);
            database.update(TABLE_TASTE_PROFILE, values,
                    TASTE_EMAIL + "=? AND " + TASTE_TAG + "=? AND " + TASTE_TAG_TYPE + "=?",
                    new String[]{email, tag, tagType});
        } else {
            if (cursor != null) cursor.close();
            ContentValues values = new ContentValues();
            values.put(TASTE_EMAIL, email);
            values.put(TASTE_TAG, tag);
            values.put(TASTE_TAG_TYPE, tagType);
            values.put(TASTE_SCORE, delta);
            database.insertWithOnConflict(TABLE_TASTE_PROFILE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public void setTasteScore(String email, String tag, String tagType, float score) {
        ContentValues values = new ContentValues();
        values.put(TASTE_EMAIL, email);
        values.put(TASTE_TAG, tag);
        values.put(TASTE_TAG_TYPE, tagType);
        values.put(TASTE_SCORE, score);
        database.insertWithOnConflict(TABLE_TASTE_PROFILE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<TasteTag> loadTasteProfile(String email) {
        List<TasteTag> tags = new ArrayList<>();
        String select = "SELECT " + TASTE_TAG + ", " + TASTE_TAG_TYPE + ", " + TASTE_SCORE +
                " FROM " + TABLE_TASTE_PROFILE + " WHERE " + TASTE_EMAIL + "=?";
        Cursor cursor = database.rawQuery(select, new String[]{email});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String tag = cursor.getString(0);
                String tagType = cursor.getString(1);
                float score = cursor.getFloat(2);
                tags.add(new TasteTag(tag, tagType, score));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return tags;
    }

    public List<TasteTag> loadCuisineProfile(String email) {
        List<TasteTag> tags = new ArrayList<>();
        String select = "SELECT " + TASTE_TAG + ", " + TASTE_TAG_TYPE + ", " + TASTE_SCORE +
                " FROM " + TABLE_TASTE_PROFILE +
                " WHERE " + TASTE_EMAIL + "=? AND " + TASTE_TAG_TYPE + "='cuisine'" +
                " ORDER BY " + TASTE_SCORE + " DESC";
        Cursor cursor = database.rawQuery(select, new String[]{email});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String tag = cursor.getString(0);
                String tagType = cursor.getString(1);
                float score = cursor.getFloat(2);
                tags.add(new TasteTag(tag, tagType, score));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return tags;
    }

    public long getLastPromptTime(String email, String topic) {
        String select = "SELECT " + PROMPT_LAST_ASKED + " FROM " + TABLE_PROMPT_LOG +
                " WHERE " + PROMPT_EMAIL + "=? AND " + PROMPT_TOPIC + "=?";
        Cursor cursor = database.rawQuery(select, new String[]{email, topic});
        if (cursor != null && cursor.moveToFirst()) {
            long time = cursor.getLong(0);
            cursor.close();
            return time;
        }
        if (cursor != null) cursor.close();
        return -1;
    }

    public void upsertPromptLog(String email, String topic, long timestampSeconds) {
        ContentValues values = new ContentValues();
        values.put(PROMPT_EMAIL, email);
        values.put(PROMPT_TOPIC, topic);
        values.put(PROMPT_LAST_ASKED, timestampSeconds);
        database.insertWithOnConflict(TABLE_PROMPT_LOG, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
