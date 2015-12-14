package com.fesskiev.player;

import android.app.Application;

import com.fesskiev.player.model.MusicFolder;

import java.util.ArrayList;
import java.util.List;


public class MusicApplication extends Application {

    private List<MusicFolder> musicFolders;

    static {
        System.loadLibrary("player");
    }

    public static native void createEngine();

    public static native boolean createUriAudioPlayer(String uri);

    public static native void setPlayingUriAudioPlayer(boolean isPlaying);

    public static native void setVolumeUriAudioPlayer(int milliBel);

    public static native void setSeekUriAudioPlayer(long milliseconds);

    public static native void releaseUriAudioPlayer();

    public static native void releaseEngine();

    public static native int getDuration();

    public static native int getPosition();

    public static native boolean isPlaying();

    /***
     * EQ methods
     */

    public static native void setEnableEQ(boolean isEnable);

    public static native void usePreset(int presetValue);

    public static native int getNumberOfBands();

    public static native int getNumberOfPresets();

    public static native int getCurrentPreset();

    public static native int [] getBandLevelRange();

    public static native void setBandLevel(int bandNumber, int milliBel);

    public static native int getBandLevel(int bandNumber);

    public static native int [] getBandFrequencyRange(int bandNumber);

    public static native int getCenterFrequency(int bandNumber);

    public static native int getNumberOfPreset();

    public static native String getPresetName(int presetNumber);


    @Override
    public void onCreate() {
        super.onCreate();
        musicFolders = new ArrayList<>();

    }

    public List<MusicFolder> getMusicFolders() {
        return musicFolders;
    }

    public void setMusicFolders(List<MusicFolder> musicFolders) {
        this.musicFolders = musicFolders;
    }
}
