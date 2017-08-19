package com.fesskiev.mediacenter.widgets.effects;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.Utils;


public class EchoControlView extends DialerView {

    public interface OnEchoControlListener {

        void onEchoControlChanged(float level, float[] values);
    }

    private OnEchoControlListener listener;
    private Paint markPaint;
    private Paint rangePaint;
    private float markRadius;
    private float markSize;
    private float rangeTextX;
    private float rangeTextY;
    private float rangeTextX1;
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

        rangeTextX = Utils.dipToPixels(context, 35);
        rangeTextY = Utils.dipToPixels(context, 30);
        rangeTextX1 = Utils.dipToPixels(context, 144);

        float markStrokeWidth = Utils.dipToPixels(context, 5);
        float rangeStrokeWidth = Utils.dipToPixels(context, 14);

        radius = (int) Utils.dipToPixels(context, 3);

        markPaint = new Paint();
        markPaint.setColor(ContextCompat.getColor(context, android.R.color.white));
        markPaint.setStyle(Paint.Style.FILL);
        markPaint.setStrokeWidth(markStrokeWidth);

        Typeface tf = ResourcesCompat.getFont(context, R.font.ubuntu);

        rangePaint = new Paint();
        rangePaint.setColor(ContextCompat.getColor(context, android.R.color.white));
        rangePaint.setStyle(Paint.Style.FILL);
        rangePaint.setTextSize(rangeStrokeWidth);
        rangePaint.setAntiAlias(true);
        rangePaint.setTypeface(tf);
        rangePaint.setTextAlign(Paint.Align.CENTER);
    }


    @Override
    protected void onDraw(Canvas canvas) {

        for (int i = 30; i < 360; i += 30) {

            float angle = (float) Math.toRadians(i);

            float startX = (float) (cx + markRadius * Math.sin(angle));
            float startY = (float) (cy - markRadius * Math.cos(angle));

            float stopX = (float) (cx + (markRadius - markSize) * Math.sin(angle));
            float stopY = (float) (cy - (markRadius - markSize) * Math.cos(angle));

            if (i == 30 || i == 330) {
                canvas.drawLine(startX, startY, stopX, stopY, markPaint);
            } else {
                canvas.drawCircle(startX, startY, radius, markPaint);
            }

        }

        canvas.drawText("max", rangeTextX, rangeTextY, rangePaint);

        canvas.drawText("min", rangeTextX1, rangeTextY, rangePaint);

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
