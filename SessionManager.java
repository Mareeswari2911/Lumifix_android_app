package com.example.imageenhancer;

import android.content.Context;
import android.content.SharedPreferences;


public class SessionManager {

    private static final String PREF_NAME = "ImageEnhancerSession";
    private static final String KEY_TOKEN   = "token";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_ROLE    = "role";
    private static final String KEY_NAME    = "name";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }


    public void saveSession(String token, String userId, String role, String name) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_ROLE, role != null ? role : "user")
                .putString(KEY_NAME, name != null ? name : "")
                .apply();
    }


    public String getToken()  { return prefs.getString(KEY_TOKEN,   null); }
    public String getUserId() { return prefs.getString(KEY_USER_ID, null); }
    public String getRole()   { return prefs.getString(KEY_ROLE,    "user"); }
    public String getName()   { return prefs.getString(KEY_NAME,    ""); }


    public boolean isLoggedIn() { return getToken() != null; }
    public boolean isAdmin()    { return "admin".equals(getRole()); }


    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public String getBearerToken() {
        return "Bearer " + getToken();
    }
}