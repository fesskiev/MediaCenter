package com.fesskiev.mediacenter.ui;


import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.ui.about.AboutActivity;
import com.fesskiev.mediacenter.ui.audio.AudioFragment;
import com.fesskiev.mediacenter.ui.billing.InAppBillingActivity;
import com.fesskiev.mediacenter.ui.effects.EffectsActivity;
import com.fesskiev.mediacenter.ui.playback.PlaybackActivity;
import com.fesskiev.mediacenter.ui.playlist.PlayListActivity;
import com.fesskiev.mediacenter.ui.search.SearchActivity;
import com.fesskiev.mediacenter.ui.settings.SettingsActivity;
import com.fesskiev.mediacenter.ui.video.VideoFragment;
import com.fesskiev.mediacenter.utils.AnimationUtils;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.CountDownTimer;
import com.fesskiev.mediacenter.utils.FetchMediaFilesManager;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.vk.VKActivity;
import com.fesskiev.mediacenter.vk.VKAuthActivity;
import com.fesskiev.mediacenter.widgets.menu.ContextMenuManager;
import com.fesskiev.mediacenter.widgets.nav.MediaNavigationView;

import rx.Observable;
import rx.schedulers.Schedulers;


public class MainActivity extends PlaybackActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Class<? extends Activity> selectedActivity;
    private CountDownTimer countDownTimer;

    private AppSettingsManager settingsManager;
    private FetchMediaFilesManager fetchMediaFilesManager;

    private Toolbar toolbar;
    private MediaNavigationView mediaNavigationView;
    private NavigationView navigationViewMain;
    private DrawerLayout drawer;
    private ImageView timerView;
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


        timerView = (ImageView) findViewById(R.id.timer);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (!Utils.isTablet()) {
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
                        startSelectedActivity();
                    }
                }

                @Override
                public void onDrawerStateChanged(int newState) {
                    if (newState == DrawerLayout.STATE_DRAGGING &&
                            ContextMenuManager.getInstance().isContextMenuShow()) {
                        ContextMenuManager.getInstance().hideContextMenu();
                    }
                }
            });
            toggle.syncState();
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        setEffectsNavView();
        setMainNavView();

        if (!settingsManager.isAuthTokenEmpty()) {
            setUserInfo();
        } else {
            setEmptyUserInfo();
        }

        checkAudioContentItem();
        addAudioFragment();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        fetchMediaFilesManager = new FetchMediaFilesManager(null);
        fetchMediaFilesManager.isNeedTimer(false);
        fetchMediaFilesManager.setOnFetchMediaFilesListener(new FetchMediaFilesManager.OnFetchMediaFilesListener() {
            @Override
            public void onFetchContentStart() {
                fetchMediaFilesManager.setFetchContentView(mediaNavigationView.getFetchContentView());
                fetchMediaFilesManager.setTextPrimary();
                showToolbarTimer();

                if (isAudioFragmentShow()) {
                    clearPlayback();
                    AudioFragment audioFragment = (AudioFragment) getSupportFragmentManager().
                            findFragmentByTag(AudioFragment.class.getName());
                    if (audioFragment != null) {
                        audioFragment.clearAudioContent();
                    }
                } else if (isVideoFragmentShow()) {
                    VideoFragment videoFragment = (VideoFragment) getSupportFragmentManager().
                            findFragmentByTag(VideoFragment.class.getName());
                    if (videoFragment != null) {
                        videoFragment.clearVideoContent();
                    }
                }
            }

            @Override
            public void onFetchContentFinish() {
                hideToolbarTimer();
            }

            @Override
            public void onAudioFolderCreated() {
                AudioFragment audioFragment = (AudioFragment) getSupportFragmentManager().
                        findFragmentByTag(AudioFragment.class.getName());
                if (audioFragment != null) {
                    audioFragment.refreshAudioContent();
                }
            }

            @Override
            public void onVideoFileCreated() {
                VideoFragment videoFragment = (VideoFragment) getSupportFragmentManager().
                        findFragmentByTag(VideoFragment.class.getName());
                if (videoFragment != null) {
                    videoFragment.refreshVideoContent();
                }
            }
        });

    }

    private void showToolbarTimer() {
        timerView.setVisibility(View.VISIBLE);
        ((Animatable) timerView.getDrawable()).start();

        countDownTimer = new CountDownTimer(3000);
        countDownTimer.setOnCountDownListener(() -> ((Animatable) timerView.getDrawable()).start());
    }

    private void hideToolbarTimer() {
        timerView.setVisibility(View.INVISIBLE);
        ((Animatable) timerView.getDrawable()).stop();

        if (countDownTimer != null) {
            countDownTimer.stop();
        }
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
                startActivity(new Intent(this, VKActivity.class));
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
                if (!Utils.isTablet()) {
                    drawer.closeDrawer(GravityCompat.END);
                } else {
                    startSelectedActivity();
                }
            }

            @Override
            public void onEQStateChanged(boolean enable) {
                PlaybackService.changeEQEnable(getApplicationContext(), enable);
                AppSettingsManager.getInstance().setEQEnable(enable);
            }

            @Override
            public void onReverbStateChanged(boolean enable) {
                PlaybackService.changeReverbEnable(getApplicationContext(), enable);
                AppSettingsManager.getInstance().setReverbEnable(enable);
            }

            @Override
            public void onWhooshStateChanged(boolean enable) {
                PlaybackService.changeWhooshEnable(getApplicationContext(), enable);
                AppSettingsManager.getInstance().setWhooshEnable(enable);
            }

            @Override
            public void onEchoStateChanged(boolean enable) {
                PlaybackService.changeEchoEnable(getApplicationContext(), enable);
                AppSettingsManager.getInstance().setEchoEnable(enable);
            }

            @Override
            public void onRecordStateChanged(boolean recording) {
                if (recording) {
                    PlaybackService.startRecording(getApplicationContext());
                } else {
                    PlaybackService.stopRecording(getApplicationContext());
                }
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
        if (requestCode == VKAuthActivity.VK_AUTH_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                setUserInfo();
            }
        }
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
                if (!fetchMediaFilesManager.isFetchStart()) {
                    View searchMenuView = toolbar.findViewById(R.id.menu_search);
                    Bundle options = ActivityOptions.makeSceneTransitionAnimation(this, searchMenuView,
                            getString(R.string.shared_search_back)).toBundle();
                    startActivity(new Intent(this, SearchActivity.class), options);
                }
                break;
        }
        return true;
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

    private void clearItems() {
        int size = navigationViewMain.getMenu().size();
        for (int i = 0; i < size; i++) {
            navigationViewMain.getMenu().getItem(i).setChecked(false);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.billing:
                selectedActivity = InAppBillingActivity.class;
                break;
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

        if (!Utils.isTablet()) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (selectedActivity != null) {
            startSelectedActivity();
        }

        return true;
    }

    private void startSelectedActivity() {
        startActivity(new Intent(MainActivity.this, selectedActivity));
        selectedActivity = null;
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
                if (fetchMediaFilesManager.isFetchStart()) {
                    Utils.showCustomSnackbar(view, getApplicationContext(),
                            getString(R.string.splash_snackbar_stop_fetch),
                            Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.snack_exit_action), v -> {
                                stopFetchFiles();
                                finish();
                            })
                            .show();
                    return;
                }
                Utils.showCustomSnackbar(view, getApplicationContext(),
                        getString(R.string.snack_exit_text),
                        Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.snack_exit_action), v -> {
                            finish = true;
                            finish();
                        }).addCallback(new Snackbar.Callback() {

                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (!finish && isAudioFragmentShow()) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        settingsManager.setEQEnable(false);
        settingsManager.setReverbEnable(false);
        settingsManager.setWhooshEnable(false);
        settingsManager.setEchoEnable(false);

        if (fetchMediaFilesManager.isFetchStart()) {
            stopFetchFiles();
        }

        fetchMediaFilesManager.unregister();
        PlaybackService.destroyPlayer(getApplicationContext());
        FileSystemService.stopFileSystemService(getApplicationContext());

    }

    private void stopFetchFiles() {
        FileSystemService.shouldContinue = false;

        DataRepository repository = MediaApplication.getInstance().getRepository();
        repository.getMemorySource().setCacheArtistsDirty(true);
        repository.getMemorySource().setCacheGenresDirty(true);
        repository.getMemorySource().setCacheFoldersDirty(true);
        repository.getMemorySource().setCacheVideoFilesDirty(true);

        Observable.zip(RxUtils.fromCallable(repository.resetAudioContentDatabase()),
                RxUtils.fromCallable(repository.resetVideoContentDatabase()), (integer, integer2) -> Observable.empty())
                .subscribeOn(Schedulers.io())
                .subscribe();
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
