package com.fesskiev.mediacenter.widgets.reverb;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.Utils;

public class ReverbControlView extends View {

    public interface OnAttachStateListener {

        void onAttachReverbControlView(ReverbControlView view);
    }

    public interface OnReverbControlListener {

        void onReverbControlChanged(String name, float level, float[] values);
    }

    private OnAttachStateListener attachStateListener;
    private OnReverbControlListener controlListener;
    private Bitmap bitmapControl;
    private Matrix matrix;
    private Paint markPaint;
    private Paint namePaint;
    private String name;
    private float cx;
    private float cy;
    private float markRadius;
    private float markSize;
    private int radius;
    private int textPadding;

    private float startAngle;
    private float[] values;

    public ReverbControlView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public ReverbControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public ReverbControlView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ReverbControlView, defStyle, 0);

        name = a.getString(R.styleable.ReverbControlView_name);

        a.recycle();


        markRadius = Utils.dipToPixels(context, 70);
        markSize = Utils.dipToPixels(context, 30);

        textPadding = (int) Utils.dipToPixels(context, 4);

        float markStrokeWidth = Utils.dipToPixels(context, 5);
        float nameStrokeWidth = Utils.dipToPixels(context, 13);

        radius = (int) Utils.dipToPixels(context, 3);
        matrix = new Matrix();

        values = new float[9];

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
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        cx = getWidth() / 2f;
        cy = getHeight() / 2f;

        bitmapControl = BitmapFactory.decodeResource(getResources(), R.drawable.icon_knob);

        matrix.postTranslate((getWidth() - bitmapControl.getWidth()) / 2,
                (getHeight() - bitmapControl.getHeight()) / 2);

        if (attachStateListener != null) {
            attachStateListener.onAttachReverbControlView(this);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < 360; i += 30) {

            float angle = (float) Math.toRadians(i);

            float startX = (float) (cx + markRadius * Math.sin(angle));
            float startY = (float) (cy - markRadius * Math.cos(angle));

            float stopX = (float) (cx + (markRadius - markSize) * Math.sin(angle));
            float stopY = (float) (cy - (markRadius - markSize) * Math.cos(angle));

            if (i == 0) {
                canvas.drawLine(startX, startY, stopX, stopY, markPaint);
            } else {
                canvas.drawCircle(startX, startY, radius, markPaint);
            }

        }

        canvas.drawBitmap(bitmapControl, matrix, null);

        canvas.drawText(name, cx, getWidth() - textPadding, namePaint);

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

        float angleFix = getAngleFix(currentAngle);

        float level = 100 - (angleFix * (100f / 360));

        if (controlListener != null) {
            matrix.getValues(values);
            controlListener.onReverbControlChanged(name, level, values);
        }

    }

    private float getAngleFix(float ag) {
        float angle = ag;
        angle -= 90;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
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

    private static int getQuadrant(float x, float y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
    }

    public void setLevel(float[] values) {
        if (values != null) {
            matrix.setValues(values);
            postInvalidate();
        }
    }

    public void setAttachStateListener(OnAttachStateListener l) {
        this.attachStateListener = l;
    }

    public void setControlListener(OnReverbControlListener l) {
        this.controlListener = l;
    }

    public String getName() {
        return name;
    }
}
