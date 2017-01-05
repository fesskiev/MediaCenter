package com.fesskiev.mediacenter.widgets.effects;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.effects.DealerView;

public class ReverbControlView extends DealerView {

    public interface OnReverbControlListener {

        void onReverbControlChanged(String name, float level, float[] values);
    }

    private OnReverbControlListener controlListener;
    private Paint markPaint;
    private Paint namePaint;
    private String name;
    private float markRadius;
    private float markSize;
    private int radius;
    private int textPadding;

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
        canvas.drawText(name, cx, getWidth() - textPadding, namePaint);

        super.onDraw(canvas);
    }

    @Override
    public void rotateBand(float currentAngle, float[] values) {

        float angleFix = getAngleFix(currentAngle);

        float level = 100 - (angleFix * (100f / 360));

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
