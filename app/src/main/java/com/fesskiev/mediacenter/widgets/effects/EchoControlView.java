package com.fesskiev.mediacenter.widgets.effects;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.fesskiev.mediacenter.utils.Utils;


public class EchoControlView extends DialerView {

    public interface OnEchoControlListener {

        void onEchoControlChanged(float level, float[] values);
    }

    private OnEchoControlListener listener;
    private Paint markPaint;
    private float markRadius;
    private float markSize;
    private int radius;


    public EchoControlView(Context context) {
        super(context);
        init(context);
    }

    public EchoControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EchoControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        markRadius = Utils.dipToPixels(context, 70);
        markSize = Utils.dipToPixels(context, 30);

        float markStrokeWidth = Utils.dipToPixels(context, 5);

        radius = (int) Utils.dipToPixels(context, 3);

        markPaint = new Paint();
        markPaint.setColor(ContextCompat.getColor(context, android.R.color.white));
        markPaint.setStyle(Paint.Style.FILL);
        markPaint.setStrokeWidth(markStrokeWidth);
    }


    @Override
    protected void onDraw(Canvas canvas) {

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

        super.onDraw(canvas);
    }

    @Override
    public void rotateDialer(float currentAngle, float[] values) {

        float level = 100 - (currentAngle * (100f / 360));

        if (listener != null) {
            listener.onEchoControlChanged(level, values);
        }
    }

    public void setOnEchoControlListener(OnEchoControlListener l) {
        this.listener = l;
    }
}
