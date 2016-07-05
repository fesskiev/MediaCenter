package com.fesskiev.player.utils;


import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import com.fesskiev.player.MediaApplication;

public class AudioFocusManager implements AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = AudioFocusManager.class.getName();

    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    private static final int AUDIO_FOCUSED = 2;

    private AudioManager audioManager;
    private int audioFocus = AUDIO_NO_FOCUS_NO_DUCK;

    public AudioFocusManager() {
        audioManager = (AudioManager)
                MediaApplication.getInstance().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.d(TAG, "onAudioFocusChange. focusChange=" + focusChange);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                audioFocus = AUDIO_FOCUSED;
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                audioFocus = AUDIO_NO_FOCUS_NO_DUCK;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                audioFocus = AUDIO_NO_FOCUS_CAN_DUCK;
                break;
            default:
                Log.e(TAG, "onAudioFocusChange: Ignoring unsupported focusChange: " + focusChange);
        }
    }

    public void tryToGetAudioFocus() {
        if (audioFocus != AUDIO_FOCUSED) {
            int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocus = AUDIO_FOCUSED;
            }
        }
    }

    public void giveUpAudioFocus() {
        if (audioFocus == AUDIO_FOCUSED) {
            if (audioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocus = AUDIO_NO_FOCUS_NO_DUCK;
            }
        }
    }
}
