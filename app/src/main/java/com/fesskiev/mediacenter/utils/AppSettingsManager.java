package com.fesskiev.mediacenter.utils;


import android.content.Context;
import android.content.SharedPreferences;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
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
    private static final String KEY_FULL_SCREEN_MODE = "com.fesskiev.player.FULL_SCREEN_MODE";

    private static final String KEY_USER_PRO = "com.fesskiev.player.KEY_USER_PRO";

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

    private static final String KEY_SHOW_HIDDEN_FILES = "com.fesskiev.player.KEY_SHOW_HIDDEN_FILES";
    private static final String KEY_RECORD_PATH = "com.fesskiev.player.KEY_RECORD_PATH";
    private static final String KEY_CUE_PATH = "com.fesskiev.player.KEY_CUE_PATH";
    private static final String KEY_CONVERT_PATH = "com.fesskiev.player.KEY_CONVERT_PATH";
    private static final String KEY_CUT_PATH = "com.fesskiev.player.KEY_CUT_PATH";

    private static final String KEY_AUDIO_PLAYER_VOLUME = "com.fesskiev.player.AUDIO_PLAYER_VOLUME";
    private static final String KEY_AUDIO_PLAYER_POSITION = "com.fesskiev.player.KEY_AUDIO_PLAYER_POSITION";


    private static final String KEY_GUIDE_MAIN_ACTIVITY = "com.fesskiev.player.KEY_GUIDE_MAIN_ACTIVITY";
    private static final String KEY_GUIDE_TRACK_LIST_ACTIVITY = "com.fesskiev.player.KEY_GUIDE_TRACK_LIST_ACTIVITY";
    private static final String KEY_GUIDE_AUDIO_PLAYER_ACTIVITY = "com.fesskiev.player.KEY_GUIDE_AUDIO_PLAYER_ACTIVITY";
    private static final String KEY_GUIDE_VIDEO_PLAYER_ACTIVITY = "com.fesskiev.player.KEY_GUIDE_VIDEO_PLAYER_ACTIVITY";
    private static final String KEY_BACKGROUND_AUDIO = "com.fesskiev.player.KEY_BACKGROUND_AUDIO";

    private SharedPreferences sharedPreferences;

    public AppSettingsManager(Context context) {
        sharedPreferences =
                context.getSharedPreferences(APP_SETTINGS_PREFERENCES, Context.MODE_PRIVATE);
    }

    public boolean isFirstStartApp() {
        return sharedPreferences.getBoolean(KEY_FIRST_START_APP, true);
    }

    public void setFirstStartApp() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_FIRST_START_APP, false);
        editor.apply();
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

    public boolean isFullScreenMode() {
        return sharedPreferences.getBoolean(KEY_FULL_SCREEN_MODE, true);
    }

    public void setFullScreenMode(boolean state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_FULL_SCREEN_MODE, state);
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

    public void setShowHiddenFiles(boolean show) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_SHOW_HIDDEN_FILES, show);
        editor.apply();
    }

    public boolean isShowHiddenFiles() {
        return sharedPreferences.getBoolean(KEY_SHOW_HIDDEN_FILES, false);
    }

    public boolean isUserPro() {
        return sharedPreferences.getBoolean(KEY_USER_PRO,
                MediaApplication.getInstance().getResources().getBoolean(R.bool.isProUser));
    }

    public void setUserPro(boolean pro) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_USER_PRO, pro);
        editor.apply();
    }

    public String getRecordPath() {
        return sharedPreferences.getString(KEY_RECORD_PATH, CacheManager.RECORDER_DEST_PATH);
    }

    public void setRecordPath(String path) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_RECORD_PATH, path);
        editor.apply();
    }

    public String geConvertFolderPath() {
        return sharedPreferences.getString(KEY_CONVERT_PATH, CacheManager.CONVERT_DEST_PATH);
    }

    public void setConvertFolderPath(String path) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CONVERT_PATH, path);
        editor.apply();
    }

    public String getCutFolderPath() {
        return sharedPreferences.getString(KEY_CUT_PATH, CacheManager.CUT_DEST_PATH);
    }

    public void setCutFolderPath(String path) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CUT_PATH, path);
        editor.apply();
    }

    public String getCuePath() {
        return sharedPreferences.getString(KEY_CUE_PATH, "");
    }

    public void setCuePath(String path) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CUE_PATH, path);
        editor.apply();
    }


    public boolean isNeedMainActivityGuide() {
        return sharedPreferences.getBoolean(KEY_GUIDE_MAIN_ACTIVITY, true);
    }

    public void setNeedMainActivityGuide(boolean need) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_GUIDE_MAIN_ACTIVITY, need);
        editor.apply();
    }

    public boolean isNeedTrackListActivityGuide() {
        return sharedPreferences.getBoolean(KEY_GUIDE_TRACK_LIST_ACTIVITY, true);
    }

    public void setNeedTrackListActivityGuide(boolean need) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_GUIDE_TRACK_LIST_ACTIVITY, need);
        editor.apply();
    }

    public boolean isNeedAudioPlayerActivityGuide() {
        return sharedPreferences.getBoolean(KEY_GUIDE_AUDIO_PLAYER_ACTIVITY, true);
    }

    public void setNeedAudioPlayerActivityGuide(boolean need) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_GUIDE_AUDIO_PLAYER_ACTIVITY, need);
        editor.apply();
    }

    public boolean isNeedVideoPlayerActivityGuide() {
        return sharedPreferences.getBoolean(KEY_GUIDE_VIDEO_PLAYER_ACTIVITY, true);
    }

    public void setNeedVideoPlayerActivityGuide(boolean need) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_GUIDE_VIDEO_PLAYER_ACTIVITY, need);
        editor.apply();
    }

    public void setAudioBackgroundPlayback(boolean play) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_BACKGROUND_AUDIO, play);
        editor.apply();
    }

    public boolean isAudioBackgroundPlayback() {
        return sharedPreferences.getBoolean(KEY_BACKGROUND_AUDIO, false);
    }

    public boolean isNeedGuide() {
        return sharedPreferences.getBoolean(KEY_GUIDE_MAIN_ACTIVITY, true) ||
                sharedPreferences.getBoolean(KEY_GUIDE_AUDIO_PLAYER_ACTIVITY, true) ||
                sharedPreferences.getBoolean(KEY_GUIDE_VIDEO_PLAYER_ACTIVITY, true) ||
                sharedPreferences.getBoolean(KEY_GUIDE_TRACK_LIST_ACTIVITY, true);
    }

    public void setNeedGuide(boolean need) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_GUIDE_MAIN_ACTIVITY, need);
        editor.putBoolean(KEY_GUIDE_AUDIO_PLAYER_ACTIVITY, need);
        editor.putBoolean(KEY_GUIDE_VIDEO_PLAYER_ACTIVITY, need);
        editor.putBoolean(KEY_GUIDE_TRACK_LIST_ACTIVITY, need);
        editor.apply();
    }

    public void setAudioPlayerPosition(int position) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_AUDIO_PLAYER_POSITION, position);
        editor.apply();
    }

    public int getAudioPlayerPosition() {
        return sharedPreferences.getInt(KEY_AUDIO_PLAYER_POSITION, 0);
    }

    public void setAudioPlayerVolume(int volume) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_AUDIO_PLAYER_VOLUME, volume);
        editor.apply();
    }

    public int getAudioPlayerVolume() {
        return sharedPreferences.getInt(KEY_AUDIO_PLAYER_VOLUME, 100);
    }
}