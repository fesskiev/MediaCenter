package com.fesskiev.player;

import android.app.Application;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.fesskiev.player.model.MusicFile;
import com.fesskiev.player.model.MusicFolder;
import com.vk.sdk.VKSdk;

import java.util.ArrayList;
import java.util.List;


public class MusicApplication extends Application {

    private static MusicApplication application;
    private RequestQueue requestQueue;
    private List<MusicFolder> musicFolders;
    private MusicFile currentMusicFile;

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
        application = this;

        VKSdk.initialize(this);

        musicFolders = new ArrayList<>();

    }

    public static synchronized MusicApplication getInstance() {
        return application;
    }


    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return requestQueue;
    }


    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(MusicApplication.class.getSimpleName());
        getRequestQueue().add(req);
    }


    public void cancelPendingRequests() {
        if (requestQueue != null) {
            requestQueue.cancelAll(MusicApplication.class.getSimpleName());
        }
    }

    public List<MusicFolder> getMusicFolders() {
        return musicFolders;
    }

    public void setMusicFolders(List<MusicFolder> musicFolders) {
        this.musicFolders = musicFolders;
    }

    public MusicFile getCurrentMusicFile() {
        return currentMusicFile;
    }

    public void setCurrentMusicFile(MusicFile currentMusicFile) {
        this.currentMusicFile = currentMusicFile;
    }
}
