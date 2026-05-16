package com.example.myloginapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "auth.db";
    private static final int DB_VERSION = 3; // Incremented version for soft delete

    private static final String TABLE_USERS = "users";
    private static final String COL_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";
    private static final String COL_PROVIDER = "provider";
    private static final String COL_CREATED_AT = "created_at";
    private static final String COL_IS_DELETED = "is_deleted";

    private static final String TABLE_FRIENDS = "friends";
    private static final String COL_USER_1 = "user_username";
    private static final String COL_USER_2 = "friend_username";

    private static final String TABLE_MESSAGES = "messages";
    private static final String COL_MSG_ID = "msg_id";
    private static final String COL_SENDER = "sender";
    private static final String COL_RECEIVER = "receiver";
    private static final String COL_CONTENT = "content";
    private static final String COL_TIMESTAMP = "timestamp";

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
                + COL_CREATED_AT + " INTEGER NOT NULL, "
                + COL_IS_DELETED + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(createUsersTable);

        String createFriendsTable = "CREATE TABLE " + TABLE_FRIENDS + " ("
                + COL_USER_1 + " TEXT NOT NULL, "
                + COL_USER_2 + " TEXT NOT NULL, "
                + "PRIMARY KEY (" + COL_USER_1 + ", " + COL_USER_2 + ")"
                + ")";
        db.execSQL(createFriendsTable);

        String createMessagesTable = "CREATE TABLE " + TABLE_MESSAGES + " ("
                + COL_MSG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SENDER + " TEXT NOT NULL, "
                + COL_RECEIVER + " TEXT NOT NULL, "
                + COL_CONTENT + " TEXT NOT NULL, "
                + COL_TIMESTAMP + " INTEGER NOT NULL"
                + ")";
        db.execSQL(createMessagesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
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
                COL_USERNAME + " = ? AND " + COL_PASSWORD + " = ? AND " + COL_IS_DELETED + " = 0",
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
        ContentValues values = new ContentValues();
        values.put(COL_IS_DELETED, 1);
        int rows = db.update(TABLE_USERS, values, COL_USERNAME + " = ?", new String[]{username});
        return rows > 0;
    }

    public String getOrCreateSocialUser(String provider) {
        String username = provider + "_user";
        if (!userExists(username)) {
            createUser(username, "social-login", provider);
        }
        return username;
    }

    // --- New Features: Tinder Swipe & Chat ---

    public List<String> getAllOtherUsers(String currentUsername) {
        List<String> users = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        List<String> friends = getFriends(currentUsername);
        
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USERNAME}, 
                COL_USERNAME + " != ? AND " + COL_IS_DELETED + " = 0", new String[]{currentUsername}, null, null, null);
                
        while (cursor.moveToNext()) {
            String u = cursor.getString(0);
            if (!friends.contains(u)) {
                users.add(u);
            }
        }
        cursor.close();
        return users;
    }

    public boolean addFriend(String currentUsername, String friendUsername) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_1, currentUsername);
        values.put(COL_USER_2, friendUsername);
        long result = db.insertWithOnConflict(TABLE_FRIENDS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        return result != -1;
    }

    public List<String> getFriends(String currentUsername) {
        List<String> friends = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        // Return both people you liked AND people who liked you
        String query = "SELECT " + COL_USER_2 + " FROM " + TABLE_FRIENDS + " WHERE " + COL_USER_1 + " = ?" +
                       " UNION " +
                       "SELECT " + COL_USER_1 + " FROM " + TABLE_FRIENDS + " WHERE " + COL_USER_2 + " = ?";
                       
        Cursor cursor = db.rawQuery(query, new String[]{currentUsername, currentUsername});
        while (cursor.moveToNext()) {
            friends.add(cursor.getString(0));
        }
        cursor.close();
        return friends;
    }

    public boolean addMessage(String sender, String receiver, String content) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SENDER, sender);
        values.put(COL_RECEIVER, receiver);
        values.put(COL_CONTENT, content);
        values.put(COL_TIMESTAMP, System.currentTimeMillis());
        long result = db.insert(TABLE_MESSAGES, null, values);
        return result != -1;
    }

    public Cursor getMessages(String user1, String user2) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_MESSAGES + 
                       " WHERE (" + COL_SENDER + "=? AND " + COL_RECEIVER + "=?)" +
                       " OR (" + COL_SENDER + "=? AND " + COL_RECEIVER + "=?) ORDER BY " + COL_TIMESTAMP + " ASC";
        return db.rawQuery(query, new String[]{user1, user2, user2, user1});
    }
}
