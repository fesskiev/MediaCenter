package com.fesskiev.player.widgets.dialogs.effects;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fesskiev.player.R;


public abstract class EffectDialog extends AlertDialog implements SeekBar.OnSeekBarChangeListener {

    private static final int SCALE = 10;

    public abstract void getEffectValue(int value);

    public abstract void getSaveEffectValue(int value);

    private ImageView effectIcon;
    private TextView effectNameText;
    private TextView levelText;
    private SeekBar seekBar;

    protected EffectDialog(Context context) {
        super(context, R.style.Custom_Widget_AlertDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_effest);

        seekBar = (SeekBar) findViewById(R.id.bassBoostSeek);
        seekBar.setOnSeekBarChangeListener(this);

        levelText = (TextView) findViewById(R.id.bassBoostLevel);
        effectNameText = (TextView) findViewById(R.id.effectName);

        effectIcon = (ImageView) findViewById(R.id.effectIcon);

        findViewById(R.id.closeContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        getEffectValue(progress * SCALE);
        levelText.setText(String.valueOf(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        getSaveEffectValue(seekBar.getProgress() * SCALE);
    }

    public void setEffectIcon(int resource) {
        effectIcon.setImageResource(resource);
    }

    public void setEffectName(String text) {
        effectNameText.setText(text);
    }

    public void setProgress(int progress) {
        seekBar.setProgress(progress / SCALE);
    }

    public void setProgressText(int progress){
        levelText.setText(String.valueOf(progress / SCALE));
    }
}
