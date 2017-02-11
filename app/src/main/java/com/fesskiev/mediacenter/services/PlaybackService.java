package com.fesskiev.mediacenter.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.effects.EQState;
import com.fesskiev.mediacenter.data.model.effects.EchoState;
import com.fesskiev.mediacenter.data.model.effects.ReverbState;
import com.fesskiev.mediacenter.data.model.effects.WhooshState;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.AudioFocusManager;
import com.fesskiev.mediacenter.utils.AudioNotificationHelper;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.CountDownTimer;

import org.greenrobot.eventbus.EventBus;

public class PlaybackService extends Service {

    private static final String TAG = PlaybackService.class.getSimpleName();

    private static final int END_TRACK = 1;

    public static final String ACTION_START_FOREGROUND =
            "com.fesskiev.player.action.ACTION_START_FOREGROUND";
    public static final String ACTION_STOP_FOREGROUND =
            "com.fesskiev.player.action.ACTION_STOP_FOREGROUND";
    public static final String ACTION_START_PLAYBACK =
            "com.fesskiev.player.action.ACTION_START_PLAYBACK";
    public static final String ACTION_OPEN_FILE =
            "com.fesskiev.player.action.ACTION_OPEN_FILE";
    public static final String ACTION_STOP_PLAYBACK =
            "com.fesskiev.player.action.ACTION_STOP_PLAYBACK";
    public static final String ACTION_PLAYBACK_SEEK =
            "com.fesskiev.player.action.ACTION_PLAYBACK_SEEK";
    public static final String ACTION_PLAYBACK_VOLUME =
            "com.fesskiev.player.action.ACTION_PLAYBACK_VOLUME";
    public static final String ACTION_PLAYBACK_EQ_STATE =
            "com.fesskiev.player.action.ACTION_PLAYBACK_EQ_STATE";
    public static final String ACTION_PLAYBACK_EQ_BAND_STATE =
            "com.fesskiev.player.action.ACTION_PLAYBACK_EQ_BAND_STATE";
    public static final String ACTION_PLAYBACK_LOOPING_STATE =
            "com.fesskiev.player.action.ACTION_PLAYBACK_LOOPING_STATE";
    public static final String ACTION_PLAYBACK_STATE =
            "com.fesskiev.player.action.ACTION_PLAYBACK_STATE";
    public static final String ACTION_PLAYBACK_REVERB_STATE =
            "com.fesskiev.player.action.ACTION_PLAYBACK_REVERB_STATE";
    public static final String ACTION_PLAYBACK_REVERB_LEVEL =
            "com.fesskiev.player.action.ACTION_PLAYBACK_REVERB_LEVEL";
    public static final String ACTION_PLAYBACK_ECHO_STATE =
            "com.fesskiev.player.action.ACTION_PLAYBACK_ECHO_STATE";
    public static final String ACTION_PLAYBACK_ECHO_LEVEL =
            "com.fesskiev.player.action.ACTION_PLAYBACK_ECHO_LEVEL";
    public static final String ACTION_PLAYBACK_WHOOSH_STATE =
            "com.fesskiev.player.action.ACTION_PLAYBACK_WHOOSH_STATE";
    public static final String ACTION_PLAYBACK_WHOOSH_LEVEL =
            "com.fesskiev.player.action.ACTION_PLAYBACK_WHOOSH_LEVEL";

    public static final String ACTION_START_RECORDING =
            "com.fesskiev.player.action.ACTION_START_RECORDING";
    public static final String ACTION_STOP_RECORDING =
            "com.fesskiev.player.action.ACTION_STOP_RECORDING";


    public static final String PLAYBACK_EXTRA_MUSIC_FILE_PATH
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_MUSIC_FILE_PATH";
    public static final String PLAYBACK_EXTRA_SEEK
            = "com.fesskiev.player.extra.SEEK";
    public static final String PLAYBACK_EXTRA_VOLUME
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_VOLUME";
    public static final String PLAYBACK_EXTRA_EQ_ENABLE
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_EQ_STATE";
    public static final String PLAYBACK_EXTRA_EQ_BAND
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_EQ_BAND";
    public static final String PLAYBACK_EXTRA_EQ_LEVEL
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_EQ_LEVEL";
    public static final String PLAYBACK_EXTRA_LOOPING_STATE
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_LOOPING_STATE";

    public static final String PLAYBACK_EXTRA_REVERB_ENABLE
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_REVERB_STATE";
    public static final String PLAYBACK_EXTRA_REVERB_LEVEL
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_REVERB_LEVEL";

    public static final String PLAYBACK_EXTRA_ECHO_ENABLE
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_ECHO_STATE";
    public static final String PLAYBACK_EXTRA_ECHO_LEVEL
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_ECHO_LEVEL";

    public static final String PLAYBACK_EXTRA_WHOOSH_ENABLE
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_WHOOSH_STATE";
    public static final String PLAYBACK_EXTRA_WHOOSH_LEVEL
            = "com.fesskiev.player.extra.PLAYBACK_EXTRA_WHOOSH_LEVEL";


    private AudioFocusManager audioFocusManager;
    private CountDownTimer timer;

    private int duration;
    private int position;
    private float positionPercent;
    private float volume;
    private int durationScale;
    private boolean playing;
    private boolean looping;
    private boolean enableEQ;
    private boolean enableReverb;
    private boolean enableEcho;
    private boolean enableWhoosh;
    private boolean headsetConnected;


    public static void startPlaybackService(Context context) {
        Intent intent = new Intent(context, PlaybackService.class);
        context.startService(intent);
    }

    public static void startPlaybackForegroundService(Context context) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_START_FOREGROUND);
        context.startService(intent);
    }

    public static void stopPlaybackForegroundService(Context context) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_STOP_FOREGROUND);
        context.startService(intent);
    }

    public static void startRecording(Context context) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_START_RECORDING);
        context.startService(intent);
    }

    public static void stopRecording(Context context) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_STOP_RECORDING);
        context.startService(intent);
    }


    public static void requestPlaybackStateIfNeed(Context context) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_STATE);
        context.startService(intent);
    }

    public static void changeReverbEnable(Context context, boolean enable) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_REVERB_STATE);
        intent.putExtra(PLAYBACK_EXTRA_REVERB_ENABLE, enable);
        context.startService(intent);
    }

    public static void changeReverbLevel(Context context, ReverbState state) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_REVERB_LEVEL);
        intent.putExtra(PLAYBACK_EXTRA_REVERB_LEVEL, state);
        context.startService(intent);
    }

    public static void changeWhooshEnable(Context context, boolean enable) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_WHOOSH_STATE);
        intent.putExtra(PLAYBACK_EXTRA_WHOOSH_ENABLE, enable);
        context.startService(intent);
    }

    public static void changeWhooshLevel(Context context, WhooshState level) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_WHOOSH_LEVEL);
        intent.putExtra(PLAYBACK_EXTRA_WHOOSH_LEVEL, level);
        context.startService(intent);
    }

    public static void changeEchoLevel(Context context, EchoState level) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_ECHO_LEVEL);
        intent.putExtra(PLAYBACK_EXTRA_ECHO_LEVEL, level);
        context.startService(intent);
    }

    public static void changeEchoEnable(Context context, boolean enable) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_ECHO_STATE);
        intent.putExtra(PLAYBACK_EXTRA_ECHO_ENABLE, enable);
        context.startService(intent);
    }

    public static void changeEQEnable(Context context, boolean enable) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_EQ_STATE);
        intent.putExtra(PLAYBACK_EXTRA_EQ_ENABLE, enable);
        context.startService(intent);
    }

    public static void changeEQBandLevel(Context context, int band, int level) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_EQ_BAND_STATE);
        intent.putExtra(PLAYBACK_EXTRA_EQ_BAND, band);
        intent.putExtra(PLAYBACK_EXTRA_EQ_LEVEL, level);
        context.startService(intent);
    }

    public static void changeLoopingState(Context context, boolean state) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAYBACK_LOOPING_STATE);
        intent.putExtra(PLAYBACK_EXTRA_LOOPING_STATE, state);
        context.startService(intent);
    }

    public static void openFile(Context context, String path) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_OPEN_FILE);
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

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Create playback service!");

        volume = 100;

        timer = new CountDownTimer(1000);
        timer.pause();
        timer.setOnCountDownListener(() -> {
            updatePlaybackState();

            if (duration > 0) {
                durationScale = duration / 100;
                positionPercent = positionPercent * 100;
                volume *= 100f;
            }

//            Log.d("event", "ev: " + PlaybackService.this.toString());
            EventBus.getDefault().post(PlaybackService.this);
        });

        audioFocusManager = new AudioFocusManager();
        audioFocusManager.setOnAudioFocusManagerListener(
                state -> {
                    switch (state) {
                        case AudioFocusManager.AUDIO_FOCUSED:
                            Log.d(TAG, "onFocusChanged: FOCUSED");
                            if (!playing) {
                                play();
                            }
                            setVolumeAudioPlayer(volume);
                            break;
                        case AudioFocusManager.AUDIO_NO_FOCUS_CAN_DUCK:
                            Log.d(TAG, "onFocusChanged: NO_FOCUS_CAN_DUCK");
                            setVolumeAudioPlayer(50);
                            break;
                        case AudioFocusManager.AUDIO_NO_FOCUS_NO_DUCK:
                            Log.d(TAG, "onFocusChanged: NO_FOCUS_NO_DUCK");
                            if (playing) {
                                stop();
                            }
                            break;
                    }
                });

        registerHeadsetReceiver();
        registerCallback();

        createPlayer();
    }

    private void next() {
        Log.w(TAG, "NEXT FROM SERVICE");
        MediaApplication.getInstance().getAudioPlayer().next();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                Log.d(TAG, "playback service handle intent: " + action);
                switch (action) {
                    case ACTION_START_FOREGROUND:
                        tryStartForeground();
                        break;
                    case ACTION_STOP_FOREGROUND:
                        tryStopForeground();
                        break;
                    case ACTION_OPEN_FILE:
                        String openPath = intent.getStringExtra(PLAYBACK_EXTRA_MUSIC_FILE_PATH);
                        openFile(openPath);
                        break;
                    case ACTION_START_PLAYBACK:
                        play();
                        break;
                    case ACTION_STOP_PLAYBACK:
                        stop();
                        break;
                    case ACTION_PLAYBACK_SEEK:
                        int seekValue = intent.getIntExtra(PLAYBACK_EXTRA_SEEK, -1);
                        setSeekAudioPlayer(seekValue);
                        break;
                    case ACTION_PLAYBACK_VOLUME:
                        int volumeValue = intent.getIntExtra(PLAYBACK_EXTRA_VOLUME, -1);
                        setVolumeAudioPlayer(volumeValue);
                        break;
                    case ACTION_PLAYBACK_EQ_STATE:
                        boolean eqEnable = intent.getBooleanExtra(PLAYBACK_EXTRA_EQ_ENABLE, false);
                        enableEQ(eqEnable);
                        EventBus.getDefault().post(PlaybackService.this);
                        break;
                    case ACTION_PLAYBACK_LOOPING_STATE:
                        boolean looping =
                                intent.getBooleanExtra(PLAYBACK_EXTRA_LOOPING_STATE, false);
                        setLoopingAudioPlayer(looping);
                        break;
                    case ACTION_PLAYBACK_EQ_BAND_STATE:
                        int band = intent.getIntExtra(PLAYBACK_EXTRA_EQ_BAND, -1);
                        int level = intent.getIntExtra(PLAYBACK_EXTRA_EQ_LEVEL, -1);
                        setEQBands(band, level);
                        break;
                    case ACTION_PLAYBACK_REVERB_STATE:
                        boolean reverbEnable = intent.getBooleanExtra(PLAYBACK_EXTRA_REVERB_ENABLE, false);
                        enableReverb(reverbEnable);
                        EventBus.getDefault().post(PlaybackService.this);
                        break;
                    case ACTION_PLAYBACK_REVERB_LEVEL:
                        ReverbState reverbState = intent.getParcelableExtra(PLAYBACK_EXTRA_REVERB_LEVEL);
                        if (reverbState != null) {
                            setReverbValue((int) reverbState.getMix(), (int) reverbState.getWeight(),
                                    (int) reverbState.getDamp(), (int) reverbState.getRoomSize());
                        }
                    case ACTION_PLAYBACK_ECHO_STATE:
                        boolean echoEnable = intent.getBooleanExtra(PLAYBACK_EXTRA_ECHO_ENABLE, false);
                        enableEcho(echoEnable);
                        EventBus.getDefault().post(PlaybackService.this);
                        break;
                    case ACTION_PLAYBACK_ECHO_LEVEL:
                        EchoState echoState = intent.getParcelableExtra(PLAYBACK_EXTRA_ECHO_LEVEL);
                        if (echoState != null) {
                            setEchoValue((int) echoState.getLevel());
                        }
                        break;
                    case ACTION_PLAYBACK_WHOOSH_STATE:
                        boolean whooshEnable = intent.getBooleanExtra(PLAYBACK_EXTRA_WHOOSH_ENABLE, false);
                        enableWhoosh(whooshEnable);
                        break;
                    case ACTION_PLAYBACK_WHOOSH_LEVEL:
                        WhooshState whooshState = intent.getParcelableExtra(PLAYBACK_EXTRA_WHOOSH_LEVEL);
                        if (whooshState != null) {
                            setWhooshValue((int) whooshState.getMix(), (int) whooshState.getFrequency());
                        }
                        break;
                    case ACTION_PLAYBACK_STATE:
                        sendPlaybackStateIfNeed();
                        break;
                    case ACTION_START_RECORDING:
                        startRecording(CacheManager.getRecordDestPath().getAbsolutePath());
                        break;
                    case ACTION_STOP_RECORDING:
                        stopRecording();
                        break;
                }
            }
        }
        return START_STICKY;
    }


    private void tryStopForeground() {
        stopForeground(true);
    }

    private void tryStartForeground() {
        startForeground(AudioNotificationHelper.NOTIFICATION_ID,
                AudioNotificationHelper.getInstance(getApplicationContext()).getNotification());
    }

    private void sendPlaybackStateIfNeed() {
        if (!playing) {
            EventBus.getDefault().post(PlaybackService.this);
        }
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
                    if (intent.hasExtra("state")) {
                        if (headsetConnected && intent.getIntExtra("state", 0) == 0) {
                            headsetConnected = false;
                            Log.w(TAG, "PLUG OUT");
                            if (playing) {
                                stop();
                            }
                        } else if (!headsetConnected && intent.getIntExtra("state", 0) == 1) {
                            if (!isInitialStickyBroadcast()) {
                                Log.w(TAG, "PLUG IN");
                                if (AppSettingsManager.getInstance().isPlayPlugInHeadset() && !playing) {
                                    play();
                                }
                            }
                            headsetConnected = true;
                        }
                    }
                    break;
            }
        }
    };


    private void createPlayer() {

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

        Log.d(TAG, "create audio player!");
        createAudioPlayer(Integer.valueOf(sampleRateString), Integer.valueOf(bufferSizeString),
                CacheManager.getRecordTempPath().getAbsolutePath());

        setEffects();
    }


    private void setEffects() {
        createEQStateIfNeed();
        createReverbStateIfNeed();
        createEchoIfNeed();
        createWhooshIfNeed();
    }

    private void createEchoIfNeed() {
        EchoState echoState = AppSettingsManager.getInstance().getEchoState();
        if (echoState != null) {
            Log.wtf(TAG, "create Echo state");
            setEchoValue((int) echoState.getLevel());
        }
    }

    private void createWhooshIfNeed() {
        WhooshState whooshState = AppSettingsManager.getInstance().getWhooshState();
        if (whooshState != null) {
            Log.wtf(TAG, "create Whoosh state");
            setWhooshValue((int) whooshState.getMix(), (int) whooshState.getFrequency());
        }
    }

    private void createReverbStateIfNeed() {
        ReverbState reverbState = AppSettingsManager.getInstance().getReverbState();
        if (reverbState != null) {
            Log.wtf(TAG, "create Reverb state");
            setReverbValue((int) reverbState.getMix(), (int) reverbState.getWeight(),
                    (int) reverbState.getDamp(), (int) reverbState.getRoomSize());
        }
    }

    private void createEQStateIfNeed() {
        EQState eqState = AppSettingsManager.getInstance().getEQState();
        if (eqState != null) {
            Log.wtf(TAG, "create EQ state");
            for (int i = 0; i < 3; i++) {
                switch (i) {
                    case 0:
                        setEQBands(i, (int) eqState.getLowLevel());
                        break;
                    case 1:
                        setEQBands(i, (int) eqState.getMidLevel());
                        break;
                    case 2:
                        setEQBands(i, (int) eqState.getHighLevel());
                        break;
                }
            }
        }
    }

    private void openFile(String path) {
        Log.d(TAG, "open audio file!");
        openAudioFile(path);
    }


    private void play() {
        Log.d(TAG, "start playback");
        togglePlayback();
        audioFocusManager.tryToGetAudioFocus();
        timer.tick();

    }


    private void stop() {
        Log.d(TAG, "stop playback");
        togglePlayback();
        audioFocusManager.giveUpAudioFocus();
        timer.pause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroy playback service");

        if (playing) {
            stop();
        }
        timer.stop();

        unregisterHeadsetReceiver();
        unregisterCallback();
        onDestroyAudioPlayer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static {
        System.loadLibrary("SuperpoweredPlayer");
    }

    public native void updatePlaybackState();

    public native void onDestroyAudioPlayer();

    public native void onBackground();

    public native void onForeground();

    public native void registerCallback();

    public native void unregisterCallback();

    public native void createAudioPlayer(int sampleRate, int bufferSize, String recorderTempPath);

    public native void openAudioFile(String path);

    public native void togglePlayback();

    public native void setVolumeAudioPlayer(float value);

    public native void setSeekAudioPlayer(int value);

    public native void setLoopingAudioPlayer(boolean isLooping);

    /***
     * EQ methods
     */
    public native void enableEQ(boolean enable);

    public native void setEQBands(int band, int value);

    /***
     * Reverb
     */
    public native void setReverbValue(int mix, int width, int damp, int roomSize);

    public native void enableReverb(boolean enable);

    /***
     * Echo
     */
    public native void setEchoValue(int value);

    public native void enableEcho(boolean enable);

    /***
     * Whoosh!
     */
    public native void setWhooshValue(int wet, int frequency);

    public native void enableWhoosh(boolean enable);

    /***
     * Recording
     */
    public native void startRecording(String destinationPath);

    public native void stopRecording();


    @Keep
    public void playStatusCallback(int status) {
        if (status == END_TRACK) {
            next();
        }
    }


    public float getPositionPercent() {
        return positionPercent;
    }

    public int getDuration() {
        return duration;
    }

    public int getPosition() {
        return position;
    }

    public boolean isPlaying() {
        return playing;
    }

    public float getVolume() {
        return volume;
    }

    public boolean isLooping() {
        return looping;
    }

    public boolean isEnableEQ() {
        return enableEQ;
    }

    public boolean isEnableReverb() {
        return enableReverb;
    }

    public boolean isEnableWhoosh() {
        return enableWhoosh;
    }

    public boolean isEnableEcho() {
        return enableEcho;
    }

    @Override
    public String toString() {
        return "PlaybackService{" +
                "duration=" + duration +
                ", position=" + position +
                ", positionPercent=" + positionPercent +
                ", volume=" + volume +
                ", durationScale=" + durationScale +
                ", playing=" + playing +
                ", looping=" + looping +
                ", enableEQ=" + enableEQ +
                ", enableReverb=" + enableReverb +
                ", enableEcho=" + enableEcho +
                ", enableWhoosh=" + enableWhoosh +
                '}';
    }
}
