package com.fesskiev.player.ui.player;


import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.IntentCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fesskiev.player.MusicApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.MusicFile;
import com.fesskiev.player.model.MusicFolder;
import com.fesskiev.player.model.MusicPlayer;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.ui.equalizer.EqualizerActivity;
import com.fesskiev.player.ui.tracklist.TrackListActivity;
import com.fesskiev.player.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;

public class PlayerActivity extends AppCompatActivity implements Playable {

    private static final String TAG = PlayerActivity.class.getSimpleName();

    private MusicPlayer musicPlayer;
    private FloatingActionButton playStopButton;
    private CardView cardDescription;
    private ImageView volumeLevel;
    private TextView trackTimeCount;
    private TextView trackTimeTotal;
    private TextView artist;
    private TextView title;
    private TextView genre;
    private TextView album;
    private TextView trackDescription;
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

        musicPlayer = MusicApplication.getInstance().getMusicPlayer();

        MusicFolder musicFolder = musicPlayer.currentMusicFolder;

        cardDescription = (CardView) findViewById(R.id.cardDescription);
        volumeLevel = (ImageView) findViewById(R.id.volumeLevel);
        trackTimeTotal = (TextView) findViewById(R.id.trackTimeTotal);
        trackTimeCount = (TextView) findViewById(R.id.trackTimeCount);
        artist = (TextView) findViewById(R.id.trackArtist);
        title = (TextView) findViewById(R.id.trackTitle);
        trackDescription = (TextView) findViewById(R.id.trackDescription);
        genre = (TextView) findViewById(R.id.genre);
        album = (TextView) findViewById(R.id.album);

        ImageView backdrop = (ImageView) findViewById(R.id.backdrop);
        if (!musicFolder.folderImages.isEmpty()) {
            File albumImagePath = musicFolder.folderImages.get(0);
            if (albumImagePath != null) {
                backdrop.setImageBitmap(Utils.getResizedBitmap(1024, 1024,
                        albumImagePath.getAbsolutePath()));
            }
        } else {
            Bitmap artwork = musicPlayer.currentMusicFile.getArtwork();
            if (artwork != null) {
                backdrop.setImageBitmap(artwork);
            } else {
                Picasso.with(this).
                        load(R.drawable.no_cover_icon).
                        into(backdrop);
            }
        }


        findViewById(R.id.equalizer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PlayerActivity.this, EqualizerActivity.class));
            }
        });

        findViewById(R.id.previousTrack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previous();
            }
        });

        findViewById(R.id.nextTrack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }
        });


        playStopButton =
                (FloatingActionButton) findViewById(R.id.playStopFAB);
        playStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    pause();
                } else {
                    play();
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


        resetIndicators();
        setTrackInformation();
        createPlayer();
        play();

        registerPlaybackBroadcastReceiver();
    }

    @Override
    public void play() {
        PlaybackService.startPlayback(PlayerActivity.this);
    }

    @Override
    public void pause() {
        PlaybackService.stopPlayback(PlayerActivity.this);
    }

    @Override
    public void next() {
        musicPlayer.next();

        animateCardDescription(true);
        resetIndicators();
        createPlayer();
    }

    @Override
    public void previous() {
        musicPlayer.previous();

        animateCardDescription(false);
        resetIndicators();
        createPlayer();
    }

    @Override
    public void createPlayer() {
        PlaybackService.createPlayer(this, musicPlayer.currentMusicFile.filePath);
    }

    private void animateCardDescription(boolean next) {
        float value = next ? cardDescription.getWidth() +
                getResources().getDimension(R.dimen.activity_horizontal_margin) :
                -(cardDescription.getWidth() -
                        getResources().getDimension(R.dimen.activity_horizontal_margin));

        cardDescription.
                animate().
                x(value).
                setDuration(500).
                setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        cardDescription.animate().
                                x(getResources().
                                        getDimension(R.dimen.activity_horizontal_margin)).
                                setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        setTrackInformation();
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                });
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }


    private void resetIndicators() {
        trackTimeTotal.setText("0:00");
        trackTimeCount.setText("0:00");
        trackSeek.setProgress(0);
    }


    private void setTrackInformation() {
        MusicFile currentMusicFile = musicPlayer.currentMusicFile;
        artist.setText(currentMusicFile.artist);
        title.setText(currentMusicFile.title);
        album.setText(currentMusicFile.album);
        genre.setText(currentMusicFile.genre);

        StringBuilder sb = new StringBuilder();
        sb.append("MP3::");
        sb.append(currentMusicFile.sampleRate);
        sb.append("::");
        sb.append(currentMusicFile.bitrate);
        trackDescription.setText(sb.toString());
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
        filter.addAction(PlaybackService.ACTION_SONG_END);
        filter.addAction(PlaybackService.ACTION_HEADSET_PLUG_IN);
        filter.addAction(PlaybackService.ACTION_HEADSET_PLUG_OUT);
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
                case PlaybackService.ACTION_SONG_END:
                    next();
                    play();
                    break;
                case PlaybackService.ACTION_HEADSET_PLUG_IN:
                    break;
                case PlaybackService.ACTION_HEADSET_PLUG_OUT:
                    pause();
                    break;
            }
        }
    };

}
