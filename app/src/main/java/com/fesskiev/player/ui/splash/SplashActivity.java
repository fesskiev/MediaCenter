package com.fesskiev.player.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.fesskiev.player.R;
import com.fesskiev.player.ui.MainActivity;
import com.fesskiev.player.widgets.MaterialProgressBar;

public class SplashActivity extends AppCompatActivity {

    public static final int STARTUP_DELAY = 300;
    public static final int ANIM_ITEM_DURATION = 1000;
    public static final int ITEM_DELAY = 300;

    private MaterialProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        if (!hasFocus) {
            return;
        }

        animate();

        super.onWindowFocusChanged(hasFocus);
    }

    private void animate() {
        ImageView appLogo = (ImageView) findViewById(R.id.appLogo);
        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        progressBar = (MaterialProgressBar) findViewById(R.id.progressBar);

        ViewCompat.animate(progressBar)
                .translationY(-200)
                .setStartDelay(STARTUP_DELAY)
                .setDuration(ANIM_ITEM_DURATION).setInterpolator(
                new DecelerateInterpolator(1.2f))
                .start();

        ViewCompat.animate(appLogo)
                .translationY(-250)
                .setStartDelay(STARTUP_DELAY + 500)
                .setDuration(ANIM_ITEM_DURATION).setInterpolator(
                new DecelerateInterpolator(1.2f))
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {

                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        loadMediaFiles();
                    }

                    @Override
                    public void onAnimationCancel(View view) {

                    }
                })
                .start();

        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            ViewCompat.animate(v)
                    .translationY(50).alpha(1)
                    .setStartDelay((ITEM_DELAY * i) + 500)
                    .setDuration(1000)
                    .start();
        }
    }

    private void loadMediaFiles() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }


    private void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

}
