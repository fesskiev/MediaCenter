package com.fesskiev.player.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fesskiev.player.SuperPoweredSDKWrapper;
import com.fesskiev.player.utils.AppSettingsManager;
import com.fesskiev.player.utils.AudioFocusManager;
import com.fesskiev.player.utils.AudioNotificationManager;

import java.util.Timer;
import java.util.TimerTask;

public class PlaybackService extends Service {

    private static final String TAG = PlaybackService.class.getSimpleName();

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
    public static final String ACTION_PLAYBACK_EQ_STATE =
            "com.fesskiev.player.action.ACTION_PLAYBACK_EQ_STATE";
    public static final String ACTION_PLAYBACK_MUTE_SOLO_STATE =
            "com.fesskiev.player.action.ACTION_PLAYBACK_MUTE_SOLO_STATE";
    public static final String ACTION_PLAYBACK_REPEAT_STATE =
            "com.fesskiev.player.action.ACTION_PLAYBACK_REPEAT_STATE";


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
    public static final String PLAYBACK_EXTRA_EQ_STATE
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_EQ_STATE";
    public static final String PLAYBACK_EXTRA_MUTE_SOLO_STATE
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_MUTE_SOLO_STATE";
    public static final String PLAYBACK_EXTRA_REPEAT_STATE
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_REPEAT_STATE";


    private Timer timer;
    private AppSettingsManager settingsManager;
    private AudioFocusManager audioFocusManager;
    private AudioNotificationManager audioNotificationManager;
    private SuperPoweredSDKWrapper superPoweredSDKWrapper;
    private int durationScale;

    public static void changeEQState(Context context) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_EQ_STATE);
        context.startService(intent);
    }

    public static void changeRepeatState(Context context, boolean state) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_REPEAT_STATE);
        intent.putExtra(PLAYBACK_EXTRA_REPEAT_STATE, state);
        context.startService(intent);
    }

    public static void changeMuteSoloState(Context context, boolean state) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_MUTE_SOLO_STATE);
        intent.putExtra(PLAYBACK_EXTRA_MUTE_SOLO_STATE, state);
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

    public static void startPlaybackService(Context context) {
        Intent intent = new Intent(context, PlaybackService.class);
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


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Create playback service!");
        superPoweredSDKWrapper = SuperPoweredSDKWrapper.getInstance();
        superPoweredSDKWrapper.setOnSuperPoweredSDKListener(() -> {
            sendBroadcastPlayingState(true);
            sendBroadcastSongEnd();
        });

        settingsManager = AppSettingsManager.getInstance(getApplicationContext());
        audioNotificationManager = new AudioNotificationManager(this, this);
        audioFocusManager = new AudioFocusManager();
        audioFocusManager.setOnAudioFocusManagerListener(
                state -> {
                    switch (state) {
                        case AudioFocusManager.AUDIO_FOCUSED:
                            Log.d(TAG, "onFocusChanged: FOCUSED");
                            play();
                            break;
                        case AudioFocusManager.AUDIO_NO_FOCUS_CAN_DUCK:
                            Log.d(TAG, "onFocusChanged: NO_FOCUS_CAN_DUCK");
                            superPoweredSDKWrapper.setVolumeAudioPlayer(50);
                            break;
                        case AudioFocusManager.AUDIO_NO_FOCUS_NO_DUCK:
                            Log.d(TAG, "onFocusChanged: NO_FOCUS_NO_DUCK");
                            stop();
                            break;
                    }
                });

        registerHeadsetReceiver();
        superPoweredSDKWrapper.registerCallback();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
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
                        int seekValue = intent.getIntExtra(PLAYBACK_EXTRA_SEEK, -1);
                        seek(seekValue);
                        break;
                    case ACTION_PLAYBACK_VOLUME:
                        int volumeValue = intent.getIntExtra(PLAYBACK_EXTRA_VOLUME, -1);
                        volume(volumeValue);
                        break;
                    case ACTION_PLAYBACK_EQ_STATE:
                        break;
                    case ACTION_PLAYBACK_REPEAT_STATE:
                        boolean repeatState =
                                intent.getBooleanExtra(PLAYBACK_EXTRA_REPEAT_STATE, false);
                        repeat(repeatState);
                        break;
                }
            }
        }

        return START_STICKY;
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

    private void repeat(boolean repeatState) {
        superPoweredSDKWrapper.setLoopingAudioPlayer(repeatState);
    }


    boolean created = false;

    private void createPlayer(String path) {

        String sampleRateString, bufferSizeString;
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        sampleRateString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        bufferSizeString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        if (sampleRateString == null) {
            sampleRateString = "44100";
        }
        if (bufferSizeString == null) {
            bufferSizeString = "512";
        }
        if (!created) {
            created = true;
            superPoweredSDKWrapper.createAudioPlayer(path, Integer.valueOf(sampleRateString), Integer.valueOf(bufferSizeString));
            Log.d(TAG, "create audio player!");
        } else {
            stop();
            Log.d(TAG, "open audio player!");
            superPoweredSDKWrapper.openAudioFile(path);
        }
    }


    private void volume(int volumeValue) {
        superPoweredSDKWrapper.setVolumeAudioPlayer(volumeValue);

    }

    private void seek(int seekValue) {
        superPoweredSDKWrapper.setSeekAudioPlayer(seekValue);
        audioNotificationManager.seekToPosition(seekValue * durationScale);
    }

    private void play() {
        if (!superPoweredSDKWrapper.isPlaying()) {
            Log.d(TAG, "start playback");
            superPoweredSDKWrapper.setPlayingAudioPlayer(true);
            startUpdateTimer();
            sendBroadcastPlayingState(true);
            audioFocusManager.tryToGetAudioFocus();
            audioNotificationManager.setPlayPauseState(true);
        }
    }

    private void createValuesScale() {
        int duration = superPoweredSDKWrapper.getDuration();
        int progress = superPoweredSDKWrapper.getPosition();
        audioNotificationManager.setProgress(progress);
        if (duration > 0) {
            durationScale = duration / 100;
            int progressScale = progress / durationScale;
            sendBroadcastPlayerValues(duration, progress, progressScale);
        }
    }

    private void stop() {
        if (superPoweredSDKWrapper.isPlaying()) {
            Log.d(TAG, "stop playback");
            superPoweredSDKWrapper.setPlayingAudioPlayer(false);
            stopUpdateTimer();
            sendBroadcastPlayingState(false);
            audioFocusManager.giveUpAudioFocus();
            audioNotificationManager.setPlayPauseState(false);
        }
    }


    private void startUpdateTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                createValuesScale();
            }
        }, 1000, 1000);
    }

    private void stopUpdateTimer() {
        timer.cancel();
    }



    private void sendBroadcastHeadsetPlugIn() {
        LocalBroadcastManager.getInstance(getApplicationContext()).
                sendBroadcast(new Intent(ACTION_HEADSET_PLUG_IN));
    }

    private void sendBroadcastHeadsetPlugOut() {
        LocalBroadcastManager.getInstance(getApplicationContext()).
                sendBroadcast(new Intent(ACTION_HEADSET_PLUG_OUT));
    }

    private void sendBroadcastSongEnd() {
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
        audioNotificationManager.stopNotification();
        unregisterHeadsetReceiver();
        superPoweredSDKWrapper.unregisterCallback();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
