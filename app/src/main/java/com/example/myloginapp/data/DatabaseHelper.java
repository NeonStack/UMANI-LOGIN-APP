package com.example.myloginapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "auth.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String COL_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";
    private static final String COL_PROVIDER = "provider";
    private static final String COL_CREATED_AT = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USERNAME + " TEXT UNIQUE NOT NULL, "
                + COL_PASSWORD + " TEXT NOT NULL, "
                + COL_PROVIDER + " TEXT NOT NULL, "
                + COL_CREATED_AT + " INTEGER NOT NULL"
                + ")";
        db.execSQL(createUsersTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean createUser(String username, String password, String provider) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);
        values.put(COL_PROVIDER, provider);
        values.put(COL_CREATED_AT, System.currentTimeMillis());
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean userExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_ID},
                COL_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean verifyUserCredentials(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_ID},
                COL_USERNAME + " = ? AND " + COL_PASSWORD + " = ?",
                new String[]{username, password},
                null,
                null,
                null
        );
        boolean isValid = cursor.moveToFirst();
        cursor.close();
        return isValid;
    }

    public String getProviderForUser(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_PROVIDER},
                COL_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        );
        String provider = "local";
        if (cursor.moveToFirst()) {
            provider = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROVIDER));
        }
        cursor.close();
        return provider;
    }

    public boolean updatePassword(String username, String currentPassword, String newPassword) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PASSWORD, newPassword);
        int rows = db.update(
                TABLE_USERS,
                values,
                COL_USERNAME + " = ? AND " + COL_PASSWORD + " = ?",
                new String[]{username, currentPassword}
        );
        return rows > 0;
    }

    public boolean deleteUser(String username) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_USERS, COL_USERNAME + " = ?", new String[]{username});
        return rows > 0;
    }

    public String getOrCreateSocialUser(String provider) {
        String username = provider + "_user";
        if (!userExists(username)) {
            createUser(username, "social-login", provider);
        }
        return username;
    }
}
