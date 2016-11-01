package com.fesskiev.player;

import android.support.annotation.Keep;

public class SuperPoweredSDKWrapper {

    private static final int END_TRACK = 1;


    public interface OnSuperPoweredSDKListener {

        void onEndTrack();
    }


    static {
        System.loadLibrary("SuperpoweredPlayer");
    }

    private static SuperPoweredSDKWrapper INSTANCE;
    private OnSuperPoweredSDKListener listener;

    private SuperPoweredSDKWrapper() {

    }

    public static SuperPoweredSDKWrapper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SuperPoweredSDKWrapper();
        }
        return INSTANCE;
    }

    public void setOnSuperPoweredSDKListener(OnSuperPoweredSDKListener l) {
        this.listener = l;
    }

    public native void onDestroy();

    public native void onBackground();

    public native void onForeground();

    public native void registerCallback();

    public native void unregisterCallback();

    public native void createAudioPlayer(int sampleRate, int bufferSize);

    public native void openAudioFile(String path);

    public native void setPlayingAudioPlayer(boolean isPlaying);

    public native void setVolumeAudioPlayer(int value);

    public native void setSeekAudioPlayer(int value);

    public native int getDuration();

    public native int getPosition();

    public native boolean isPlaying();

    public native void setLoopingAudioPlayer(boolean isLooping);


    /***
     * EQ methods
     */

    public native void enableEQ(boolean enable);

    public native void setEQBands(int band, int value);


    /**
     * Callback method from C++ to Java
     **/
    @Keep
    public void playStatusCallback(int status) {
        if (status == END_TRACK) {
            if (listener != null) {
                listener.onEndTrack();
            }
        }
    }

}
