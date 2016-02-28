package com.fesskiev.player.ui.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.services.PlaybackService;
import com.squareup.picasso.Picasso;

import java.io.File;

public class PlaybackActivity extends AppCompatActivity {

    private static final String TAG = PlaybackActivity.class.getSimpleName();

    private BottomSheetBehavior bottomSheetBehavior;
    private ImageView playPause;
    private TextView track;
    private TextView artist;
    private ImageView cover;
    private View peakView;
    private boolean isPlaying;
    private int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerPlaybackBroadcastReceiver();

    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        track = (TextView) findViewById(R.id.track);
        artist = (TextView) findViewById(R.id.artist);
        cover = (ImageView) findViewById(R.id.cover);
        playPause = (ImageView) findViewById(R.id.playPauseButton);
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    PlaybackService.stopPlayback(PlaybackActivity.this);
                } else {
                    PlaybackService.startPlayback(PlaybackActivity.this);
                }
            }
        });


        AudioFile audioFile = MediaApplication.getInstance().getAudioPlayer().currentAudioFile;
        if(audioFile != null) {
            setMusicFileInfo(audioFile);
        }

        FrameLayout bottomMenuContainer = (FrameLayout) findViewById(R.id.bottomMenuContainer);
        bottomMenuContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioPlayerActivity.startPlayerActivity(PlaybackActivity.this, false, cover);
            }
        });


        bottomSheetBehavior = BottomSheetBehavior.from(bottomMenuContainer);
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {

                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                }
            });

            peakView = findViewById(R.id.basicNavPlayerContainer);
            peakView.post(new Runnable() {
                @Override
                public void run() {
                    height = peakView.getHeight();
                }
            });
        }
    }

    public void setPlyingStateButton(boolean isPlaying) {
        this.isPlaying = isPlaying;
        int resource = isPlaying ? R.drawable.pause_icon : R.drawable.play_icon;
        playPause.setImageDrawable(ContextCompat.getDrawable(this, resource));
    }

    public void setMusicFileInfo(AudioFile audioFile) {
        track.setText(audioFile.title);
        artist.setText(audioFile.artist);

        Bitmap artwork = audioFile.getArtwork();
        if (artwork != null) {
            cover.setImageBitmap(artwork);
        } else {
            AudioFolder audioFolder = MediaApplication.getInstance().getAudioPlayer().currentAudioFolder;
            if (audioFolder != null && audioFolder.folderImages.size() > 0) {
                File coverFile = audioFolder.folderImages.get(0);
                if (coverFile != null) {
                    Picasso.with(this).load(coverFile).into(cover);
                }
            } else {
                Picasso.with(this).load(R.drawable.no_cover_icon).into(cover);
            }
        }
    }

    public void showPlayback() {
        bottomSheetBehavior.setPeekHeight(height);
        peakView.requestLayout();
    }

    public void hidePlayback() {
        bottomSheetBehavior.setPeekHeight(0);
        peakView.requestLayout();
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
                        setPlyingStateButton(true);
                        setMusicFileInfo(MediaApplication.getInstance().
                                getAudioPlayer().currentAudioFile);
                        showPlayback();
                    } else {
                        setPlyingStateButton(false);
                    }
                    break;
            }
        }
    };
}
