package com.fesskiev.mediacenter.widgets.settings;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.fesskiev.mediacenter.utils.AppAnimationUtils;

public class AnimateStateShow extends FrameLayout {

    private boolean isShow;
    private AppAnimationUtils animationUtils;

    public AnimateStateShow(Context context) {
        super(context);
        init(context);
    }

    public AnimateStateShow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AnimateStateShow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        animationUtils = new AppAnimationUtils(context);
    }

    public void toggleWithAnimate() {
        isShow = !isShow;
        if (isShow) {
            setVisibility(View.VISIBLE);
            animationUtils.createCircularRevealAnim(this);
        } else {
            setVisibility(View.GONE);
        }
    }

}
