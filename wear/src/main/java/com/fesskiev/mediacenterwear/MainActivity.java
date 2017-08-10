package com.fesskiev.mediacenterwear;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.app.FragmentManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.drawer.WearableDrawerLayout;
import android.support.wearable.view.drawer.WearableNavigationDrawer;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewTreeObserver;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;


public class MainActivity extends WearableActivity implements DataApi.DataListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Enables Always-on
        setAmbientEnabled();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        final WearableDrawerLayout wearableDrawerLayout = (WearableDrawerLayout) findViewById(R.id.drawerLayout);
        WearableNavigationDrawer wearableNavigationDrawer = (WearableNavigationDrawer) findViewById(R.id.navigationDrawer);
        wearableNavigationDrawer.setAdapter(new NavigationAdapter());


        // Temporarily peeks the navigation and action drawers to ensure the user is aware of them.
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
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(googleApiClient, this);
        addControlFragment();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/count") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
//                    dataMap.getInt()
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(googleApiClient, this);
        googleApiClient.disconnect();
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
            Log.d("test", "ADD ControlFragment");
        } else {
            Log.d("test", "SHOW ControlFragment");

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
            Log.d("test", "ADD TrackListFragment");
        } else {
            Log.d("test", "SHOW TrackListFragment");
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
