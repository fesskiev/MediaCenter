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

import com.fesskiev.player.ui.equalizer.EqualizerFragment;
import com.fesskiev.player.utils.AppSettingsManager;
import com.fesskiev.player.utils.AudioFocusManager;
import com.fesskiev.player.utils.AudioNotificationManager;

import java.util.List;
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
    public static final String ACTION_BASS_BOOST_LEVEL =
            "com.fesskiev.player.action.ACTION_BASS_BOOST_LEVEL";
    public static final String ACTION_PLAYBACK_BASS_BOOST_STATE =
            "com.fesskiev.player.action.ACTION_PLAYBACK_BASS_BOOST_STATE";
    public static final String ACTION_PLAYBACK_BASS_BOOST_SUPPORT =
            "com.fesskiev.player.action.ACTION_PLAYBACK_BASS_BOOST_SUPPORT";
    public static final String ACTION_PLAYBACK_VIRTUALIZER_STATE =
            "com.fesskiev.player.action.ACTION_PLAYBACK_VIRTUALIZER_STATE";
    public static final String ACTION_PLAYBACK_VIRTUALIZER_SUPPORT =
            "com.fesskiev.player.action.ACTION_PLAYBACK_VIRTUALIZER_SUPPORT";
    public static final String ACTION_PLAYBACK_VIRTUALIZER_LEVEL =
            "com.fesskiev.player.action.ACTION_PLAYBACK_VIRTUALIZER_LEVEL";
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
    public static final String PLAYBACK_EXTRA_BASS_BOOST_SUPPORT
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_BASS_BOOST_SUPPORT";
    public static final String PLAYBACK_EXTRA_BASS_BOOST_STATE
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_BASS_BOOST_STATE";
    public static final String PLAYBACK_EXTRA_VIRTUALIZER_SUPPORT
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_VIRTUALIZER_SUPPORT";
    public static final String PLAYBACK_EXTRA_VIRTUALIZER_STATE
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_VIRTUALIZER_STATE";
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
    private int durationScale;

    public static void changeEQState(Context context) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_EQ_STATE);
        context.startService(intent);
    }

    public static void changeVirtualizerState(Context context) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_VIRTUALIZER_STATE);
        context.startService(intent);
    }

    public static void changeVirtualizerLevel(Context context) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_VIRTUALIZER_LEVEL);
        context.startService(intent);
    }

    public static void changeBassBoostState(Context context) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_BASS_BOOST_STATE);
        context.startService(intent);
    }

    public static void changeBassBoostLevel(Context context) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_BASS_BOOST_LEVEL);
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

    public native void setMuteUriAudioPlayer(boolean mute);

    public static native void setLoopingUriAudioPlayer(boolean isLooping);

    public native void enableStereoPositionUriAudioPlayer(boolean enable);

    public native void setStereoPositionUriAudioPlayer(int permille);

    /***
     * EQ methods
     */

    public native void setEnableEQ(boolean isEnable);

    public native boolean isEQEnabled();

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

    /***
     * Bass Boost methods
     */

    public native boolean isSupportedBassBoost();

    public native boolean isEnabledBassBoost();

    public native void setEnableBassBoost(boolean isEnable);

    public native void setBassBoostValue(int value);


    /**
     * Virtualizer methods
     */
    public native boolean isSupportedVirtualizer();

    public native boolean isEnabledVirtualizer();

    public native void setEnableVirtualizer(boolean isEnable);

    public native void setVirtualizerValue(int value);


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
                            setVolumeUriAudioPlayer(500);
                            break;
                        case AudioFocusManager.AUDIO_NO_FOCUS_NO_DUCK:
                            Log.d(TAG, "onFocusChanged: NO_FOCUS_NO_DUCK");
                            stop();
                            break;
                    }
                });

        registerHeadsetReceiver();
        registerCallback();
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
                    case ACTION_BASS_BOOST_LEVEL:
                    case ACTION_PLAYBACK_BASS_BOOST_STATE:
                        setBassBoost();
                        break;
                    case ACTION_PLAYBACK_VIRTUALIZER_LEVEL:
                    case ACTION_PLAYBACK_VIRTUALIZER_STATE:
                        setVirtualizer();
                        break;
                    case ACTION_PLAYBACK_EQ_STATE:
                        setEQ();
                        break;
                    case ACTION_PLAYBACK_MUTE_SOLO_STATE:
                        boolean muteSoloState =
                                intent.getBooleanExtra(PLAYBACK_EXTRA_MUTE_SOLO_STATE, false);
                        muteSolo(muteSoloState);
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
        setLoopingUriAudioPlayer(repeatState);
    }


    private void muteSolo(boolean muteSoloState) {
        setMuteUriAudioPlayer(muteSoloState);
    }

    private void createPlayer(String path) {
        if (isPlaying()) {
            stop();
        }
        releaseUriAudioPlayer();
        releaseEngine();

        createEngine();
        createUriAudioPlayer(path);
        setEffects();
    }

    private void setEffects() {
        setEQ();
        setVirtualizer();
        setBassBoost();
    }

    private void setEQ() {
        if (settingsManager.isEQOn()) {
            setEnableEQ(true);
            Log.d(TAG, "EQ ON");
            switch (settingsManager.getEQPresetState()) {
                case EqualizerFragment.POSITION_CUSTOM_PRESET:
                    Log.d(TAG, "set custom preset");
                    setCustomPreset();
                    break;
                case EqualizerFragment.POSITION_PRESET:
                    Log.d(TAG, "set preset: " + (settingsManager.getEQPresetValue() - EqualizerFragment.OFFSET));
                    usePreset(settingsManager.getEQPresetValue() - EqualizerFragment.OFFSET);
                    break;
                default:
                    break;
            }
            sendBroadcastEQState(true);
        } else {
            Log.d(TAG, "EQ OFF");
            setEnableEQ(false);
            sendBroadcastEQState(false);
        }

        Log.d(TAG, "EQ IS ON: " + isEQEnabled());
    }

    private void setCustomPreset() {
        List<Double> levels = settingsManager.getCustomBandsLevels();
        int bandsNumber = getNumberOfBands();
        for (int i = 0; i < levels.size(); i++) {
            if (i <= bandsNumber) {
                double value = levels.get(i);
                Log.wtf(TAG, "custom band value: " + value);
                setBandLevel(i, (int) value);
            }
        }
    }

    private void setVirtualizer() {
        if (isSupportedVirtualizer()) {
            Log.d(TAG, "virtualizer supported");
            sendBroadcastVirtualizerSupport(true);
            if (settingsManager != null && settingsManager.isVirtualizerOn()) {
                sendBroadcastVirtualizerState(true);
                setEnableVirtualizer(true);
                int value = settingsManager.getVirtualizerValue();
                if (value != -1) {
                    setVirtualizerValue(1000);
                    Log.d(TAG, "set vitrualizer effect: " + value);
                }
            } else {
                setEnableVirtualizer(false);
                sendBroadcastVirtualizerState(false);
            }
        } else {
            Log.d(TAG, "virtualizer not supported!");
            sendBroadcastVirtualizerSupport(false);
        }
    }

    private void setBassBoost() {
        if (isSupportedBassBoost()) {
            Log.d(TAG, "bass boost supported");
            sendBroadcastBassBoostSupport(true);
            if (settingsManager != null && settingsManager.isBassBoostOn()) {
                sendBroadcastBassBoostState(true);
                setEnableBassBoost(true);
                int value = settingsManager.getBassBoostValue();
                if (value != -1) {
                    setBassBoostValue(value);
                    Log.d(TAG, "set bass boost effect: " + value);
                }
            } else {
                setEnableBassBoost(false);
                sendBroadcastBassBoostState(false);
            }
        } else {
            Log.d(TAG, "bass boost not supported!");
            sendBroadcastBassBoostSupport(false);
        }
    }

    private void volume(int volumeValue) {
        int attenuation = 100 - volumeValue;
        int millibel = attenuation * -50;
        Log.d(TAG, "volume millibel: " + millibel);
        setVolumeUriAudioPlayer(millibel);

    }

    private void seek(int seekValue) {
        setSeekUriAudioPlayer(seekValue * durationScale);
        audioNotificationManager.seekToPosition(seekValue * durationScale);
    }

    private void play() {
        if (!isPlaying()) {
            Log.d(TAG, "start playback");
            setPlayingUriAudioPlayer(true);
            startUpdateTimer();
            sendBroadcastPlayingState(true);
            audioFocusManager.tryToGetAudioFocus();
            audioNotificationManager.setPlayPauseState(true);
        }
    }

    private void createValuesScale() {
        int duration = getDuration();
        int progress = getPosition();
        audioNotificationManager.setProgress(progress);
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
        }, 0, 1000);
    }

    private void stopUpdateTimer() {
        timer.cancel();
    }

    private void sendBroadcastBassBoostState(boolean state) {
        Intent intent = new Intent();
        intent.setAction(ACTION_PLAYBACK_BASS_BOOST_STATE);
        intent.putExtra(PLAYBACK_EXTRA_BASS_BOOST_STATE, state);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendBroadcastBassBoostSupport(boolean support) {
        Intent intent = new Intent();
        intent.setAction(ACTION_PLAYBACK_BASS_BOOST_SUPPORT);
        intent.putExtra(PLAYBACK_EXTRA_BASS_BOOST_SUPPORT, support);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendBroadcastVirtualizerSupport(boolean support) {
        Intent intent = new Intent();
        intent.setAction(ACTION_PLAYBACK_VIRTUALIZER_SUPPORT);
        intent.putExtra(PLAYBACK_EXTRA_VIRTUALIZER_SUPPORT, support);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendBroadcastVirtualizerState(boolean state) {
        Intent intent = new Intent();
        intent.setAction(ACTION_PLAYBACK_VIRTUALIZER_STATE);
        intent.putExtra(PLAYBACK_EXTRA_VIRTUALIZER_STATE, state);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
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


    public void sendBroadcastEQState(boolean state) {
        Intent intent = new Intent();
        intent.setAction(PlaybackService.ACTION_PLAYBACK_EQ_STATE);
        intent.putExtra(PlaybackService.PLAYBACK_EXTRA_EQ_STATE, state);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroy playback service");
        stop();
        releaseUriAudioPlayer();
        releaseEngine();
        audioNotificationManager.stopNotification();
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
