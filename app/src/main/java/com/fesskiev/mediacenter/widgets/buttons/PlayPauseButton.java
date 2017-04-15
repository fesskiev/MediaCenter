package com.fesskiev.mediacenter.widgets.buttons;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import com.fesskiev.mediacenter.R;

public class PlayPauseButton extends AppCompatImageView {

    private static final long PLAY_PAUSE_ANIMATION_DURATION = 200;

    private PlayPauseDrawable drawable;
    private AnimatorSet animatorSet;
    private Drawable timerDrawable;
    private boolean showTimer;

    public PlayPauseButton(Context context) {
        super(context);
        init(context);
    }

    public PlayPauseButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PlayPauseButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        final Resources res = context.getResources();
        drawable = new PlayPauseDrawable(context);
        drawable.setCallback(this);
        drawable.setPauseBarWidth(res.getDimensionPixelSize(R.dimen.pause_bar_big_width));
        drawable.setPauseBarHeight(res.getDimensionPixelSize(R.dimen.pause_bar_big_height));
        drawable.setPauseBarDistance(res.getDimensionPixelSize(R.dimen.pause_bar_big_distance));

        timerDrawable = ContextCompat.getDrawable(context, R.drawable.avd_clock_timer_primary);
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

    public void setColor(int color) {
        drawable.setColor(color);
    }

    public void startLoading() {
        showTimer = true;
        setImageDrawable(timerDrawable);
        ((AnimatedVectorDrawable) getDrawable()).start();

    }

    public void finishLoading() {
        showTimer = false;
        AnimatedVectorDrawable drawable = ((AnimatedVectorDrawable) getDrawable());
        if(drawable != null){
            drawable.stop();
            setImageDrawable(null);
        }
    }

}
