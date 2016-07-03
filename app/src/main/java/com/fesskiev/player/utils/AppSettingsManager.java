package com.fesskiev.player.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AppSettingsManager {

    private static final String APP_SETTINGS_PREFERENCES = "com.fesskiev.player_settings_preferences";
    private static final String KEY_OAUTH_TOKEN = "com.fesskiev.player.SAVE_STATE_KEY_OAUTH_TOKEN";
    private static final String KEY_OAUTH_SECRET = "com.fesskiev.player.SAVE_STATE_KEY_OAUTH_SECRET";
    private static final String KEY_USER_ID = "com.fesskiev.player.SAVE_STATE_KEY_USER_ID";
    private static final String KEY_USER_FIRST_NAME = "com.fesskiev.player.KEY_USER_FIRST_NAME";
    private static final String KEY_USER_LAST_NAME = "com.fesskiev.player.KEY_USER_LAST_NAME";
    private static final String KEY_FIRST_START_APP = "com.fesskiev.player.KEY_FIRST_START_APP";

    private static final String KEY_BASS_BOOST_VALUE = "com.fesskiev.player.KEY_BASS_BOOST_VALUE";
    private static final String KEY_BASS_BOOST_STATE = "com.fesskiev.player.KEY_BASS_BOOST_STATE";

    private static final String KEY_EQ_BANDS_LEVEL = "com.fesskiev.player.KEY_EQ_BANDS_LEVEL";
    private static final String KEY_EQ_STATE = "com.fesskiev.player.KEY_EQ_STATE";
    private static final String KEY_EQ_PRESET = "com.fesskiev.player.KEY_EQ_PRESET";
    private static final String KEY_EQ_PRESET_STATE = "com.fesskiev.player.KEY_EQ_PRESET_STATE";

    private static final String KEY_VIRTUALIZER_VALUE = "com.fesskiev.player.KEY_VIRTUALIZER_VALUE";
    private static final String KEY_VIRTUALIZER_STATE = "com.fesskiev.player.KEY_VIRTUALIZER_STATE";

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


    public int getVirtualizerValue() {
        return sharedPreferences.getInt(KEY_VIRTUALIZER_VALUE, -1);
    }

    public void setVirtualizerValue(int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_VIRTUALIZER_VALUE, value);
        editor.apply();
    }

    public boolean isVirtualizerOn() {
        return sharedPreferences.getBoolean(KEY_VIRTUALIZER_STATE, false);
    }

    public void setVirtualizerState(boolean state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_VIRTUALIZER_STATE, state);
        editor.apply();
    }

    public int getBassBoostValue() {
        return sharedPreferences.getInt(KEY_BASS_BOOST_VALUE, -1);
    }

    public void setBassBoostValue(int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_BASS_BOOST_VALUE, value);
        editor.apply();
    }

    public boolean isBassBoostOn() {
        return sharedPreferences.getBoolean(KEY_BASS_BOOST_STATE, false);
    }

    public void setBassBoostState(boolean state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_BASS_BOOST_STATE, state);
        editor.apply();
    }

    public boolean isEQOn() {
        return sharedPreferences.getBoolean(KEY_EQ_STATE, false);
    }

    public void setEQState(boolean state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_EQ_STATE, state);
        editor.apply();
    }

    public int getEQPresetValue() {
        return sharedPreferences.getInt(KEY_EQ_PRESET, -1);
    }

    public void setEQPresetValue(int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_EQ_PRESET, value);
        editor.apply();
    }

    public void setCustomBandsLevel(List<Double> levels) {
        if(levels == null){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_EQ_BANDS_LEVEL, "");
            return;
        }

        JSONArray jsonArray = new JSONArray(levels);
        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("levels", jsonArray);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_EQ_BANDS_LEVEL, jsonObject.toString());
            editor.apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<Double> getCustomBandsLevels() {
        List<Double> bandsLevel = new ArrayList<>();

        String bandsLevelString = sharedPreferences.getString(KEY_EQ_BANDS_LEVEL, "");

        try {

            JSONObject jsonObject = new JSONObject(bandsLevelString);
            JSONArray jsonArray = jsonObject.getJSONArray("levels");
            for (int i = 0; i < jsonArray.length(); i++) {
                double level = jsonArray.getDouble(i);
                bandsLevel.add(level);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return bandsLevel;
    }

    public int getEQPresetState() {
        return sharedPreferences.getInt(KEY_EQ_PRESET_STATE, 0);
    }

    public void setEQPresetState(int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_EQ_PRESET_STATE, value);
        editor.apply();
    }

}


