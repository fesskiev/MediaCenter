package com.fesskiev.player.widgets.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fesskiev.player.R;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.utils.AppSettingsManager;

public class BassBoostDialog extends AlertDialog implements SeekBar.OnSeekBarChangeListener {

    public static void getInstance(Context context) {
        BassBoostDialog dialog = new BassBoostDialog(context);
        dialog.show();
    }

    private AppSettingsManager settingsManager;
    private TextView bassBoostLevel;
    private SeekBar bassBoostBar;

    protected BassBoostDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_bass_boost);

        settingsManager = AppSettingsManager.getInstance(getContext());

        bassBoostBar = (SeekBar) findViewById(R.id.bassBoostSeek);
        bassBoostBar.setOnSeekBarChangeListener(this);

        bassBoostLevel = (TextView) findViewById(R.id.bassBoostLevel);

        findViewById(R.id.closeContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        getBassBoostValue();
    }

    private void getBassBoostValue() {
        if (settingsManager != null) {
            int value = settingsManager.getBassBoostValue();
            if (value != -1) {
                bassBoostBar.setProgress(value / 10);
                bassBoostLevel.setText(String.valueOf(value / 10));
            } else {
                bassBoostBar.setProgress(0);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int scaleValue = progress * 10;
        bassBoostLevel.setText(String.valueOf(progress));
        PlaybackService.changeBassBoostLevel(getContext(), scaleValue);

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        settingsManager.setBassBoostValue(seekBar.getProgress() * 10);

    }


}
