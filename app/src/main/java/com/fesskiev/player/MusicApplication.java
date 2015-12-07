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

    public static native void setVolumeUriAudioPlayer(int millibel);

    public static native void setSeekUriAudioPlayer(long milliseconds);

    public static native void releaseUriAudioPlayer();

    public static native void releaseEngine();

    public static native int getDuration();

    public static native int getPosition();

    public static native boolean isPlaying();


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
