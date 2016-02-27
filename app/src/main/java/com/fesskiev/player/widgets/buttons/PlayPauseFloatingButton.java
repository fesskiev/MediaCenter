package com.fesskiev.player.widgets.buttons;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Property;
import android.view.animation.DecelerateInterpolator;

import com.fesskiev.player.R;


public class PlayPauseFloatingButton extends FloatingActionButton {

    private static final Property<PlayPauseFloatingButton, Integer> COLOR =
            new Property<PlayPauseFloatingButton, Integer>(Integer.class, "color") {
                @Override
                public Integer get(PlayPauseFloatingButton v) {
                    return v.getColor();
                }

                @Override
                public void set(PlayPauseFloatingButton v, Integer value) {
                    v.setColor(value);
                }
            };

    private static final long PLAY_PAUSE_ANIMATION_DURATION = 200;

    private final PlayPauseDrawable drawable;
    private final Paint paint = new Paint();
    private final int pauseBackgroundColor;
    private final int playBackgroundColor;

    private AnimatorSet animatorSet;
    private int backgroundColor;
    private int width;
    private int height;
    private int radius;

    public PlayPauseFloatingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        backgroundColor = ContextCompat.getColor(context, R.color.accent);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        drawable = new PlayPauseDrawable(context);
        drawable.setCallback(this);

        pauseBackgroundColor = ContextCompat.getColor(context, R.color.accent);
        playBackgroundColor = ContextCompat.getColor(context, R.color.accent);
        radius = getResources().getDimensionPixelSize(R.dimen.pause_radius);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        drawable.setBounds(0, 0, w, h);
        width = w;
        height = h;
    }

    private void setColor(int color) {
        backgroundColor = color;
        invalidate();
    }

    private int getColor() {
        return backgroundColor;
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == drawable || super.verifyDrawable(who);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(backgroundColor);
        canvas.drawCircle(width / 2f, height / 2f, radius, paint);
        drawable.draw(canvas);
    }

    public void toggle() {
        if (animatorSet != null) {
            animatorSet.cancel();
        }

        animatorSet = new AnimatorSet();
        final boolean isPlay = drawable.isPlay();
        final ObjectAnimator colorAnim = ObjectAnimator.ofInt(this, COLOR, isPlay ? pauseBackgroundColor : playBackgroundColor);
        colorAnim.setEvaluator(new ArgbEvaluator());
        final Animator pausePlayAnim = drawable.getPausePlayAnimator();
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(PLAY_PAUSE_ANIMATION_DURATION);
        animatorSet.playTogether(colorAnim, pausePlayAnim);
        animatorSet.start();
    }
}
