package com.example.myloginapp.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREFS_NAME = "auth_session";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PROVIDER = "provider";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String username, String provider) {
        prefs.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_PROVIDER, provider)
                .apply();
    }

    public boolean isLoggedIn() {
        return getUsername() != null;
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public String getProvider() {
        return prefs.getString(KEY_PROVIDER, "local");
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
