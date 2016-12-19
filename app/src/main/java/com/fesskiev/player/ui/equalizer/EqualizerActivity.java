package com.fesskiev.player.ui.equalizer;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import com.fesskiev.player.R;
import com.fesskiev.player.analytics.AnalyticsActivity;

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
