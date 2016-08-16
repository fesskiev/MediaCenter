package com.fesskiev.player.widgets.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
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
    private Slider seekSlider;
    private Slider volumeSlider;
    private Paint linePaint;
    private Paint progressPaint;
    private Paint circleFillPaint;
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
    private float volumeStrokeWidth;
    private float seekStrokeWidth;
    private float progressVolume;
    private float progressSeek;
    private boolean checkVolume;
    private boolean checkSeek;
    private boolean enableChangeVolume;


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

        seekSlider = new Slider(dipToPixels(context, 20), R.drawable.icon_time_control);
        volumeSlider = new Slider(dipToPixels(context, 20), R.drawable.icon_volume_control);

        volumeStrokeWidth = (int) dipToPixels(context, 80);
        seekStrokeWidth = (int) dipToPixels(context, 25);
        radiusVolume = (int) dipToPixels(context, 80);
        radiusSeek = (int) dipToPixels(context, 135);
        lineRadius = (int) dipToPixels(context, 135);
        circleStrokeWidth = (int) dipToPixels(context, 15);
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

        circleFillPaint = new Paint();
        circleFillPaint.setStyle(Paint.Style.FILL);
        circleFillPaint.setColor(progressColor);
        circleFillPaint.setAntiAlias(true);
        circleFillPaint.setStrokeWidth(circleStrokeWidth);

        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(progressColor);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeWidth(circleStrokeWidth);

        setBackgroundColor(Color.TRANSPARENT);

        enableChangeVolume = true;

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
                if (inCircle(event.getX(), event.getY(), seekSlider.x, seekSlider.y, seekSlider.radius)) {
                    checkSeek = true;
                }
                if (enableChangeVolume) {
                    if (inCircle(event.getX(), event.getY(), volumeSlider.x, volumeSlider.y, volumeSlider.radius)) {
                        checkVolume = true;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (checkSeek) {
                    incrementSeekProgress(event);
                    return true;
                }
                if (checkVolume) {
                    incrementVolumeProgress(event);
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


    private void incrementSeekProgress(MotionEvent event) {
        int angle = (int) ((Math.toDegrees(Math.atan2(event.getX() - 360.0,
                360.0 - event.getY())) + 360.0) % 360.0);

        progressSeek = angle;
        float scaleValue = angle * (100f / 360);

        invalidate();

        if (listener != null) {
            listener.changeSeekStart((int) scaleValue);
        }
    }


    private int checkTouchSector(int angle) {
        if (angle >= 0 && angle < 44) {
            return 0;
        } else if (angle >= 44 && angle < 90) {
            return 1;
        } else if (angle >= 90 && angle < 135) {
            return 2;
        } else if (angle >= 135 && angle < 180) {
            return 3;
        } else if (angle >= 180 && angle < 225) {
            return 4;
        } else if (angle >= 225 && angle < 270) {
            return 5;
        } else if (angle >= 270 && angle < 315) {
            return 6;
        } else if (angle >= 315 && angle < 360) {
            return 7;
        }
        return -1;
    }

    private int previousSector;

    private boolean isEndTouch(int angle) {
        int sector = checkTouchSector(angle);
        if ((previousSector - sector) > 5) {
            return true;
        }
        previousSector = sector;
        return false;
    }


    private void incrementVolumeProgress(MotionEvent event) {
        int angle = (int) ((Math.toDegrees(Math.atan2(event.getX() - 360.0,
                360.0 - event.getY())) + 360.0) % 360.0);

        if (isEndTouch(angle)) {
            return;
        }
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

        /****************************************************************************/

        canvas.drawCircle(cx, cy, radiusVolume, circlePaint);
        canvas.drawArc(volumeRect, 270, progressVolume, false, progressPaint);

        volumeSlider.x = (float) (cx + radiusVolume * Math.cos(Math.toRadians(270 + progressVolume)));
        volumeSlider.y = (float) (cy + radiusVolume * Math.sin(Math.toRadians(270 + progressVolume)));

        canvas.drawCircle(volumeSlider.x, volumeSlider.y, volumeSlider.radius, circleFillPaint);
        canvas.drawBitmap(volumeSlider.bitmap, (volumeSlider.x - (volumeSlider.radius / 2)) - 10,
                (volumeSlider.y - (volumeSlider.radius / 2)) - 10, null);

        /****************************************************************************/

        canvas.drawCircle(cx, cy, radiusSeek, circlePaint);
        canvas.drawArc(seekRect, 270, progressSeek, false, progressPaint);

        seekSlider.x = (float) (cx + radiusSeek * Math.cos(Math.toRadians(270 + progressSeek)));
        seekSlider.y = (float) (cy + radiusSeek * Math.sin(Math.toRadians(270 + progressSeek)));

        canvas.drawCircle(seekSlider.x, seekSlider.y, seekSlider.radius, circleFillPaint);
        canvas.drawBitmap(seekSlider.bitmap, (seekSlider.x - (seekSlider.radius / 2)) - 10,
                (seekSlider.y - (seekSlider.radius / 2)) - 10, null);

    }

    public boolean inCircle(float x, float y, float centerX, float centerY, float radius) {
        return (float) Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2)) < radius;

    }

    public void setVolumeValue(int value) {
        progressVolume = value * 3.6f;
        invalidate();
    }

    public void setSeekValue(int value) {
        progressSeek = value * 3.6f;
        invalidate();
    }

    public void setEnableChangeVolume(boolean enable) {
        enableChangeVolume = enable;
    }


    private class Slider {

        Bitmap bitmap;
        float radius;
        float x;
        float y;

        public Slider(float radius, int res) {
            this.radius = radius;
            bitmap = BitmapFactory.decodeResource(getResources(), res);
        }
    }

}
