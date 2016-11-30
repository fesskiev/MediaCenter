package com.fesskiev.player.ui.audio.player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.analytics.AnalyticsActivity;
import com.fesskiev.player.data.model.AudioFile;
import com.fesskiev.player.data.model.PlaybackState;
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



public class AudioPlayerActivity extends AnalyticsActivity {

    public static final String EXTRA_IS_NEW_TRACK = "com.fesskiev.player.EXTRA_IS_NEW_TRACK";

    private PlaybackState playbackState;
    private AudioPlayer audioPlayer;

    private AudioControlView controlView;
    private DescriptionCardView cardDescription;
    private MuteSoloButton muteSoloButton;
    private RepeatButton repeatButton;
    private AppBarLayout appBarLayout;
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
        playbackState = PlaybackService.getPlaybackState();

        appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
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
            //TODO change mute/solo logic!?
            if (mute) {
                disableChangeVolume();
            } else {
                enableChangeVolume();
            }

            PlaybackService.changeMuteSoloState(getApplicationContext(), mute);
        });

        repeatButton = (RepeatButton) findViewById(R.id.repeatButton);
        repeatButton.setOnRepeatStateChangedListener(repeat -> {
            PlaybackService.changeRepeatState(getApplicationContext(), repeat);
        });

        final DisabledScrollView scrollView = (DisabledScrollView) findViewById(R.id.scrollView);

        controlView = (AudioControlView) findViewById(R.id.audioControl);
        controlView.setOnAudioControlListener(new AudioControlView.OnAudioControlListener() {
            @Override
            public void onPlayStateChanged() {
                if (playbackState.isPlaying()) {
                    pause();
                } else {
                    play();
                }
            }

            @Override
            public void onVolumeStateChanged(int volume, boolean change) {
                if (change) {
                    setVolumeLevel(volume);
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



        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_IS_NEW_TRACK)) {
            boolean isNewTrack = intent.getBooleanExtra(EXTRA_IS_NEW_TRACK, false);
            if (isNewTrack) {
                open();
            } else {
                setPauseValues();
                setAudioTrackValues(audioPlayer.getCurrentTrack());
                controlView.setPlay(playbackState.isPlaying());
            }
        }

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayingEvent(Boolean playing) {
        controlView.setPlay(playing);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioPlayerEvent(AudioPlayer audioPlayer) {
        Log.d("test", "onMessageEvent: " + audioPlayer.toString());
        setAudioTrackValues(audioPlayer.getCurrentTrack());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlaybackStateEvent(PlaybackState playbackState) {

        controlView.setSeekValue(playbackState.getProgressScale());
        trackTimeTotal.setText(Utils.getTimeFromMillisecondsString(playbackState.getDuration()));
        trackTimeCount.setText(Utils.getTimeFromMillisecondsString(playbackState.getProgress()));
    }

    protected void setAudioTrackValues(AudioFile audioFile) {
        if (audioFile != null) {
            setTrackInformation(audioFile);
            setVolumeLevel(playbackState.getVolume());
            setBackdropImage(audioFile);
            setRepeat();
            setMuteSolo();
        }
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }


    private void setPauseValues() {
        controlView.setSeekValue(playbackState.getProgressScale());
        trackTimeTotal.setText(Utils.getTimeFromMillisecondsString(playbackState.getDuration()));
        trackTimeCount.setText(Utils.getTimeFromMillisecondsString(playbackState.getProgress()));
    }


    private void setBackdropImage(AudioFile audioFile) {
        boolean load = BitmapHelper.getInstance().loadAudioPlayerArtwork(audioFile, backdrop);

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        if (!load) {
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                lp.height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                appBarLayout.setExpanded(false);
                backdrop.setVisibility(View.GONE);
            }
        } else {
            lp.height = getResources().getDimensionPixelSize(R.dimen.app_bar_height);
            appBarLayout.setExpanded(true);
            backdrop.setVisibility(View.VISIBLE);
        }
    }


    public void play() {
        audioPlayer.play();
    }


    public void pause() {
        audioPlayer.pause();
    }


    public void next() {
        if (!playbackState.isRepeat()) {
            audioPlayer.next();
            cardDescription.next();
            resetIndicators();

        }
    }

    public void previous() {
        if (!playbackState.isRepeat()) {
            audioPlayer.previous();
            cardDescription.previous();
            resetIndicators();
        }
    }

    public void open() {
        audioPlayer.open(null);
    }

    private void setMuteSolo() {
        muteSoloButton.changeState(playbackState.isMute());
        PlaybackService.changeMuteSoloState(getApplicationContext(), playbackState.isMute());
    }

    private void setRepeat() {
        repeatButton.changeState(playbackState.isRepeat());
        PlaybackService.changeRepeatState(getApplicationContext(), playbackState.isRepeat());
    }

    private void setVolumeLevel(int volume) {
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
