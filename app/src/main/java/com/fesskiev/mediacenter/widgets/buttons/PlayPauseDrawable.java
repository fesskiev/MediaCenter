package com.fesskiev.mediacenter.widgets.buttons;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Property;

import com.fesskiev.mediacenter.R;

public class PlayPauseDrawable extends Drawable {

    private static final Property<PlayPauseDrawable, Float> PROGRESS =
            new Property<PlayPauseDrawable, Float>(Float.class, "progress") {
                @Override
                public Float get(PlayPauseDrawable d) {
                    return d.getProgress();
                }

                @Override
                public void set(PlayPauseDrawable d, Float value) {
                    d.setProgress(value);
                }
            };

    private final Path leftPauseBar = new Path();
    private final Path rightPauseBar = new Path();
    private final Paint paint = new Paint();
    private final RectF bounds = new RectF();
    private float pauseBarWidth;
    private float pauseBarHeight;
    private float pauseBarDistance;

    private float width;
    private float height;

    private float progress;
    private boolean isPlay;

    public PlayPauseDrawable(Context context) {
        final Resources res = context.getResources();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        pauseBarWidth = res.getDimensionPixelSize(R.dimen.pause_bar_width);
        pauseBarHeight = res.getDimensionPixelSize(R.dimen.pause_bar_height);
        pauseBarDistance = res.getDimensionPixelSize(R.dimen.pause_bar_distance);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        this.bounds.set(bounds);
        width = this.bounds.width();
        height = this.bounds.height();
    }

    @Override
    public void draw(Canvas canvas) {
        leftPauseBar.rewind();
        rightPauseBar.rewind();
        // The current distance between the two pause bars.
        final float barDist = lerp(pauseBarDistance, 0, progress);
        // The current width of each pause bar.
        final float barWidth = lerp(pauseBarWidth, pauseBarHeight / 2f, progress);
        // The current position of the left pause bar's top left coordinate.
        final float firstBarTopLeft = lerp(0, barWidth, progress);
        // The current position of the right pause bar's top right coordinate.
        final float secondBarTopRight = lerp(2 * barWidth + barDist, barWidth + barDist, progress);

        // Draw the left pause bar. The left pause bar transforms into the
        // top half of the play button triangle by animating the position of the
        // rectangle's top left coordinate and expanding its bottom width.
        leftPauseBar.moveTo(0, 0);
        leftPauseBar.lineTo(firstBarTopLeft, -pauseBarHeight);
        leftPauseBar.lineTo(barWidth, -pauseBarHeight);
        leftPauseBar.lineTo(barWidth, 0);
        leftPauseBar.close();

        // Draw the right pause bar. The right pause bar transforms into the
        // bottom half of the play button triangle by animating the position of the
        // rectangle's top right coordinate and expanding its bottom width.
        rightPauseBar.moveTo(barWidth + barDist, 0);
        rightPauseBar.lineTo(barWidth + barDist, -pauseBarHeight);
        rightPauseBar.lineTo(secondBarTopRight, -pauseBarHeight);
        rightPauseBar.lineTo(2 * barWidth + barDist, 0);
        rightPauseBar.close();

        canvas.save();

        // Translate the play button a tiny bit to the right so it looks more centered.
        canvas.translate(lerp(0, pauseBarHeight / 8f, progress), 0);

        // (1) Pause --> Play: rotate 0 to 90 degrees clockwise.
        // (2) Play --> Pause: rotate 90 to 180 degrees clockwise.
        final float rotationProgress = isPlay ? 1 - progress : progress;
        final float startingRotation = isPlay ? 90 : 0;
        canvas.rotate(lerp(startingRotation, startingRotation + 90, rotationProgress), width / 2f, height / 2f);

        // Position the pause/play button in the center of the drawable's bounds.
        canvas.translate(width / 2f - ((2 * barWidth + barDist) / 2f), height / 2f + (pauseBarHeight / 2f));

        // Draw the two bars that form the animated pause/play button.
        canvas.drawPath(leftPauseBar, paint);
        canvas.drawPath(rightPauseBar, paint);

        canvas.restore();
    }

    public Animator getPausePlayAnimator(boolean play) {
        isPlay = play;
        return ObjectAnimator.ofFloat(this, PROGRESS, isPlay ? 1 : 0, isPlay ? 0 : 1);
    }

    public boolean isPlay() {
        return isPlay;
    }

    private void setProgress(float progress) {
        this.progress = progress;
        invalidateSelf();
    }

    private float getProgress() {
        return progress;
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    /**
     * Linear interpolate between a and b with parameter t.
     */
    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public void setPauseBarWidth(float pauseBarWidth) {
        this.pauseBarWidth = pauseBarWidth;
    }

    public void setPauseBarHeight(float pauseBarHeight) {
        this.pauseBarHeight = pauseBarHeight;
    }

    public void setPauseBarDistance(float pauseBarDistance) {
        this.pauseBarDistance = pauseBarDistance;
    }

    public void setColor(int color) {
        paint.setColor(color);
    }
}
