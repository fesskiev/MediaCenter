package com.fesskiev.mediacenter.ui.effects;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;

public class EffectsActivity extends AnalyticsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effects);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.title_effects));
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.action_eq:
                            addEQFragment();
                            break;
                        case R.id.action_reverb:
                            addReverbFragment();
                            break;
                        case R.id.action_other:
                            addOtherEffectsFragment();
                            break;
                    }
                    return true;
                });

        addEQFragment();
    }

    private void addOtherEffectsFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        hideVisibleFragment(transaction);

        OtherEffectsFragment otherEffectsFragment = (OtherEffectsFragment) getSupportFragmentManager().
                findFragmentByTag(OtherEffectsFragment.class.getName());
        if (otherEffectsFragment == null) {
            transaction.add(R.id.content, OtherEffectsFragment.newInstance(),
                    OtherEffectsFragment.class.getName());
            transaction.addToBackStack(OtherEffectsFragment.class.getName());
        } else {
            transaction.show(otherEffectsFragment);
        }
        transaction.commit();
    }

    private void addReverbFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        hideVisibleFragment(transaction);

        ReverbFragment reverbFragment = (ReverbFragment) getSupportFragmentManager().
                findFragmentByTag(ReverbFragment.class.getName());
        if (reverbFragment == null) {
            transaction.add(R.id.content, ReverbFragment.newInstance(),
                    ReverbFragment.class.getName());
            transaction.addToBackStack(ReverbFragment.class.getName());
        } else {
            transaction.show(reverbFragment);
        }
        transaction.commit();
    }


    private void addEQFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        hideVisibleFragment(transaction);

        EqualizerFragment equalizerFragment = (EqualizerFragment) getSupportFragmentManager().
                findFragmentByTag(EqualizerFragment.class.getName());
        if (equalizerFragment == null) {
            transaction.add(R.id.content, EqualizerFragment.newInstance(),
                    EqualizerFragment.class.getName());
            transaction.addToBackStack(EqualizerFragment.class.getName());
        } else {
            transaction.show(equalizerFragment);
        }
        transaction.commit();
    }

    private void hideVisibleFragment(FragmentTransaction transaction) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        for (int entry = 0; entry < fragmentManager.getBackStackEntryCount(); entry++) {
            Fragment fragment =
                    fragmentManager.findFragmentByTag(fragmentManager.getBackStackEntryAt(entry).getName());
            if (fragment != null && fragment.isAdded() && fragment.isVisible()) {
                transaction.hide(fragment);
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }
}
