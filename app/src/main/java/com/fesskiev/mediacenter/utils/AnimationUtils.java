package com.fesskiev.mediacenter.utils;


import android.content.Context;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;

//TODO replace all animations here
public class AnimationUtils {

    private static final int DURATION_300 = 300;
    private static final int DURATION_FAST = 600;
    private static final int DURATION_MIDDLE = 1200;
    private static final int DURATION_SLOW = 1800;
    private static final int STARTUP_DELAY = 600;

    private static AnimationUtils INSTANCE;

    private Context context;
    private FastOutSlowInInterpolator fastOutSlowInInterpolator;
    private DecelerateInterpolator decelerateInterpolator;

    public static AnimationUtils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AnimationUtils();
        }
        return INSTANCE;
    }

    private AnimationUtils() {
        context = MediaApplication.getInstance().getApplicationContext();

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

    public void translate(View menu, float value) {
        menu.animate()
                .translationY(value)
                .setDuration(DURATION_300)
                .setInterpolator(fastOutSlowInInterpolator);
    }

    public void errorAnimation(View view) {
        view.startAnimation(android.view.animation.AnimationUtils.loadAnimation(context, R.anim.shake_error));
    }

    public void rotateAnimation(View view) {
        view.startAnimation(android.view.animation.AnimationUtils.loadAnimation(context, R.anim.rotate));
    }

}
