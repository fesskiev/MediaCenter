package com.fesskiev.player.widgets.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.fesskiev.player.R;


public class AudioVolumeSeekView extends View {

    public interface OnAudioVolumeSeekListener {
        void changeVolumeStart(int volume);

        void changeVolumeFinish();

        void changeSeekStart(int seek);

        void changeSeekFinish();
    }

    private OnAudioVolumeSeekListener listener;
    private Paint linePaint;
    private Paint progressPaint;
    private Paint circlePaint;
    private RectF volumeRect;
    private RectF seekRect;
    private int circleColor;
    private int progressColor;
    private int width;
    private int height;
    private int radiusVolume;
    private int radiusSeek;
    private int lineRadius;
    private int markSize;
    private int circleStrokeWidth;
    private int textStrokeWidth;
    private float volumeStrokeWidth;
    private float seekStrokeWidth;
    private float progressVolume;
    private float progressSeek;
    private boolean checkVolume;
    private boolean checkSeek;


    public AudioVolumeSeekView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public AudioVolumeSeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public AudioVolumeSeekView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }


    private void init(Context context, AttributeSet attrs, int defStyle) {

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.AudioVolumeSeekView, defStyle, 0);

        circleColor = a.getColor(
                R.styleable.AudioVolumeSeekView_circleColor,
                ContextCompat.getColor(context, R.color.primary_light));
        progressColor = a.getColor(
                R.styleable.AudioVolumeSeekView_progressColor,
                ContextCompat.getColor(context, R.color.primary_dark));

        a.recycle();

        volumeStrokeWidth = (int) dipToPixels(context, 60);
        seekStrokeWidth = (int) dipToPixels(context, 5);
        radiusVolume = (int) dipToPixels(context, 80);
        radiusSeek = (int) dipToPixels(context, 135);
        lineRadius = (int) dipToPixels(context, 135);
        textStrokeWidth = (int) dipToPixels(context, 60);
        circleStrokeWidth = (int) dipToPixels(context, 10);
        markSize = (int) dipToPixels(context, 50);

        linePaint = new Paint();
        linePaint.setColor(circleColor);
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setStrokeWidth(15f);


        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(circleColor);
        circlePaint.setAntiAlias(true);
        circlePaint.setStrokeWidth(circleStrokeWidth);

        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(progressColor);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeWidth(circleStrokeWidth);

        setBackgroundColor(Color.TRANSPARENT);

    }

    public void setListener(OnAudioVolumeSeekListener l) {
        this.listener = l;
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w;
        height = h;

        volumeRect = new RectF(
                volumeStrokeWidth,
                volumeStrokeWidth,
                width - volumeStrokeWidth,
                height - volumeStrokeWidth);

        seekRect = new RectF(
                seekStrokeWidth,
                seekStrokeWidth,
                width - seekStrokeWidth,
                height - seekStrokeWidth);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                checkSeek = checkTouchSeek(event);
                checkVolume = checkTouchVolume(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (checkSeek) {
                    incrementSeekProgress(event);
                    return true;
                }
                if (checkVolume) {
                    incrementTouchProgress(event);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (listener != null) {
                    if (checkVolume) {
                        listener.changeVolumeFinish();
                    }
                    if (checkSeek) {
                        listener.changeSeekFinish();
                    }
                }
                checkVolume = false;
                checkSeek = false;
                break;
        }
        return true;
    }

    private boolean checkTouchSeek(MotionEvent event) {
        double dist = Math.sqrt(Math.pow(event.getX() - seekRect.centerX(), 2)
                + Math.pow(event.getY() - seekRect.centerY(), 2));
        return Math.abs(dist - radiusSeek) <= 150 &&
                seekRect.contains(event.getX(), event.getY());
    }

    private boolean checkTouchVolume(MotionEvent event) {
        double dist = Math.sqrt(Math.pow(event.getX() - volumeRect.centerX(), 2)
                + Math.pow(event.getY() - volumeRect.centerY(), 2));
        return Math.abs(dist - radiusVolume) <= 150 &&
                volumeRect.contains(event.getX(), event.getY());
    }

    private void incrementSeekProgress(MotionEvent event) {
        progressSeek = (int) ((Math.toDegrees(Math.atan2(event.getX() - 360.0,
                360.0 - event.getY())) + 360.0) % 360.0);

        invalidate();

        if (listener != null) {
            listener.changeSeekStart(0);
        }
    }

    private void incrementTouchProgress(MotionEvent event) {
        int angle = (int) ((Math.toDegrees(Math.atan2(event.getX() - 360.0,
                360.0 - event.getY())) + 360.0) % 360.0);

        progressVolume = angle;
        float scaleValue = angle * (100f / 360);

        invalidate();

        if (listener != null) {
            listener.changeVolumeStart((int) scaleValue);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        for (int i = 0; i < 360; i += 45) {
            float angle = (float) Math.toRadians(i);

            float startX = (float) (cx + lineRadius * Math.sin(angle));
            float startY = (float) (cy - lineRadius * Math.cos(angle));

            float stopX = (float) (cx + (lineRadius - markSize) * Math.sin(angle));
            float stopY = (float) (cy - (lineRadius - markSize) * Math.cos(angle));

            canvas.drawLine(startX, startY, stopX, stopY, linePaint);
        }

        canvas.drawCircle(cx, cy, radiusVolume, circlePaint);
        canvas.drawArc(volumeRect, 270, progressVolume, false, progressPaint);

        canvas.drawCircle(cx, cy, radiusSeek, circlePaint);
        canvas.drawArc(seekRect, 270, progressSeek, false, progressPaint);

    }

    public void setVolumeValue(int value) {
        progressVolume = value * 3.6f;
        postInvalidate();
    }


}
