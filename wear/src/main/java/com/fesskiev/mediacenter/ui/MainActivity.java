package com.fesskiev.mediacenter.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.drawer.WearableDrawerLayout;
import android.support.wearable.view.drawer.WearableNavigationDrawer;
import android.view.Gravity;
import android.view.ViewTreeObserver;

import com.fesskiev.mediacenter.R;


public class MainActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WearableDrawerLayout wearableDrawerLayout = (WearableDrawerLayout) findViewById(R.id.drawerLayout);
        WearableNavigationDrawer wearableNavigationDrawer = (WearableNavigationDrawer) findViewById(R.id.navigationDrawer);
        wearableNavigationDrawer.setAdapter(new NavigationAdapter());

        ViewTreeObserver observer = wearableDrawerLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                wearableDrawerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                wearableDrawerLayout.peekDrawer(Gravity.TOP);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        addControlFragment();
    }

    private final class NavigationAdapter extends WearableNavigationDrawer.WearableNavigationDrawerAdapter {

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public void onItemSelected(int position) {
            switch (position) {
                case 0:
                    addControlFragment();
                    break;
                case 1:
                    addTrackListFragment();
                    break;
            }
        }

        @Override
        public String getItemText(int pos) {
            switch (pos) {
                case 0:
                    return getString(R.string.drawer_item_control);
                case 1:
                    return getString(R.string.drawer_item_tracklist);
            }
            return "";
        }

        @Override
        public Drawable getItemDrawable(int pos) {
            switch (pos) {
                case 0:
                    return ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_control);
                case 1:
                    return ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_tracklist);
            }
            return null;
        }
    }

    private void addControlFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        hideVisibleFragment(transaction);

        ControlFragment controlFragment = (ControlFragment) getFragmentManager().
                findFragmentByTag(ControlFragment.class.getName());
        if (controlFragment == null) {
            transaction.add(R.id.content, ControlFragment.newInstance(),
                    ControlFragment.class.getName());
            transaction.addToBackStack(ControlFragment.class.getName());
        } else {
            transaction.show(controlFragment);
        }
        transaction.commitAllowingStateLoss();
    }

    private void addTrackListFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        hideVisibleFragment(transaction);

        TrackListFragment trackListFragment = (TrackListFragment) getFragmentManager().
                findFragmentByTag(TrackListFragment.class.getName());
        if (trackListFragment == null) {
            transaction.add(R.id.content, TrackListFragment.newInstance(),
                    TrackListFragment.class.getName());
            transaction.addToBackStack(TrackListFragment.class.getName());
        } else {
            transaction.show(trackListFragment);
        }
        transaction.commitAllowingStateLoss();
    }

    private void hideVisibleFragment(FragmentTransaction transaction) {
        FragmentManager fragmentManager = getFragmentManager();
        for (int entry = 0; entry < fragmentManager.getBackStackEntryCount(); entry++) {
            Fragment fragment =
                    fragmentManager.findFragmentByTag(fragmentManager.getBackStackEntryAt(entry).getName());
            if (fragment != null && fragment.isAdded() && fragment.isVisible()) {
                transaction.hide(fragment);
                break;
            }
        }
    }
}
