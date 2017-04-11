package com.fesskiev.mediacenter.ui.walkthrough;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;


public class WalkthroughActivity extends AnalyticsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, WalkthroughFragment.newInstance(),
                WalkthroughFragment.class.getName());
        transaction.commit();
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }
}
