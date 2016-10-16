package com.fesskiev.player.widgets.eq;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.fesskiev.player.R;


public class BandControlView extends View implements RotationGestureDetector.OnRotationGestureListener {

    private Bitmap bitmapControl;
    private Matrix matrix;
    private int radius;
    private float cx;
    private float cy;
    private Paint markPaint;
    private RotationGestureDetector rotationGestureDetector;

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
        rotationGestureDetector = new RotationGestureDetector(this);
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
        rotationGestureDetector.onTouchEvent(event);
        return true;
    }

    private int count;

    @Override
    public void OnRotation(RotationGestureDetector rotationDetector) {
        Log.d("RotationGestureDetector", "Rotation: " + Float.toString(rotationDetector.getAngle()));
        count++;
        if (count == 25) {
            matrix.postRotate(rotationDetector.getAngle(), cx, cy);
            postInvalidate();
            count = 0;
        }

    }
}
