package com.fesskiev.mediacenter.vk;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;


public class VkontakteActivity extends AnalyticsActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_vk);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.title_music_vk_activity));
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.action_wall:
                            addUserAudioFragment();
                            break;
                        case R.id.action_group:
                            addGroupsFragment();
                            break;
                        case R.id.action_search:
                            addSearchAudioFragment();
                            break;
                    }
                    return true;
                });
        addUserAudioFragment();
    }

    private void addSearchAudioFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        hideVisibleFragment(transaction);

        SearchAudioFragment searchAudioFragment = (SearchAudioFragment) getSupportFragmentManager().
                findFragmentByTag(SearchAudioFragment.class.getName());
        if (searchAudioFragment == null) {
            transaction.add(R.id.content, SearchAudioFragment.newInstance(),
                    SearchAudioFragment.class.getName());
            transaction.addToBackStack(SearchAudioFragment.class.getName());
        } else {
            transaction.show(searchAudioFragment);
        }
        transaction.commitAllowingStateLoss();
    }

    private void addGroupsFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        hideVisibleFragment(transaction);

        GroupsFragment groupsFragment = (GroupsFragment) getSupportFragmentManager().
                findFragmentByTag(GroupsFragment.class.getName());
        if (groupsFragment == null) {
            transaction.add(R.id.content, GroupsFragment.newInstance(),
                    GroupsFragment.class.getName());
            transaction.addToBackStack(GroupsFragment.class.getName());
        } else {
            transaction.show(groupsFragment);
        }
        transaction.commitAllowingStateLoss();
    }

    private void addUserAudioFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        hideVisibleFragment(transaction);

        UserAudioFragment userAudioFragment = (UserAudioFragment) getSupportFragmentManager().
                findFragmentByTag(UserAudioFragment.class.getName());
        if (userAudioFragment == null) {
            transaction.add(R.id.content, UserAudioFragment.newInstance(),
                    UserAudioFragment.class.getName());
            transaction.addToBackStack(UserAudioFragment.class.getName());
        } else {
            transaction.show(userAudioFragment);
        }
        transaction.commitAllowingStateLoss();
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
