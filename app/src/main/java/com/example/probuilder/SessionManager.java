package com.example.probuilder;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "client_session";
    private static final String KEY_API_TOKEN = "client_token"; // Matching existing key for safety
    private static final String KEY_CLIENT_ID = "client_id";

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveApiToken(String token) {
        editor.putString(KEY_API_TOKEN, token);
        editor.apply();
    }
    
    public void saveClientId(int id) {
        editor.putInt(KEY_CLIENT_ID, id);
        editor.apply();
    }

    public String getApiToken() {
        return prefs.getString(KEY_API_TOKEN, null);
    }
    
    public int getClientId() {
        return prefs.getInt(KEY_CLIENT_ID, 0);
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
