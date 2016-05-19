package com.fesskiev.player.analytics;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.fesskiev.player.MediaApplication;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public abstract class AnalyticsActivity extends AppCompatActivity {

    private static final String TAG = AnalyticsActivity.class.getName();

    public abstract String getActivityName();

    protected Tracker tracker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tracker = MediaApplication.getInstance().getDefaultTracker();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tracker != null) {
            Log.i(TAG, "Setting screen name: " + getActivityName());
            tracker.setScreenName("Image~" + getActivityName());
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }
}
