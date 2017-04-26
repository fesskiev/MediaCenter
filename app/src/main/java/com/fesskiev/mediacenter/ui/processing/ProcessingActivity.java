package com.fesskiev.mediacenter.ui.processing;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.converter.AudioConverterHelper;
import com.fesskiev.mediacenter.utils.converter.AudioFormat;
import com.fesskiev.mediacenter.widgets.MaterialProgressBar;

public class ProcessingActivity extends AnalyticsActivity implements RadioGroup.OnCheckedChangeListener {

    private AudioPlayer audioPlayer;

    private AudioFormat audioFormat;
    private AudioConverterHelper audioConverter;

    private RadioButton[] radioButtons;
    private MaterialProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.title_about_activity));
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        audioConverter = AudioConverterHelper.getInstance();

        progressBar = (MaterialProgressBar) findViewById(R.id.progressBar);
        ((RadioGroup) findViewById(R.id.radioGroupConvertFormat)).setOnCheckedChangeListener(this);

        radioButtons = new RadioButton[]{
                (RadioButton) findViewById(R.id.radioMP3),
                (RadioButton) findViewById(R.id.radioFLAC),
                (RadioButton) findViewById(R.id.radioM4A),
                (RadioButton) findViewById(R.id.radioWAV),
                (RadioButton) findViewById(R.id.radioAAC)
        };

        findViewById(R.id.convertFileFab).setOnClickListener(v -> startConvertFile());
    }

    private void startConvertFile() {
        if (audioFormat == null) {
            return;
        }
        audioConverter.convertAudio(audioPlayer.getCurrentTrack(), audioFormat,
                new AudioConverterHelper.OnConvertProcessListener() {
                    @Override
                    public void onStart() {
                        showProgressBar();
                    }

                    @Override
                    public void onSuccess(AudioFile audioFile) {
                        hideProgressBar();
                        showSuccessSnackbar();
                    }

                    @Override
                    public void onFailure(Exception error) {
                        error.printStackTrace();
                        hideProgressBar();
                        showErrorSnackbar(error);
                    }
                });
    }

    private void showErrorSnackbar(Exception error) {
        Utils.showCustomSnackbar(findViewById(R.id.processingRoot), getApplicationContext(),
                "Error convert: " + error.getMessage(), Snackbar.LENGTH_INDEFINITE).show();
    }

    private void showSuccessSnackbar() {
        Utils.showCustomSnackbar(findViewById(R.id.processingRoot), getApplicationContext(),
                "Convert Success!", Snackbar.LENGTH_INDEFINITE).show();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radioMP3:
                audioFormat = AudioFormat.MP3;
                break;
            case R.id.radioFLAC:
                audioFormat = AudioFormat.FLAC;
                break;
            case R.id.radioM4A:
                audioFormat = AudioFormat.M4A;
                break;
            case R.id.radioWAV:
                audioFormat = AudioFormat.WAV;
                break;
            case R.id.radioAAC:
                audioFormat = AudioFormat.AAC;
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (audioConverter.isCommandRunning()) {
            Utils.showCustomSnackbar(findViewById(R.id.processingRoot), getApplicationContext(),
                    "Command is running! Kill process", Snackbar.LENGTH_SHORT)
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onShown(Snackbar transientBottomBar) {
                            super.onShown(transientBottomBar);
                            hideProgressBar();
                            audioConverter.killRunningProcesses();
                        }

                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            finish();
                        }
                    }).show();
        } else {
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }


    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
}
