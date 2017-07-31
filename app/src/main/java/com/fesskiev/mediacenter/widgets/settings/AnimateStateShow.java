package com.fesskiev.mediacenter.widgets.settings;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.fesskiev.mediacenter.utils.AppAnimationUtils;

public class AnimateStateShow extends FrameLayout {

    private boolean isShow;

    public AnimateStateShow(Context context) {
        super(context);
    }

    public AnimateStateShow(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimateStateShow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void toggleWithAnimate() {
        isShow = !isShow;
        if (isShow) {
            setVisibility(View.VISIBLE);
            AppAnimationUtils.getInstance().createCircularRevealAnim(this);
        } else {
            setVisibility(View.GONE);
        }
    }

}
