package com.fesskiev.mediacenter.widgets.guide;

import android.graphics.Color;
import android.view.Gravity;

public class Pointer {

    public int mGravity = Gravity.CENTER;
    public int mColor = Color.WHITE;

    public Pointer() {
        this(Gravity.CENTER, Color.parseColor("#FFFFFF"));
    }

    public Pointer(int gravity, int color) {
        this.mGravity = gravity;
        this.mColor = color;
    }

    /**
     * Set color
     * @param color
     * @return return Pointer instance for chaining purpose
     */
    public Pointer setColor(int color) {
        mColor = color;
        return this;
    }

    /**
     * Set gravity
     *
     * @param gravity
     * @return return Pointer instance for chaining purpose
     */
    public Pointer setGravity(int gravity) {
        mGravity = gravity;
        return this;
    }
}
