package com.fesskiev.mediacenter.widgets.pager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class DisableSwipingViewPager extends ViewPager {

    private boolean enableSwipe;

    public DisableSwipingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (enableSwipe) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (enableSwipe) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    public void setSwipingEnabled(boolean enabled) {
        enableSwipe = enabled;
    }
}
