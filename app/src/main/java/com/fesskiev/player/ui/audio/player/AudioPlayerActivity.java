package com.fesskiev.player.ui.audio.player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.analytics.AnalyticsActivity;
import com.fesskiev.player.data.model.AudioFile;
import com.fesskiev.player.players.AudioPlayer;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.buttons.MuteSoloButton;
import com.fesskiev.player.widgets.buttons.RepeatButton;
import com.fesskiev.player.widgets.cards.DescriptionCardView;
import com.fesskiev.player.widgets.controls.AudioControlView;
import com.fesskiev.player.widgets.utils.DisabledScrollView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class AudioPlayerActivity extends AnalyticsActivity {

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

    private boolean lastPlaying;
    private boolean lastLooping;
    private int lastPositionSeconds = -1;
    private int lastDurationSeconds = -1;
    private float lastVolume = -1f;

    public static void startPlayerActivity(Activity activity) {
        activity.startActivity(new Intent(activity, AudioPlayerActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);


        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);

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

            }
        });

        muteSoloButton.setOnMuteSoloListener(mute -> {
            if (mute) {
                lastVolume = 0;
                disableChangeVolume();
            } else {
                enableChangeVolume();
            }
            setVolumeLevel(lastVolume);
            PlaybackService.volumePlayback(getApplicationContext(), 0);
        });

        repeatButton = (RepeatButton) findViewById(R.id.repeatButton);
        repeatButton.setOnRepeatStateChangedListener(repeat ->
                PlaybackService.changeLoopingState(getApplicationContext(), repeat));

        final DisabledScrollView scrollView = (DisabledScrollView) findViewById(R.id.scrollView);

        controlView = (AudioControlView) findViewById(R.id.audioControl);
        controlView.setOnAudioControlListener(new AudioControlView.OnAudioControlListener() {
            @Override
            public void onPlayStateChanged() {
                if (lastPlaying) {
                    pause();
                } else {
                    play();
                }
                lastPlaying = !lastPlaying;
                controlView.setPlay(lastPlaying);
            }

            @Override
            public void onVolumeStateChanged(int volume, boolean change) {
                if (change) {
                    PlaybackService.volumePlayback(getApplicationContext(), volume);
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


        setAudioTrackValues(audioPlayer.getCurrentTrack());

        EventBus.getDefault().register(this);

        controlView.setPlay(false);
        PlaybackService.requestPlaybackStateIfNeed(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCurrentTrackEvent(AudioFile currentTrack) {
        Log.e("test", "PLAYER onCurrentTrackEvent: " + currentTrack.toString());

        setAudioTrackValues(currentTrack);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlaybackStateEvent(PlaybackService playbackState) {
        boolean playing = playbackState.isPlaying();
        if (lastPlaying != playing) {
            lastPlaying = playing;
            controlView.setPlay(playing);
        }

        int positionSeconds = playbackState.getPosition();

        if (lastPositionSeconds != positionSeconds) {
            lastPositionSeconds = positionSeconds;
            controlView.setSeekValue((int) playbackState.getPositionPercent());
            trackTimeCount.setText(Utils.getPositionSecondsString(lastPositionSeconds));
        }

        int durationSeconds = playbackState.getDuration();

        if (lastDurationSeconds != durationSeconds) {
            lastDurationSeconds = durationSeconds;
            trackTimeTotal.setText(Utils.getPositionSecondsString(lastDurationSeconds));
        }

        boolean looping = playbackState.isLooping();
        if (lastLooping != looping) {
            lastLooping = looping;
            repeatButton.changeState(lastLooping);
        }

        float volume = playbackState.getVolume();
        if (lastVolume != volume) {
            lastVolume = volume;
            setVolumeLevel(lastVolume);
        }
    }


    protected void setAudioTrackValues(AudioFile audioFile) {
        if (audioFile != null) {
            setTrackInformation(audioFile);
            setBackdropImage(audioFile);
        }
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }


    private void setBackdropImage(AudioFile audioFile) {
        audioPlayer.getCurrentAudioFolder()
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioFolder -> {
                    BitmapHelper.getInstance().loadAudioPlayerArtwork(audioFolder, audioFile, backdrop);
                });
    }


    public void play() {
        audioPlayer.play();
    }


    public void pause() {
        audioPlayer.pause();
    }


    public void next() {
        if (audioPlayer.last()) {
            Utils.showCustomSnackbar(findViewById(R.id.audioPlayerRoot),
                    getApplicationContext(),
                    getString(R.string.snackbar_last_track),
                    Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }
        if (!lastLooping) {
            audioPlayer.next();
            cardDescription.next();
            resetIndicators();

        }
    }

    public void previous() {
        if (audioPlayer.first()) {
            Utils.showCustomSnackbar(findViewById(R.id.audioPlayerRoot),
                    getApplicationContext(),
                    getString(R.string.snackbar_first_track),
                    Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }
        if (!lastLooping) {
            audioPlayer.previous();
            cardDescription.previous();
            resetIndicators();
        }
    }

    private void setVolumeLevel(float volume) {
        volumeLevel.setText(String.format("%.0f", volume));
        controlView.setVolumeValue(volume);
        if (volume >= 60) {
            muteSoloButton.setHighSoloState();
        } else if (volume >= 30) {
            muteSoloButton.setMediumSoloState();
        } else {
            muteSoloButton.setLowSoloState();
        }
    }

    private void resetIndicators() {
        trackTimeTotal.setText(getString(R.string.infinity_symbol));
        trackTimeCount.setText(getString(R.string.infinity_symbol));
        controlView.setSeekValue(0);
    }


    private void setTrackInformation(AudioFile audioFile) {
        artist.setText(audioFile.artist);
        title.setText(audioFile.title);
        album.setText(audioFile.album);
        genre.setText(audioFile.genre);

        StringBuilder sb = new StringBuilder();
        sb.append(audioFile.sampleRate);
        sb.append("::");
        sb.append(audioFile.bitrate);
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
}
