package com.fesskiev.mediacenter.widgets.settings;


import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.AppSettingsManager;

public class MediaContentUpdateTimeView extends AnimateStateShow implements RadioGroup.OnCheckedChangeListener {

    public interface OnMediaContentTimeUpdateListener {

        void onUpdateByTime(int time);

        void onCancelUpdateByTime();
    }

    public static final int MILLISECOND = 1000;
    public static final int MINUTE = MILLISECOND * 60;

    public static final int HALF_HOUR = MINUTE * 30;
    public static final int HOUR = MINUTE * 60;
    public static final int THREE_HOUR = HOUR * 3;
    public static final int SIX_HOURS = HOUR * 6;
    public static final int ONE_DAY = HOUR * 24;


    private RadioButton[] radioButtons;
    private AppSettingsManager settingsManager;
    private OnMediaContentTimeUpdateListener listener;

    public MediaContentUpdateTimeView(Context context) {
        super(context);
        init(context);
    }

    public MediaContentUpdateTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MediaContentUpdateTimeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.media_content_update_timer_layout, this, true);

        ((RadioGroup) view.findViewById(R.id.radioGroupTimeUpdate)).setOnCheckedChangeListener(this);

        radioButtons = new RadioButton[]{
                view.findViewById(R.id.cancelUpdateByTime),
                view.findViewById(R.id.update30minuteButton),
                view.findViewById(R.id.update1HourButton),
                view.findViewById(R.id.update3HoursButton),
                view.findViewById(R.id.update6HoursButton),
                view.findViewById(R.id.update24HoursButton)
        };
        for(RadioButton radioButton : radioButtons){
            radioButton.setTypeface(ResourcesCompat.getFont(getContext(), R.font.ubuntu));
        }

        settingsManager = AppSettingsManager.getInstance();
        setSelectedUpdateTimeType();
    }

    private void setSelectedUpdateTimeType() {
        long updateTime = settingsManager.getMediaContentUpdateTime();
        if (updateTime == HALF_HOUR) {
            radioButtons[1].setChecked(true);
        } else if (updateTime == HOUR) {
            radioButtons[2].setChecked(true);
        } else if (updateTime == THREE_HOUR) {
            radioButtons[3].setChecked(true);
        } else if (updateTime == SIX_HOURS) {
            radioButtons[4].setChecked(true);
        } else if (updateTime == ONE_DAY) {
            radioButtons[5].setChecked(true);
        } else {
            radioButtons[0].setChecked(true);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (listener != null) {
            switch (checkedId) {
                case R.id.cancelUpdateByTime:
                    settingsManager.setMediaContentUpdateTime(0);
                    listener.onCancelUpdateByTime();
                    break;
                case R.id.update30minuteButton:
                    settingsManager.setMediaContentUpdateTime(HALF_HOUR);
                    listener.onUpdateByTime(HALF_HOUR);
                    break;
                case R.id.update1HourButton:
                    settingsManager.setMediaContentUpdateTime(HOUR);
                    listener.onUpdateByTime(HOUR);
                    break;
                case R.id.update3HoursButton:
                    settingsManager.setMediaContentUpdateTime(THREE_HOUR);
                    listener.onUpdateByTime(THREE_HOUR);
                    break;
                case R.id.update6HoursButton:
                    settingsManager.setMediaContentUpdateTime(SIX_HOURS);
                    listener.onUpdateByTime(SIX_HOURS);
                    break;
                case R.id.update24HoursButton:
                    settingsManager.setMediaContentUpdateTime(ONE_DAY);
                    listener.onUpdateByTime(ONE_DAY);
                    break;
            }
        }
    }

    public void setOnMediaContentTimeUpdateListener(OnMediaContentTimeUpdateListener l) {
        this.listener = l;
    }
}
