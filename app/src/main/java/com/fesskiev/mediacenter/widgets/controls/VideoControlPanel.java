package com.fesskiev.mediacenter.widgets.controls;


import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.Utils;

public class VideoControlPanel extends FrameLayout {


    private boolean show;
    private boolean animate;
    private int height;

    public VideoControlPanel(Context context) {
        super(context);
        init(context);
    }

    public VideoControlPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoControlPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.video_control_panel, this, true);

        show = true;

        getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        height = VideoControlPanel.this.getHeight();
                        hide(100);

                        getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                });

    }

    public void hide(int duration) {
        if (!animate) {
            animate = true;
            ViewCompat.animate(this)
                    .translationY(-height)
                    .setDuration(duration)
                    .setInterpolator(new DecelerateInterpolator(1.2f))
                    .setListener(new ViewPropertyAnimatorListener() {
                        @Override
                        public void onAnimationStart(View view) {
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            show = false;
                            animate = false;
                        }

                        @Override
                        public void onAnimationCancel(View view) {

                        }
                    }).start();
        }

    }

    public void show() {
        if (!animate) {
            animate = true;
            ViewCompat.animate(this)
                    .translationY(0)
                    .setDuration(800)
                    .setInterpolator(new DecelerateInterpolator(1.2f))
                    .setListener(new ViewPropertyAnimatorListener() {
                        @Override
                        public void onAnimationStart(View view) {

                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            show = true;
                            animate = false;
                        }

                        @Override
                        public void onAnimationCancel(View view) {

                        }
                    }).start();
        }
    }

    public void toggle() {
        if (show) {
            hide(800);
        } else {
            show();
        }
    }


}
