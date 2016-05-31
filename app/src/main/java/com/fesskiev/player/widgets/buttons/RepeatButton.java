package com.fesskiev.player.widgets.buttons;


import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.fesskiev.player.R;

public class RepeatButton extends FrameLayout implements View.OnClickListener {

    public interface OnRepeatStateChangedListener {
        void onRepeatStateChanged(boolean repeat);
    }

    private final static int DURATION = 200;

    private OnRepeatStateChangedListener listener;
    private boolean repeat;

    public RepeatButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RepeatButton(Context context) {
        super(context);
        init();
    }

    public RepeatButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnClickListener(this);
        setRepeatOff();
    }

    public void setOnRepeatStateChangedListener(OnRepeatStateChangedListener l) {
        this.listener = l;
    }

    public void setRepeatOn() {
        setBackgroundResource(R.drawable.icon_repeat_on);
    }

    public void setRepeatOff() {
        setBackgroundResource(R.drawable.icon_repeat_off);
    }

    @Override
    public void onClick(View v) {
        repeat = !repeat;
        if (listener != null) {
            listener.onRepeatStateChanged(repeat);
        }
        changeState();
    }

    private void animateShowButton() {
        animate().scaleX(1).scaleY(1).setDuration(DURATION).setListener(new Animator.AnimatorListener() {
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
        animate().scaleX(0).scaleY(0).setDuration(DURATION).setListener(new Animator.AnimatorListener() {
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
}
