package com.fesskiev.mediacenter.widgets.nav;

import android.content.Context;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.services.PlaybackService;

import java.util.Locale;


public class MediaNavigationView extends NavigationView implements View.OnClickListener {

    public interface OnEffectChangedListener {

        void onEffectClick();

        void onEQStateChanged(boolean enable);

        void onReverbStateChanged(boolean enable);

        void onWhooshStateChanged(boolean enable);

        void onEchoStateChanged(boolean enable);

        void onRecordStateChanged(boolean recording);
    }

    private OnEffectChangedListener listener;

    private SwitchCompat[] switchCompats;

    public MediaNavigationView(Context context) {
        super(context);
        init(context);
    }


    public MediaNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MediaNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.nav_effects_layout, this, true);

        view.findViewById(R.id.effectsSettingsContainer).setOnClickListener(v -> effectClick());
        view.findViewById(R.id.imageHeader).setOnClickListener(v -> effectClick());

        switchCompats = new SwitchCompat[]{
                view.findViewById(R.id.eqSwitch),
                view.findViewById(R.id.reverbSwitch),
                view.findViewById(R.id.echoSwitch),
                view.findViewById(R.id.whooshSwitch),
                view.findViewById(R.id.recordSwitch)

        };

        for (SwitchCompat switchCompat : switchCompats) {
            switchCompat.setOnClickListener(this);
        }

        TextView tempoValue = view.findViewById(R.id.tempoValue);
        SeekBar seekTempo = view.findViewById(R.id.seekTempo);
        seekTempo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                PlaybackService.setTempo(getContext().getApplicationContext(), progress);

                tempoValue.setText(String.format(Locale.ENGLISH, "%1$.2f X", (float) progress / 50));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekTempo.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            v.onTouchEvent(event);
            return true;
        });

        TextView pitchShiftValue = view.findViewById(R.id.pitchShiftValue);
        SeekBar seekPitchShift = view.findViewById(R.id.seekPitchShift);
        seekPitchShift.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                PlaybackService.setPitchShift(getContext().getApplicationContext(), progress);

                pitchShiftValue.setText(String.format(Locale.ENGLISH, "%1$.2f X", (float) progress / 50));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekPitchShift.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            v.onTouchEvent(event);
            return true;
        });
    }

    @Override
    public void onClick(View v) {
        boolean checked = ((SwitchCompat) v).isChecked();
        if (listener != null) {
            switch (v.getId()) {
                case R.id.eqSwitch:
                    listener.onEQStateChanged(checked);
                    break;
                case R.id.reverbSwitch:
                    listener.onReverbStateChanged(checked);
                    break;
                case R.id.echoSwitch:
                    listener.onEchoStateChanged(checked);
                    break;
                case R.id.whooshSwitch:
                    listener.onWhooshStateChanged(checked);
                    break;
                case R.id.recordSwitch:
                    listener.onRecordStateChanged(checked);
                    break;
            }
        }
    }

    private void effectClick() {
        if (listener != null) {
            listener.onEffectClick();
        }
    }

    public void setOnEffectChangedListener(OnEffectChangedListener l) {
        this.listener = l;
    }

    public void setEQEnable(boolean enable) {
        switchCompats[0].setChecked(enable);
    }

    public void setReverbEnable(boolean enable) {
        switchCompats[1].setChecked(enable);
    }

    public void setEchoEnable(boolean enable) {
        switchCompats[2].setChecked(enable);
    }

    public void setWhooshEnable(boolean enable) {
        switchCompats[3].setChecked(enable);
    }

    public void setRecordEnable(boolean enable) {
        switchCompats[4].setChecked(enable);
    }

    public View getSettingsView() {
        return findViewById(R.id.effectsTitle);
    }
}
