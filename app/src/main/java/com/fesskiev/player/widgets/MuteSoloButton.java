package com.fesskiev.player.widgets;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.fesskiev.player.R;


public class MuteSoloButton extends FrameLayout implements View.OnClickListener {

    public interface OnMuteSoloListener {
        void onMuteStateChanged(boolean mute);
    }

    private final static int DURATION = 200;
    private OnMuteSoloListener listener;
    private boolean mute;

    public MuteSoloButton(Context context) {
        super(context);
        init();
    }

    public MuteSoloButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MuteSoloButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setHighSoloState();
        setOnClickListener(this);
        mute = false;
    }

    public void setOnMuteSoloListener(OnMuteSoloListener l) {
        this.listener = l;
    }

    public void resetMuteSolo(int volume){
        mute = false;
        if (volume <= 45) {
            setLowSoloState();
        } else {
            setHighSoloState();
        }
    }

    public void setMuteState() {
        setBackgroundResource(R.drawable.icon_mute);
    }

    public void setHighSoloState() {
        setBackgroundResource(R.drawable.high_volume_icon);
    }

    public void setLowSoloState() {
        setBackgroundResource(R.drawable.low_volume_icon);
    }

    @Override
    public void onClick(View v) {
        mute = !mute;
        if (listener != null) {
            listener.onMuteStateChanged(mute);
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
                if (mute) {
                    setMuteState();
                } else {
                    setHighSoloState();
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
