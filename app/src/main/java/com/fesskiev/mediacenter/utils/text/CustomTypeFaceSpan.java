package com.fesskiev.mediacenter.utils.text;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;


public class CustomTypeFaceSpan extends TypefaceSpan {

    private final Typeface typeface;

    public CustomTypeFaceSpan(Typeface typeface) {
        super("");
        this.typeface = typeface;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        applyTypeFace(ds, typeface);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        applyTypeFace(paint, typeface);
    }

    private void applyTypeFace(TextPaint paint, Typeface typeface) {
        paint.setTypeface(typeface);
    }

}
