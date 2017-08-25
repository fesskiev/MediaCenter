package com.fesskiev.mediacenter.utils;


import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.fesskiev.mediacenter.MediaApplication;

public class AudioFocusManager implements AudioManager.OnAudioFocusChangeListener {


    public interface OnAudioFocusManagerListener {
        void onFocusChanged(int state);
    }

    private static final String TAG = AudioFocusManager.class.getName();

    public static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    public static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    public static final int AUDIO_FOCUSED = 2;

    private OnAudioFocusManagerListener listener;
    private AudioManager audioManager;
    private AudioFocusRequestOreo audioFocusRequestOreo;
    private int audioFocusState;

    public AudioFocusManager() {
        audioManager = (AudioManager)
                MediaApplication.getInstance().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
        }
        if(Utils.isOreo()){
            audioFocusRequestOreo = new AudioFocusRequestOreo(this);
        }

        audioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.d(TAG, "onAudioFocusChange. focusChange=" + focusChange);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                audioFocusState = AUDIO_FOCUSED;
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                audioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                audioFocusState = AUDIO_NO_FOCUS_CAN_DUCK;
                break;
            default:
                Log.e(TAG, "onAudioFocusChange: Ignoring unsupported focusChange: " + focusChange);
        }
        changeState();
    }

    public void tryToGetAudioFocus() {
        if (audioFocusState != AUDIO_FOCUSED) {
            Log.d(TAG, "requestAudioFocus");

            int result;
            if (Utils.isOreo()) {
                result = audioManager.requestAudioFocus(audioFocusRequestOreo.getAudioFocusRequest());
            } else {
                result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);
            }


            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocusState = AUDIO_FOCUSED;
            }
        }
    }

    public void giveUpAudioFocus() {
        if (audioFocusState == AUDIO_FOCUSED) {
            Log.d(TAG, "giveUpAudioFocus");

            if (audioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
            }
        }
    }

    private void changeState() {
        if (listener != null) {
            listener.onFocusChanged(audioFocusState);
        }
    }

    public void setOnAudioFocusManagerListener(OnAudioFocusManagerListener l) {
        this.listener = l;
    }


    @TargetApi(Build.VERSION_CODES.O)
    private class AudioFocusRequestOreo {

        private AudioFocusRequest audioFocusRequest;

        public AudioFocusRequestOreo(AudioManager.OnAudioFocusChangeListener listener) {

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(listener, new Handler())
                    .build();

        }

        public AudioFocusRequest getAudioFocusRequest() {
            return audioFocusRequest;
        }
    }
}
