package com.fesskiev.player.widgets.buttons;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.fesskiev.player.R;

public class PlayPauseButton extends ImageView {

    private static final long PLAY_PAUSE_ANIMATION_DURATION = 200;

    private PlayPauseDrawable drawable;
    private AnimatorSet animatorSet;

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

    private void init(Context context){
        final Resources res = context.getResources();
        drawable = new PlayPauseDrawable(context);
        drawable.setCallback(this);
        drawable.setPauseBarWidth(res.getDimensionPixelSize(R.dimen.pause_bar_big_width));
        drawable.setPauseBarHeight(res.getDimensionPixelSize(R.dimen.pause_bar_big_height));
        drawable.setPauseBarDistance(res.getDimensionPixelSize(R.dimen.pause_bar_big_distance));
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
}
