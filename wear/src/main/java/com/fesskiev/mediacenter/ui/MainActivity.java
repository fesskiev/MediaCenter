package com.fesskiev.mediacenter.ui;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.wear.widget.drawer.WearableNavigationDrawerView;
import android.support.wearable.activity.WearableActivity;
import android.view.ViewGroup;

import com.fesskiev.common.data.MapAudioFile;
import com.fesskiev.common.data.MapPlayback;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.service.DataLayerService;

import java.util.ArrayList;
import java.util.List;

import static com.fesskiev.mediacenter.service.DataLayerService.ACTION_PLAYBACK;
import static com.fesskiev.mediacenter.service.DataLayerService.ACTION_TRACK;
import static com.fesskiev.mediacenter.service.DataLayerService.ACTION_TRACK_LIST;
import static com.fesskiev.mediacenter.service.DataLayerService.EXTRA_PLAYBACK;
import static com.fesskiev.mediacenter.service.DataLayerService.EXTRA_TRACK;
import static com.fesskiev.mediacenter.service.DataLayerService.EXTRA_TRACK_LIST;


public class MainActivity extends WearableActivity {

    private ViewPagerAdapter adapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WearableNavigationDrawerView wearableNavigationDrawer = findViewById(R.id.navigationDrawer);
        wearableNavigationDrawer.getController().peekDrawer();

        wearableNavigationDrawer.setAdapter(new NavigationAdapter());
        wearableNavigationDrawer.addOnItemSelectedListener(pos -> viewPager.setCurrentItem(pos));

        viewPager = findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);
        setupViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                wearableNavigationDrawer.setCurrentItem(position, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        registerTrackListReceiver();
        startService(new Intent(this, DataLayerService.class));
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getFragmentManager());
        Fragment[] fragments = new Fragment[]{
                PlaybackFragment.newInstance(),
                ControlFragment.newInstance(),
                TrackListFragment.newInstance()
        };
        for (Fragment fragment : fragments) {
            adapter.addFragment(fragment);
        }
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterTrackListReceiver();
        stopService(new Intent(this, DataLayerService.class));
    }

    private void registerTrackListReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_TRACK_LIST);
        intentFilter.addAction(ACTION_TRACK);
        intentFilter.addAction(ACTION_PLAYBACK);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                receiver, intentFilter);
    }

    private void unregisterTrackListReceiver() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        case ACTION_TRACK_LIST:
                            ArrayList<MapAudioFile> audioFiles =
                                    intent.getParcelableArrayListExtra(EXTRA_TRACK_LIST);
                            if (audioFiles != null) {
                                adapter.getTrackListFragment().refreshAdapter(audioFiles);
                            }
                            break;
                        case ACTION_TRACK:
                            MapAudioFile audioFile =
                                    intent.getParcelableExtra(EXTRA_TRACK);
                            if (audioFile != null) {
                                adapter.getPlaybackFragment().updateCurrentTrack(audioFile);
                                adapter.getTrackListFragment().updateCurrentTrack(audioFile);
                                adapter.getControlFragment().updateCurrentTrack(audioFile);
                            }
                            break;
                        case ACTION_PLAYBACK:
                            MapPlayback playback =
                                    intent.getParcelableExtra(EXTRA_PLAYBACK);
                            if (playback != null) {
                                adapter.getPlaybackFragment().updatePlayback(playback);
                                adapter.getControlFragment().updatePlayback(playback);
                            }
                            break;
                    }
                }
            }
        }
    };

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> fragmentList = new ArrayList<>();
        private List<Fragment> registeredFragments = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment) {
            fragmentList.add(fragment);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.add(fragment);

            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public ControlFragment getControlFragment() {
            for (Fragment fragment : registeredFragments) {
                if (fragment instanceof ControlFragment) {
                    return (ControlFragment) fragment;
                }
            }
            return null;
        }


        public PlaybackFragment getPlaybackFragment() {
            for (Fragment fragment : registeredFragments) {
                if (fragment instanceof PlaybackFragment) {
                    return (PlaybackFragment) fragment;
                }
            }
            return null;
        }

        public TrackListFragment getTrackListFragment() {
            for (Fragment fragment : registeredFragments) {
                if (fragment instanceof TrackListFragment) {
                    return (TrackListFragment) fragment;
                }
            }
            return null;
        }
    }

    private class NavigationAdapter extends WearableNavigationDrawerView.WearableNavigationDrawerAdapter {

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public String getItemText(int pos) {
            switch (pos) {
                case 0:
                    return getString(R.string.drawer_item_playback);
                case 1:
                    return getString(R.string.drawer_item_control);
                case 2:
                    return getString(R.string.drawer_item_tracklist);
            }
            return "";
        }

        @Override
        public Drawable getItemDrawable(int pos) {
            switch (pos) {
                case 0:
                    return ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_playback);
                case 1:
                    return ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_control);
                case 2:
                    return ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_tracklist);
            }
            return null;
        }
    }
}
