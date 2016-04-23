package com.fesskiev.player.widgets.dialogs;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fesskiev.player.R;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.utils.AppSettingsManager;

public class BassBoostDialog extends AlertDialog implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = BassBoostDialog.class.getSimpleName();

    public static void getInstance(Context context) {
        BassBoostDialog dialog = new BassBoostDialog(context);
        dialog.show();
    }

    private AppSettingsManager settingsManager;
    private View bassBoostControl;
    private TextView bassBoostLevel;
    private TextView notSupportedText;
    private  SeekBar bassBoostBar;

    protected BassBoostDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_bass_boost);

        settingsManager = AppSettingsManager.getInstance(getContext());

        bassBoostControl = findViewById(R.id.bassBoostControlContainer);

        bassBoostBar = (SeekBar) findViewById(R.id.bassBoostSeek);
        bassBoostBar.setOnSeekBarChangeListener(this);

        bassBoostLevel = (TextView) findViewById(R.id.bassBoostLevel);
        notSupportedText = (TextView) findViewById(R.id.bassBoostNotSupportedText);

        findViewById(R.id.closeContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }

    private void getBassBoostValue(){
        if(settingsManager != null){
            int value = settingsManager.getBassBoostValue();
            if(value != -1){
                bassBoostBar.setProgress(value);
                bassBoostLevel.setText(String.valueOf(value));
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerBroadcastReceiver();
        PlaybackService.checkBassBoost(getContext());
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlaybackService.ACTION_PLAYBACK_BASS_BOOST_STATE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(bassBoostReceiver,
                filter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(bassBoostReceiver);
    }


    private void hideControl() {
        bassBoostControl.setVisibility(View.GONE);
    }

    private void showNotSupportedText(){
        notSupportedText.setVisibility(View.VISIBLE);
    }


    private BroadcastReceiver bassBoostReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {
            switch (intent.getAction()) {
                case PlaybackService.ACTION_PLAYBACK_BASS_BOOST_STATE:
                    int bassBoostState =
                            intent.getIntExtra(PlaybackService.PLAYBACK_EXTRA_BASS_BOOST_STATE, -1);
                    if (bassBoostState != -1) {
                        switch (bassBoostState) {
                            case PlaybackService.BASS_BOOST_SUPPORT:
                                getBassBoostValue();
                                break;
                            case PlaybackService.BASS_BOOST_NOT_SUPPORT:
                                hideControl();
                                showNotSupportedText();
                                break;
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        bassBoostLevel.setText(String.valueOf(progress));
        settingsManager.setBassBoostValue(progress);

        PlaybackService.changeBassBoostLevel(getContext(), progress);

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
