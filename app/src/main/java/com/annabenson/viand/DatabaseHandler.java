package com.annabenson.viand;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHandler";
    private static final int DATABASE_VERSION = 1; // change schema --> increment version
    private static final String DATABASE_NAME   = "ViandDatabase";

    /* accounts table */

    private static final String TABLE_ACCOUNTS = "AccountsTable";
    private static final String EMAIL = "Email";
    private static final String PASSWORD = "Password";

    /* other tables later */



    /* table creation */
    private static final String SQL_CREATE_ACCOUNTS_TABLE =
        "CREATE TABLE " + TABLE_ACCOUNTS + " (" +
                EMAIL + " TEXT not null unique," +
                PASSWORD + " TEXT not null)";

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
        // create accounts table
        db.execSQL(SQL_CREATE_ACCOUNTS_TABLE);
    }

    /* onUpgrade empty on purpose */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }


    public void addAccount(Account account){

    }
}
