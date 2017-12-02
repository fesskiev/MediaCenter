package com.fesskiev.mediacenter.ui.about;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.ViewGroup;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.ui.analytics.AnalyticsActivity;

public class AboutActivity extends AnalyticsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.activity_window_height, typedValue, true);
        float scaleValue = typedValue.getFloat();

        int height = (int) (getResources().getDisplayMetrics().heightPixels * scaleValue);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.title_about_activity));
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, AboutFragment.newInstance(),
                    AboutFragment.class.getName());
            transaction.commit();
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
