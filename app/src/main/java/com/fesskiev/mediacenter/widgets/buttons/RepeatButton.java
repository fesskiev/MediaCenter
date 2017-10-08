package com.fesskiev.mediacenter.widgets.buttons;


import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.Utils;

public class RepeatButton extends FrameLayout implements View.OnClickListener {

    public interface OnRepeatStateChangedListener {
        void onRepeatStateChanged(boolean repeat);

        void onLoopingBetweenClick();
    }

    private final static int DURATION = 200;

    private OnRepeatStateChangedListener listener;

    private ImageView repeatButton;
    private ImageView startLoopImage;
    private ImageView endLoopImage;
    private TextView repeatState;
    private TextView startLoopTime;
    private TextView endLoopTime;

    private boolean repeat;


    public RepeatButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RepeatButton(Context context) {
        super(context);
        init(context);
    }

    public RepeatButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.repeat_button_layout, this, true);

        repeatButton = view.findViewById(R.id.repeatButtonState);
        repeatButton.setOnClickListener(this);

        repeatState = view.findViewById(R.id.repeatState);

        startLoopImage = view.findViewById(R.id.loopStartImage);
        startLoopImage.setOnClickListener(this);
        endLoopImage = view.findViewById(R.id.loopEndImage);
        endLoopImage.setOnClickListener(this);

        startLoopTime = view.findViewById(R.id.loopStartTime);
        endLoopTime = view.findViewById(R.id.loopEndTime);

        setRepeatOff();
    }

    public void setOnRepeatStateChangedListener(OnRepeatStateChangedListener l) {
        this.listener = l;
    }

    public void setRepeatOn() {
        repeatState.setText(R.string.repeat_button_on);
    }

    public void setRepeatOff() {
        repeatState.setText(R.string.repeat_button_off);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.repeatButtonState:
                changeRepeatState();
                break;
            case R.id.loopStartImage:
            case R.id.loopEndImage:
                loopBetweenState();
                break;
        }
    }

    private void loopBetweenState() {
        if (listener != null) {
            listener.onLoopingBetweenClick();
        }
    }

    private void changeRepeatState() {
        repeat = !repeat;
        if (listener != null) {
            listener.onRepeatStateChanged(repeat);
        }
        changeState();
    }


    private void animateShowButton() {
        repeatState.animate().scaleX(1).scaleY(1).setDuration(DURATION).setInterpolator(new LinearInterpolator()).
                setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (repeat) {
                            setRepeatOn();
                        } else {
                            setRepeatOff();
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    private void animateHideButton() {
        repeatState.animate().scaleX(0).scaleY(0).setDuration(DURATION).setInterpolator(new LinearInterpolator()).
                setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animateShowButton();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    private void changeState() {
        animateHideButton();
    }

    public void changeState(boolean repeat) {
        this.repeat = repeat;
        if (repeat) {
            setRepeatOn();
        } else {
            setRepeatOff();
            clearLoopBetweenTime();
        }
    }

    public void setLoopBetweenTime(int start, int end) {
        startLoopTime.setText(Utils.getDurationString(start));
        endLoopTime.setText(Utils.getDurationString(end));
    }

    public void clearLoopBetweenTime() {
        startLoopTime.setText(getResources().getText(R.string.repeat_loop_off));
        endLoopTime.setText(getResources().getText(R.string.repeat_loop_off));
    }

    public void setColorFilter(int color) {
        startLoopTime.setTextColor(color);
        endLoopTime.setTextColor(color);
        repeatState.setTextColor(color);
        startLoopImage.setColorFilter(color);
        endLoopImage.setColorFilter(color);
        repeatButton.setColorFilter(color);
    }

}
