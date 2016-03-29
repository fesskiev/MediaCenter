package com.fesskiev.player.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.text.TextUtils;

public class AppSettingsManager {

    private static final String APP_SETTINGS_PREFERENCES = "com.fesskiev.player_settings_preferences";
    private static final String KEY_OAUTH_TOKEN = "com.fesskiev.player.SAVE_STATE_KEY_OAUTH_TOKEN";
    private static final String KEY_OAUTH_SECRET = "com.fesskiev.player.SAVE_STATE_KEY_OAUTH_SECRET";
    private static final String KEY_USER_ID = "com.fesskiev.player.SAVE_STATE_KEY_USER_ID";
    private static final String KEY_USER_FIRST_NAME = "com.fesskiev.player.KEY_USER_FIRST_NAME";
    private static final String KEY_USER_LAST_NAME = "com.fesskiev.player.KEY_USER_LAST_NAME";
    private static final String KEY_FIRST_START_APP = "com.fesskiev.player.KEY_FIRST_START_APP";

    private SharedPreferences sharedPreferences;
    private static AppSettingsManager appSettingsManager;


    private AppSettingsManager(Context context) {
        sharedPreferences =
                context.getSharedPreferences(APP_SETTINGS_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static AppSettingsManager getInstance(Context context) {
        if (appSettingsManager == null) {
            appSettingsManager = new AppSettingsManager(context);
        }
        return appSettingsManager;
    }


    public boolean isFirstStartApp() {
        return sharedPreferences.getBoolean(KEY_FIRST_START_APP, true);
    }

    public void setFirstStartApp() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_FIRST_START_APP, false);
        editor.apply();
    }


    public String getAuthToken() {
        return sharedPreferences.getString(KEY_OAUTH_TOKEN, "");
    }

    public void setAuthToken(String authToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_OAUTH_TOKEN, authToken);
        editor.apply();
    }

    public String getAuthSecret() {
        return sharedPreferences.getString(KEY_OAUTH_SECRET, "");
    }

    public void setAuthSecret(String authSecret) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_OAUTH_SECRET, authSecret);
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, "");
    }

    public void setUserId(String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    public String getUserFirstName() {
        return sharedPreferences.getString(KEY_USER_FIRST_NAME, "");
    }

    public void setUserFirstName(String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_FIRST_NAME, userId);
        editor.apply();
    }

    public String getUserLastName() {
        return sharedPreferences.getString(KEY_USER_LAST_NAME, "");
    }

    public void setUserLastName(String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_LAST_NAME, userId);
        editor.apply();
    }


    public boolean isAuthTokenEmpty() {
        return TextUtils.isEmpty(getAuthToken());
    }


    public void saveUserPhoto(Bitmap bitmap) {
        BitmapHelper.saveBitmap(bitmap, CacheConstants.getUserPhotoPath());
    }

    public Bitmap getUserPhoto() {
        return BitmapHelper.getBitmapFromPath(CacheConstants.getUserPhotoPath().getAbsolutePath());
    }
}


