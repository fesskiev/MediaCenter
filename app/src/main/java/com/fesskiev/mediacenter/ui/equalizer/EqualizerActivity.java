package com.fesskiev.mediacenter.ui.equalizer;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;

public class EqualizerActivity extends AnalyticsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);
        if (savedInstanceState == null) {

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, EqualizerFragment.newInstance(),
                    EqualizerFragment.class.getName());
            transaction.commit();

        }

    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }
}
