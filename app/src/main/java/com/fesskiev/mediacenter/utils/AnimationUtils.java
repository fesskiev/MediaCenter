package com.fesskiev.mediacenter.utils;


import android.animation.Animator;
import android.content.Context;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewAnimationUtils;
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

    public static AnimationUtils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AnimationUtils();
        }
        return INSTANCE;
    }

    private AnimationUtils() {
        context = MediaApplication.getInstance().getApplicationContext();

        fastOutSlowInInterpolator = new FastOutSlowInInterpolator();

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

    public void scaleToSmallViews(View... menu) {
        for (View view : menu) {
            view.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(DURATION_300)
                    .setInterpolator(fastOutSlowInInterpolator);
        }
    }

    public void scaleToOriginalView(View view) {
        view.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(DURATION_300)
                .setInterpolator(fastOutSlowInInterpolator);
    }

    public void errorAnimation(View view) {
        view.startAnimation(android.view.animation.AnimationUtils.loadAnimation(context, R.anim.shake_error));
    }

    public void rotateAnimation(View view) {
        view.startAnimation(android.view.animation.AnimationUtils.loadAnimation(context, R.anim.rotate));
    }

    public void createCircularRevealAnim(View view) {
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right,
                                       int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

                if (left == 0 && top == 0 && right == 0 && bottom == 0) {
                    return;
                }
                int cx = view.getWidth() / 2;
                int cy = view.getHeight() / 2;
                int finalRadius = Math.max(view.getWidth(), view.getHeight());
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
                anim.setDuration(DURATION_MIDDLE);
                anim.start();
                view.setVisibility(View.VISIBLE);
                view.removeOnLayoutChangeListener(this);
            }
        });
    }

    public void translateMenu(View menu, float value) {
        menu.animate()
                .translationY(value)
                .setDuration(DURATION_300)
                .setInterpolator(fastOutSlowInInterpolator);
    }


    public FastOutSlowInInterpolator getFastOutSlowInInterpolator() {
        return fastOutSlowInInterpolator;
    }
}
