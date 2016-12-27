package com.fesskiev.mediacenter.widgets.utils;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class DisabledScrollView extends NestedScrollView {

    private boolean enableScrolling = true;


    public DisabledScrollView(Context context) {
        super(context);
    }

    public DisabledScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DisabledScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (enableScrolling) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (enableScrolling) {
            return super.onTouchEvent(ev);
        } else {
            return false;
        }
    }

    public boolean isEnableScrolling() {
        return enableScrolling;
    }

    public void setEnableScrolling(boolean enableScrolling) {
        this.enableScrolling = enableScrolling;
    }
}
