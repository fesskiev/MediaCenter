package com.fesskiev.mediacenter.widgets.menu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;


public class ContextMenuManager extends RecyclerView.OnScrollListener implements View.OnAttachStateChangeListener {

    private static ContextMenuManager instance;

    private ContextMenu contextMenuView;

    private boolean isContextMenuDismissing;
    private boolean isContextMenuShowing;

    public static ContextMenuManager getInstance() {
        if (instance == null) {
            instance = new ContextMenuManager();
        }
        return instance;
    }

    private ContextMenuManager() {

    }

    public void toggleVideoContextMenu(View openingView,
                                       VideoContextMenu.OnVideoContextMenuListener listener) {
        if (contextMenuView == null) {
            if (!isContextMenuShowing) {
                isContextMenuShowing = true;

                contextMenuView = new VideoContextMenu(openingView.getContext());
                ((VideoContextMenu) contextMenuView).setOnVideoContextMenuListener(listener);

                contextMenuView.addOnAttachStateChangeListener(this);

                ((ViewGroup) openingView.getRootView().findViewById(android.R.id.content)).addView(contextMenuView);

                contextMenuView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        contextMenuView.getViewTreeObserver().removeOnPreDrawListener(this);
                        setupContextMenuInitialPosition(openingView);
                        performShowAnimation();
                        return false;
                    }
                });
            }
        } else {
            hideContextMenu();
        }
    }

    public void toggleAudioContextMenu(View openingView,
                                       AudioContextMenu.OnAudioContextMenuListener listener) {
        if (contextMenuView == null) {
            if (!isContextMenuShowing) {
                isContextMenuShowing = true;

                contextMenuView = new AudioContextMenu(openingView.getContext());
                ((AudioContextMenu) contextMenuView).setOnAudioContextMenuListener(listener);
                contextMenuView.addOnAttachStateChangeListener(this);

                ((ViewGroup) openingView.getRootView().findViewById(android.R.id.content)).addView(contextMenuView);

                contextMenuView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        contextMenuView.getViewTreeObserver().removeOnPreDrawListener(this);
                        setupContextMenuInitialPosition(openingView);
                        performShowAnimation();
                        return false;
                    }
                });
            }
        } else {
            hideContextMenu();
        }
    }


    private void setupContextMenuInitialPosition(View openingView) {
        final int[] openingViewLocation = new int[2];
        openingView.getLocationOnScreen(openingViewLocation);
        contextMenuView.setTranslationX(openingViewLocation[0]);
        contextMenuView.setTranslationY(openingViewLocation[1] - contextMenuView.getHeight());
    }

    private void performShowAnimation() {
        contextMenuView.setPivotX(contextMenuView.getWidth() / 2);
        contextMenuView.setPivotY(contextMenuView.getHeight());
        contextMenuView.setScaleX(0.1f);
        contextMenuView.setScaleY(0.1f);
        contextMenuView.animate()
                .scaleX(1f).scaleY(1f)
                .setDuration(150)
                .setInterpolator(new OvershootInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isContextMenuShowing = false;
                    }
                });
    }

    public void hideContextMenu() {
        if (!isContextMenuDismissing) {
            isContextMenuDismissing = true;
            performDismissAnimation();
        }
    }

    private void performDismissAnimation() {
        contextMenuView.setPivotX(contextMenuView.getWidth() / 2);
        contextMenuView.setPivotY(contextMenuView.getHeight());
        contextMenuView.animate()
                .scaleX(0.1f).scaleY(0.1f)
                .setDuration(150)
                .setInterpolator(new AccelerateInterpolator())
                .setStartDelay(100)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (contextMenuView != null) {
                            contextMenuView.dismiss();
                        }
                        isContextMenuDismissing = false;
                    }
                });
    }

    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (contextMenuView != null) {
            hideContextMenu();
            contextMenuView.setTranslationY(contextMenuView.getTranslationY() - dy);
        }
    }

    @Override
    public void onViewAttachedToWindow(View v) {

    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        contextMenuView = null;
    }

    public boolean isContextMenuShow() {
        return contextMenuView != null;
    }
}
