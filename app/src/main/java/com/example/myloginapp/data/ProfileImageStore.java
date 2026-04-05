package com.example.myloginapp.data;

import android.content.Context;
import android.content.SharedPreferences;

public class ProfileImageStore {

    private static final String PREFS_NAME = "profile_images";

    private final SharedPreferences prefs;

    public ProfileImageStore(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveProfileImage(String username, String uri) {
        prefs.edit().putString(username, uri).apply();
    }

    public String getProfileImage(String username) {
        return prefs.getString(username, null);
    }

    public void clearProfileImage(String username) {
        prefs.edit().remove(username).apply();
    }
}
