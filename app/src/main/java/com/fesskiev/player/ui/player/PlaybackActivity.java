package com.fesskiev.player.ui.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.fesskiev.player.MusicApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.services.PlaybackService;

public class PlaybackActivity extends AppCompatActivity {

    private static final String TAG = PlaybackActivity.class.getSimpleName();

    private PlaybackControlFragment controlFragment;
    private boolean playBackControlShow;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();

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

    protected void showPlaybackControl() {
        if (!playBackControlShow) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom,
                                    R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom)
                            .show(controlFragment)
                            .commitAllowingStateLoss();
                    playBackControlShow = true;
                }
            }, 1000);
        }
    }

    protected void hidePlaybackControl() {
        if (playBackControlShow) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getFragmentManager().beginTransaction()
                            .hide(controlFragment)
                            .commitAllowingStateLoss();
                }
            }, 1000);
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
                    int duration =
                            intent.getIntExtra(PlaybackService.PLAYBACK_EXTRA_DURATION, 0);

                    break;
                case PlaybackService.ACTION_PLAYBACK_PLAYING_STATE:
                    boolean isPlaying = intent.getBooleanExtra(PlaybackService.PLAYBACK_EXTRA_PLAYING, false);
                    Log.w(TAG, "playback activity is plying: " + isPlaying);
                    if (isPlaying) {
                        controlFragment.setPlyingStateButton(true);
                        controlFragment.setMusicFileInfo(MusicApplication.getInstance().
                                getMusicPlayer().currentMusicFile);
                        showPlaybackControl();
                    } else {
                        controlFragment.setPlyingStateButton(false);
                        hidePlaybackControl();
                    }

                    break;
            }
        }
    };
}
