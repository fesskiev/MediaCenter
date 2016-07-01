package com.fesskiev.player.widgets.eq;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fesskiev.player.R;

public class EQBandView extends FrameLayout {

    private static final int PROGRESS_DELAY = 300;

    public interface OnEQBandChangeListener {
        void onBandValueChanged(double value, int band);
    }

    private OnEQBandChangeListener listener;
    private VerticalSeekBar seekBar;
    private TextView frequencyText;
    private double scaleProgress;
    private int bandNumber;
    private int scale;


    public EQBandView(Context context) {
        super(context);
        init(context);
    }

    public EQBandView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EQBandView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.band_view, this, true);

        frequencyText = (TextView) findViewById(R.id.bandFrequency);

        seekBar = (VerticalSeekBar) view.findViewById(R.id.bandLevel);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.progress = progress;
                scaleProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });

        scale = 1500 / 50;
    }


    private void scaleProgress(int progress) {
        if (progress > 50) {
            scaleProgress = (progress - 50) * scale;
        } else {
            scaleProgress = (progress - 50) * scale;
        }

        if (listener != null && seekBar.isTouch()) {
            listener.onBandValueChanged(scaleProgress, bandNumber);
        }
    }

    public void setBandLevel(int level) {
        if (level == 0) {
            setCenterProgress();
            return;
        }

        int scaleProgress = 50 + (level / scale);
        setProgress(scaleProgress);
    }

    public void setCenterProgress() {
        setProgress(50);
    }

    public void setOnEQBandChangeListener(OnEQBandChangeListener l) {
        this.listener = l;
    }

    public void setFrequencyText(String text) {
        frequencyText.setText(text);
    }

    private void setProgress(final int progress) {
        seekBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(progress);
            }
        }, PROGRESS_DELAY);
    }

    public void setBandNumber(int bandNumber) {
        this.bandNumber = bandNumber;
    }

    public double getProgress() {
        return scaleProgress;
    }

    public void enable(boolean enable){
        seekBar.enable(enable);
    }
}
