package com.fesskiev.player.widgets.buttons;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;


public class PlayPauseFloatingButton extends FloatingActionButton {

    private static final long PLAY_PAUSE_ANIMATION_DURATION = 200;

    private final PlayPauseDrawable drawable;
    private AnimatorSet animatorSet;

    public PlayPauseFloatingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        drawable = new PlayPauseDrawable(context);
        drawable.setCallback(this);
    }

    private void disableBehaviour() {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) getLayoutParams();
        params.setBehavior(new PlaPauseBehaviour());
    }

    private void enableBehaviour(){
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) getLayoutParams();
        params.setBehavior(new FloatingActionButton.Behavior());
    }


    public void translateToPosition(float x, float y) {
        disableBehaviour();

        animate().translationX(x).setDuration(500);
        animate().translationY(y).setDuration(500);

        animate().scaleX(1.4f);
        animate().scaleY(1.4f);
    }

    public void returnFromPosition() {
        enableBehaviour();

        animate().translationX(0);
        animate().translationY(0);

        animate().scaleX(1.0f);
        animate().scaleY(1.0f);
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
        drawable.draw(canvas);
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

    public static class PlaPauseBehaviour extends CoordinatorLayout.Behavior<FloatingActionButton> {

    }
}
