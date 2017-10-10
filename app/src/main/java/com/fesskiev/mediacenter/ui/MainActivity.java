package com.fesskiev.mediacenter.ui;


import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import com.fesskiev.mediacenter.ui.wear.WearActivity;
import com.fesskiev.mediacenter.utils.AppAnimationUtils;
import com.fesskiev.mediacenter.utils.AppGuide;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.FetchMediaFilesManager;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.ffmpeg.FFmpegHelper;
import com.fesskiev.mediacenter.widgets.dialogs.SimpleDialog;
import com.fesskiev.mediacenter.widgets.fetch.FetchContentScreen;
import com.fesskiev.mediacenter.widgets.menu.ContextMenuManager;
import com.fesskiev.mediacenter.widgets.nav.MediaNavigationView;

import static com.fesskiev.mediacenter.services.FileSystemService.ACTION_REFRESH_AUDIO_FRAGMENT;
import static com.fesskiev.mediacenter.services.FileSystemService.ACTION_REFRESH_VIDEO_FRAGMENT;
import static com.fesskiev.mediacenter.ui.walkthrough.PermissionFragment.PERMISSION_REQ;
import static com.fesskiev.mediacenter.ui.walkthrough.PermissionFragment.checkPermissionsResultGranted;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends PlaybackActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int SELECTED_AUDIO = 0;
    private static final int SELECTED_VIDEO = 1;

    private Class<? extends Activity> selectedActivity;

    private AppSettingsManager settingsManager;
    private FetchMediaFilesManager fetchMediaFilesManager;
    private FetchContentScreen fetchContentScreen;

    private Toolbar toolbar;
    private MediaNavigationView mediaNavigationView;
    private NavigationView navigationViewMain;
    private DrawerLayout drawer;

    private ImageView appIcon;
    private TextView appName;
    private TextView appPromo;

    private AppGuide appGuide;

    private int selectedState;
    private boolean recordingState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedState = SELECTED_AUDIO;

        settingsManager = AppSettingsManager.getInstance();
        FileSystemService.startFileSystemService(getApplicationContext());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AppAnimationUtils.getInstance().animateToolbar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
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
                    animateHeaderViews();

                    openDrawerGuide(drawerView);
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
        setFetchManager();

        if (savedInstanceState == null) {
            addAudioFragment();
            checkAudioContentItem();
        } else {
            restoreState(savedInstanceState);
        }

        registerRefreshFragmentsReceiver();

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

    @Override
    protected void onStart() {
        super.onStart();
        mediaNavigationView.postDelayed(this::makeGuideIfNeed, 1500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (appGuide != null) {
            appGuide.clear();
        }
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
        unregisterRefreshFragmentsReceiver();
    }


    private void registerRefreshFragmentsReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_REFRESH_AUDIO_FRAGMENT);
        filter.addAction(ACTION_REFRESH_VIDEO_FRAGMENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(fileSystemReceiver, filter);
    }

    private void unregisterRefreshFragmentsReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(fileSystemReceiver);
    }

    private BroadcastReceiver fileSystemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_REFRESH_AUDIO_FRAGMENT:
                        refreshAudioFragment();
                        break;
                    case ACTION_REFRESH_VIDEO_FRAGMENT:
                        refreshVideoFragment();
                        break;

                }
            }
        }
    };

    private void makeGuideIfNeed() {
        if (settingsManager.isNeedMainActivityGuide()) {
            drawer.openDrawer(GravityCompat.START);

            appGuide = new AppGuide(this, 3);
            appGuide.OnAppGuideListener(new AppGuide.OnAppGuideListener() {
                @Override
                public void next(int count) {
                    switch (count) {
                        case 1:
                            drawer.closeDrawer(GravityCompat.START);
                            makeSearchGuide();
                            break;
                        case 2:
                            drawer.openDrawer(GravityCompat.END);
                            break;
                    }
                }

                @Override
                public void watched() {
                    settingsManager.setNeedMainActivityGuide(false);
                }
            });
        }
    }


    private void openDrawerGuide(View drawerView) {
        if (drawerView instanceof MediaNavigationView) {
            makeAudioEffectsGuide();
        } else {
            makeWelcomeGuide();
        }
    }

    private void makeAudioEffectsGuide() {
        if (appGuide != null) {
            appGuide.makeGuide(mediaNavigationView.getSettingsView(),
                    getString(R.string.app_guide_effects_title),
                    getString(R.string.app_guide_effects_desc));
        }
    }

    private void makeSearchGuide() {
        if (appGuide != null) {
            appGuide.makeGuide(toolbar.findViewById(R.id.menu_search),
                    getString(R.string.app_guide_search_title),
                    getString(R.string.app_guide_search_desc));
        }
    }

    private void makeWelcomeGuide() {
        if (appGuide != null) {
            appGuide.makeGuide(appIcon,
                    getString(R.string.app_guide_welcome_title),
                    getString(R.string.app_guide_welcome_desc));
        }
    }

    float angle = 360f;

    private void animateHeaderViews() {
        ViewCompat.animate(appIcon)
                .rotationX(angle)
                .rotationY(angle)
                .setDuration(1800)
                .setInterpolator(AppAnimationUtils.getInstance().getFastOutSlowInInterpolator())
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {

                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        if (angle == 360f) {
                            angle = 0;
                        } else {
                            angle = 360;
                        }
                    }

                    @Override
                    public void onAnimationCancel(View view) {

                    }
                })
                .start();

        ObjectAnimator colorAnim = ObjectAnimator.ofInt(appName, "textColor",
                getResources().getColor(R.color.yellow), getResources().getColor(R.color.white));
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.setDuration(1800);
        colorAnim.start();

        ObjectAnimator colorAnim1 = ObjectAnimator.ofInt(appPromo, "textColor",
                getResources().getColor(R.color.yellow), getResources().getColor(R.color.white));
        colorAnim1.setEvaluator(new ArgbEvaluator());
        colorAnim1.setDuration(1800);
        colorAnim1.start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedState", selectedState);
    }

    private void restoreState(Bundle savedInstanceState) {
        if (fetchMediaFilesManager.isFetchStart()) {
            fetchContentScreen.disableTouchActivity();
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


    @Override
    public MediaNavigationView getMediaNavigationView() {
        return mediaNavigationView;
    }


    private void setFetchManager() {
        fetchContentScreen = new FetchContentScreen(this);

        fetchMediaFilesManager = FetchMediaFilesManager.getInstance();
        fetchMediaFilesManager.setFetchContentView(fetchContentScreen.getFetchContentView());
        fetchMediaFilesManager.register();
        fetchMediaFilesManager.setTextWhite();
        fetchMediaFilesManager.setOnFetchMediaFilesListener(new FetchMediaFilesManager.OnFetchMediaFilesListener() {

            @Override
            public void onFetchMediaPrepare() {
                AppLog.INFO("PREPARE!");
                fetchContentScreen.disableTouchActivity();
            }

            @Override
            public void onFetchAudioContentStart() {
                AppLog.INFO("onFetchAudioContentStart");
                clearPlayback();
                AppAnimationUtils.getInstance().animateBottomSheet(bottomSheet, false);
                clearAudioFragment();
            }

            @Override
            public void onFetchVideoContentStart() {
                AppLog.INFO("onFetchVideoContentStart");
                clearVideoFragment();
            }

            @Override
            public void onFetchMediaContentFinish() {
                AppLog.INFO("onFetchMediaContentFinish");

                AppAnimationUtils.getInstance().animateBottomSheet(bottomSheet, true);
                fetchContentScreen.enableTouchActivity();
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
        navigationViewMain = findViewById(R.id.nav_view_main);
        navigationViewMain.setNavigationItemSelectedListener(this);
        navigationViewMain.setItemIconTintList(null);
        View headerLayout =
                navigationViewMain.inflateHeaderView(R.layout.nav_header_main);

        appIcon = headerLayout.findViewById(R.id.appIcon);
        appName = headerLayout.findViewById(R.id.headerTitle);
        appPromo = headerLayout.findViewById(R.id.headerText);
    }

    private void setEffectsNavView() {
        mediaNavigationView = findViewById(R.id.nav_view_effects);
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
                recordingState = recording;
                checkPermissionsRecordProcess();
            }
        });

        mediaNavigationView.setNavigationItemSelectedListener(this);

    }

    private void checkPermissionsRecordProcess() {
        if (Utils.isMarshmallow() && !checkPermissions()) {
            requestPermissions();
        } else {
            processRecording();
        }
    }

    private void processRecording() {
        if (recordingState) {
            PlaybackService.startRecording(getApplicationContext());
        } else {
            PlaybackService.stopRecording(getApplicationContext());
        }
    }

    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                this, Manifest.permission.MODIFY_AUDIO_SETTINGS) &&
                PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                        this, Manifest.permission.RECORD_AUDIO);
    }


    private void requestPermissions() {
        requestPermissions(new String[]{
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQ);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQ: {
                if (grantResults != null && grantResults.length > 0) {
                    if (checkPermissionsResultGranted(grantResults)) {
                        processRecording();
                    } else {
                        boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.MODIFY_AUDIO_SETTINGS) ||
                                shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO);
                        if (showRationale) {
                            permissionsDenied();
                        } else {
                            createExplanationPermissionDialog();
                        }
                    }
                }
                break;
            }
        }
    }

    private void createExplanationPermissionDialog() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        SimpleDialog dialog = SimpleDialog.newInstance(getString(R.string.dialog_permission_title),
                getString(R.string.dialog_permission_message), R.drawable.icon_permission_settings);
        dialog.show(transaction, SimpleDialog.class.getName());
        dialog.setPositiveListener(() -> Utils.startSettingsActivity(getApplicationContext()));
        dialog.setNegativeListener(this::finish);
    }

    private void permissionsDenied() {
        Utils.showCustomSnackbar(getCurrentFocus(), getApplicationContext(),
                getString(R.string.snackbar_permission_title), Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.snackbar_permission_button, v -> requestPermissions())
                .show();
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
            case R.id.wear:
                selectedActivity = WearActivity.class;
                break;
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
        navigationViewMain.getMenu().getItem(6).setVisible(false);
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

        SimpleDialog exitDialog;
        if (fetchMediaFilesManager.isFetchStart()) {
            exitDialog = SimpleDialog.newInstance(getString(R.string.dialog_exit_title),
                    getString(R.string.dialog_text_stop_fetch),
                    R.drawable.icon_exit);
        } else {
            exitDialog = SimpleDialog.newInstance(getString(R.string.dialog_exit_title),
                    getString(R.string.dialog_text_exit),
                    R.drawable.icon_exit);
        }
        exitDialog.show(transaction, SimpleDialog.class.getName());
        exitDialog.setPositiveListener(this::processFinishPlayback);
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

        finishAffinity();
    }

    private void stopFetchFiles() {
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
