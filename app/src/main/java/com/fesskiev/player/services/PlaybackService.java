package com.fesskiev.player.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class PlaybackService extends Service {

    private static final String TAG = PlaybackService.class.getSimpleName();

    private static final int END_SONG = 1;

    public static final String ACTION_HEADSET_PLUG_IN =
            "com.fesskiev.player.action.ACTION_HEADSET_PLUG_IN";
    public static final String ACTION_HEADSET_PLUG_OUT =
            "com.fesskiev.player.action.ACTION_HEADSET_PLUG_OUT";
    public static final String ACTION_SONG_END =
            "com.fesskiev.player.action.ACTION_SONG_END";

    public static final String ACTION_CREATE_PLAYER =
            "com.fesskiev.player.action.ACTION_CREATE_PLAYER";
    public static final String ACTION_START_PLAYBACK =
            "com.fesskiev.player.action.ACTION_START_PLAYBACK";
    public static final String ACTION_STOP_PLAYBACK =
            "com.fesskiev.player.action.ACTION_STOP_PLAYBACK";
    public static final String ACTION_PLAYBACK_SEEK =
            "com.fesskiev.player.action.ACTION_PLAYBACK_SEEK";
    public static final String ACTION_PLAYBACK_VALUES =
            "com.fesskiev.player.action.ACTION_PLAYBACK_VALUES";
    public static final String ACTION_PLAYBACK_VOLUME =
            "com.fesskiev.player.action.ACTION_PLAYBACK_VOLUME";
    public static final String ACTION_PLAYBACK_PLAYING_STATE =
            "com.fesskiev.player.action.ACTION_PLAYBACK_PLAYING_STATE";


    public static final String PLAYBACK_EXTRA_MUSIC_FILE_PATH
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_MUSIC_FILE_PATH";
    public static final String PLAYBACK_EXTRA_DURATION
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_DURATION";
    public static final String PLAYBACK_EXTRA_PROGRESS_SCALE
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_PROGRESS_SCALE";
    public static final String PLAYBACK_EXTRA_PROGRESS
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_PROGRESS";
    public static final String PLAYBACK_EXTRA_SEEK
            = "com.fesskiev.player.extra.SEEK";
    public static final String PLAYBACK_EXTRA_VOLUME
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_VOLUME";
    public static final String PLAYBACK_EXTRA_PLAYING
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_PLAYING";


    private Timer timer;
    private int durationScale;

    public static void startPlaybackService(Context context) {
        Intent intent = new Intent(context, PlaybackService.class);
        context.startService(intent);
    }

    public static void createPlayer(Context context, String path) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_CREATE_PLAYER);
        intent.putExtra(PLAYBACK_EXTRA_MUSIC_FILE_PATH, path);
        context.startService(intent);
    }

    public static void startPlayback(Context context) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_START_PLAYBACK);
        context.startService(intent);
    }

    public static void stopPlayback(Context context) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_STOP_PLAYBACK);
        context.startService(intent);
    }

    public static void seekPlayback(Context context, int seek) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_SEEK);
        intent.putExtra(PLAYBACK_EXTRA_SEEK, seek);
        context.startService(intent);
    }

    public static void volumePlayback(Context context, int volume) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_VOLUME);
        intent.putExtra(PLAYBACK_EXTRA_VOLUME, volume);
        context.startService(intent);
    }

    public static void destroyPlayer(Context context) {
        context.stopService(new Intent(context, PlaybackService.class));
    }

    static {
        System.loadLibrary("khronos-media");
    }

    public native void unregisterCallback();

    public native void registerCallback();

    public native void createEngine();

    public native boolean createUriAudioPlayer(String uri);

    public native void setPlayingUriAudioPlayer(boolean isPlaying);

    public native void setVolumeUriAudioPlayer(int milliBel);

    public native void setSeekUriAudioPlayer(long milliseconds);

    public native void releaseUriAudioPlayer();

    public native void releaseEngine();

    public native int getDuration();

    public native int getPosition();

    public native boolean isPlaying();

    /***
     * EQ methods
     */

    public native void setEnableEQ(boolean isEnable);

    public native void usePreset(int presetValue);

    public native int getNumberOfBands();

    public native int getNumberOfPresets();

    public native int getCurrentPreset();

    public native int[] getBandLevelRange();

    public native void setBandLevel(int bandNumber, int milliBel);

    public native int getBandLevel(int bandNumber);

    public native int[] getBandFrequencyRange(int bandNumber);

    public native int getCenterFrequency(int bandNumber);

    public native int getNumberOfPreset();

    public native String getPresetName(int presetNumber);

    /**
     * Callback method from C to Java
     **/
    public void playStatusCallback(int status) {
        if (status == END_SONG) {
            sendBroadcastPlayingState(true);
            sendBroadcastSongEnd();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Create playback service!");
        createEngine();
        registerHeadsetReceiver();
        registerCallback();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            Log.d(TAG, "playback service handle intent: " + action);
            switch (action) {
                case ACTION_CREATE_PLAYER:
                    String musicFilePath = intent.getStringExtra(PLAYBACK_EXTRA_MUSIC_FILE_PATH);
                    createPlayer(musicFilePath);
                    break;
                case ACTION_START_PLAYBACK:
                    play();
                    break;
                case ACTION_STOP_PLAYBACK:
                    stop();
                    break;
                case ACTION_PLAYBACK_SEEK:
                    int seekValue = intent.getIntExtra(PLAYBACK_EXTRA_SEEK, 0);
                    seek(seekValue);
                    break;
                case ACTION_PLAYBACK_VOLUME:
                    int volumeValue = intent.getIntExtra(PLAYBACK_EXTRA_VOLUME, 0);
                    volume(volumeValue);
                    break;

            }
        }

        return START_NOT_STICKY;
    }

    private void registerHeadsetReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetReceiver,
                intentFilter);
    }

    private void unregisterHeadsetReceiver() {
        unregisterReceiver(headsetReceiver);
    }

    private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_HEADSET_PLUG:
                    int state = intent.getIntExtra("state", -1);
                    if (state == 1) {
                        sendBroadcastHeadsetPlugIn();
                    } else {
                        sendBroadcastHeadsetPlugOut();
                    }
                    break;
            }
        }
    };

    private void createPlayer(String path) {
        if (isPlaying()) {
            stop();
            releaseUriAudioPlayer();
        }
        createUriAudioPlayer(path);
    }

    private void volume(int volumeValue) {
        int attenuation = 100 - volumeValue;
        int millibel = attenuation * -50;
        Log.d(TAG, "volume millibel: " + millibel);
        setVolumeUriAudioPlayer(millibel);

    }

    private void seek(int seekValue) {
        setSeekUriAudioPlayer(seekValue * durationScale);
    }

    private void play() {
        if (!isPlaying()) {
            Log.d(TAG, "start playback");
            setPlayingUriAudioPlayer(true);
            startUpdateTimer();
            sendBroadcastPlayingState(true);
        }
    }

    private void createValuesScale() {
        int duration = getDuration();
        int progress = getPosition();
        if (duration > 0) {
            durationScale = duration / 100;
            int progressScale = progress / durationScale;
            sendBroadcastPlayerValues(duration, progress, progressScale);
        }
    }

    private void stop() {
        if (isPlaying()) {
            Log.d(TAG, "stop playback");
            setPlayingUriAudioPlayer(false);
            stopUpdateTimer();
            sendBroadcastPlayingState(false);
        }
    }


    private void startUpdateTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                createValuesScale();
            }
        }, 0, 1000);
    }

    private void stopUpdateTimer() {
        timer.cancel();
    }

    private void sendBroadcastHeadsetPlugIn(){
        LocalBroadcastManager.getInstance(getApplicationContext()).
                sendBroadcast(new Intent(ACTION_HEADSET_PLUG_IN));
    }

    private void sendBroadcastHeadsetPlugOut(){
        LocalBroadcastManager.getInstance(getApplicationContext()).
                sendBroadcast(new Intent(ACTION_HEADSET_PLUG_OUT));
    }

    private void sendBroadcastSongEnd(){
        LocalBroadcastManager.getInstance(getApplicationContext()).
                sendBroadcast(new Intent(ACTION_SONG_END));
    }


    private void sendBroadcastPlayerValues(int duration, int progress, int progressScale) {
        Intent intent = new Intent();
        intent.setAction(ACTION_PLAYBACK_VALUES);
        intent.putExtra(PLAYBACK_EXTRA_DURATION, duration);
        intent.putExtra(PLAYBACK_EXTRA_PROGRESS, progress);
        intent.putExtra(PLAYBACK_EXTRA_PROGRESS_SCALE, progressScale);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendBroadcastPlayingState(boolean isPlaying) {
        Intent intent = new Intent();
        intent.setAction(ACTION_PLAYBACK_PLAYING_STATE);
        intent.putExtra(PLAYBACK_EXTRA_PLAYING, isPlaying);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroy playback service");
        stop();
        releaseUriAudioPlayer();
        releaseEngine();
        unregisterHeadsetReceiver();
        unregisterCallback();
    }


    private final IBinder binder = new PlaybackServiceBinder();

    public class PlaybackServiceBinder extends Binder {
        public PlaybackService getService() {
            return PlaybackService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
