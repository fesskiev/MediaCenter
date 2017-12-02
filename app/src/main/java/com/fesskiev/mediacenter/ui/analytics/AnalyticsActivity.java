package com.fesskiev.mediacenter.ui.analytics;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public abstract class AnalyticsActivity extends AppCompatActivity {

    private static final String TAG = AnalyticsActivity.class.getName();

    public abstract String getActivityName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
