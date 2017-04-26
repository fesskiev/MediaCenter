package com.fesskiev.mediacenter.ui.processing;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.converter.AudioConverterHelper;
import com.fesskiev.mediacenter.widgets.InkPageIndicator;

public class ProcessingActivity extends AnalyticsActivity {

    private Fragment[] fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.title_processing_activity));
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fragments = new Fragment[]{
                ConverterFragment.newInstance(),
                CutAudioFragment.newInstance(),
        };

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(2);

        ProcessingPagerAdapter adapter = new ProcessingPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        InkPageIndicator pageIndicator = (InkPageIndicator) findViewById(R.id.indicator);
        pageIndicator.setViewPager(viewPager);
    }

    @Override
    public void onBackPressed() {
        exitProcessing();
    }

    @Override
    public boolean onSupportNavigateUp() {
        exitProcessing();
        return true;
    }

    private void exitProcessing() {
        AudioConverterHelper audioConverter = AudioConverterHelper.getInstance();
        if (audioConverter.isCommandRunning()) {
            Utils.showCustomSnackbar(findViewById(R.id.processingRoot), getApplicationContext(),
                    "Command is running! Kill process", Snackbar.LENGTH_SHORT)
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onShown(Snackbar transientBottomBar) {
                            super.onShown(transientBottomBar);
                            audioConverter.killRunningProcesses();
                        }

                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            finish();
                        }
                    }).show();
        } else {
            finish();
        }
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }

    private class ProcessingPagerAdapter extends FragmentStatePagerAdapter {

        ProcessingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

    }
}

