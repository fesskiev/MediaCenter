package com.fesskiev.mediacenter.widgets.effects;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.fesskiev.mediacenter.R;

public abstract class DialerView extends View {

    public interface OnDialerViewListener {

        void onAttachDealerView(DialerView view);
    }

    public abstract void rotateDialer(float currentAngle, float[] values);

    private OnDialerViewListener listener;

    protected float cx;
    protected float cy;
    protected Matrix matrix;
    private Bitmap bitmapControl;
    private float startAngle;
    private float[] values;

    public DialerView(Context context) {
        super(context);
        init();
    }

    public DialerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DialerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        matrix = new Matrix();
        values = new float[9];

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        bitmapControl = BitmapFactory.decodeResource(getResources(), R.drawable.icon_knob);

        matrix.postTranslate((getWidth() - bitmapControl.getWidth()) / 2,
                (getHeight() - bitmapControl.getHeight()) / 2);

        cx = getWidth() / 2f;
        cy = getHeight() / 2f;

        if (listener != null) {
            listener.onAttachDealerView(this);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(bitmapControl, matrix, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);

                startAngle = getAngle(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                float currentAngle = getAngle(event.getX(), event.getY());

                matrix.postRotate(startAngle - currentAngle, cx, cy);
                matrix.getValues(values);

                float angleFix = getAngleFix(currentAngle);

                rotateDialer(angleFix, values);

                startAngle = currentAngle;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        postInvalidate();
        return true;
    }

    public float getAngleFix(float ag) {
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

    public void setLevel(float[] values, boolean customStartPos) {
        if (values != null) {
            matrix.setValues(values);
            postInvalidate();
        } else if (customStartPos) {
            matrix.postRotate(180, cx, cy);
        }
    }

    public void setOnDealerViewListener(OnDialerViewListener l) {
        this.listener = l;
    }
}
