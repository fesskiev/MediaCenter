package com.fesskiev.mediacenter.ui.cut;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;


public class CutMediaActivity extends AnalyticsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cut);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.title_cut_activity));
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }
}
