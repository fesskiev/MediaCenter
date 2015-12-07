package com.fesskiev.player.ui.player;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.IntentCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fesskiev.player.R;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.ui.tracklist.MusicTrackListFragment;
import com.fesskiev.player.ui.tracklist.TrackListActivity;
import com.fesskiev.player.utils.Utils;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = PlayerActivity.class.getSimpleName();

    private FloatingActionButton playStopButton;
    private ImageView volumeLevel;
    private TextView trackTimeCount;
    private TextView trackTimeTotal;
    private SeekBar trackSeek;
    private boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        if (savedInstanceState == null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        navigateUpToFromChild(PlayerActivity.this,
                                IntentCompat.makeMainActivity(new ComponentName(PlayerActivity.this,
                                        TrackListActivity.class)));
                    }
                });
            }
        }

        String musicFilePath =
                getIntent().getExtras().getString(MusicTrackListFragment.MUSIC_FILE_PATH);
        String coverFilePath =
                getIntent().getExtras().getString(MusicTrackListFragment.COVER_IMAGE_PATH);

        volumeLevel = (ImageView) findViewById(R.id.volumeLevel);
        trackTimeTotal = (TextView) findViewById(R.id.trackTimeTotal);
        trackTimeCount = (TextView) findViewById(R.id.trackTimeCount);

        ImageView backdrop = (ImageView) findViewById(R.id.backdrop);
        backdrop.setImageBitmap(Utils.getResizedBitmap(1024, 1024, coverFilePath));

        playStopButton =
                (FloatingActionButton) findViewById(R.id.playStopFAB);
        playStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    PlaybackService.stopPlayback(PlayerActivity.this);
                } else {
                    PlaybackService.startPlayback(PlayerActivity.this);
                }
            }
        });

        trackSeek = (SeekBar) findViewById(R.id.seekSong);
        trackSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.progress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PlaybackService.seekPlayback(PlayerActivity.this, progress);
            }
        });


        SeekBar volumeSeek = (SeekBar) findViewById(R.id.seekVolume);
        volumeSeek.setProgress(100);
        volumeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int lastProgress = 100;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    lastProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PlaybackService.volumePlayback(PlayerActivity.this, lastProgress);

                if (lastProgress <= 45) {
                    volumeLevel.setImageResource(R.drawable.low_volume_icon);
                } else {
                    volumeLevel.setImageResource(R.drawable.high_volume_icon);
                }
            }
        });


        if (musicFilePath != null) {
            PlaybackService.createPlayer(this, musicFilePath);
        }

        registerPlaybackBroadcastReceiver();
    }


    @Override
    protected void onStop() {
        super.onStop();
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
                    int progress =
                            intent.getIntExtra(PlaybackService.PLAYBACK_EXTRA_PROGRESS, 0);
                    int progressScale =
                            intent.getIntExtra(PlaybackService.PLAYBACK_EXTRA_PROGRESS_SCALE, 0);

                    trackSeek.setProgress(progressScale);
                    trackTimeTotal.setText(Utils.getTimeString(duration));
                    trackTimeCount.setText(Utils.getTimeString(progress));
                    break;
                case PlaybackService.ACTION_PLAYBACK_PLAYING_STATE:
                    isPlaying = intent.getBooleanExtra(PlaybackService.PLAYBACK_EXTRA_PLAYING, false);
                    if (isPlaying) {
                        playStopButton.
                                setImageDrawable(ContextCompat.getDrawable(PlayerActivity.this,
                                        R.drawable.pause_icon));
                    } else {
                        playStopButton.
                                setImageDrawable(ContextCompat.getDrawable(PlayerActivity.this,
                                        R.drawable.play_icon));
                    }
                    break;
            }
        }
    };

}
