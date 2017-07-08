package com.fesskiev.mediacenter.ui;


import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
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
import com.fesskiev.mediacenter.ui.converter.ConverterActivity;
import com.fesskiev.mediacenter.ui.search.SearchActivity;
import com.fesskiev.mediacenter.ui.settings.SettingsActivity;
import com.fesskiev.mediacenter.ui.splash.SplashActivity;
import com.fesskiev.mediacenter.ui.video.VideoFoldersFragment;
import com.fesskiev.mediacenter.utils.AnimationUtils;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.CountDownTimer;
import com.fesskiev.mediacenter.utils.FetchMediaFilesManager;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.ffmpeg.FFmpegHelper;
import com.fesskiev.mediacenter.widgets.dialogs.ExitDialog;
import com.fesskiev.mediacenter.widgets.menu.ContextMenuManager;
import com.fesskiev.mediacenter.widgets.nav.MediaNavigationView;


public class MainActivity extends PlaybackActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int SELECTED_AUDIO = 0;
    private static final int SELECTED_VIDEO = 1;

    private Class<? extends Activity> selectedActivity;
    private CountDownTimer countDownTimer;

    private AppSettingsManager settingsManager;
    private FetchMediaFilesManager fetchMediaFilesManager;

    private Toolbar toolbar;
    private MediaNavigationView mediaNavigationView;
    private NavigationView navigationViewMain;
    private DrawerLayout drawer;
    private ImageView timerView;

    private ImageView appIcon;
    private TextView appName;
    private TextView appPromo;

    private int selectedState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedState = SELECTED_AUDIO;

        settingsManager = AppSettingsManager.getInstance();
        FileSystemService.startFileSystemService(getApplicationContext());

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
                    animateHeaderViews(slideOffset);
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    startAnimate = false;
                    animateHeaderViews(1f);
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    startAnimate = false;
                    animateHeaderViews(0f);

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
        setFetchManager();

        if (savedInstanceState == null) {
            addAudioFragment();
            checkAudioContentItem();
        } else {
            restoreState(savedInstanceState);
        }
    }

    private boolean startAnimate;

    private void animateHeaderViews(float slideOffset) {
        if (!startAnimate) {
            ViewCompat.animate(appIcon)
                    .alpha(slideOffset)
                    .setDuration(600)
                    .setInterpolator(AnimationUtils.getInstance().getFastOutSlowInInterpolator())
                    .start();

            ViewCompat.animate(appName)
                    .scaleX(slideOffset)
                    .scaleY(slideOffset)
                    .alpha(slideOffset)
                    .setDuration(800)
                    .setInterpolator(AnimationUtils.getInstance().getFastOutSlowInInterpolator())
                    .start();

            ViewCompat.animate(appPromo)
                    .scaleX(slideOffset)
                    .scaleY(slideOffset)
                    .alpha(slideOffset)
                    .setDuration(1000)
                    .setInterpolator(AnimationUtils.getInstance().getFastOutSlowInInterpolator())
                    .setListener(new ViewPropertyAnimatorListener() {
                        @Override
                        public void onAnimationStart(View view) {
                            startAnimate = true;
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            startAnimate = false;
                        }

                        @Override
                        public void onAnimationCancel(View view) {
                            startAnimate = false;
                        }
                    })
                    .start();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedState", selectedState);
    }

    private void restoreState(Bundle savedInstanceState) {
        if (fetchMediaFilesManager.isFetchStart()) {
            showToolbarTimer();
        }

        selectedState = savedInstanceState.getInt("selectedState");
        checkSelectedFragment();
    }

    private void checkSelectedFragment() {
        clearItems();
        switch (selectedState) {
            case SELECTED_AUDIO:
                checkAudioContentItem();
                break;
            case SELECTED_VIDEO:
                checkVideoContentItem();
                break;
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        AppLog.DEBUG("onNewIntent: " + intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey(SplashActivity.EXTRA_OPEN_FROM_ACTION)) {
                audioPlayer.getCurrentTrackAndTrackList();
                refreshAudioFragment();
            }
        }
    }

    private void refreshAudioFragment() {
        AudioFragment audioFragment = (AudioFragment) getSupportFragmentManager().
                findFragmentByTag(AudioFragment.class.getName());
        if (audioFragment != null) {
            audioFragment.refreshAudioContent();
        }
    }

    private void refreshVideoFragment() {
        VideoFoldersFragment videoFragment = (VideoFoldersFragment) getSupportFragmentManager().
                findFragmentByTag(VideoFoldersFragment.class.getName());
        if (videoFragment != null) {
            videoFragment.refreshVideoContent();
        }
    }

    private void clearAudioFragment() {
        AudioFragment audioFragment = (AudioFragment) getSupportFragmentManager().
                findFragmentByTag(AudioFragment.class.getName());
        if (audioFragment != null) {
            audioFragment.clearAudioContent();
        }
    }

    private void clearVideoFragment() {
        VideoFoldersFragment videoFragment = (VideoFoldersFragment) getSupportFragmentManager().
                findFragmentByTag(VideoFoldersFragment.class.getName());
        if (videoFragment != null) {
            videoFragment.clearVideoContent();
        }
    }

    private void disableSwipeRefresh() {
        VideoFoldersFragment videoFragment = (VideoFoldersFragment) getSupportFragmentManager().
                findFragmentByTag(VideoFoldersFragment.class.getName());
        if (videoFragment != null) {
            videoFragment.getSwipeRefreshLayout().setEnabled(false);
        }

        AudioFragment audioFragment = (AudioFragment) getSupportFragmentManager().
                findFragmentByTag(AudioFragment.class.getName());
        if (audioFragment != null) {
            audioFragment.getSwipeRefreshLayout().setEnabled(false);
        }

    }

    private void enableSwipeRefresh() {
        VideoFoldersFragment videoFragment = (VideoFoldersFragment) getSupportFragmentManager().
                findFragmentByTag(VideoFoldersFragment.class.getName());
        if (videoFragment != null) {
            videoFragment.getSwipeRefreshLayout().setEnabled(true);
        }

        AudioFragment audioFragment = (AudioFragment) getSupportFragmentManager().
                findFragmentByTag(AudioFragment.class.getName());
        if (audioFragment != null) {
            audioFragment.getSwipeRefreshLayout().setEnabled(true);
        }
    }

    private void showToolbarTimer() {
        timerView.setVisibility(View.VISIBLE);
        ((Animatable) timerView.getDrawable()).start();

        countDownTimer = new CountDownTimer(2000);
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


    private void setFetchManager() {
        fetchMediaFilesManager = FetchMediaFilesManager.getInstance();
        fetchMediaFilesManager.setFetchContentView(mediaNavigationView.getFetchContentView());
        fetchMediaFilesManager.register();
        fetchMediaFilesManager.isNeedTimer(false);
        fetchMediaFilesManager.setOnFetchMediaFilesListener(new FetchMediaFilesManager.OnFetchMediaFilesListener() {

            @Override
            public void onFetchMediaPrepare() {
                AppLog.INFO("PREPARE!");

                fetchMediaFilesManager.setTextPrimary();
                showToolbarTimer();
                disableSwipeRefresh();
            }

            @Override
            public void onFetchAudioContentStart(boolean clear) {
                AppLog.INFO("onFetchAudioContentStart");
                if (clear) {
                    clearPlayback();
                    AnimationUtils.getInstance().animateBottomSheet(bottomSheet, false);
                    clearAudioFragment();
                }
            }

            @Override
            public void onFetchVideoContentStart(boolean clear) {
                AppLog.INFO("onFetchVideoContentStart");
                if (clear) {
                    clearVideoFragment();
                }
            }

            @Override
            public void onFetchMediaContentFinish() {
                AppLog.INFO("onFetchMediaContentFinish");

                AnimationUtils.getInstance().animateBottomSheet(bottomSheet, true);
                hideToolbarTimer();
                enableSwipeRefresh();
            }

            @Override
            public void onAudioFolderCreated() {
                AppLog.INFO("onAudioFolderCreated");
                refreshAudioFragment();
            }

            @Override
            public void onVideoFolderCreated() {
                AppLog.INFO("onVideoFolderCreated");
                refreshVideoFragment();

            }
        });

    }

    private void setMainNavView() {
        navigationViewMain = (NavigationView) findViewById(R.id.nav_view_main);
        navigationViewMain.setNavigationItemSelectedListener(this);
        navigationViewMain.setItemIconTintList(null);
        View headerLayout =
                navigationViewMain.inflateHeaderView(R.layout.nav_header_main);

        appIcon = (ImageView) headerLayout.findViewById(R.id.appIcon);
        appName = (TextView) headerLayout.findViewById(R.id.headerTitle);
        appPromo = (TextView) headerLayout.findViewById(R.id.headerText);


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
            case R.id.converter:
                selectedActivity = ConverterActivity.class;
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

    private void hideDrawerPurchaseItem() {
        navigationViewMain.getMenu().getItem(5).setVisible(false);
    }

    private void hideDrawerConverterItem() {
        navigationViewMain.getMenu().getItem(1).setVisible(false);
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            processExit();
        }
    }

    private void processExit() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);

        ExitDialog exitDialog;
        if (fetchMediaFilesManager.isFetchStart()) {
            exitDialog = ExitDialog.newInstance(getString(R.string.splash_snackbar_stop_fetch));
        } else {
            exitDialog = ExitDialog.newInstance(getString(R.string.snack_exit_text));
        }
        exitDialog.show(transaction, ExitDialog.class.getName());
        exitDialog.setOnExitListener(this::processFinishPlayback);
    }

    @Override
    public void processFinishPlayback() {

        if (fetchMediaFilesManager.isFetchStart()) {
            stopFetchFiles();
        }

        if (startForeground) {
            PlaybackService.stopPlaybackForegroundService(getApplicationContext());
            startForeground = false;
        }

        FFmpegHelper FFmpeg = FFmpegHelper.getInstance();
        if (FFmpeg.isCommandRunning()) {
            FFmpeg.killRunningProcesses();
        }

        FileSystemService.stopFileSystemService(getApplicationContext());
        CacheManager.clearTempDir();

        settingsManager.setEQEnable(false);
        settingsManager.setReverbEnable(false);
        settingsManager.setWhooshEnable(false);
        settingsManager.setEchoEnable(false);

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSelectedFragment();
        if (!settingsManager.isUserPro()) {
            hideDrawerConverterItem();
        } else {
            hideDrawerPurchaseItem();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        fetchMediaFilesManager.unregister();
    }

    private void stopFetchFiles() {
        FileSystemService.shouldContinue = false;

        DataRepository repository = MediaApplication.getInstance().getRepository();
        repository.getMemorySource().setCacheArtistsDirty(true);
        repository.getMemorySource().setCacheGenresDirty(true);
        repository.getMemorySource().setCacheFoldersDirty(true);
        repository.getMemorySource().setCacheVideoFoldersDirty(true);
    }

    private void addAudioFragment() {
        selectedState = SELECTED_AUDIO;

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

    private void addVideoFragment() {
        selectedState = SELECTED_VIDEO;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        hideVisibleFragment(transaction);

        VideoFoldersFragment videoFragment = (VideoFoldersFragment) getSupportFragmentManager().
                findFragmentByTag(VideoFoldersFragment.class.getName());
        if (videoFragment == null) {
            transaction.add(R.id.content, VideoFoldersFragment.newInstance(),
                    VideoFoldersFragment.class.getName());
            transaction.addToBackStack(VideoFoldersFragment.class.getName());
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
