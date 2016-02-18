package com.fesskiev.player.ui.player;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.IntentCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.ui.equalizer.EqualizerActivity;
import com.fesskiev.player.ui.tracklist.TrackListActivity;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.cards.DescriptionCardView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;

public class AudioPlayerActivity extends AppCompatActivity implements Playable {

    private static final String TAG = AudioPlayerActivity.class.getSimpleName();
    public static final String EXTRA_IS_NEW_TRACK = "com.fesskiev.player.EXTRA_IS_NEW_TRACK";

    private AudioPlayer audioPlayer;
    private FloatingActionButton playStopButton;
    private DescriptionCardView cardDescription;
    private ImageView volumeLevel;
    private TextView trackTimeCount;
    private TextView trackTimeTotal;
    private TextView artist;
    private TextView title;
    private TextView genre;
    private TextView album;
    private TextView trackDescription;
    private SeekBar trackSeek;
    private SeekBar volumeSeek;

    public static void startPlayerActivity(Context context, boolean isNewTrack) {
        context.startActivity(new Intent(context,
                AudioPlayerActivity.class).putExtra(AudioPlayerActivity.EXTRA_IS_NEW_TRACK, isNewTrack));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        if (savedInstanceState == null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        navigateUpToFromChild(AudioPlayerActivity.this,
                                IntentCompat.makeMainActivity(new ComponentName(AudioPlayerActivity.this,
                                        TrackListActivity.class)));
                    }
                });
            }
        }

        audioPlayer = MediaApplication.getInstance().getAudioPlayer();

        setBackdropImage();

        volumeLevel = (ImageView) findViewById(R.id.volumeLevel);
        trackTimeTotal = (TextView) findViewById(R.id.trackTimeTotal);
        trackTimeCount = (TextView) findViewById(R.id.trackTimeCount);
        artist = (TextView) findViewById(R.id.trackArtist);
        title = (TextView) findViewById(R.id.trackTitle);
        trackDescription = (TextView) findViewById(R.id.trackDescription);
        genre = (TextView) findViewById(R.id.genre);
        album = (TextView) findViewById(R.id.album);

        cardDescription = (DescriptionCardView) findViewById(R.id.cardDescription);
        cardDescription.setOnCardAnimationListener(new DescriptionCardView.OnCardAnimationListener() {
            @Override
            public void animationStart() {

            }

            @Override
            public void animationEnd() {
                setTrackInformation();
            }
        });

        findViewById(R.id.equalizer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AudioPlayerActivity.this, EqualizerActivity.class));
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
                if (audioPlayer.isPlaying) {
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
                PlaybackService.seekPlayback(AudioPlayerActivity.this, progress);
            }
        });


        volumeSeek = (SeekBar) findViewById(R.id.seekVolume);
        volumeSeek.setProgress(100);
        volumeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioPlayer.volume = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setVolumeLevel();
            }
        });


        boolean isNewTrack = getIntent().getBooleanExtra(EXTRA_IS_NEW_TRACK, false);
        if (isNewTrack) {
            createNewTrack();
        } else {
            resumePlaying();
        }

        registerPlaybackBroadcastReceiver();
    }

    private void createNewTrack() {
        createPlayer();
        play();
        setTrackInformation();
        setVolumeLevel();
    }

    private void resumePlaying() {
        setTrackInformation();
        setVolumeLevel();
        setPlayingIcon();
    }

    private void setBackdropImage() {
        ImageView backdrop = (ImageView) findViewById(R.id.backdrop);
        Bitmap artwork = audioPlayer.currentAudioFile.getArtwork();
        if (artwork != null) {
            backdrop.setImageBitmap(artwork);
        } else {
            setFolderBackdropImage(backdrop);
        }

    }

    private void setFolderBackdropImage(final ImageView backdrop) {
        AudioFolder audioFolder = audioPlayer.currentAudioFolder;
        if (!audioFolder.folderImages.isEmpty()) {
            File albumImagePath = audioFolder.folderImages.get(0);
            if (albumImagePath != null) {
                Picasso.with(this)
                        .load(albumImagePath.getAbsolutePath())
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                                backdrop.setImageBitmap(bitmap);
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {

                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                        });
            }
        } else {
            Picasso.with(this).
                    load(R.drawable.no_cover_icon).
                    into(backdrop);
        }
    }

    @Override
    public void play() {
        PlaybackService.startPlayback(AudioPlayerActivity.this);
    }

    @Override
    public void pause() {
        PlaybackService.stopPlayback(AudioPlayerActivity.this);
    }

    @Override
    public void next() {
        audioPlayer.next();

        cardDescription.next();
        resetIndicators();
        createPlayer();
    }

    @Override
    public void previous() {
        audioPlayer.previous();

        cardDescription.previous();
        resetIndicators();
        createPlayer();
    }

    @Override
    public void createPlayer() {
        PlaybackService.createPlayer(this, audioPlayer.currentAudioFile.filePath);
    }

    private void setVolumeLevel() {
        volumeSeek.setProgress(audioPlayer.volume);
        PlaybackService.volumePlayback(AudioPlayerActivity.this, audioPlayer.volume);
        if (audioPlayer.volume <= 45) {
            volumeLevel.setImageResource(R.drawable.low_volume_icon);
        } else {
            volumeLevel.setImageResource(R.drawable.high_volume_icon);
        }
    }

    private void resetIndicators() {
        trackTimeTotal.setText("0:00");
        trackTimeCount.setText("0:00");
        trackSeek.setProgress(0);
    }


    private void setTrackInformation() {
        AudioFile currentAudioFile = audioPlayer.currentAudioFile;
        artist.setText(currentAudioFile.artist);
        title.setText(currentAudioFile.title);
        album.setText(currentAudioFile.album);
        genre.setText(currentAudioFile.genre);

        StringBuilder sb = new StringBuilder();
        sb.append(currentAudioFile.sampleRate);
        sb.append("::");
        sb.append(currentAudioFile.bitrate);
        trackDescription.setText(sb.toString());
    }

    private void setPlayingIcon() {
        if (audioPlayer.isPlaying) {
            playStopButton.
                    setImageDrawable(ContextCompat.getDrawable(AudioPlayerActivity.this,
                            R.drawable.pause_icon));
        } else {
            playStopButton.
                    setImageDrawable(ContextCompat.getDrawable(AudioPlayerActivity.this,
                            R.drawable.play_icon));
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
                    audioPlayer.isPlaying = intent.getBooleanExtra(PlaybackService.PLAYBACK_EXTRA_PLAYING, false);
                    setPlayingIcon();
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
