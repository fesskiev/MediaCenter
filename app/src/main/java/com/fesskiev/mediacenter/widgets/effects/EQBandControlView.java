package com.fesskiev.mediacenter.widgets.effects;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.effects.DealerView;

/**
 * The gains on the 3 band EQ are the "knobs" for each band, in other words,
 * the faders a user may use to alter the sound. 1.0f means unity gain.
 * The values are limited between 0.00001f and 8.0f, providing a range between -100 decibels and +18 decibels.
 * <p>
 * http://superpowered.com/3-band-equalizer-64-bit-armv8-support-and-time-stretching-on-mobile-processors
 */
public class EQBandControlView extends DealerView {

    public interface OnBandLevelListener {

        void onBandLevelChanged(int band, float level, float[] values);
    }

    private OnBandLevelListener listener;
    private Paint markPaint;
    private Paint rangePaint;
    private Paint namePaint;
    private String bandName;
    private int band;
    private int radius;
    private int textPadding;
    private float markRadius;
    private float markSize;
    private float rangeTextX;
    private float rangeTextY;
    private float rangeTextX1;


    public EQBandControlView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public EQBandControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public EQBandControlView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.EQBandControlView, defStyle, 0);

        band = a.getInt(R.styleable.EQBandControlView_band, -1);
        bandName = a.getString(R.styleable.EQBandControlView_bandName);

        a.recycle();

        markRadius = Utils.dipToPixels(context, 70);
        markSize = Utils.dipToPixels(context, 30);

        textPadding = (int) Utils.dipToPixels(context, 6);

        rangeTextX = Utils.dipToPixels(context, 30);
        rangeTextY = Utils.dipToPixels(context, 30);
        rangeTextX1 = Utils.dipToPixels(context, 145);

        float markStrokeWidth = Utils.dipToPixels(context, 5);
        float nameStrokeWidth = Utils.dipToPixels(context, 14);
        float rangeStrokeWidth = Utils.dipToPixels(context, 14);

        radius = (int) Utils.dipToPixels(context, 3);

        markPaint = new Paint();
        markPaint.setColor(ContextCompat.getColor(context, android.R.color.white));
        markPaint.setStyle(Paint.Style.FILL);
        markPaint.setStrokeWidth(markStrokeWidth);

        namePaint = new Paint();
        namePaint.setColor(ContextCompat.getColor(context, android.R.color.white));
        namePaint.setStyle(Paint.Style.FILL);
        namePaint.setTextSize(nameStrokeWidth);
        namePaint.setAntiAlias(true);
        namePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        namePaint.setTextAlign(Paint.Align.CENTER);

        rangePaint = new Paint();
        rangePaint.setColor(ContextCompat.getColor(context, android.R.color.white));
        rangePaint.setStyle(Paint.Style.FILL);
        rangePaint.setTextSize(rangeStrokeWidth);
        rangePaint.setAntiAlias(true);
        rangePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        rangePaint.setTextAlign(Paint.Align.CENTER);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        for (int i = 30; i < 360; i += 30) {

            float angle = (float) Math.toRadians(i);

            float startX = (float) (cx + markRadius * Math.sin(angle));
            float startY = (float) (cy - markRadius * Math.cos(angle));

            float stopX = (float) (cx + (markRadius - markSize) * Math.sin(angle));
            float stopY = (float) (cy - (markRadius - markSize) * Math.cos(angle));

            if (i == 30 || i == 180 || i == 330) {
                canvas.drawLine(startX, startY, stopX, stopY, markPaint);
            } else {
                canvas.drawCircle(startX, startY, radius, markPaint);
            }
        }

        canvas.drawText(bandName, cx, getWidth() - textPadding, namePaint);

        canvas.drawText("-100", rangeTextX, rangeTextY, rangePaint);

        canvas.drawText("+18", rangeTextX1, rangeTextY, rangePaint);

        super.onDraw(canvas);
    }

    @Override
    public void rotateBand(float currentAngle, float[] values) {

        float angleFix = getAngleFix(currentAngle);

        float level = (angleFix * (100f / 360));

        if (listener != null) {
            listener.onBandLevelChanged(band, level, values);
        }
    }

    public void setOnBandLevelListener(OnBandLevelListener listener) {
        this.listener = listener;
    }

    public int getBand() {
        return band;
    }
}
