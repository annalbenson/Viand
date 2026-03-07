package com.annabenson.viand.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.annabenson.viand.models.CustomRecipe;
import com.annabenson.viand.models.MealLogEntry;
import com.annabenson.viand.models.MealPlanEntry;
import com.annabenson.viand.models.Recipe;
import com.annabenson.viand.models.TasteTag;
import com.annabenson.viand.models.UserAccount;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper implements Serializable {

    private static final String TAG = "DatabaseHandler";
    private static final int DATABASE_VERSION = 11;
    private static final String DATABASE_NAME = "ViandDatabase";

    /* ── accounts ─────────────────────────────────────────────────────────── */
    private static final String TABLE_ACCOUNTS = "UserAccountsTable";
    private static final String ACCT_ID       = "Id";
    private static final String EMAIL         = "Email";
    private static final String PASSWORD      = "Password";
    private static final String NAME          = "Name";
    private static final String DIETARY_PREFS = "DietaryPreferences";

    /* ── shared FK column (→ UserAccountsTable.Id) ────────────────────────── */
    private static final String USER_ID_FK = "UserId";

    /* ── favorites ────────────────────────────────────────────────────────── */
    private static final String TABLE_FAVORITES  = "FavoritesTable";
    private static final String RECIPE_ID        = "RecipeId";
    private static final String RECIPE_TITLE     = "Title";
    private static final String RECIPE_IMAGE_URL = "ImageUrl";
    private static final String RECIPE_RATING    = "Rating";
    private static final String RECIPE_MEAL_TYPE = "MealType";

    /* ── custom recipes ───────────────────────────────────────────────────── */
    private static final String TABLE_CUSTOM        = "CustomRecipesTable";
    private static final String CUSTOM_ID           = "Id";
    private static final String CUSTOM_TITLE        = "Title";
    private static final String CUSTOM_INGREDIENTS  = "Ingredients";
    private static final String CUSTOM_INSTRUCTIONS = "Instructions";

    /* ── taste profile ────────────────────────────────────────────────────── */
    private static final String TABLE_TASTE_PROFILE = "TasteProfileTable";
    private static final String TASTE_TAG           = "Tag";
    private static final String TASTE_TAG_TYPE      = "TagType";
    private static final String TASTE_SCORE         = "Score";

    /* ── meal log ─────────────────────────────────────────────────────────── */
    private static final String TABLE_MEAL_LOG     = "MealLogTable";
    private static final String MEAL_LOG_ID        = "Id";
    private static final String MEAL_LOG_RECIPE_ID = "RecipeId";
    private static final String MEAL_LOG_TITLE     = "RecipeTitle";
    private static final String MEAL_LOG_IMAGE     = "RecipeImage";
    private static final String MEAL_LOG_MEAL_TYPE = "MealType";
    private static final String MEAL_LOG_MADE_AT   = "MadeAt";

    /* ── meal plan ────────────────────────────────────────────────────────── */
    private static final String TABLE_MEAL_PLAN       = "MealPlanTable";
    private static final String MEAL_PLAN_ID          = "Id";
    private static final String MEAL_PLAN_RECIPE_ID   = "RecipeId";
    private static final String MEAL_PLAN_TITLE       = "RecipeTitle";
    private static final String MEAL_PLAN_IMAGE       = "RecipeImage";
    private static final String MEAL_PLAN_DAY         = "DayOfWeek";
    private static final String MEAL_PLAN_MEAL_TYPE   = "MealType";
    private static final String MEAL_PLAN_INGREDIENTS = "Ingredients";
    private static final String MEAL_PLAN_WEEK_START  = "WeekStart";

    /* ── preference prompt log ────────────────────────────────────────────── */
    private static final String TABLE_PROMPT_LOG  = "PreferencePromptLogTable";
    private static final String PROMPT_TOPIC      = "Topic";
    private static final String PROMPT_LAST_ASKED = "LastAskedAt";

    /* ── CREATE TABLE statements ──────────────────────────────────────────── */

    private static final String SQL_CREATE_ACCOUNTS =
        "CREATE TABLE " + TABLE_ACCOUNTS + " (" +
                ACCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                EMAIL + " TEXT NOT NULL UNIQUE," +
                PASSWORD + " TEXT NOT NULL," +
                NAME + " TEXT NOT NULL," +
                DIETARY_PREFS + " TEXT)";

    private static final String SQL_CREATE_FAVORITES =
        "CREATE TABLE IF NOT EXISTS " + TABLE_FAVORITES + " (" +
                USER_ID_FK + " INTEGER NOT NULL REFERENCES " + TABLE_ACCOUNTS + "(" + ACCT_ID + ") ON DELETE CASCADE," +
                RECIPE_ID + " INTEGER NOT NULL," +
                RECIPE_TITLE + " TEXT NOT NULL," +
                RECIPE_IMAGE_URL + " TEXT," +
                RECIPE_RATING + " TEXT," +
                RECIPE_MEAL_TYPE + " TEXT," +
                "PRIMARY KEY (" + USER_ID_FK + ", " + RECIPE_ID + "))";

    private static final String SQL_CREATE_CUSTOM =
        "CREATE TABLE IF NOT EXISTS " + TABLE_CUSTOM + " (" +
                CUSTOM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                USER_ID_FK + " INTEGER NOT NULL REFERENCES " + TABLE_ACCOUNTS + "(" + ACCT_ID + ") ON DELETE CASCADE," +
                CUSTOM_TITLE + " TEXT NOT NULL," +
                CUSTOM_INGREDIENTS + " TEXT," +
                CUSTOM_INSTRUCTIONS + " TEXT)";

    private static final String SQL_CREATE_TASTE_PROFILE =
        "CREATE TABLE IF NOT EXISTS " + TABLE_TASTE_PROFILE + " (" +
                USER_ID_FK + " INTEGER NOT NULL REFERENCES " + TABLE_ACCOUNTS + "(" + ACCT_ID + ") ON DELETE CASCADE," +
                TASTE_TAG + " TEXT NOT NULL," +
                TASTE_TAG_TYPE + " TEXT NOT NULL," +
                TASTE_SCORE + " REAL NOT NULL DEFAULT 0," +
                "PRIMARY KEY (" + USER_ID_FK + ", " + TASTE_TAG + ", " + TASTE_TAG_TYPE + "))";

    private static final String SQL_CREATE_MEAL_LOG =
        "CREATE TABLE IF NOT EXISTS " + TABLE_MEAL_LOG + " (" +
                MEAL_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                USER_ID_FK + " INTEGER NOT NULL REFERENCES " + TABLE_ACCOUNTS + "(" + ACCT_ID + ") ON DELETE CASCADE," +
                MEAL_LOG_RECIPE_ID + " INTEGER NOT NULL," +
                MEAL_LOG_TITLE + " TEXT NOT NULL," +
                MEAL_LOG_IMAGE + " TEXT," +
                MEAL_LOG_MEAL_TYPE + " TEXT," +
                MEAL_LOG_MADE_AT + " INTEGER NOT NULL)";

    private static final String SQL_CREATE_MEAL_PLAN =
        "CREATE TABLE IF NOT EXISTS " + TABLE_MEAL_PLAN + " (" +
                MEAL_PLAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                USER_ID_FK + " INTEGER NOT NULL REFERENCES " + TABLE_ACCOUNTS + "(" + ACCT_ID + ") ON DELETE CASCADE," +
                MEAL_PLAN_RECIPE_ID + " INTEGER NOT NULL," +
                MEAL_PLAN_TITLE + " TEXT NOT NULL," +
                MEAL_PLAN_IMAGE + " TEXT," +
                MEAL_PLAN_DAY + " TEXT," +
                MEAL_PLAN_MEAL_TYPE + " TEXT," +
                MEAL_PLAN_INGREDIENTS + " TEXT," +
                MEAL_PLAN_WEEK_START + " INTEGER NOT NULL)";

    // (UserId, Topic) is the natural composite PK — no surrogate Id needed
    private static final String SQL_CREATE_PROMPT_LOG =
        "CREATE TABLE IF NOT EXISTS " + TABLE_PROMPT_LOG + " (" +
                USER_ID_FK + " INTEGER NOT NULL REFERENCES " + TABLE_ACCOUNTS + "(" + ACCT_ID + ") ON DELETE CASCADE," +
                PROMPT_TOPIC + " TEXT NOT NULL," +
                PROMPT_LAST_ASKED + " INTEGER NOT NULL," +
                "PRIMARY KEY (" + USER_ID_FK + ", " + PROMPT_TOPIC + "))";

    private SQLiteDatabase database;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
        Log.d(TAG, "DatabaseHandler: Created");
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: Making new database");
        db.execSQL(SQL_CREATE_ACCOUNTS);
        db.execSQL(SQL_CREATE_FAVORITES);
        db.execSQL(SQL_CREATE_CUSTOM);
        db.execSQL(SQL_CREATE_TASTE_PROFILE);
        db.execSQL(SQL_CREATE_PROMPT_LOG);
        db.execSQL(SQL_CREATE_MEAL_LOG);
        db.execSQL(SQL_CREATE_MEAL_PLAN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Preserve historical upgrade path
        if (oldVersion < 2) { db.execSQL(SQL_CREATE_FAVORITES); }
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
            db.execSQL(SQL_CREATE_ACCOUNTS);
        }
        if (oldVersion < 4) { db.execSQL(SQL_CREATE_CUSTOM); }
        if (oldVersion < 5) {
            db.execSQL(SQL_CREATE_TASTE_PROFILE);
            db.execSQL(SQL_CREATE_PROMPT_LOG);
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE " + TABLE_FAVORITES + " ADD COLUMN " + RECIPE_RATING + " TEXT");
        }
        if (oldVersion < 7) {
            db.execSQL("ALTER TABLE " + TABLE_FAVORITES + " ADD COLUMN " + RECIPE_MEAL_TYPE + " TEXT");
        }
        if (oldVersion < 8) { db.execSQL(SQL_CREATE_MEAL_LOG); }
        if (oldVersion < 9) { db.execSQL(SQL_CREATE_MEAL_PLAN); }
        if (oldVersion < 10) {
            // Rebuild FavoritesTable with UserEmail (text FK, pre-integer-PK era)
            db.execSQL("CREATE TABLE FavoritesTable_v10 (" +
                    "UserEmail TEXT NOT NULL DEFAULT ''," + RECIPE_ID + " INTEGER NOT NULL," +
                    RECIPE_TITLE + " TEXT NOT NULL," + RECIPE_IMAGE_URL + " TEXT," +
                    RECIPE_RATING + " TEXT," + RECIPE_MEAL_TYPE + " TEXT," +
                    "PRIMARY KEY (UserEmail, " + RECIPE_ID + "))");
            db.execSQL("INSERT INTO FavoritesTable_v10 SELECT '', " + RECIPE_ID + "," +
                    RECIPE_TITLE + "," + RECIPE_IMAGE_URL + "," + RECIPE_RATING + "," +
                    RECIPE_MEAL_TYPE + " FROM " + TABLE_FAVORITES);
            db.execSQL("DROP TABLE " + TABLE_FAVORITES);
            db.execSQL("ALTER TABLE FavoritesTable_v10 RENAME TO " + TABLE_FAVORITES);
            db.execSQL("ALTER TABLE " + TABLE_CUSTOM + " ADD COLUMN UserEmail TEXT NOT NULL DEFAULT ''");
        }
        if (oldVersion < 11) {
            // ── Step 1: Add Id PK to UserAccountsTable ──
            db.execSQL("CREATE TABLE UserAccountsTable_new (" +
                    ACCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    EMAIL + " TEXT NOT NULL UNIQUE," + PASSWORD + " TEXT NOT NULL," +
                    NAME + " TEXT NOT NULL," + DIETARY_PREFS + " TEXT)");
            db.execSQL("INSERT INTO UserAccountsTable_new (" + EMAIL + "," + PASSWORD + "," +
                    NAME + "," + DIETARY_PREFS + ") SELECT " + EMAIL + "," + PASSWORD + "," +
                    NAME + "," + DIETARY_PREFS + " FROM " + TABLE_ACCOUNTS);
            db.execSQL("DROP TABLE " + TABLE_ACCOUNTS);
            db.execSQL("ALTER TABLE UserAccountsTable_new RENAME TO " + TABLE_ACCOUNTS);

            // ── Step 2: Migrate FavoritesTable (UserEmail → UserId) ──
            db.execSQL("CREATE TABLE FavoritesTable_new (" +
                    USER_ID_FK + " INTEGER NOT NULL REFERENCES " + TABLE_ACCOUNTS + "(" + ACCT_ID + ") ON DELETE CASCADE," +
                    RECIPE_ID + " INTEGER NOT NULL," + RECIPE_TITLE + " TEXT NOT NULL," +
                    RECIPE_IMAGE_URL + " TEXT," + RECIPE_RATING + " TEXT," + RECIPE_MEAL_TYPE + " TEXT," +
                    "PRIMARY KEY (" + USER_ID_FK + ", " + RECIPE_ID + "))");
            db.execSQL("INSERT INTO FavoritesTable_new SELECT a." + ACCT_ID + ",f." + RECIPE_ID +
                    ",f." + RECIPE_TITLE + ",f." + RECIPE_IMAGE_URL + ",f." + RECIPE_RATING +
                    ",f." + RECIPE_MEAL_TYPE + " FROM " + TABLE_FAVORITES + " f" +
                    " JOIN " + TABLE_ACCOUNTS + " a ON a." + EMAIL + "=f.UserEmail");
            db.execSQL("DROP TABLE " + TABLE_FAVORITES);
            db.execSQL("ALTER TABLE FavoritesTable_new RENAME TO " + TABLE_FAVORITES);

            // ── Step 3: Migrate CustomRecipesTable ──
            db.execSQL("CREATE TABLE CustomRecipesTable_new (" +
                    CUSTOM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    USER_ID_FK + " INTEGER NOT NULL REFERENCES " + TABLE_ACCOUNTS + "(" + ACCT_ID + ") ON DELETE CASCADE," +
                    CUSTOM_TITLE + " TEXT NOT NULL," + CUSTOM_INGREDIENTS + " TEXT," + CUSTOM_INSTRUCTIONS + " TEXT)");
            db.execSQL("INSERT INTO CustomRecipesTable_new SELECT c." + CUSTOM_ID + ",a." + ACCT_ID +
                    ",c." + CUSTOM_TITLE + ",c." + CUSTOM_INGREDIENTS + ",c." + CUSTOM_INSTRUCTIONS +
                    " FROM " + TABLE_CUSTOM + " c JOIN " + TABLE_ACCOUNTS + " a ON a." + EMAIL + "=c.UserEmail");
            db.execSQL("DROP TABLE " + TABLE_CUSTOM);
            db.execSQL("ALTER TABLE CustomRecipesTable_new RENAME TO " + TABLE_CUSTOM);

            // ── Step 4: Migrate TasteProfileTable ──
            db.execSQL("CREATE TABLE TasteProfileTable_new (" +
                    USER_ID_FK + " INTEGER NOT NULL REFERENCES " + TABLE_ACCOUNTS + "(" + ACCT_ID + ") ON DELETE CASCADE," +
                    TASTE_TAG + " TEXT NOT NULL," + TASTE_TAG_TYPE + " TEXT NOT NULL," +
                    TASTE_SCORE + " REAL NOT NULL DEFAULT 0," +
                    "PRIMARY KEY (" + USER_ID_FK + "," + TASTE_TAG + "," + TASTE_TAG_TYPE + "))");
            db.execSQL("INSERT INTO TasteProfileTable_new SELECT a." + ACCT_ID + ",t." + TASTE_TAG +
                    ",t." + TASTE_TAG_TYPE + ",t." + TASTE_SCORE +
                    " FROM " + TABLE_TASTE_PROFILE + " t JOIN " + TABLE_ACCOUNTS + " a ON a." + EMAIL + "=t.UserEmail");
            db.execSQL("DROP TABLE " + TABLE_TASTE_PROFILE);
            db.execSQL("ALTER TABLE TasteProfileTable_new RENAME TO " + TABLE_TASTE_PROFILE);

            // ── Step 5: Migrate MealLogTable ──
            db.execSQL("CREATE TABLE MealLogTable_new (" +
                    MEAL_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    USER_ID_FK + " INTEGER NOT NULL REFERENCES " + TABLE_ACCOUNTS + "(" + ACCT_ID + ") ON DELETE CASCADE," +
                    MEAL_LOG_RECIPE_ID + " INTEGER NOT NULL," + MEAL_LOG_TITLE + " TEXT NOT NULL," +
                    MEAL_LOG_IMAGE + " TEXT," + MEAL_LOG_MEAL_TYPE + " TEXT," + MEAL_LOG_MADE_AT + " INTEGER NOT NULL)");
            db.execSQL("INSERT INTO MealLogTable_new SELECT m." + MEAL_LOG_ID + ",a." + ACCT_ID +
                    ",m." + MEAL_LOG_RECIPE_ID + ",m." + MEAL_LOG_TITLE + ",m." + MEAL_LOG_IMAGE +
                    ",m." + MEAL_LOG_MEAL_TYPE + ",m." + MEAL_LOG_MADE_AT +
                    " FROM " + TABLE_MEAL_LOG + " m JOIN " + TABLE_ACCOUNTS + " a ON a." + EMAIL + "=m.UserEmail");
            db.execSQL("DROP TABLE " + TABLE_MEAL_LOG);
            db.execSQL("ALTER TABLE MealLogTable_new RENAME TO " + TABLE_MEAL_LOG);

            // ── Step 6: Migrate MealPlanTable ──
            db.execSQL("CREATE TABLE MealPlanTable_new (" +
                    MEAL_PLAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    USER_ID_FK + " INTEGER NOT NULL REFERENCES " + TABLE_ACCOUNTS + "(" + ACCT_ID + ") ON DELETE CASCADE," +
                    MEAL_PLAN_RECIPE_ID + " INTEGER NOT NULL," + MEAL_PLAN_TITLE + " TEXT NOT NULL," +
                    MEAL_PLAN_IMAGE + " TEXT," + MEAL_PLAN_DAY + " TEXT," + MEAL_PLAN_MEAL_TYPE + " TEXT," +
                    MEAL_PLAN_INGREDIENTS + " TEXT," + MEAL_PLAN_WEEK_START + " INTEGER NOT NULL)");
            db.execSQL("INSERT INTO MealPlanTable_new SELECT p." + MEAL_PLAN_ID + ",a." + ACCT_ID +
                    ",p." + MEAL_PLAN_RECIPE_ID + ",p." + MEAL_PLAN_TITLE + ",p." + MEAL_PLAN_IMAGE +
                    ",p." + MEAL_PLAN_DAY + ",p." + MEAL_PLAN_MEAL_TYPE + ",p." + MEAL_PLAN_INGREDIENTS +
                    ",p." + MEAL_PLAN_WEEK_START +
                    " FROM " + TABLE_MEAL_PLAN + " p JOIN " + TABLE_ACCOUNTS + " a ON a." + EMAIL + "=p.UserEmail");
            db.execSQL("DROP TABLE " + TABLE_MEAL_PLAN);
            db.execSQL("ALTER TABLE MealPlanTable_new RENAME TO " + TABLE_MEAL_PLAN);

            // ── Step 7: Migrate PreferencePromptLogTable (drop redundant Id, use composite PK) ──
            db.execSQL("CREATE TABLE PreferencePromptLogTable_new (" +
                    USER_ID_FK + " INTEGER NOT NULL REFERENCES " + TABLE_ACCOUNTS + "(" + ACCT_ID + ") ON DELETE CASCADE," +
                    PROMPT_TOPIC + " TEXT NOT NULL," + PROMPT_LAST_ASKED + " INTEGER NOT NULL," +
                    "PRIMARY KEY (" + USER_ID_FK + "," + PROMPT_TOPIC + "))");
            db.execSQL("INSERT INTO PreferencePromptLogTable_new SELECT a." + ACCT_ID + ",p." + PROMPT_TOPIC +
                    ",p." + PROMPT_LAST_ASKED + " FROM " + TABLE_PROMPT_LOG + " p" +
                    " JOIN " + TABLE_ACCOUNTS + " a ON a." + EMAIL + "=p.UserEmail");
            db.execSQL("DROP TABLE " + TABLE_PROMPT_LOG);
            db.execSQL("ALTER TABLE PreferencePromptLogTable_new RENAME TO " + TABLE_PROMPT_LOG);
        }
    }

    // ── Accounts ──────────────────────────────────────────────────────────────

    public void addUserAccount(UserAccount account) {
        Log.d(TAG, "addAccount: ");
        ContentValues values = new ContentValues();
        values.put(EMAIL, account.getEmail());
        values.put(PASSWORD, account.getPassword());
        values.put(NAME, account.getName());
        values.put(DIETARY_PREFS, account.getDietaryPreferences());
        database.insertWithOnConflict(TABLE_ACCOUNTS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public String getDietaryPreferences(int userId) {
        String select = "SELECT " + DIETARY_PREFS + " FROM " + TABLE_ACCOUNTS +
                " WHERE " + ACCT_ID + "=?";
        Cursor cursor = database.rawQuery(select, new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            String prefs = cursor.getString(0);
            cursor.close();
            return prefs != null ? prefs : "";
        }
        if (cursor != null) cursor.close();
        return "";
    }

    // Replaces a seeded (negative-ID) recipe with real Spoonacular data in both
    // FavoritesTable and MealLogTable. Because RecipeId is part of the favorites PK,
    // we INSERT the real row then DELETE the fake one instead of updating in place.
    public void updateSeedRecipe(int userId, int fakeId, int realId, String title, String imageUrl) {
        Cursor c = database.rawQuery(
                "SELECT " + RECIPE_MEAL_TYPE + "," + RECIPE_RATING +
                " FROM " + TABLE_FAVORITES +
                " WHERE " + USER_ID_FK + "=? AND " + RECIPE_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(fakeId)});
        if (c != null && c.moveToFirst()) {
            String mealType = c.getString(0);
            String rating   = c.getString(1);
            c.close();
            ContentValues cv = new ContentValues();
            cv.put(USER_ID_FK,       userId);
            cv.put(RECIPE_ID,        realId);
            cv.put(RECIPE_TITLE,     title);
            cv.put(RECIPE_IMAGE_URL, imageUrl);
            cv.put(RECIPE_MEAL_TYPE, mealType);
            cv.put(RECIPE_RATING,    rating);
            database.insertWithOnConflict(TABLE_FAVORITES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            database.delete(TABLE_FAVORITES,
                    USER_ID_FK + "=? AND " + RECIPE_ID + "=?",
                    new String[]{String.valueOf(userId), String.valueOf(fakeId)});
        } else {
            if (c != null) c.close();
        }
        // Update meal log rows that referenced the fake ID
        ContentValues logCv = new ContentValues();
        logCv.put(MEAL_LOG_RECIPE_ID, realId);
        logCv.put(MEAL_LOG_TITLE,     title);
        logCv.put(MEAL_LOG_IMAGE,     imageUrl);
        database.update(TABLE_MEAL_LOG, logCv,
                USER_ID_FK + "=? AND " + MEAL_LOG_RECIPE_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(fakeId)});
    }

    public void updateDietaryPreferences(int userId, String prefs) {
        ContentValues values = new ContentValues();
        values.put(DIETARY_PREFS, prefs);
        database.update(TABLE_ACCOUNTS, values, ACCT_ID + "=?",
                new String[]{String.valueOf(userId)});
    }

    public UserAccount loadUserAccount(String email, String password) {
        String select = "SELECT " + ACCT_ID + "," + EMAIL + "," + PASSWORD + "," + NAME + "," +
                DIETARY_PREFS + " FROM " + TABLE_ACCOUNTS +
                " WHERE " + EMAIL + "=? AND " + PASSWORD + "=?";
        Cursor cursor = database.rawQuery(select, new String[]{email, password});
        if (cursor != null && cursor.getCount() == 1) {
            cursor.moveToFirst();
            int    id         = cursor.getInt(0);
            String savedEmail = cursor.getString(1);
            String savedPass  = cursor.getString(2);
            String savedName  = cursor.getString(3);
            String savedPrefs = cursor.getString(4);
            cursor.close();
            return new UserAccount(id, savedEmail, savedPass, savedName, savedPrefs);
        }
        if (cursor != null) cursor.close();
        return null;
    }

    // ── Favorites ─────────────────────────────────────────────────────────────

    public void addFavorite(int userId, int recipeId, String title, String imageUrl, String mealType) {
        ContentValues values = new ContentValues();
        values.put(USER_ID_FK, userId);
        values.put(RECIPE_ID, recipeId);
        values.put(RECIPE_TITLE, title);
        values.put(RECIPE_IMAGE_URL, imageUrl);
        values.put(RECIPE_MEAL_TYPE, mealType);
        database.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public List<Recipe> loadFavorites(int userId) {
        List<Recipe> favorites = new ArrayList<>();
        String select = "SELECT " + RECIPE_ID + "," + RECIPE_TITLE + "," + RECIPE_IMAGE_URL + "," +
                RECIPE_RATING + "," + RECIPE_MEAL_TYPE +
                " FROM " + TABLE_FAVORITES + " WHERE " + USER_ID_FK + "=?";
        Cursor cursor = database.rawQuery(select, new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Recipe recipe = new Recipe(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
                recipe.setRating(cursor.getString(3));
                String mt = cursor.getString(4);
                recipe.setMealType(mt != null ? mt : "Other");
                favorites.add(recipe);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return favorites;
    }

    public void updateFavoriteRating(int userId, int recipeId, String rating) {
        ContentValues values = new ContentValues();
        values.put(RECIPE_RATING, rating);
        database.update(TABLE_FAVORITES, values,
                USER_ID_FK + "=? AND " + RECIPE_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(recipeId)});
    }

    public void updateFavoriteMealType(int userId, int recipeId, String mealType) {
        ContentValues values = new ContentValues();
        values.put(RECIPE_MEAL_TYPE, mealType);
        database.update(TABLE_FAVORITES, values,
                USER_ID_FK + "=? AND " + RECIPE_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(recipeId)});
    }

    public void deleteFavorite(int userId, int recipeId) {
        database.delete(TABLE_FAVORITES,
                USER_ID_FK + "=? AND " + RECIPE_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(recipeId)});
    }

    // ── Custom Recipes ────────────────────────────────────────────────────────

    public void addCustomRecipe(int userId, CustomRecipe recipe) {
        ContentValues values = new ContentValues();
        values.put(USER_ID_FK, userId);
        values.put(CUSTOM_TITLE, recipe.getTitle());
        values.put(CUSTOM_INGREDIENTS, recipe.getIngredients());
        values.put(CUSTOM_INSTRUCTIONS, recipe.getInstructions());
        database.insert(TABLE_CUSTOM, null, values);
    }

    public List<CustomRecipe> loadCustomRecipes(int userId) {
        List<CustomRecipe> recipes = new ArrayList<>();
        String select = "SELECT " + CUSTOM_ID + "," + CUSTOM_TITLE + "," +
                CUSTOM_INGREDIENTS + "," + CUSTOM_INSTRUCTIONS +
                " FROM " + TABLE_CUSTOM + " WHERE " + USER_ID_FK + "=?";
        Cursor cursor = database.rawQuery(select, new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                recipes.add(new CustomRecipe(cursor.getInt(0), cursor.getString(1),
                        cursor.getString(2), cursor.getString(3)));
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
        database.delete(TABLE_CUSTOM, CUSTOM_ID + "=?", new String[]{String.valueOf(id)});
    }

    // ── Taste Profile ─────────────────────────────────────────────────────────

    public void upsertTasteScore(int userId, String tag, String tagType, float delta) {
        String select = "SELECT " + TASTE_SCORE + " FROM " + TABLE_TASTE_PROFILE +
                " WHERE " + USER_ID_FK + "=? AND " + TASTE_TAG + "=? AND " + TASTE_TAG_TYPE + "=?";
        Cursor cursor = database.rawQuery(select, new String[]{String.valueOf(userId), tag, tagType});
        if (cursor != null && cursor.moveToFirst()) {
            float current = cursor.getFloat(0);
            cursor.close();
            ContentValues values = new ContentValues();
            values.put(TASTE_SCORE, current + delta);
            database.update(TABLE_TASTE_PROFILE, values,
                    USER_ID_FK + "=? AND " + TASTE_TAG + "=? AND " + TASTE_TAG_TYPE + "=?",
                    new String[]{String.valueOf(userId), tag, tagType});
        } else {
            if (cursor != null) cursor.close();
            ContentValues values = new ContentValues();
            values.put(USER_ID_FK, userId);
            values.put(TASTE_TAG, tag);
            values.put(TASTE_TAG_TYPE, tagType);
            values.put(TASTE_SCORE, delta);
            database.insertWithOnConflict(TABLE_TASTE_PROFILE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public void setTasteScore(int userId, String tag, String tagType, float score) {
        ContentValues values = new ContentValues();
        values.put(USER_ID_FK, userId);
        values.put(TASTE_TAG, tag);
        values.put(TASTE_TAG_TYPE, tagType);
        values.put(TASTE_SCORE, score);
        database.insertWithOnConflict(TABLE_TASTE_PROFILE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<TasteTag> loadTasteProfile(int userId) {
        List<TasteTag> tags = new ArrayList<>();
        String select = "SELECT " + TASTE_TAG + "," + TASTE_TAG_TYPE + "," + TASTE_SCORE +
                " FROM " + TABLE_TASTE_PROFILE + " WHERE " + USER_ID_FK + "=?";
        Cursor cursor = database.rawQuery(select, new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                tags.add(new TasteTag(cursor.getString(0), cursor.getString(1), cursor.getFloat(2)));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return tags;
    }

    public List<TasteTag> loadCuisineProfile(int userId) {
        List<TasteTag> tags = new ArrayList<>();
        String select = "SELECT " + TASTE_TAG + "," + TASTE_TAG_TYPE + "," + TASTE_SCORE +
                " FROM " + TABLE_TASTE_PROFILE +
                " WHERE " + USER_ID_FK + "=? AND " + TASTE_TAG_TYPE + "='cuisine'" +
                " ORDER BY " + TASTE_SCORE + " DESC";
        Cursor cursor = database.rawQuery(select, new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                tags.add(new TasteTag(cursor.getString(0), cursor.getString(1), cursor.getFloat(2)));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return tags;
    }

    // ── Preference Prompt Log ──────────────────────────────────────────────────

    public long getLastPromptTime(int userId, String topic) {
        String select = "SELECT " + PROMPT_LAST_ASKED + " FROM " + TABLE_PROMPT_LOG +
                " WHERE " + USER_ID_FK + "=? AND " + PROMPT_TOPIC + "=?";
        Cursor cursor = database.rawQuery(select, new String[]{String.valueOf(userId), topic});
        if (cursor != null && cursor.moveToFirst()) {
            long time = cursor.getLong(0);
            cursor.close();
            return time;
        }
        if (cursor != null) cursor.close();
        return -1;
    }

    public void upsertPromptLog(int userId, String topic, long timestampSeconds) {
        ContentValues values = new ContentValues();
        values.put(USER_ID_FK, userId);
        values.put(PROMPT_TOPIC, topic);
        values.put(PROMPT_LAST_ASKED, timestampSeconds);
        database.insertWithOnConflict(TABLE_PROMPT_LOG, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // ── Meal Log ───────────────────────────────────────────────────────────────

    public void logMeal(int userId, int recipeId, String title, String image, String mealType) {
        ContentValues values = new ContentValues();
        values.put(USER_ID_FK, userId);
        values.put(MEAL_LOG_RECIPE_ID, recipeId);
        values.put(MEAL_LOG_TITLE, title);
        values.put(MEAL_LOG_IMAGE, image);
        values.put(MEAL_LOG_MEAL_TYPE, mealType);
        values.put(MEAL_LOG_MADE_AT, System.currentTimeMillis() / 1000);
        database.insert(TABLE_MEAL_LOG, null, values);
    }

    public List<MealLogEntry> loadRecentMeals(int userId, int daysBack) {
        List<MealLogEntry> entries = new ArrayList<>();
        long cutoff = System.currentTimeMillis() / 1000 - (long) daysBack * 86400;
        String select =
            "SELECT m." + MEAL_LOG_ID + ",m." + MEAL_LOG_RECIPE_ID + ",m." + MEAL_LOG_TITLE +
            ",m." + MEAL_LOG_IMAGE + ",m." + MEAL_LOG_MEAL_TYPE + ",m." + MEAL_LOG_MADE_AT +
            ",f." + RECIPE_RATING +
            " FROM " + TABLE_MEAL_LOG + " m" +
            " LEFT JOIN " + TABLE_FAVORITES + " f" +
            " ON m." + MEAL_LOG_RECIPE_ID + "=f." + RECIPE_ID + " AND f." + USER_ID_FK + "=m." + USER_ID_FK +
            " WHERE m." + USER_ID_FK + "=? AND m." + MEAL_LOG_MADE_AT + ">=?" +
            " ORDER BY m." + MEAL_LOG_MADE_AT + " DESC";
        Cursor cursor = database.rawQuery(select, new String[]{String.valueOf(userId), String.valueOf(cutoff)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                MealLogEntry entry = new MealLogEntry();
                entry.id          = cursor.getInt(0);
                entry.recipeId    = cursor.getInt(1);
                entry.recipeTitle = cursor.getString(2);
                entry.recipeImage = cursor.getString(3);
                entry.mealType    = cursor.getString(4);
                entry.madeAt      = cursor.getLong(5);
                entry.rating      = cursor.getString(6);
                entries.add(entry);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return entries;
    }

    // ── Test-data Seed ────────────────────────────────────────────────────────

    public void seedTestDataIfNeeded(int userId) {
        if (!loadFavorites(userId).isEmpty()) return;
        if (!loadRecentMeals(userId, 365).isEmpty()) return;

        // Negative IDs mark local-only seed recipes; RecipeDetailActivity skips
        // the Spoonacular fetch for recipeId <= 0 so these show gracefully.
        Object[][] favs = {
            { -900001, "Avocado Toast",           "", "Breakfast", "liked"    },
            { -900002, "Fluffy Pancakes",          "", "Breakfast", "liked"    },
            { -900003, "Shakshuka",                "", "Breakfast", "neutral"  },
            { -900004, "Overnight Oats",           "", "Breakfast", "liked"    },
            { -900005, "Breakfast Burrito",        "", "Breakfast", "disliked" },
            { -900011, "Chicken Caesar Salad",     "", "Lunch",     "liked"    },
            { -900012, "Grilled Cheese Sandwich",  "", "Lunch",     "liked"    },
            { -900013, "Tomato Soup",              "", "Lunch",     "neutral"  },
            { -900014, "Falafel Wrap",             "", "Lunch",     "liked"    },
            { -900015, "Greek Salad",              "", "Lunch",     "disliked" },
            { -900021, "Spaghetti Bolognese",      "", "Dinner",    "liked"    },
            { -900022, "Chicken Tikka Masala",     "", "Dinner",    "liked"    },
            { -900023, "Beef Tacos",               "", "Dinner",    "neutral"  },
            { -900024, "Salmon with Vegetables",   "", "Dinner",    "liked"    },
            { -900025, "Mushroom Risotto",         "", "Dinner",    "disliked" },
            { -900031, "Chocolate Chip Cookies",   "", "Dessert",   "liked"    },
            { -900032, "Cheesecake",               "", "Dessert",   "liked"    },
            { -900033, "Tiramisu",                 "", "Dessert",   "liked"    },
            { -900034, "Apple Pie",                "", "Dessert",   "neutral"  },
            { -900035, "Brownies",                 "", "Dessert",   "disliked" },
            { -900041, "Guacamole",                "", "Snack",     "liked"    },
            { -900042, "Hummus and Pita",          "", "Snack",     "neutral"  },
            { -900043, "Bruschetta",               "", "Snack",     "liked"    },
            { -900044, "Deviled Eggs",             "", "Snack",     "neutral"  },
            { -900045, "Caprese Salad",            "", "Snack",     "disliked" },
        };
        for (Object[] f : favs) {
            ContentValues cv = new ContentValues();
            cv.put(USER_ID_FK,       userId);
            cv.put(RECIPE_ID,        (int)    f[0]);
            cv.put(RECIPE_TITLE,     (String) f[1]);
            cv.put(RECIPE_IMAGE_URL, (String) f[2]);
            cv.put(RECIPE_MEAL_TYPE, (String) f[3]);
            cv.put(RECIPE_RATING,    (String) f[4]);
            database.insertWithOnConflict(TABLE_FAVORITES, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        }

        // recipeId, title, mealType, daysAgo
        Object[][] logs = {
            { -900001, "Avocado Toast",          "Breakfast",  1  },
            { -900011, "Chicken Caesar Salad",   "Lunch",      1  },
            { -900021, "Spaghetti Bolognese",    "Dinner",     1  },
            { -900002, "Fluffy Pancakes",        "Breakfast",  3  },
            { -900024, "Salmon with Vegetables", "Dinner",     3  },
            { -900031, "Chocolate Chip Cookies", "Dessert",    4  },
            { -900013, "Tomato Soup",            "Lunch",      5  },
            { -900022, "Chicken Tikka Masala",   "Dinner",     5  },
            { -900041, "Guacamole",              "Snack",      6  },
            { -900003, "Shakshuka",              "Breakfast",  8  },
            { -900014, "Falafel Wrap",           "Lunch",      8  },
            { -900023, "Beef Tacos",             "Dinner",     9  },
            { -900032, "Cheesecake",             "Dessert",    10 },
            { -900004, "Overnight Oats",         "Breakfast",  12 },
            { -900043, "Bruschetta",             "Snack",      13 },
            { -900025, "Mushroom Risotto",       "Dinner",     14 },
            { -900012, "Grilled Cheese Sandwich","Lunch",      16 },
            { -900033, "Tiramisu",               "Dessert",    18 },
            { -900021, "Spaghetti Bolognese",    "Dinner",     19 },
            { -900044, "Deviled Eggs",           "Snack",      21 },
            { -900001, "Avocado Toast",          "Breakfast",  22 },
            { -900024, "Salmon with Vegetables", "Dinner",     23 },
            { -900015, "Greek Salad",            "Lunch",      25 },
            { -900034, "Apple Pie",              "Dessert",    27 },
            { -900005, "Breakfast Burrito",      "Breakfast",  29 },
        };
        long now = System.currentTimeMillis() / 1000;
        for (Object[] log : logs) {
            ContentValues cv = new ContentValues();
            cv.put(USER_ID_FK,         userId);
            cv.put(MEAL_LOG_RECIPE_ID, (int)    log[0]);
            cv.put(MEAL_LOG_TITLE,     (String) log[1]);
            cv.put(MEAL_LOG_IMAGE,     "");
            cv.put(MEAL_LOG_MEAL_TYPE, (String) log[2]);
            cv.put(MEAL_LOG_MADE_AT,   now - (long)(int) log[3] * 86400L);
            database.insert(TABLE_MEAL_LOG, null, cv);
        }
    }

    // ── Meal Plan ──────────────────────────────────────────────────────────────

    public void addToMealPlan(int userId, int recipeId, String title, String image,
                              String dayOfWeek, String mealType, String ingredientsJson, long weekStart) {
        ContentValues values = new ContentValues();
        values.put(USER_ID_FK, userId);
        values.put(MEAL_PLAN_RECIPE_ID, recipeId);
        values.put(MEAL_PLAN_TITLE, title);
        values.put(MEAL_PLAN_IMAGE, image);
        values.put(MEAL_PLAN_DAY, dayOfWeek);
        values.put(MEAL_PLAN_MEAL_TYPE, mealType);
        values.put(MEAL_PLAN_INGREDIENTS, ingredientsJson);
        values.put(MEAL_PLAN_WEEK_START, weekStart);
        database.insert(TABLE_MEAL_PLAN, null, values);
    }

    public List<MealPlanEntry> loadWeekMealPlan(int userId, long weekStart) {
        List<MealPlanEntry> entries = new ArrayList<>();
        String select =
            "SELECT " + MEAL_PLAN_ID + "," + MEAL_PLAN_RECIPE_ID + "," + MEAL_PLAN_TITLE +
            "," + MEAL_PLAN_IMAGE + "," + MEAL_PLAN_DAY + "," + MEAL_PLAN_MEAL_TYPE +
            "," + MEAL_PLAN_INGREDIENTS + "," + MEAL_PLAN_WEEK_START +
            " FROM " + TABLE_MEAL_PLAN +
            " WHERE " + USER_ID_FK + "=? AND " + MEAL_PLAN_WEEK_START + "=?" +
            " ORDER BY CASE " + MEAL_PLAN_DAY +
            " WHEN 'Monday' THEN 1 WHEN 'Tuesday' THEN 2 WHEN 'Wednesday' THEN 3" +
            " WHEN 'Thursday' THEN 4 WHEN 'Friday' THEN 5 WHEN 'Saturday' THEN 6" +
            " WHEN 'Sunday' THEN 7 ELSE 8 END, " + MEAL_PLAN_ID;
        Cursor cursor = database.rawQuery(select, new String[]{String.valueOf(userId), String.valueOf(weekStart)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                MealPlanEntry entry = new MealPlanEntry();
                entry.id            = cursor.getInt(0);
                entry.recipeId      = cursor.getInt(1);
                entry.recipeTitle   = cursor.getString(2);
                entry.recipeImage   = cursor.getString(3);
                entry.dayOfWeek     = cursor.getString(4);
                entry.mealType      = cursor.getString(5);
                entry.ingredientsJson = cursor.getString(6);
                entry.weekStart     = cursor.getLong(7);
                entries.add(entry);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return entries;
    }

    public void removeMealPlanEntry(int id) {
        database.delete(TABLE_MEAL_PLAN, MEAL_PLAN_ID + "=?", new String[]{String.valueOf(id)});
    }
}
