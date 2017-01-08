package com.fesskiev.mediacenter.ui;


import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.ui.about.AboutActivity;
import com.fesskiev.mediacenter.ui.audio.AudioFragment;
import com.fesskiev.mediacenter.ui.effects.EffectsActivity;
import com.fesskiev.mediacenter.ui.playback.PlaybackActivity;
import com.fesskiev.mediacenter.ui.playlist.PlayListActivity;
import com.fesskiev.mediacenter.ui.search.SearchActivity;
import com.fesskiev.mediacenter.ui.settings.SettingsActivity;
import com.fesskiev.mediacenter.ui.video.VideoFragment;
import com.fesskiev.mediacenter.utils.AnimationUtils;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.FetchMediaFilesManager;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.vk.VKAuthActivity;
import com.fesskiev.mediacenter.vk.VkontakteActivity;
import com.fesskiev.mediacenter.widgets.nav.MediaNavigationView;


public class MainActivity extends PlaybackActivity implements NavigationView.OnNavigationItemSelectedListener {

    private MediaNavigationView mediaNavigationView;
    private NavigationView navigationViewMain;
    private DrawerLayout drawer;
    private Toolbar toolbar;

    private Class selectedActivity;
    private AppSettingsManager settingsManager;
    private FetchMediaFilesManager fetchMediaFilesManager;
    private ImageView userPhoto;
    private ImageView logoutButton;
    private TextView firstName;
    private TextView lastName;
    private boolean finish;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingsManager = AppSettingsManager.getInstance();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AnimationUtils.getInstance().animateToolbar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {

                if (selectedActivity != null) {
                    startActivity(new Intent(MainActivity.this, selectedActivity));
                    selectedActivity = null;
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        toggle.syncState();


        setEffectsNavView();
        setMainNavView();

        fetchMediaFilesManager = new FetchMediaFilesManager(this);
        fetchMediaFilesManager.setOnFetchMediaFilesListener(new FetchMediaFilesManager.OnFetchMediaFilesListener() {
            @Override
            public void onFetchContentStart() {

            }

            @Override
            public void onFetchContentFinish() {

                if (isAudioFragmentShow()) {
                    AudioFragment audioFragment = (AudioFragment) getSupportFragmentManager().
                            findFragmentByTag(AudioFragment.class.getName());
                    audioFragment.refreshAudioContent();
                } else {
                    VideoFragment videoFragment = (VideoFragment) getSupportFragmentManager().
                            findFragmentByTag(VideoFragment.class.getName());
                    videoFragment.refreshVideoContent();
                }
            }
        });

        if (!settingsManager.isAuthTokenEmpty()) {
            setUserInfo();
        } else {
            setEmptyUserInfo();
        }

        checkAudioContentItem();
    }

    @Override
    public MediaNavigationView getMediaNavigationView() {
        return mediaNavigationView;
    }

    private void setMainNavView() {
        navigationViewMain = (NavigationView) findViewById(R.id.nav_view_main);
        navigationViewMain.setNavigationItemSelectedListener(this);
        navigationViewMain.setItemIconTintList(null);
        View headerLayout =
                navigationViewMain.inflateHeaderView(R.layout.nav_header_main);


        logoutButton = (ImageView) headerLayout.findViewById(R.id.logout);
        logoutButton.setOnClickListener(v -> {
            setEmptyUserInfo();
            clearUserInfo();
            logoutHide();
        });


        userPhoto = (ImageView) headerLayout.findViewById(R.id.photo);
        userPhoto.setOnClickListener(v -> {

            if (settingsManager.isAuthTokenEmpty()) {
                startActivityForResult(new Intent(this, VKAuthActivity.class), VKAuthActivity.VK_AUTH_RESULT);
            } else {
                startActivity(new Intent(this, VkontakteActivity.class));
            }
        });
        firstName = (TextView) headerLayout.findViewById(R.id.firstName);
        lastName = (TextView) headerLayout.findViewById(R.id.lastName);
    }

    private void setEffectsNavView() {
        mediaNavigationView = (MediaNavigationView) findViewById(R.id.nav_view_effects);
        mediaNavigationView.setOnEffectChangedListener(new MediaNavigationView.OnEffectChangedListener() {
            @Override
            public void onEffectClick() {
                selectedActivity = EffectsActivity.class;
                drawer.closeDrawer(GravityCompat.END);
            }

            @Override
            public void onEQStateChanged(boolean enable) {
                PlaybackService.changeEQEnable(getApplicationContext(), enable);
            }

            @Override
            public void onReverbStateChanged(boolean enable) {
                PlaybackService.changeReverbEnable(getApplicationContext(), enable);
            }
        });

        mediaNavigationView.setNavigationItemSelectedListener(this);

    }

    private void setUserInfo() {
        Bitmap bitmap = BitmapHelper.getInstance().getUserPhoto();
        if (bitmap != null) {
            BitmapHelper.getInstance().loadBitmapAvatar(bitmap, userPhoto);
        } else {
            BitmapHelper.getInstance().loadURLAvatar(settingsManager.getPhotoURL(),
                    userPhoto, new BitmapHelper.OnBitmapLoadListener() {
                        @Override
                        public void onLoaded(Bitmap bitmap) {
                            BitmapHelper.getInstance().saveUserPhoto(bitmap);
                        }

                        @Override
                        public void onFailed() {

                        }
                    });
        }
        firstName.setText(settingsManager.getUserFirstName());
        lastName.setText(settingsManager.getUserLastName());

        logoutShow();
    }

    private void logoutShow() {
        logoutButton.setVisibility(View.VISIBLE);
    }

    private void logoutHide() {
        logoutButton.setVisibility(View.GONE);
    }

    private void setEmptyUserInfo() {
        BitmapHelper.getInstance().loadEmptyAvatar(userPhoto);

        firstName.setText(getString(R.string.empty_first_name));
        lastName.setText(getString(R.string.empty_last_name));
    }

    private void clearUserInfo() {
        settingsManager.setUserFirstName("");
        settingsManager.setUserLastName("");
        settingsManager.setAuthToken("");
        settingsManager.setUserId("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("test", "RESULT request code: " + requestCode + " result code: " + resultCode);

        if (requestCode == VKAuthActivity.VK_AUTH_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                setUserInfo();
            }
            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                View searchMenuView = toolbar.findViewById(R.id.menu_search);
                Bundle options = ActivityOptions.makeSceneTransitionAnimation(this, searchMenuView,
                        getString(R.string.shared_search_back)).toBundle();
                startActivity(new Intent(this, SearchActivity.class), options);
                break;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        addAudioFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        clearItems();
        if (isAudioFragmentShow()) {
            checkAudioContentItem();
        } else if (isVideoFragmentShow()) {
            checkVideoContentItem();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        settingsManager.setEQEnable(false);
        settingsManager.setReverbEnable(false);

        fetchMediaFilesManager.unregister();
        PlaybackService.destroyPlayer(getApplicationContext());

    }

    private void clearItems() {
        int size = navigationViewMain.getMenu().size();
        for (int i = 0; i < size; i++) {
            navigationViewMain.getMenu().getItem(i).setChecked(false);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                selectedActivity = SettingsActivity.class;
                break;
            case R.id.about:
                selectedActivity = AboutActivity.class;
                break;
            case R.id.playlist:
                selectedActivity = PlayListActivity.class;
                break;
            case R.id.audio_content:
                checkAudioContentItem();
                addAudioFragment();
                showPlayback();
                break;
            case R.id.video_content:
                checkVideoContentItem();
                addVideoFragment();
                hidePlayback();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void checkAudioContentItem() {
        navigationViewMain.getMenu().getItem(0).getSubMenu().getItem(0).setChecked(true);
        navigationViewMain.getMenu().getItem(0).getSubMenu().getItem(1).setChecked(false);
    }

    private void checkVideoContentItem() {
        navigationViewMain.getMenu().getItem(0).getSubMenu().getItem(0).setChecked(false);
        navigationViewMain.getMenu().getItem(0).getSubMenu().getItem(1).setChecked(true);
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            hidePlayback();
            View view = findViewById(R.id.content);
            if (view != null) {
                Utils.showCustomSnackbar(view, getApplicationContext(),
                        getString(R.string.snack_exit_text),
                        Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.snack_exit_action), v -> {
                            finish = true;
                            finish();
                        }).setCallback(new Snackbar.Callback() {

                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (!finish) {
                            showPlayback();
                        }
                    }

                    @Override
                    public void onShown(Snackbar snackbar) {

                    }
                }).show();
            } else {
                super.onBackPressed();
            }
        }
    }

    private void addAudioFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        hideVisibleFragment(transaction);

        AudioFragment audioFragment = (AudioFragment) getSupportFragmentManager().
                findFragmentByTag(AudioFragment.class.getName());
        if (audioFragment == null) {
            transaction.add(R.id.content, AudioFragment.newInstance(),
                    AudioFragment.class.getName());
            transaction.addToBackStack(AudioFragment.class.getName());
        } else {
            transaction.show(audioFragment);
        }
        transaction.commitAllowingStateLoss();
    }

    private boolean isAudioFragmentShow() {
        AudioFragment audioFragment = (AudioFragment) getSupportFragmentManager().
                findFragmentByTag(AudioFragment.class.getName());
        return audioFragment != null && audioFragment.isAdded() && audioFragment.isVisible();
    }

    private boolean isVideoFragmentShow() {
        VideoFragment videoFragment = (VideoFragment) getSupportFragmentManager().
                findFragmentByTag(VideoFragment.class.getName());
        return videoFragment != null && videoFragment.isAdded() && videoFragment.isVisible();
    }

    private void addVideoFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        hideVisibleFragment(transaction);

        VideoFragment videoFragment = (VideoFragment) getSupportFragmentManager().
                findFragmentByTag(VideoFragment.class.getName());
        if (videoFragment == null) {
            transaction.add(R.id.content, VideoFragment.newInstance(),
                    VideoFragment.class.getName());
            transaction.addToBackStack(VideoFragment.class.getName());
        } else {
            transaction.show(videoFragment);
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
}