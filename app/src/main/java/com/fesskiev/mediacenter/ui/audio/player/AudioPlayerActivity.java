package com.fesskiev.mediacenter.ui.audio.player;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.ui.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.ui.audio.tracklist.PlayerTrackListActivity;
import com.fesskiev.mediacenter.ui.cue.CueActivity;
import com.fesskiev.mediacenter.ui.cut.CutMediaActivity;
import com.fesskiev.mediacenter.ui.effects.EffectsActivity;
import com.fesskiev.mediacenter.utils.AppGuide;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.ffmpeg.FFmpegHelper;
import com.fesskiev.mediacenter.widgets.buttons.MuteSoloButton;
import com.fesskiev.mediacenter.widgets.buttons.RepeatButton;
import com.fesskiev.mediacenter.widgets.cards.DescriptionCardView;
import com.fesskiev.mediacenter.widgets.controls.AudioControlView;
import com.fesskiev.mediacenter.widgets.dialogs.LoopingDialog;
import com.fesskiev.mediacenter.widgets.utils.DisabledScrollView;

public class AudioPlayerActivity extends AnalyticsActivity {

    private AudioPlayerViewModel viewModel;

    private AppGuide appGuide;

    private AudioControlView controlView;
    private DescriptionCardView cardDescription;
    private MuteSoloButton muteSoloButton;
    private RepeatButton repeatButton;
    private TextView trackTimeCount;
    private TextView trackTimeTotal;
    private TextView artist;
    private TextView volumeLevel;
    private TextView title;
    private TextView genre;
    private TextView album;
    private TextView trackDescription;
    private ImageView prevTrack;
    private ImageView nextTrack;
    private ImageView backdrop;
    private ImageView equalizer;
    private ImageView trackList;
    private ImageView timer;


    public static void startPlayerActivity(Activity activity) {
        activity.startActivity(new Intent(activity, AudioPlayerActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        timer = findViewById(R.id.timerImage);
        backdrop = findViewById(R.id.backdrop);
        muteSoloButton = findViewById(R.id.muteSoloButton);
        trackTimeTotal = findViewById(R.id.trackTimeTotal);
        trackTimeCount = findViewById(R.id.trackTimeCount);
        artist = findViewById(R.id.trackArtist);
        title = findViewById(R.id.trackTitle);
        trackDescription = findViewById(R.id.trackDescription);
        genre = findViewById(R.id.genre);
        album = findViewById(R.id.album);
        volumeLevel = findViewById(R.id.volumeLevel);

        prevTrack = findViewById(R.id.previousTrack);
        prevTrack.setOnClickListener(v -> {
            ((Animatable) prevTrack.getDrawable()).start();
            viewModel.previous();
        });

        nextTrack = findViewById(R.id.nextTrack);
        nextTrack.setOnClickListener(v -> {
            ((Animatable) nextTrack.getDrawable()).start();
            viewModel.next();
        });

        equalizer = findViewById(R.id.equalizer);
        equalizer.setOnClickListener(v -> startEqualizerActivity());
        trackList = findViewById(R.id.trackList);
        trackList.setOnClickListener(v -> openTrackList());

        cardDescription = findViewById(R.id.cardDescription);

        muteSoloButton.setOnMuteSoloListener(mute -> {
            if (mute) {
                disableChangeVolume();
            } else {
                enableChangeVolume();
            }
            viewModel.volumeStateChanged(0);
        });

        repeatButton = findViewById(R.id.repeatButton);
        repeatButton.setOnRepeatStateChangedListener(new RepeatButton.OnRepeatStateChangedListener() {
            @Override
            public void onRepeatStateChanged(boolean repeat) {
                viewModel.changeLoopingState(repeat);
            }

            @Override
            public void onLoopingBetweenClick() {
                makeLoopingDialog();
            }
        });

        final DisabledScrollView scrollView = findViewById(R.id.scrollView);

        controlView = findViewById(R.id.audioControl);
        controlView.setOnAudioControlListener(new AudioControlView.OnAudioControlListener() {
            @Override
            public void onPlayStateChanged() {
                viewModel.playStateChanged();
            }

            @Override
            public void onVolumeStateChanged(int volume, boolean change) {
                if (change) {
                    viewModel.volumeStateChanged(volume);
                }
                scrollView.setEnableScrolling(!change);
            }

            @Override
            public void onSeekStateChanged(int seek, boolean change) {
                if (change) {
                    viewModel.seekPlayback(seek);
                }
                scrollView.setEnableScrolling(!change);
            }
        });
        observeData();
    }

    private void observeData() {
        viewModel = ViewModelProviders.of(this).get(AudioPlayerViewModel.class);

        viewModel.getCurrentTrackLiveData().observe(this, this::setCurrentAudioFileView);
        viewModel.getPositionLiveData().observe(this, this::setPositionView);
        viewModel.getPositionPercentLiveData().observe(this, this::setPositionPercentView);
        viewModel.getPlayingLiveData().observe(this, this::setPlayingView);
        viewModel.getLoopingLiveData().observe(this, this::setLoopingView);
        viewModel.getDurationLiveData().observe(this, this::setDurationView);
        viewModel.getVolumeLiveData().observe(this, this::setVolumeLevel);
        viewModel.getConvertingLiveData().observe(this, this::setConvertingView);
        viewModel.getLoadingSuccessLiveData().observe(this, this::setLoadingSuccessView);
        viewModel.getLoadingErrorLiveData().observe(this, this::setLoadingErrorView);
        viewModel.getFirstTrackLiveData().observe(this, this::setPreviousView);
        viewModel.getLastTrackLiveData().observe(this, this::setNextView);
        viewModel.getCoverLiveData().observe(this, this::setCoverView);
        viewModel.getPaletteLiveData().observe(this, this::setPaletteView);
        viewModel.getNextTrackLiveData().observe(this, aVoid -> setNextTrackView());
        viewModel.getPreviousTrackLiveData().observe(this, aVoid -> setPreviousTrackView());
    }

    @Override
    protected void onStart() {
        super.onStart();
        controlView.postDelayed(this::makeGuideIfNeed, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (appGuide != null) {
            appGuide.clear();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && viewModel.isFullScreenMode()) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean isProUser = viewModel.isUserPro();
        if (isProUser) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_player, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_cue:
                startCueActivity();
                return true;
            case R.id.menu_cut:
                startCutActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setPreviousTrackView() {
        cardDescription.previous();
        resetIndicators();
    }

    private void setNextTrackView() {
        cardDescription.next();
        resetIndicators();
    }

    private void setCoverView(Bitmap bitmap) {
        backdrop.setImageBitmap(bitmap);
    }

    private void setLoadingErrorView(boolean error) {
        if (error && !FFmpegHelper.isAudioFileFLAC(viewModel.getCurrentTrackLiveData().getValue())) {
            showErrorAndClose();
        }
    }

    private void setLoadingSuccessView(boolean success) {
        if (!success) {
            controlView.startLoading();
        } else {
            controlView.finishLoading();
        }
    }

    private void setCurrentAudioFileView(AudioFile audioFile) {
        controlView.startLoading();
        setTrackInformation(audioFile);
    }

    private void setDurationView(int duration) {
        trackTimeTotal.setText(Utils.getPositionSecondsString(duration));
    }

    private void setLoopingView(boolean looping) {
        repeatButton.changeState(looping);
    }

    private void setPlayingView(boolean playing) {
        controlView.setPlay(playing);
    }

    private void setPositionPercentView(float positionPercent) {
        controlView.setSeekValue((int) positionPercent);
    }

    private void setPositionView(int position) {
        trackTimeCount.setText(Utils.getPositionSecondsString(position));
    }

    private void setConvertingView(boolean converting) {
        controlView.startLoading();
    }

    private void setPreviousView(boolean first) {
        if (first) {
            disablePreviousTrackButton();
        } else {
            enablePreviousTrackButton();
        }
    }

    public void setNextView(boolean last) {
        if (last) {
            disableNextTrackButton();
        } else {
            enableNextTrackButton();
        }
    }

    private void makeGuideIfNeed() {
        if (viewModel.isNeedAudioPlayerActivityGuide()) {
            appGuide = new AppGuide(this, 4);
            appGuide.OnAppGuideListener(new AppGuide.OnAppGuideListener() {
                @Override
                public void next(int count) {
                    switch (count) {
                        case 1:
                            appGuide.makeGuide(repeatButton,
                                    getString(R.string.app_guide_looping_title),
                                    getString(R.string.app_guide_looping_desc));
                            break;
                        case 2:
                            appGuide.makeGuide(prevTrack,
                                    getString(R.string.app_guide_audio_prev_title),
                                    getString(R.string.app_guide_audio_prev_desc));
                            break;
                        case 3:
                            appGuide.makeGuide(nextTrack,
                                    getString(R.string.app_guide_audio_next_title),
                                    getString(R.string.app_guide_audio_next_desc));
                            break;
                    }
                }

                @Override
                public void watched() {
                    viewModel.setNeedAudioPlayerActivityGuide(false);
                }
            });
            appGuide.makeGuide(findViewById(R.id.trackList),
                    getString(R.string.app_guide_track_list_title),
                    getString(R.string.app_guide_track_list_desc));
        }
    }

    private void makeLoopingDialog() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        LoopingDialog dialog = LoopingDialog.newInstance(viewModel.getDurationLiveData().getValue());
        dialog.show(transaction, LoopingDialog.class.getName());
        dialog.setLoopingBetweenListener(this::setLoopBetween);
    }

    private void setLoopBetween(int start, int end) {
        viewModel.startLooping(start, end);
        repeatButton.setLoopBetweenTime(start, end);
    }

    private void openTrackList() {
        startActivity(new Intent(AudioPlayerActivity.this, PlayerTrackListActivity.class));
    }

    private void startEqualizerActivity() {
        startActivity(new Intent(AudioPlayerActivity.this, EffectsActivity.class));
    }

    private void startCueActivity() {
        startActivity(new Intent(AudioPlayerActivity.this, CueActivity.class));
    }

    private void startCutActivity() {
        CutMediaActivity.startCutMediaActivity(AudioPlayerActivity.this, CutMediaActivity.CUT_AUDIO);
    }

    public void enablePreviousTrackButton() {
        prevTrack.setAlpha(1f);
        prevTrack.setEnabled(true);
        prevTrack.setClickable(true);
    }

    public void disablePreviousTrackButton() {
        prevTrack.setAlpha(0.5f);
        prevTrack.setEnabled(false);
        prevTrack.setClickable(false);
    }

    public void enableNextTrackButton() {
        nextTrack.setAlpha(1f);
        nextTrack.setEnabled(true);
        nextTrack.setClickable(true);
    }

    public void disableNextTrackButton() {
        nextTrack.setAlpha(0.5f);
        nextTrack.setEnabled(false);
        nextTrack.setClickable(false);
    }

    private void showErrorAndClose() {
        Utils.showCustomSnackbar(findViewById(R.id.audioPlayerRoot), getApplicationContext(),
                getString(R.string.snackbar_loading_error),
                Snackbar.LENGTH_LONG).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                finish();
            }
        }).show();
    }

    private void setPaletteView(BitmapHelper.PaletteColor color) {
        int muted = color.getMuted();
        trackTimeCount.setTextColor(muted);
        trackTimeTotal.setTextColor(muted);
        artist.setTextColor(muted);
        volumeLevel.setTextColor(muted);
        title.setTextColor(muted);
        genre.setTextColor(muted);
        album.setTextColor(muted);
        trackDescription.setTextColor(muted);
        prevTrack.setColorFilter(muted);
        nextTrack.setColorFilter(muted);
        equalizer.setColorFilter(muted);
        trackList.setColorFilter(muted);
        muteSoloButton.setColorFilter(muted);
        repeatButton.setColorFilter(muted);
        timer.setColorFilter(muted);
        controlView.setColorFilter(muted, color.getVibrantDark());
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
        sb.append("::");
        sb.append(audioFile.getFilePath());

        /**
         *  http://stackoverflow.com/questions/3332924/textview-marquee-not-working?noredirect=1&lq=1
         */
        trackDescription.setSelected(true);
        trackDescription.setText(sb.toString());
    }

    private void disableChangeVolume() {
        controlView.setEnableChangeVolume(false);
    }

    private void enableChangeVolume() {
        controlView.setEnableChangeVolume(true);
    }

}
