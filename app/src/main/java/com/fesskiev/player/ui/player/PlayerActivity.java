package com.fesskiev.player.ui.player;


import android.animation.Animator;
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
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.ui.MusicFoldersFragment;
import com.fesskiev.player.ui.equalizer.EqualizerActivity;
import com.fesskiev.player.ui.tracklist.TrackListActivity;
import com.fesskiev.player.ui.tracklist.TrackListFragment;
import com.fesskiev.player.utils.Utils;

import java.io.File;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = PlayerActivity.class.getSimpleName();

    private FloatingActionButton playStopButton;
    private CardView cardDescription;
    private ImageView volumeLevel;
    private ImageView previousTrack;
    private ImageView nextTrack;
    private TextView trackTimeCount;
    private TextView trackTimeTotal;
    private TextView artist;
    private TextView title;
    private TextView genre;
    private TextView album;
    private TextView trackDescription;
    private SeekBar trackSeek;
    private MusicFolder musicFolder;
    private MusicFile currentMusicFile;
    private boolean isPlaying;
    private int folderPosition;
    private int filePosition;


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

        folderPosition = getIntent().getExtras().getInt(MusicFoldersFragment.FOLDER_POSITION);
        filePosition = getIntent().getExtras().getInt(TrackListFragment.FILE_POSITION);

        musicFolder =
                ((MusicApplication) getApplication()).getMusicFolders().get(folderPosition);
        setCurrentMusicFile(filePosition);

        cardDescription = (CardView) findViewById(R.id.cardDescription);
        volumeLevel = (ImageView) findViewById(R.id.volumeLevel);
        previousTrack = (ImageView) findViewById(R.id.previousTrack);
        nextTrack = (ImageView) findViewById(R.id.nextTrack);
        trackTimeTotal = (TextView) findViewById(R.id.trackTimeTotal);
        trackTimeCount = (TextView) findViewById(R.id.trackTimeCount);
        artist = (TextView) findViewById(R.id.trackArtist);
        title = (TextView) findViewById(R.id.trackTitle);
        trackDescription = (TextView) findViewById(R.id.trackDescription);
        genre = (TextView) findViewById(R.id.genre);
        album = (TextView) findViewById(R.id.album);

        ImageView backdrop = (ImageView) findViewById(R.id.backdrop);
        if(!musicFolder.folderImages.isEmpty()) {
            File albumImagePath = musicFolder.folderImages.get(0);
            if (albumImagePath != null) {
                backdrop.setImageBitmap(Utils.getResizedBitmap(1024, 1024,
                        albumImagePath.getAbsolutePath()));
            }
        }


        findViewById(R.id.equalizer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PlayerActivity.this, EqualizerActivity.class));
            }
        });

        previousTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePosition > 0) {
                    filePosition--;
                }
                setCurrentMusicFile(filePosition);

                animateCardDescription(false);
                resetTimers();
                createPlayer();
            }
        });

        nextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePosition < musicFolder.musicFilesDescription.size() - 1) {
                    filePosition++;
                }

                setCurrentMusicFile(filePosition);

                animateCardDescription(true);
                resetTimers();
                createPlayer();
            }
        });


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


        resetTimers();
        setTrackInformation();
        createPlayer();

        registerPlaybackBroadcastReceiver();
    }

    private void setCurrentMusicFile(int filePosition) {
        currentMusicFile = musicFolder.musicFilesDescription.get(filePosition);
        ((MusicApplication) getApplication()).setCurrentMusicFile(currentMusicFile);
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


    private void resetTimers() {
        trackTimeTotal.setText("0:00");
        trackTimeCount.setText("0:00");
    }


    private void createPlayer() {
        if (currentMusicFile != null) {
            PlaybackService.createPlayer(this, currentMusicFile.filePath);
        }
    }

    private void setTrackInformation() {
        if (currentMusicFile != null) {
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
