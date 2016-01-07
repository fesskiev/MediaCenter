package com.fesskiev.player.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;


public class AppSettingsManager {

    private static final String APP_SETTINGS_PREFERENCES = "com.fesskiev.player_settings_preferences";
    private static final String KEY_OAUTH_TOKEN = "com.fesskiev.player.SAVE_STATE_KEY_OAUTH_TOKEN";
    private static final String KEY_OAUTH_SECRET = "com.fesskiev.player.SAVE_STATE_KEY_OAUTH_SECRET";
    private static final String KEY_USER_ID = "com.fesskiev.player.SAVE_STATE_KEY_USER_ID";

    private SharedPreferences sharedPreferences;


    public AppSettingsManager(Context context) {
        sharedPreferences =
                context.getSharedPreferences(APP_SETTINGS_PREFERENCES, Context.MODE_PRIVATE);
    }


    public String getAuthToken() {
        return sharedPreferences.getString(KEY_OAUTH_TOKEN, "");
    }

    public void setAuthToken(String authToken){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_OAUTH_TOKEN, authToken);
        editor.apply();
    }

    public String getAuthSecret() {
        return sharedPreferences.getString(KEY_OAUTH_SECRET, "");
    }

    public void setAuthSecret(String authSecret){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_OAUTH_SECRET, authSecret);
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, "");
    }

    public void setUserId(String userId){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    public boolean isAuthTokenEmpty(){
        return TextUtils.isEmpty(getAuthToken());
    }
}


