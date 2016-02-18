package com.fesskiev.player.ui.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.services.PlaybackService;

public class PlaybackActivity extends AppCompatActivity {

    private static final String TAG = PlaybackActivity.class.getSimpleName();

    private PlaybackControlFragment controlFragment;
    private boolean playBackControlShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerPlaybackBroadcastReceiver();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        controlFragment = (PlaybackControlFragment) getFragmentManager()
                .findFragmentById(R.id.fragmentPlaybackControl);
        playBackControlShow = true;
        hidePlaybackControl();

    }

    public void showPlaybackControl() {
        if (!playBackControlShow) {
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.animator.slide_up, R.animator.slide_down,
                            R.animator.slide_up, R.animator.slide_down)
                    .show(controlFragment)
                    .commitAllowingStateLoss();
            playBackControlShow = true;
        }
    }

    public void hidePlaybackControl() {
        if (playBackControlShow) {
            getFragmentManager().beginTransaction()
                    .hide(controlFragment)
                    .commitAllowingStateLoss();
            playBackControlShow = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterPlaybackBroadcastReceiver();
    }


    private void registerPlaybackBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlaybackService.ACTION_PLAYBACK_VALUES);
        filter.addAction(PlaybackService.ACTION_PLAYBACK_PLAYING_STATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(playbackReceiver, filter);
    }

    private void unregisterPlaybackBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playbackReceiver);
    }


    private BroadcastReceiver playbackReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PlaybackService.ACTION_PLAYBACK_VALUES:
                    int duration = intent.getIntExtra(PlaybackService.PLAYBACK_EXTRA_DURATION, 0);

                    break;
                case PlaybackService.ACTION_PLAYBACK_PLAYING_STATE:
                    boolean isPlaying = intent.getBooleanExtra(PlaybackService.PLAYBACK_EXTRA_PLAYING, false);
                    Log.w(TAG, "playback activity is plying: " + isPlaying);
                    if (isPlaying) {
                        controlFragment.setPlyingStateButton(true);
                        controlFragment.setMusicFileInfo(MediaApplication.getInstance().
                                getAudioPlayer().currentAudioFile);
                        showPlaybackControl();
                    } else {
                        controlFragment.setPlyingStateButton(false);
                    }

                    break;
            }
        }
    };
}
