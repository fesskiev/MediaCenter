package com.fesskiev.mediacenter.utils;


import android.content.Context;
import android.media.AudioManager;
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
    private int audioFocusState;

    public AudioFocusManager() {
        audioManager = (AudioManager)
                MediaApplication.getInstance().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
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
            int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocusState = AUDIO_FOCUSED;
            }
        }
    }

    public void giveUpAudioFocus() {
        if (audioFocusState == AUDIO_FOCUSED) {
            if (audioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
            }
        }
    }

    private void changeState(){
        if (listener != null){
            listener.onFocusChanged(audioFocusState);
        }
    }

    public void setOnAudioFocusManagerListener(OnAudioFocusManagerListener l) {
        this.listener = l;
    }
}
