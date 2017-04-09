package com.fesskiev.mediacenter.widgets.buttons;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import com.fesskiev.mediacenter.R;


public class PlayPauseFloatingButton extends FloatingActionButton {

    private static final long PLAY_PAUSE_ANIMATION_DURATION = 200;

    private final PlayPauseDrawable drawable;
    private AnimatorSet animatorSet;
    private Drawable timerDrawable;
    private boolean showTimer;

    public PlayPauseFloatingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        timerDrawable = ContextCompat.getDrawable(context, R.drawable.avd_clock_timer);

        drawable = new PlayPauseDrawable(context);
        drawable.setCallback(this);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        drawable.setBounds(0, 0, w, h);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == drawable || super.verifyDrawable(who);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!showTimer) {
            drawable.draw(canvas);
        }
    }

    public void setPlay(boolean play) {
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        animatorSet = new AnimatorSet();
        final Animator pausePlayAnim = drawable.getPausePlayAnimator(play);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(PLAY_PAUSE_ANIMATION_DURATION);
        animatorSet.playTogether(pausePlayAnim);
        animatorSet.start();
    }

    public void startLoading() {
        showTimer = true;
        setImageDrawable(timerDrawable);
        ((AnimatedVectorDrawable) getDrawable()).start();

    }

    public void finishLoading() {
        showTimer = false;
        ((AnimatedVectorDrawable) getDrawable()).stop();
        setImageDrawable(null);
    }
}
