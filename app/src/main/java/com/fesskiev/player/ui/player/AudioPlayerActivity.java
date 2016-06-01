package com.fesskiev.player.ui.player;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.analytics.AnalyticsActivity;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.buttons.MuteSoloButton;
import com.fesskiev.player.widgets.buttons.PlayPauseFloatingButton;
import com.fesskiev.player.widgets.buttons.RepeatButton;
import com.fesskiev.player.widgets.cards.DescriptionCardView;

public class AudioPlayerActivity extends AnalyticsActivity implements Playable {

    private static final String TAG = AudioPlayerActivity.class.getSimpleName();
    public static final String EXTRA_IS_NEW_TRACK = "com.fesskiev.player.EXTRA_IS_NEW_TRACK";

    private AudioPlayer audioPlayer;
    private PlayPauseFloatingButton playPauseButton;
    private DescriptionCardView cardDescription;
    private MuteSoloButton muteSoloButton;
    private RepeatButton repeatButton;
    private ImageView backdrop;
    private TextView trackTimeCount;
    private TextView trackTimeTotal;
    private TextView artist;
    private TextView title;
    private TextView genre;
    private TextView album;
    private TextView trackDescription;
    private SeekBar trackSeek;
    private SeekBar volumeSeek;
    private View holder;
    private float fabTranslateX;
    private float fabTranslateY;

    public static void startPlayerActivity(Activity activity, boolean isNewTrack, View coverView) {
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(activity, coverView, "cover");
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
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        hideWithAnimation();
                    }
                });
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

        holder = findViewById(R.id.holderButton);

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

        muteSoloButton.setOnMuteSoloListener(new MuteSoloButton.OnMuteSoloListener() {
            @Override
            public void onMuteStateChanged(boolean mute) {
                audioPlayer.mute = mute;

                if (mute) {
                    disableChangeVolume();
                } else {
                    enableChangeVolume();
                }

                PlaybackService.changeMuteSoloState(getApplicationContext(), mute);
            }
        });

        repeatButton = (RepeatButton) findViewById(R.id.repeatButton);
        repeatButton.setOnRepeatStateChangedListener(new RepeatButton.OnRepeatStateChangedListener() {
            @Override
            public void onRepeatStateChanged(boolean repeat) {
                audioPlayer.repeat = repeat;
                PlaybackService.changeRepeatState(getApplicationContext(), repeat);
            }
        });

        playPauseButton =
                (PlayPauseFloatingButton) findViewById(R.id.playPauseFAB);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
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
                PlaybackService.seekPlayback(getApplicationContext(), progress);
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
            createPlayer();
            play();
        } else {
            if (!audioPlayer.isPlaying) {
                setPauseValues();
                playPauseButton.setPlay(audioPlayer.isPlaying);
            } else {
                translateFAB();
            }
        }

        setTrackInformation();
        setVolumeLevel();
        setBackdropImage();
        setRepeat();
        setMuteSolo();

        registerPlaybackBroadcastReceiver();
    }

    private void hideWithAnimation() {
        playPauseButton.hide(new FloatingActionButton.OnVisibilityChangedListener() {
            @Override
            public void onHidden(FloatingActionButton fab) {
                supportFinishAfterTransition();
            }
        });
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }


    private void setPauseValues() {
        trackSeek.setProgress(audioPlayer.progressScale);
        trackTimeTotal.setText(Utils.getTimeFromMillisecondsString(audioPlayer.duration));
        trackTimeCount.setText(Utils.getTimeFromMillisecondsString(audioPlayer.progress));
    }

    private void translateFAB() {
        if (fabTranslateX == 0 && fabTranslateY == 0) {
            holder.getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                        @Override
                        public void onGlobalLayout() {

                            int location[] = new int[2];
                            holder.getLocationOnScreen(location);
                            int viewX = location[0];
                            int viewY = location[1];

                            fabTranslateX = -(viewX / 2) - holder.getWidth();
                            fabTranslateY = (viewY / 2);

                            Log.d(TAG, "center X: " + fabTranslateX + " center Y: " + fabTranslateY);

                            playPauseButton.translateToPosition(fabTranslateX, fabTranslateY);

                            holder.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
        } else {
            playPauseButton.translateToPosition(fabTranslateX, fabTranslateY);
        }
    }

    private void setBackdropImage() {
        BitmapHelper.loadAudioPlayerArtwork(this, audioPlayer, backdrop);
    }

    @Override
    public void play() {
        PlaybackService.startPlayback(getApplicationContext());
        translateFAB();
    }

    @Override
    public void pause() {
        PlaybackService.stopPlayback(getApplicationContext());
        playPauseButton.returnFromPosition();
    }

    @Override
    public void next() {
        if (!audioPlayer.repeat) {
            audioPlayer.next();
            cardDescription.next();
            reset();
        }
    }

    @Override
    public void previous() {
        if (!audioPlayer.repeat) {
            audioPlayer.previous();
            cardDescription.previous();
            reset();
        }
    }

    private void reset() {
        resetIndicators();
        createPlayer();
        setBackdropImage();
    }


    @Override
    public void createPlayer() {
        PlaybackService.createPlayer(getApplicationContext(),
                audioPlayer.currentAudioFile.filePath.getAbsolutePath());
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
        volumeSeek.setProgress(audioPlayer.volume);
        PlaybackService.volumePlayback(getApplicationContext(), audioPlayer.volume);
        if (audioPlayer.volume <= 45) {
            muteSoloButton.setLowSoloState();
        } else {
            muteSoloButton.setHighSoloState();
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

    private void disableChangeVolume() {
        volumeSeek.setEnabled(false);
    }

    private void enableChangeVolume() {
        volumeSeek.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        hideWithAnimation();
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

                    audioPlayer.duration = duration;
                    audioPlayer.progress = progress;
                    audioPlayer.progressScale = progressScale;

                    trackSeek.setProgress(progressScale);
                    trackTimeTotal.setText(Utils.getTimeFromMillisecondsString(duration));
                    trackTimeCount.setText(Utils.getTimeFromMillisecondsString(progress));
                    break;
                case PlaybackService.ACTION_PLAYBACK_PLAYING_STATE:
                    audioPlayer.isPlaying = intent.getBooleanExtra(PlaybackService.PLAYBACK_EXTRA_PLAYING, false);
                    playPauseButton.setPlay(audioPlayer.isPlaying);
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
