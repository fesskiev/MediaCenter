package com.fesskiev.mediacenter.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.effects.EQState;
import com.fesskiev.mediacenter.data.model.effects.EchoState;
import com.fesskiev.mediacenter.data.model.effects.ReverbState;
import com.fesskiev.mediacenter.data.model.effects.WhooshState;
import com.fesskiev.mediacenter.data.model.video.RendererState;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Set;

public class AppSettingsManager {

    private static final String APP_SETTINGS_PREFERENCES = "com.fesskiev.player_settings_preferences";

    private static final String KEY_PLAY_PLUG_IN_HEADSET = "com.fesskiev.player.PLAY_PLUG_IN_HEADSET";
    private static final String KEY_DOWNLOAD_WIFI_ONLY = "com.fesskiev.player.DOWNLOAD_WIFI_ONLY";

    private static final String KEY_OAUTH_TOKEN = "com.fesskiev.player.SAVE_STATE_KEY_OAUTH_TOKEN";
    private static final String KEY_USER_ID = "com.fesskiev.player.SAVE_STATE_KEY_USER_ID";
    private static final String KEY_USER_FIRST_NAME = "com.fesskiev.player.KEY_USER_FIRST_NAME";
    private static final String KEY_USER_LAST_NAME = "com.fesskiev.player.KEY_USER_LAST_NAME";
    private static final String KEY_PHOTO_URL = "com.fesskiev.player.KEY_KEY_PHOTO_URL";
    private static final String KEY_FIRST_START_APP = "com.fesskiev.player.KEY_FIRST_START_APP";

    private static final String KEY_SORT_TYPE = "com.fesskiev.player.KEY_SORT_TYPE";

    private static final String KEY_REVERB_ENABLE = "com.fesskiev.player.KEY_REVERB_ENABLE";
    private static final String KEY_REVERB_STATE = "com.fesskiev.player.KEY_REVERB_STATE";

    private static final String KEY_EQ_ENABLE = "com.fesskiev.player.KEY_EQ_ENABLE";
    private static final String KEY_EQ_STATE = "com.fesskiev.player.KEY_EQ_STATE";

    private static final String KEY_WHOOSH_ENABLE = "com.fesskiev.player.KEY_WHOOSH_ENABLE";
    private static final String KEY_WHOOSH_STATE = "com.fesskiev.player.KEY_WHOOSH_STATE";

    private static final String KEY_ECHO_ENABLE = "com.fesskiev.player.KEY_ECHO_ENABLE";
    private static final String KEY_ECHO_STATE = "com.fesskiev.player.KEY_ECHO_STATE";

    private static final String KEY_RENDERER_STATE = "com.fesskiev.player.KEY_RENDERER_STATE";

    private static final String KEY_MEDIA_CONTENT_UPDATE_TIME = "com.fesskiev.player.KEY_MEDIA_CONTENT_UPDATE_TIME";
    private static final String KEY_ENCRYPT = "com.fesskiev.player.KEY_ENCRYPT";
    private static final String KEY_SHOW_HIDDEN_FILES = "com.fesskiev.player.KEY_SHOW_HIDDEN_FILES";


    private SharedPreferences sharedPreferences;
    private static AppSettingsManager appSettingsManager;


    private AppSettingsManager(Context context) {
        sharedPreferences =
                context.getSharedPreferences(APP_SETTINGS_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static AppSettingsManager getInstance() {
        if (appSettingsManager == null) {
            appSettingsManager = new AppSettingsManager(MediaApplication.getInstance().getApplicationContext());
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

    public String getPhotoURL() {
        return sharedPreferences.getString(KEY_PHOTO_URL, "");
    }

    public void setPhotoURL(String url) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PHOTO_URL, url);
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


    public boolean isReverbEnable() {
        return sharedPreferences.getBoolean(KEY_REVERB_ENABLE, false);
    }

    public void setReverbEnable(boolean enable) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REVERB_ENABLE, enable);
        editor.apply();
    }

    public void setReverbState(ReverbState state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_REVERB_STATE, new Gson().toJson(state));
        editor.apply();
    }

    public ReverbState getReverbState() {
        String stateJson = sharedPreferences.getString(KEY_REVERB_STATE, "");
        return new Gson().fromJson(stateJson, ReverbState.class);
    }


    public boolean isEQEnable() {
        return sharedPreferences.getBoolean(KEY_EQ_ENABLE, false);
    }

    public void setEQEnable(boolean enable) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_EQ_ENABLE, enable);
        editor.apply();
    }

    public void setEQState(EQState state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EQ_STATE, new Gson().toJson(state));
        editor.apply();
    }

    public EQState getEQState() {
        String stateJson = sharedPreferences.getString(KEY_EQ_STATE, "");
        return new Gson().fromJson(stateJson, EQState.class);
    }


    public void setWhooshState(WhooshState state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_WHOOSH_STATE, new Gson().toJson(state));
        editor.apply();
    }

    public WhooshState getWhooshState() {
        String stateJson = sharedPreferences.getString(KEY_WHOOSH_STATE, "");
        return new Gson().fromJson(stateJson, WhooshState.class);
    }

    public boolean isWhooshEnable() {
        return sharedPreferences.getBoolean(KEY_WHOOSH_ENABLE, false);
    }

    public void setWhooshEnable(boolean enable) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_WHOOSH_ENABLE, enable);
        editor.apply();
    }


    public void setEchoState(EchoState state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ECHO_STATE, new Gson().toJson(state));
        editor.apply();
    }

    public EchoState getEchoState() {
        String stateJson = sharedPreferences.getString(KEY_ECHO_STATE, "");
        return new Gson().fromJson(stateJson, EchoState.class);
    }

    public boolean isEchoEnable() {
        return sharedPreferences.getBoolean(KEY_ECHO_ENABLE, false);
    }

    public void setEchoEnable(boolean enable) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_ECHO_ENABLE, enable);
        editor.apply();
    }


    public void setRendererState(Set<RendererState> rendererState) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_RENDERER_STATE, new Gson().toJson(rendererState));
        editor.apply();
    }

    public Set<RendererState> getRendererState() {
        String stateJson = sharedPreferences.getString(KEY_RENDERER_STATE, "");
        return new Gson().fromJson(stateJson, new TypeToken<Set<RendererState>>() {
        }.getType());
    }

    public void clearRendererState() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_RENDERER_STATE, "");
        editor.apply();
    }

    public boolean isPlayPlugInHeadset() {
        return sharedPreferences.getBoolean(KEY_PLAY_PLUG_IN_HEADSET, false);
    }

    public void setPlayPlugInHeadset(boolean state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_PLAY_PLUG_IN_HEADSET, state);
        editor.apply();
    }

    public boolean isDownloadWiFiOnly() {
        return sharedPreferences.getBoolean(KEY_DOWNLOAD_WIFI_ONLY, false);
    }

    public void setDownloadWiFiOnly(boolean state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_DOWNLOAD_WIFI_ONLY, state);
        editor.apply();
    }

    public long getMediaContentUpdateTime() {
        return sharedPreferences.getInt(KEY_MEDIA_CONTENT_UPDATE_TIME, 0);
    }

    public void setMediaContentUpdateTime(int update) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_MEDIA_CONTENT_UPDATE_TIME, update);
        editor.apply();
    }

    public int getSortType() {
        return sharedPreferences.getInt(KEY_SORT_TYPE, 3);
    }

    public void setSortType(int sortType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_SORT_TYPE, sortType);
        editor.apply();
    }

    public void setEncrypt(boolean encrypt) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_ENCRYPT, encrypt);
        editor.apply();
    }

    public boolean isNeedEncrypt() {
        return sharedPreferences.getBoolean(KEY_ENCRYPT, false);
    }

    public void setShowHiddenFiles(boolean show) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_SHOW_HIDDEN_FILES, show);
        editor.apply();
    }

    public boolean isShowHiddenFiles() {
        return sharedPreferences.getBoolean(KEY_SHOW_HIDDEN_FILES, false);
    }


}


