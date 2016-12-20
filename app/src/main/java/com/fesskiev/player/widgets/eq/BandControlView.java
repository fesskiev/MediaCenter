package com.fesskiev.player.widgets.eq;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.fesskiev.player.R;


public class BandControlView extends View {

    public interface OnBandLevelListener {

        void onBandLevelChanged(int band, int level);
    }

    private OnBandLevelListener listener;
    private Bitmap bitmapControl;
    private Matrix matrix;
    private Paint markPaint;
    private Paint textPaint;
    private String bandName;
    private int band;
    private int radius;
    private float cx;
    private float cy;
    private float startAngle;

    public BandControlView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public BandControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public BandControlView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.BandControlView, defStyle, 0);

        band = a.getInt(R.styleable.BandControlView_band, -1);
        bandName = a.getString(R.styleable.BandControlView_bandName);

        a.recycle();

        radius = 8;
        matrix = new Matrix();

        markPaint = new Paint();
        markPaint.setColor(ContextCompat.getColor(context, android.R.color.white));
        markPaint.setStyle(Paint.Style.FILL);
        markPaint.setStrokeWidth(15f);

        textPaint = new Paint();
        textPaint.setColor(ContextCompat.getColor(context, android.R.color.white));
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(60f);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        cx = getWidth() / 2f;
        cy = getHeight() / 2f;

        bitmapControl = BitmapFactory.decodeResource(getResources(), R.drawable.icon_knob);

        matrix.postTranslate((getWidth() - bitmapControl.getWidth()) / 2,
                (getHeight() - bitmapControl.getHeight()) / 2);


    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < 360; i += 30) {

            if (i == 180) {
                continue;
            }

            float angle = (float) Math.toRadians(i);

            float startX = (float) (cx + 245 * Math.sin(angle));
            float startY = (float) (cy - 245 * Math.cos(angle));

            float stopX = (float) (cx + (245 - 50) * Math.sin(angle));
            float stopY = (float) (cy - (245 - 50) * Math.cos(angle));

            if (i == 0 || i == 150 || i == 210) {
                canvas.drawLine(startX, startY, stopX, stopY, markPaint);
            } else {
                canvas.drawCircle(startX, startY, radius, markPaint);
            }

        }

        canvas.drawBitmap(bitmapControl, matrix, null);

        canvas.drawText(bandName, cx, getWidth(), textPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startAngle = getAngle(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                float currentAngle = getAngle(event.getX(), event.getY());
                rotateBand(startAngle - currentAngle, currentAngle);
                startAngle = currentAngle;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        postInvalidate();
        return true;
    }

    private void rotateBand(float degrees, float currentAngle) {
        matrix.postRotate(degrees, cx, cy);

        float value = (currentAngle * (100f / 360));

        if (listener != null) {
            listener.onBandLevelChanged(band, (int) value);
        }
    }


    private float getAngle(float xTouch, float yTouch) {
        float x = xTouch - cx;
        float y = getHeight() - yTouch - cy;

        switch (getQuadrant(x, y)) {
            case 1:
                return (float) (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
            case 2:
                return (float) (180 - Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
            case 3:
                return (float) (180 + (-1 * Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI));
            case 4:
                return (float) (360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
            default:
                return 0;
        }
    }

    private static int getQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
    }


    public void setOnBandLevelListener(OnBandLevelListener listener) {
        this.listener = listener;
    }

    public void setLevel(float level) {
        //TODO add rotate logic
    }
}
