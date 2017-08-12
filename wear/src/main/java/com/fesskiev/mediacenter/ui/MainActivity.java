package com.fesskiev.mediacenter.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wear.widget.drawer.WearableNavigationDrawerView;
import android.support.wearable.activity.WearableActivity;

import com.fesskiev.common.data.MapAudioFile;
import com.fesskiev.mediacenter.R;

import java.util.ArrayList;

import static com.fesskiev.mediacenter.service.DataLayerListenerService.ACTION_TRACK_LIST;
import static com.fesskiev.mediacenter.service.DataLayerListenerService.EXTRA_TRACK_LIST;


public class MainActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WearableNavigationDrawerView wearableNavigationDrawer = findViewById(R.id.navigationDrawer);
        wearableNavigationDrawer.getController().peekDrawer();

        wearableNavigationDrawer.setAdapter(new NavigationAdapter());
        wearableNavigationDrawer.addOnItemSelectedListener(pos -> {
            switch (pos) {
                case 0:
                    addControlFragment();
                    break;
                case 1:
                    addTrackListFragment();
                    break;
            }

        });
    }

    @Override
    public void onStart() {
        super.onStart();
        registerTrackListReceiver();
        addControlFragment();

    }


    @Override
    public void onPause() {
        super.onPause();
        unregisterTrackListReceiver();
    }


    private void registerTrackListReceiver() {
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                receiver, new IntentFilter(ACTION_TRACK_LIST));
    }

    private void unregisterTrackListReceiver() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<MapAudioFile> audioFiles = intent.getParcelableArrayListExtra(EXTRA_TRACK_LIST);
            if (audioFiles != null) {
                updateTrackList(audioFiles);
            }
        }
    };

    private void updateTrackList(ArrayList<MapAudioFile> audioFiles) {
        TrackListFragment trackListFragment = (TrackListFragment) getFragmentManager().
                findFragmentByTag(TrackListFragment.class.getName());
        if (trackListFragment != null) {
            trackListFragment.refreshAdapter(audioFiles);
        }
    }

    private class NavigationAdapter extends WearableNavigationDrawerView.WearableNavigationDrawerAdapter {

        @Override
        public int getCount() {
            return 2;
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
