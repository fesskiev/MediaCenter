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
import android.view.MotionEvent;
import android.view.View;

import com.fesskiev.player.R;
import com.fesskiev.player.utils.Utils;


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


    private int radiusVolume;
    private int radiusSeek;
    private int lineRadius;
    private int markSize;
    private float circleStrokeWidth;

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

        int circleColor = a.getColor(
                R.styleable.AudioVolumeSeekView_circleColor,
                ContextCompat.getColor(context, R.color.primary_light));
        int progressColor = a.getColor(
                R.styleable.AudioVolumeSeekView_progressColor,
                ContextCompat.getColor(context, R.color.primary_dark));

        a.recycle();

        seekSlider = new Slider(Utils.dipToPixels(context, 20), R.drawable.icon_time_control);
        volumeSlider = new Slider(Utils.dipToPixels(context, 20), R.drawable.icon_volume_control);

        radiusVolume = (int) Utils.dipToPixels(context, 80);
        radiusSeek = (int) Utils.dipToPixels(context, 135);
        lineRadius = (int) Utils.dipToPixels(context, 135);
        circleStrokeWidth = (int) Utils.dipToPixels(context, 15);
        markSize = (int) Utils.dipToPixels(context, 50);

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


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        volumeRect = new RectF(w / 2 - radiusVolume, h / 2 - radiusVolume,
                w / 2 + radiusVolume, h / 2 + radiusVolume);

        seekRect = new RectF(w / 2 - radiusSeek, h / 2 - radiusSeek,
                w / 2 + radiusSeek, h / 2 + radiusSeek);

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

                final float x = event.getX();
                final float y = event.getY();

                if (checkSeek) {
                    setSeekProgress(x, y);
                    return true;
                }
                if (checkVolume) {
                    setVolumeProgress(x, y);
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


    private void setSeekProgress(float dx, float dy) {

        dx += 10;
        dy += 10;
        float angle = (float) ((Math.toDegrees(Math.atan2(dx - 360.0, 360.0 - dy)) + 360.0) % 360.0);
        progressSeek = angle;
        float scaleValue = angle * (100f / 360);

//        Log.d("test", "angle: " + angle + " x: " + dx + " y: " + dy);

        invalidate();

        if (listener != null) {
            listener.changeSeekStart((int) scaleValue);
        }
    }


    private int checkTouchSector(float angle) {
        if (angle >= 0f && angle < 44f) {
            return 0;
        } else if (angle >= 44f && angle < 90f) {
            return 1;
        } else if (angle >= 90f && angle < 135f) {
            return 2;
        } else if (angle >= 135f && angle < 180f) {
            return 3;
        } else if (angle >= 180f && angle < 225f) {
            return 4;
        } else if (angle >= 225f && angle < 270f) {
            return 5;
        } else if (angle >= 270f && angle < 315f) {
            return 6;
        } else if (angle >= 315f && angle < 360f) {
            return 7;
        }
        return -1;
    }


    private void setVolumeProgress(float dx, float dy) {
        dx += 10;
        dy += 10;

        float angle = (float) ((Math.toDegrees(Math.atan2(dx - 360.0, 360.0 - dy)) + 360.0) % 360.0);

//        Log.w("test", "volume progress: x: " + dx + " y: " + dy + " angle: " + angle);

        float scaleValue = angle * (100f / 360);

        progressVolume = angle;

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

        volumeSlider.x = (float) (cx + radiusVolume * Math.cos(Math.toRadians(270f + progressVolume)));
        volumeSlider.y = (float) (cy + radiusVolume * Math.sin(Math.toRadians(270f + progressVolume)));

        canvas.drawCircle(volumeSlider.x, volumeSlider.y, volumeSlider.radius, circleFillPaint);
        canvas.drawBitmap(volumeSlider.bitmap, (volumeSlider.x - (volumeSlider.radius / 2)) - 10,
                (volumeSlider.y - (volumeSlider.radius / 2)) - 10, null);

        /****************************************************************************/

        canvas.drawCircle(cx, cy, radiusSeek, circlePaint);
        canvas.drawArc(seekRect, 270, progressSeek, false, progressPaint);

        seekSlider.x = (float) (cx + radiusSeek * Math.cos(Math.toRadians(270f + progressSeek)));
        seekSlider.y = (float) (cy + radiusSeek * Math.sin(Math.toRadians(270f + progressSeek)));


//        Log.d("test", "draw volume x: " + volumeSlider.x + " y: " + volumeSlider.y);

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
