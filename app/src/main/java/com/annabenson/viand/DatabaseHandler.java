package com.annabenson.viand;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHandler";
    private static final int DATABASE_VERSION = 1; // change schema --> increment version
    private static final String DATABASE_NAME   = "ViandDatabase";

    /* accounts tables */

    private static final String TABLE_ACCOUNTS = "UserAccountsTable";
    private static final String EMAIL = "Email";
    private static final String PASSWORD = "Password";
    private static final String PHONE = "Phone";
    private static final String FIRST_NAME = "FirstName";
    private static final String LAST_NAME = "LastName";


    /* TODO other tables later */


    /* table creation */
    private static final String SQL_CREATE_USER_ACCOUNTS_TABLE =
        "CREATE TABLE " + TABLE_ACCOUNTS + " (" +
                EMAIL + " TEXT not null unique," +
                PASSWORD + " TEXT not null," +
                PHONE + "TEXT not null," +
                FIRST_NAME + "TEXT not null," +
                LAST_NAME + "TEXT not null)"
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
    }

    /* onUpgrade empty on purpose */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }


    public void addUserAccount(UserAccount account){
        Log.d(TAG, "addAccount: ");
        ContentValues values = new ContentValues();

        values.put(EMAIL,account.getEmail());
        values.put(PASSWORD,account.getPassword());
        values.put(PHONE,account.getPassword());
        values.put(FIRST_NAME,account.getFirstName());

        long key = database.insert(TABLE_ACCOUNTS,null,values);

    }

    public UserAccount loadUserAccount(String email, String password){
        String select = "SELECT * FROM " + TABLE_ACCOUNTS +
                " WHERE " + EMAIL + "='" + email + "' and " + PASSWORD + "='"
                + password + "'; " ;
        Cursor cursor = database.rawQuery(select,null);
        if(cursor != null && cursor.getCount() == 1 ) { // cursor exists and there's only one account
            cursor.moveToFirst(); // imp
            String savedEmail = cursor.getString(0);
            String savedPass = cursor.getString(1);
            String savedPhone = cursor.getString(2);
            String savedFirst = cursor.getString(3);
            String savedLast = cursor.getString(4);
            return new UserAccount(savedEmail,savedPass,savedPhone,
                    new ArrayList<Order>(),savedFirst,savedLast);

        }
        else return null;
    }

}
