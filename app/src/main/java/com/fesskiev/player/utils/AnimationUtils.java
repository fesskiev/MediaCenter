package com.fesskiev.player.utils;


import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

//TODO replace all animations here
public class AnimationUtils {

    private static AnimationUtils INSTANCE;

    private FastOutSlowInInterpolator fastOutSlowInInterpolator;
    private DecelerateInterpolator decelerateInterpolator;

    public static AnimationUtils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AnimationUtils();
        }
        return INSTANCE;
    }

    private AnimationUtils() {
        fastOutSlowInInterpolator = new FastOutSlowInInterpolator();
        decelerateInterpolator = new DecelerateInterpolator(3.f);

    }

    public void animateToolbar(Toolbar toolbar) {
        View view = toolbar.getChildAt(0);
        if (view != null && view instanceof TextView) {
            TextView title = (TextView) view;
            title.setAlpha(0f);
            title.setScaleX(0.6f);
            title.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .setStartDelay(300)
                    .setDuration(900)
                    .setInterpolator(fastOutSlowInInterpolator);
        }
    }

}
