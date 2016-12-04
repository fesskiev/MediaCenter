package com.fesskiev.player.widgets.eq;

import android.content.Context;
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

    public interface OnBandListener {

    }

    private Bitmap bitmapControl;
    private Matrix matrix;
    private int radius;
    private float cx;
    private float cy;
    private Paint markPaint;

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
        radius = 8;
        matrix = new Matrix();

        markPaint = new Paint();
        markPaint.setColor(ContextCompat.getColor(context, android.R.color.white));
        markPaint.setStyle(Paint.Style.FILL);
        markPaint.setStrokeWidth(15f);

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
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                setEQBandValue(x, y);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        postInvalidate();
        return true;
    }

    private void setEQBandValue(float x, float y) {
        float angle = getAngle(x, y);
        matrix.postRotate(angle, cx, cy);


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
}
