package com.fesskiev.player.ui.audio.player;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.analytics.AnalyticsActivity;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.ui.playback.Playable;
import com.fesskiev.player.utils.AppLog;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.buttons.MuteSoloButton;
import com.fesskiev.player.widgets.buttons.RepeatButton;
import com.fesskiev.player.widgets.cards.DescriptionCardView;
import com.fesskiev.player.widgets.controls.AudioControlView;
import com.fesskiev.player.widgets.utils.DisabledScrollView;


public class AudioPlayerActivity extends AnalyticsActivity implements Playable {

    public static final String EXTRA_IS_NEW_TRACK = "com.fesskiev.player.EXTRA_IS_NEW_TRACK";

    private AudioPlayer audioPlayer;
    private AudioControlView controlView;
    private DescriptionCardView cardDescription;
    private MuteSoloButton muteSoloButton;
    private RepeatButton repeatButton;
    private ImageView backdrop;
    private TextView trackTimeCount;
    private TextView trackTimeTotal;
    private TextView artist;
    private TextView volumeLevel;
    private TextView title;
    private TextView genre;
    private TextView album;
    private TextView trackDescription;

    public static void startPlayerActivity(Activity activity, boolean isNewTrack, View coverView) {
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(activity, coverView,
                        activity.getString(R.string.shared_cover_name));
        activity.startActivity(new Intent(activity, AudioPlayerActivity.class).
                putExtra(AudioPlayerActivity.EXTRA_IS_NEW_TRACK, isNewTrack), options.toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        if (savedInstanceState == null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }


        audioPlayer = MediaApplication.getInstance().getAudioPlayer();

        backdrop = (ImageView) findViewById(R.id.backdrop);
        muteSoloButton = (MuteSoloButton) findViewById(R.id.muteSoloButton);
        trackTimeTotal = (TextView) findViewById(R.id.trackTimeTotal);
        trackTimeCount = (TextView) findViewById(R.id.trackTimeCount);
        artist = (TextView) findViewById(R.id.trackArtist);
        title = (TextView) findViewById(R.id.trackTitle);
        trackDescription = (TextView) findViewById(R.id.trackDescription);
        genre = (TextView) findViewById(R.id.genre);
        album = (TextView) findViewById(R.id.album);
        volumeLevel = (TextView) findViewById(R.id.volumeLevel);

        findViewById(R.id.previousTrack).setOnClickListener(v -> previous());
        findViewById(R.id.nextTrack).setOnClickListener(v -> next());

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

        muteSoloButton.setOnMuteSoloListener(mute -> {
            audioPlayer.mute = mute;

            if (mute) {
                disableChangeVolume();
            } else {
                enableChangeVolume();
            }

            PlaybackService.changeMuteSoloState(getApplicationContext(), mute);
        });

        repeatButton = (RepeatButton) findViewById(R.id.repeatButton);
        repeatButton.setOnRepeatStateChangedListener(repeat -> {
            audioPlayer.repeat = repeat;
            PlaybackService.changeRepeatState(getApplicationContext(), repeat);
        });

        final DisabledScrollView scrollView = (DisabledScrollView) findViewById(R.id.scrollView);

        controlView = (AudioControlView) findViewById(R.id.audioControl);
        controlView.setOnAudioControlListener(new AudioControlView.OnAudioControlListener() {
            @Override
            public void onPlayStateChanged() {
                if (audioPlayer.isPlaying) {
                    pause();
                } else {
                    play();
                }
            }

            @Override
            public void onVolumeStateChanged(int volume, boolean change) {
                if (change) {
                    audioPlayer.volume = volume;
                    setVolumeLevel();
                }
                scrollView.setEnableScrolling(!change);
            }

            @Override
            public void onSeekStateChanged(int seek, boolean change) {
                if (change) {
                    PlaybackService.seekPlayback(getApplicationContext(), seek);
                }
                scrollView.setEnableScrolling(!change);
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_IS_NEW_TRACK)) {
            boolean isNewTrack = intent.getBooleanExtra(EXTRA_IS_NEW_TRACK, false);
            if (isNewTrack) {
                createPlayer();
                play();
            } else {
                if (!audioPlayer.isPlaying) {
                    setPauseValues();
                    controlView.setPlay(audioPlayer.isPlaying);
                }
            }
        }

        setAudioTrackValues();
        registerPlaybackBroadcastReceiver();
    }

    protected void setAudioTrackValues() {
        if (audioPlayer.currentAudioFile != null) {
            setTrackInformation();
            setVolumeLevel();
            setBackdropImage();
            setRepeat();
            setMuteSolo();
        }
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }


    private void setPauseValues() {
        controlView.setSeekValue(audioPlayer.progressScale);
        trackTimeTotal.setText(Utils.getTimeFromMillisecondsString(audioPlayer.duration));
        trackTimeCount.setText(Utils.getTimeFromMillisecondsString(audioPlayer.progress));
    }


    private void setBackdropImage() {
        BitmapHelper.loadAudioPlayerArtwork(this, audioPlayer, backdrop);
    }

    @Override
    public void play() {
        PlaybackService.startPlayback(getApplicationContext());
    }

    @Override
    public void pause() {
        PlaybackService.stopPlayback(getApplicationContext());
    }

    @Override
    public void next() {
        if (!audioPlayer.repeat) {
            audioPlayer.next();
            cardDescription.next();
            reset();
            createPlayer();
        }
    }

    @Override
    public void previous() {
        if (!audioPlayer.repeat) {
            audioPlayer.previous();
            cardDescription.previous();
            reset();
            createPlayer();
        }
    }

    private void reset() {
        resetIndicators();
        setBackdropImage();
    }


    @Override
    public void createPlayer() {
        PlaybackService.createPlayer(getApplicationContext(),
                audioPlayer.currentAudioFile.getFilePath());
    }

    private void setMuteSolo() {
        muteSoloButton.changeState(audioPlayer.mute);
        PlaybackService.changeMuteSoloState(getApplicationContext(), audioPlayer.mute);
    }

    private void setRepeat() {
        repeatButton.changeState(audioPlayer.repeat);
        PlaybackService.changeRepeatState(getApplicationContext(), audioPlayer.repeat);
    }

    private void setVolumeLevel() {
        int volume = audioPlayer.volume;
        volumeLevel.setText(String.valueOf(volume));
        controlView.setVolumeValue(volume);
        PlaybackService.volumePlayback(getApplicationContext(), volume);
        if (volume >= 60) {
            muteSoloButton.setHighSoloState();
        } else if (volume >= 30) {
            muteSoloButton.setMediumSoloState();
        } else {
            muteSoloButton.setLowSoloState();
        }
    }

    private void resetIndicators() {
        trackTimeTotal.setText(getString(R.string.timer_zero));
        trackTimeCount.setText(getString(R.string.timer_zero));
        controlView.setSeekValue(0);
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

    private void disableChangeVolume() {
        controlView.setEnableChangeVolume(false);
    }

    private void enableChangeVolume() {
        controlView.setEnableChangeVolume(true);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterPlaybackBroadcastReceiver();
    }


    protected void registerPlaybackBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlaybackService.ACTION_PLAYBACK_VALUES);
        filter.addAction(PlaybackService.ACTION_PLAYBACK_PLAYING_STATE);
        filter.addAction(AudioPlayer.ACTION_CHANGE_CURRENT_AUDIO_FILE);
        filter.addAction(PlaybackService.ACTION_SONG_END);
        LocalBroadcastManager.getInstance(this).registerReceiver(playbackReceiver, filter);
    }

    protected void unregisterPlaybackBroadcastReceiver() {
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

                    audioPlayer.duration = duration;
                    audioPlayer.progress = progress;
                    audioPlayer.progressScale = progressScale;

                    controlView.setSeekValue(progressScale);
                    trackTimeTotal.setText(Utils.getTimeFromMillisecondsString(duration));
                    trackTimeCount.setText(Utils.getTimeFromMillisecondsString(progress));
                    break;
                case PlaybackService.ACTION_PLAYBACK_PLAYING_STATE:
                    audioPlayer.isPlaying = intent.getBooleanExtra(PlaybackService.PLAYBACK_EXTRA_PLAYING, false);
                    controlView.setPlay(audioPlayer.isPlaying);
                    break;
                case PlaybackService.ACTION_SONG_END:
                    cardDescription.next();
                    reset();
                case AudioPlayer.ACTION_CHANGE_CURRENT_AUDIO_FILE:
                    cardDescription.next();
                    reset();
                    break;
            }
        }
    };
}
