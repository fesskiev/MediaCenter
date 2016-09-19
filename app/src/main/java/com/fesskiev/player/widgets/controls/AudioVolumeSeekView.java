package com.fesskiev.player.widgets.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
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

    private float cx;
    private float cy;

    private GestureDetectorCompat gestureDetector;


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

        gestureDetector = new GestureDetectorCompat(context, gestureListener);

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

        cx = getWidth() / 2f;
        cy = getHeight() / 2f;

        seekSlider = new Slider(Utils.dipToPixels(getContext(), 20), R.drawable.icon_time_control);
        volumeSlider = new Slider(Utils.dipToPixels(getContext(), 20), R.drawable.icon_volume_control);
    }


    private final GestureDetector.SimpleOnGestureListener
            gestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d("test1", "onDoubleTap");
            return true;
        }

    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        int action = event.getActionMasked();

        float x = event.getX();
        float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                if (inCircle(x, y, seekSlider.x, seekSlider.y, seekSlider.radius)) {
                    checkSeek = true;
                }
                if (enableChangeVolume) {
                    if (inCircle(x, y, volumeSlider.x, volumeSlider.y, volumeSlider.radius)) {
                        checkVolume = true;
                    }
                }

                break;
            case MotionEvent.ACTION_MOVE:

                if (checkSeek) {
                    setSeekProgress(x, y);
                }
                if (checkVolume) {
                    setVolumeProgress(x, y);
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
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

        postInvalidate();
        return true;
    }

    public double angleBetween2Lines(float centerX, float centerY, float x1,
                                     float y1, float x2, float y2) {
        double angle1 = Math.atan2(y1 - centerY, x1 - centerX);
        double angle2 = Math.atan2(y2 - centerY, x2 - centerX);
        return angle1 - angle2;
    }

    private float getAngle(float x, float y) {
        float angle = (float) Math.toDegrees(angleBetween2Lines(cx, cy, 0, 0, x, y)) * -1;
        angle -= 45;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }


    private void setSeekProgress(float dx, float dy) {

        progressSeek = getAngle(dx, dy);

        float scaleValue = progressSeek * (100f / 360);

        if (listener != null) {
            listener.changeSeekStart((int) scaleValue);
        }
    }


    private void setVolumeProgress(float dx, float dy) {

        progressVolume = getAngle(dx, dy);

        float scaleValue = progressVolume * (100f / 360);

        if (listener != null) {
            listener.changeVolumeStart((int) scaleValue);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


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
        canvas.drawArc(volumeRect, 270f, progressVolume, false, progressPaint);

        volumeSlider.x = (float) (cx + radiusVolume * Math.sin(Math.toRadians(progressVolume)));
        volumeSlider.y = (float) (cy - radiusVolume * Math.cos(Math.toRadians(progressVolume)));


        canvas.drawCircle(volumeSlider.x, volumeSlider.y, volumeSlider.radius, circleFillPaint);
        canvas.drawBitmap(volumeSlider.bitmap, (volumeSlider.x - (volumeSlider.radius / 2)) - 10,
                (volumeSlider.y - (volumeSlider.radius / 2)) - 10, null);

        /****************************************************************************/

        canvas.drawCircle(cx, cy, radiusSeek, circlePaint);
        canvas.drawArc(seekRect, 270f, progressSeek, false, progressPaint);

        seekSlider.x = (float) (cx + radiusSeek * Math.sin(Math.toRadians(progressSeek)));
        seekSlider.y = (float) (cy - radiusSeek * Math.cos(Math.toRadians(progressSeek)));


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
