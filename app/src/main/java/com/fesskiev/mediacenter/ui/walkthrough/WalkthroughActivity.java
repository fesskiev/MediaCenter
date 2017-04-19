package com.fesskiev.mediacenter.ui.walkthrough;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.utils.Utils;


public class WalkthroughActivity extends AnalyticsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough);
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, WalkthroughFragment.newInstance(),
                    WalkthroughFragment.class.getName());
            transaction.commit();
        }
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }

    @Override
    public void onBackPressed() {
        WalkthroughFragment walkthroughFragment =
                (WalkthroughFragment) getSupportFragmentManager().findFragmentByTag(WalkthroughFragment.class.getName());
        if(walkthroughFragment != null){
            FetchMediaFragment fetchMediaFragment = walkthroughFragment.getFetchMediaFragment();
            if(fetchMediaFragment != null) {
                if (fetchMediaFragment.getFetchMediaFilesManager().isFetchStart()) {
                    View view = findViewById(R.id.content);
                    if (view != null) {
                        Utils.showCustomSnackbar(view, getApplicationContext(),
                                getString(R.string.splash_snackbar_stop_fetch),
                                Snackbar.LENGTH_LONG)
                                .setAction(getString(R.string.snack_exit_action), v ->
                                        stopFetchMediaAndExit(fetchMediaFragment))
                                .show();
                    }
                }
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }

    }

    private void stopFetchMediaAndExit(FetchMediaFragment fetchMediaFragment) {
        fetchMediaFragment.stopFetchMedia();
        finish();
    }
}
