package com.fesskiev.mediacenter.ui.fetch;


import android.animation.Animator;
import android.app.Activity;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.AppAnimationUtils;
import com.fesskiev.mediacenter.widgets.fetch.FetchContentView;

public class FetchContentScreen {

    private Activity activity;
    private LinearLayout linearLayout;
    private FastOutSlowInInterpolator interpolator;
    private FetchContentView fetchContentView;

    public FetchContentScreen(Activity activity, AppAnimationUtils animationUtils) {
        this.activity = activity;
        this.interpolator = animationUtils.getFastOutSlowInInterpolator();

        linearLayout = new LinearLayout(activity.getApplicationContext());
        linearLayout.setBackgroundColor(activity.getResources().getColor(R.color.colorFabBackground));
        View view = LayoutInflater.from(activity).inflate(R.layout.search_content_layout, linearLayout,
                false);

        fetchContentView = view.findViewById(R.id.fetchContentView);

        linearLayout.addView(view);
    }

    public void showContentScreen() {
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        ViewGroup contentArea = activity.getWindow()
                .getDecorView().findViewById(android.R.id.content);
        int[] pos = new int[2];
        contentArea.getLocationOnScreen(pos);
        layoutParams.setMargins(0, -pos[1], 0, 0);

        contentArea.addView(linearLayout, layoutParams);

        linearLayout.setAlpha(0f);
        linearLayout.animate()
                .alpha(0.95f)
                .setDuration(1200)
                .setInterpolator(interpolator)
                .setListener(null);

        fetchContentView.setDefaultState(activity.getString(R.string.dialog_fetch_folder_name),
                activity.getString(R.string.dialog_fetch_file_name));
        fetchContentView.setProgress(0f);
    }

    public void hideContentScreen() {
        linearLayout.animate()
                .alpha(0f)
                .setDuration(800)
                .setInterpolator(interpolator)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        ((ViewGroup) activity.getWindow().getDecorView()
                                .findViewById(android.R.id.content)).removeView(linearLayout);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

    }

    public void prepareFetch() {
        fetchContentView.fetchStart();
    }

    public void finishFetch() {
        fetchContentView.fetchFinish();
    }

    public void setProgress(float percent) {
        fetchContentView.setProgress(percent);
    }

    public void setFolderName(String folderName) {
        fetchContentView.setFolderName(folderName);
    }

    public void setFileName(String fileName) {
        fetchContentView.setFileName(fileName);
    }
}
