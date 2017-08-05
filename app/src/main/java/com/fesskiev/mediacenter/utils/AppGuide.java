package com.fesskiev.mediacenter.utils;


import android.app.Activity;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.fesskiev.mediacenter.widgets.guide.Overlay;
import com.fesskiev.mediacenter.widgets.guide.ToolTip;
import com.fesskiev.mediacenter.widgets.guide.TourGuide;

import java.util.ArrayList;
import java.util.List;

public class AppGuide {

    public interface OnAppGuideListener {
        void next(int count);

        void allViewWatched();
    }

    private Activity activity;

    private TourGuide tourGuide;
    private AlphaAnimation enterAnimation;
    private AlphaAnimation exitAnimation;

    private OnAppGuideListener listener;
    private int count;
    private int viewSize;

    private List<View> watchedViews;

    public AppGuide(Activity activity, int viewSize) {
        this.activity = activity;
        this.viewSize = viewSize;
        watchedViews = new ArrayList<>();

        enterAnimation = new AlphaAnimation(0f, 1f);
        enterAnimation.setDuration(1000);
        enterAnimation.setFillAfter(true);

        exitAnimation = new AlphaAnimation(1f, 0f);
        exitAnimation.setDuration(500);
        exitAnimation.setFillAfter(true);

    }

    public void makeGuide(View view, String title, String desc) {
        view.setTag(title);
        if (!isWatchedView(view)) {
            tourGuide = TourGuide.init(activity)
                    .setToolTip(new ToolTip().setTitle(title).setDescription(desc))
                    .setOverlay(new Overlay()
                            .setEnterAnimation(enterAnimation)
                            .setExitAnimation(exitAnimation)
                            .setOnClickListener(this::processClick))
                    .playOn(view);
        }
    }

    private boolean isWatchedView(View view) {
        for (View watched : watchedViews) {
            if (watched.getTag().equals(view.getTag())) {
                return true;
            }
        }
        return false;
    }

    private void processClick(View v) {
        if (tourGuide != null) {
            tourGuide.cleanUp();

            watchedViews.add(tourGuide.getHighlightedView());
            if (viewSize == watchedViews.size()) {
                allViewWatchedChanged();
                return;
            }
            count++;
            onNextChanged();
        }
    }

    private void allViewWatchedChanged() {
        if (listener != null) {
            listener.allViewWatched();
        }
    }

    private void onNextChanged() {
        if (listener != null) {
            listener.next(count);
        }
    }

    public void clear() {
        if (tourGuide != null) {
            tourGuide.cleanUp();
        }
    }

    public void OnAppGuideListener(OnAppGuideListener l) {
        this.listener = l;
    }

}
