package com.fesskiev.mediacenter.widgets.effects;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.Utils;

public class ReverbControlView extends DialerView {

    public interface OnReverbControlListener {

        void onReverbControlChanged(String name, float level, float[] values);
    }

    private OnReverbControlListener controlListener;
    private Paint markPaint;
    private Paint rangePaint;
    private Paint namePaint;
    private String name;
    private float markRadius;
    private float markSize;
    private int radius;
    private int textPadding;
    private float rangeTextX;
    private float rangeTextY;
    private float rangeTextX1;

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

        name = a.getString(R.styleable.ReverbControlView_reverbName);

        a.recycle();


        markRadius = Utils.dipToPixels(context, 70);
        markSize = Utils.dipToPixels(context, 30);

        rangeTextX = Utils.dipToPixels(context, 35);
        rangeTextY = Utils.dipToPixels(context, 30);
        rangeTextX1 = Utils.dipToPixels(context, 144);

        textPadding = (int) Utils.dipToPixels(context, 4);

        float markStrokeWidth = Utils.dipToPixels(context, 5);
        float nameStrokeWidth = Utils.dipToPixels(context, 13);
        float rangeStrokeWidth = Utils.dipToPixels(context, 14);

        radius = (int) Utils.dipToPixels(context, 3);

        markPaint = new Paint();
        markPaint.setColor(ContextCompat.getColor(context, android.R.color.white));
        markPaint.setStyle(Paint.Style.FILL);
        markPaint.setStrokeWidth(markStrokeWidth);

        Typeface tf = ResourcesCompat.getFont(context, R.font.ubuntu);

        namePaint = new Paint();
        namePaint.setColor(ContextCompat.getColor(context, android.R.color.white));
        namePaint.setStyle(Paint.Style.FILL);
        namePaint.setTextSize(nameStrokeWidth);
        namePaint.setAntiAlias(true);
        namePaint.setTypeface(tf);
        namePaint.setTextAlign(Paint.Align.CENTER);

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
        canvas.drawText(name, cx, getWidth() - textPadding, namePaint);

        canvas.drawText("max", rangeTextX, rangeTextY, rangePaint);

        canvas.drawText("min", rangeTextX1, rangeTextY, rangePaint);

        super.onDraw(canvas);
    }

    @Override
    public void rotateDialer(float currentAngle, float[] values) {

        float level = 100 - (currentAngle * (100f / 360));

        if (controlListener != null) {
            controlListener.onReverbControlChanged(name, level, values);
        }
    }

    public void setControlListener(OnReverbControlListener l) {
        this.controlListener = l;
    }

    public String getName() {
        return name;
    }
}
