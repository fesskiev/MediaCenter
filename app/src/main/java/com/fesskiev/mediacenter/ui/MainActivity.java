package com.fesskiev.mediacenter.ui;


import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.ui.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.ui.about.AboutActivity;
import com.fesskiev.mediacenter.ui.audio.AudioFragment;
import com.fesskiev.mediacenter.ui.audio.player.AudioPlayerActivity;
import com.fesskiev.mediacenter.ui.billing.InAppBillingActivity;
import com.fesskiev.mediacenter.ui.effects.EffectsActivity;
import com.fesskiev.mediacenter.ui.fetch.FetchContentViewModel;
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
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.buttons.PlayPauseFloatingButton;
import com.fesskiev.mediacenter.widgets.dialogs.SimpleDialog;
import com.fesskiev.mediacenter.ui.fetch.FetchContentScreen;
import com.fesskiev.mediacenter.widgets.menu.ContextMenuManager;
import com.fesskiev.mediacenter.widgets.nav.MediaNavigationView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.fesskiev.mediacenter.services.FileSystemService.ACTION_REFRESH_AUDIO_FRAGMENT;
import static com.fesskiev.mediacenter.services.FileSystemService.ACTION_REFRESH_VIDEO_FRAGMENT;
import static com.fesskiev.mediacenter.ui.walkthrough.PermissionFragment.checkPermissionsResultGranted;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AnalyticsActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final static int PERMISSION_RECORD = 1;
    private final static int PERMISSION_STORAGE = 2;

    private static final int SELECTED_AUDIO = 0;
    private static final int SELECTED_VIDEO = 1;

    @Inject
    AppAnimationUtils animationUtils;

    private Class<? extends Activity> selectedActivity;

    private FetchContentScreen fetchContentScreen;

    private Toolbar toolbar;
    private MediaNavigationView mediaNavigationView;
    private NavigationView navigationViewMain;
    private DrawerLayout drawer;
    private ImageView appIcon;
    private TextView appName;
    private TextView appPromo;
    private View bottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private TrackListAdapter adapter;
    private PlayPauseFloatingButton playPauseButton;
    private TextView durationText;
    private TextView track;
    private TextView artist;
    private ImageView cover;
    private View emptyFolder;
    private View emptyTrack;
    private View peakView;

    private AppGuide appGuide;

    private int selectedState;
    private boolean recordingState;
    private float angle = 360f;

    private int height;

    private boolean startForeground;

    private FileSystemService boundService;
    private boolean serviceBound;

    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MediaApplication.getInstance().getAppComponent().inject(this);

        selectedState = SELECTED_AUDIO;

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        animationUtils.animateToolbar(toolbar);

        fetchContentScreen = new FetchContentScreen(this, animationUtils);

        track = findViewById(R.id.track);
        artist = findViewById(R.id.artist);
        cover = findViewById(R.id.cover);
        durationText = findViewById(R.id.duration);

        emptyTrack = findViewById(R.id.emptyTrackCard);
        emptyFolder = findViewById(R.id.emptyFolderCard);

        RecyclerView recyclerView = findViewById(R.id.trackListControl);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TrackListAdapter();
        recyclerView.setAdapter(adapter);

        playPauseButton = findViewById(R.id.playPauseFAB);
        playPauseButton.setOnClickListener(v -> {
            if (mainViewModel.isTrackSelected() && !mainViewModel.isConverting()) {
                mainViewModel.playStateChanged();
            } else {
                animationUtils.errorAnimation(playPauseButton);
            }
        });
        playPauseButton.setPlay(false);

        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_SETTLING);
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(View bottomSheet, int newState) {
                    switch (newState) {
                        case BottomSheetBehavior.STATE_HIDDEN:
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            break;
                    }
                }

                @Override
                public void onSlide(View bottomSheet, float slideOffset) {

                }
            });

            peakView = findViewById(R.id.basicNavPlayerContainer);
            peakView.setOnClickListener(v -> {
                if (mainViewModel.isTrackSelected() && !mainViewModel.isConverting()) {
                    AudioPlayerActivity.startPlayerActivity(MainActivity.this);
                } else {
                    animationUtils.errorAnimation(playPauseButton);
                }
            });
            peakView.post(() -> {
                int marginTop = getResources().getDimensionPixelSize(R.dimen.bottom_sheet_margin_top);
                height = peakView.getHeight() + marginTop;
                bottomSheetBehavior.setPeekHeight(height);
            });
        }

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
        registerRefreshFragmentsReceiver();

        addAudioFragment();
        checkAudioContentItem();

        showEmptyTrackCard();
        showEmptyFolderCard();
        observeData();
        observeFetchData();
        bindFileSystemService();
    }

    private void observeData() {
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.getCurrentTrackListLiveData().observe(this, audioFiles -> {
            adapter.refreshAdapter(audioFiles);
            hideEmptyFolderCard();
        });
        mainViewModel.getCurrentTrackLiveData().observe(this, audioFile -> {
            setTrackInfo(audioFile);
            adapter.notifyDataSetChanged();
            hideEmptyTrackCard();
            startForegroundService();
        });

        mainViewModel.getConvertingLiveData().observe(this, this::setConvertingView);
        mainViewModel.getLoadingSuccessLiveData().observe(this, this::setLoadingSuccessView);
        mainViewModel.getPositionLiveData().observe(this, this::setPositionView);
        mainViewModel.getPlayingLiveData().observe(this, this::setPlayingView);
        mainViewModel.getEQLiveData().observe(this, this::setEQStateView);
        mainViewModel.getReverbLiveData().observe(this, this::setReverbStateView);
        mainViewModel.getWhooshLiveData().observe(this, this::setWhooshStateView);
        mainViewModel.getEchoLiveData().observe(this, this::setEchoStateView);
        mainViewModel.getFinishPlaybackLiveData().observe(this, Void -> processFinishPlayback());
        mainViewModel.getCoverLiveData().observe(this, this::setCoverView);
    }

    private void observeFetchData() {
        FetchContentViewModel viewModel = ViewModelProviders.of(this).get(FetchContentViewModel.class);
        viewModel.getPrepareFetchLiveData().observe(this, Void -> fetchContentScreen.prepareFetch());
        viewModel.getFinishFetchLiveData().observe(this, Void -> {
            ;
            fetchContentScreen.finishFetch();
            animationUtils.animateBottomSheet(bottomSheet, true);

        });
        viewModel.getPercentLiveData().observe(this, percent -> fetchContentScreen.setProgress(percent));
        viewModel.getFetchDescriptionLiveData().observe(this, description -> {
            String folderName = description.getFolderName();
            if (folderName != null) {
                fetchContentScreen.setFolderName(folderName);
            }
            String fileName = description.getFileName();
            if (fileName != null) {
                fetchContentScreen.setFileName(fileName);
            }
        });
        viewModel.getFetchAudioStartLiveData().observe(this, Void -> {
            clearPlayback();
            animationUtils.animateBottomSheet(bottomSheet, false);
            clearAudioFragment();
        });
        viewModel.getFetchVideoStartLiveData().observe(this, Void -> clearVideoFragment());
        viewModel.getAudioFoldersCreatedLiveData().observe(this, Void -> refreshAudioFragment());
        viewModel.getVideoFoldersCreatedLiveData().observe(this, Void -> refreshVideoFragment());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        AppLog.DEBUG("onNewIntent: " + intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey(SplashActivity.EXTRA_OPEN_FROM_ACTION)) {
                refreshAudioFragment();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaNavigationView.postDelayed(this::makeGuideIfNeed, 1500);
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FileSystemService.FileSystemLocalBinder binder = (FileSystemService.FileSystemLocalBinder) service;
            boundService = binder.getService();
            if (boundService.getScanState() == FileSystemService.SCAN_STATE.SCANNING) {
                fetchContentScreen.showContentScreen();
            } else if (boundService.getScanState() == FileSystemService.SCAN_STATE.FINISHED) {
                fetchContentScreen.hideContentScreen();
            }
            serviceBound = true;
        }
    };

    private void bindFileSystemService() {
        Intent intent = new Intent(this, FileSystemService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindFileSystemService() {
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSelectedFragment();
        if (!mainViewModel.isUserPro()) {
            hideDrawerConverterItem();
        } else {
            hideDrawerPurchaseItem();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (appGuide != null) {
            appGuide.clear();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindFileSystemService();
        unregisterRefreshFragmentsReceiver();
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("startForeground", startForeground);
        outState.putInt("selectedState", selectedState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        startForeground = savedInstanceState.getBoolean("startForeground");
        selectedState = savedInstanceState.getInt("selectedState");
        checkSelectedFragment();
    }

    private void setTrackInfo(AudioFile audioFile) {
        track.setText(audioFile.title);
        artist.setText(audioFile.artist);
        clearDuration();
    }

    private void startForegroundService() {
        if (!startForeground) {
            startForeground = true;
            PlaybackService.startPlaybackForegroundService(getApplicationContext());
        }
    }

    private void stopPlayBackService() {
        if (startForeground) {
            PlaybackService.stopPlaybackForegroundService(getApplicationContext());
            startForeground = false;
        }
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
        if (mainViewModel.isNeedMainActivityGuide()) {
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
                    mainViewModel.setMainActivityGuideWatched();
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

    private void animateHeaderViews() {
        ViewCompat.animate(appIcon)
                .rotationX(angle)
                .rotationY(angle)
                .setDuration(1800)
                .setInterpolator(animationUtils.getFastOutSlowInInterpolator())
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
                mainViewModel.setEQEnable(enable);
            }

            @Override
            public void onReverbStateChanged(boolean enable) {
                mainViewModel.setReverbEnable(enable);
            }

            @Override
            public void onWhooshStateChanged(boolean enable) {
                mainViewModel.setWhooshEnable(enable);
            }

            @Override
            public void onEchoStateChanged(boolean enable) {
                mainViewModel.setEchoEnable(enable);
            }

            @Override
            public void onRecordStateChanged(boolean recording) {
                recordingState = recording;
                checkPermissionsRecordProcess();
            }
        });

        mediaNavigationView.setNavigationItemSelectedListener(this);

    }

    public void checkPermissionAndFetchAudio() {
        if (Utils.isMarshmallow() && !checkPermissionsStorage()) {
            requestPermissionsStorage();
        } else {
            mainViewModel.fetchFileSystemAudio();
        }
    }

    public void checkPermissionAndFetchVideo() {
        if (Utils.isMarshmallow() && !checkPermissionsStorage()) {
            requestPermissionsStorage();
        } else {
            mainViewModel.fetchFileSystemVideo();
        }
    }

    private void checkPermissionsRecordProcess() {
        if (Utils.isMarshmallow() && !checkPermissionsRecords()) {
            requestPermissionsRecords();
            mediaNavigationView.setRecordEnable(false);
        } else {
            processRecording();
        }
    }

    private void processRecording() {
        if (recordingState) {
            mediaNavigationView.setRecordEnable(true);
            PlaybackService.startRecording(getApplicationContext());
        } else {
            PlaybackService.stopRecording(getApplicationContext());
        }
    }

    private boolean checkPermissionsStorage() {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private boolean checkPermissionsRecords() {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                this, Manifest.permission.MODIFY_AUDIO_SETTINGS) &&
                PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                        this, Manifest.permission.RECORD_AUDIO);
    }

    private void requestPermissionsRecords() {
        requestPermissions(new String[]{
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.RECORD_AUDIO},
                PERMISSION_RECORD);
    }

    private void requestPermissionsStorage() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_STORAGE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_RECORD:
                if (grantResults.length > 0) {
                    if (checkPermissionsResultGranted(grantResults)) {
                        processRecording();
                    } else {
                        boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.MODIFY_AUDIO_SETTINGS) ||
                                shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO);
                        if (showRationale) {
                            showPermissionsDeniedRecordsSnackBar();
                        } else {
                            createExplanationPermissionDialog();
                        }
                    }
                }
                break;
            case PERMISSION_STORAGE:
                if (grantResults.length > 0) {
                    if (checkPermissionsResultGranted(grantResults)) {
                        if (selectedState == SELECTED_AUDIO) {
                            mainViewModel.fetchFileSystemAudio();
                        } else if (selectedState == SELECTED_VIDEO) {
                            mainViewModel.fetchFileSystemVideo();
                        }
                    } else {
                        boolean showRationale =
                                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        if (showRationale) {
                            showPermissionsDeniedStorageSnackBar();
                        } else {
                            createExplanationPermissionDialog();
                        }
                    }
                }
                break;
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

    private void showPermissionsDeniedRecordsSnackBar() {
        Utils.showCustomSnackbar(getWindow().getDecorView().findViewById(android.R.id.content),
                getApplicationContext(),
                getString(R.string.snackbar_permission_title), Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.snackbar_permission_button, v -> requestPermissionsRecords())
                .show();
    }

    private void showPermissionsDeniedStorageSnackBar() {
        Utils.showCustomSnackbar(getWindow().getDecorView().findViewById(android.R.id.content),
                getApplicationContext(),
                getString(R.string.snackbar_permission_title), Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.snackbar_permission_button, v -> requestPermissionsStorage())
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
                if (!isFetchMediaStart()) {
                    View searchMenuView = toolbar.findViewById(R.id.menu_search);
                    Bundle options = ActivityOptions.makeSceneTransitionAnimation(this, searchMenuView,
                            getString(R.string.shared_search_back)).toBundle();
                    startActivity(new Intent(this, SearchActivity.class), options);
                }
                break;
        }
        return true;
    }

    private boolean isFetchMediaStart() {
        return serviceBound && boundService.getScanState() == FileSystemService.SCAN_STATE.SCANNING;
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
                break;
            case R.id.video_content:
                checkVideoContentItem();
                addVideoFragment();
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
        if (isFetchMediaStart()) {
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

    private void processFinishPlayback() {
        stopPlayBackService();

        CacheManager.clearTempDir();

        mainViewModel.dropEffects();
        mainViewModel.killFFmpeg();

        finish();
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

    public void clearPlayback() {
        adapter.clearAdapter();
        showEmptyFolderCard();
        playPauseButton.setPlay(false);
        adapter.notifyDataSetChanged();
    }

    public void setCoverView(Bitmap bitmap) {
        cover.setImageBitmap(bitmap);
    }

    private void setEchoStateView(boolean enable) {
        mediaNavigationView.setEchoEnable(enable);
    }

    private void setWhooshStateView(boolean enable) {
        mediaNavigationView.setWhooshEnable(enable);
    }

    private void setReverbStateView(boolean enable) {
        mediaNavigationView.setReverbEnable(enable);
    }

    private void setEQStateView(boolean enable) {
        mediaNavigationView.setEQEnable(enable);
    }

    private void setPlayingView(boolean playing) {
        playPauseButton.setPlay(playing);
        adapter.notifyDataSetChanged();
    }

    private void setPositionView(int position) {
        durationText.setText(Utils.getPositionSecondsString(position));
    }

    private void setLoadingSuccessView(boolean success) {
        if (!success) {
            playPauseButton.startLoading();
        } else {
            playPauseButton.finishLoading();
        }
    }

    private void setConvertingView(boolean converting) {
        playPauseButton.startLoading();
    }

    private void showEmptyFolderCard() {
        emptyFolder.setVisibility(View.VISIBLE);
    }

    private void showEmptyTrackCard() {
        emptyTrack.setVisibility(View.VISIBLE);
    }

    private void hideEmptyFolderCard() {
        emptyFolder.setVisibility(View.GONE);
    }

    private void hideEmptyTrackCard() {
        emptyTrack.setVisibility(View.GONE);
    }

    private void clearDuration() {
        durationText.setText("");
    }

    private class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.ViewHolder> {

        private List<AudioFile> currentTrackList;

        public TrackListAdapter() {
            currentTrackList = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView playEq;
            TextView title;
            TextView duration;
            TextView filePath;

            public ViewHolder(View v) {
                super(v);
                v.setOnClickListener(view -> {
                    AudioFile audioFile = currentTrackList.get(getAdapterPosition());
                    if (audioFile != null && !mainViewModel.isConverting()) {
                        if (audioFile.exists()) {
                            mainViewModel.setCurrentAudioFileAndPlay(audioFile);
                        } else {
                            Utils.showCustomSnackbar(getCurrentFocus(),
                                    getApplicationContext(), getString(R.string.snackbar_file_not_exist),
                                    Snackbar.LENGTH_LONG)
                                    .show();
                        }
                    }
                });

                playEq = v.findViewById(R.id.playEq);
                title = v.findViewById(R.id.title);
                duration = v.findViewById(R.id.duration);
                filePath = v.findViewById(R.id.filePath);
                filePath.setSelected(true);

            }
        }

        @Override
        public TrackListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_track_playback, parent, false);

            return new TrackListAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(TrackListAdapter.ViewHolder holder, int position) {
            AudioFile audioFile = currentTrackList.get(position);
            if (audioFile != null) {
                holder.title.setText(audioFile.title);
                holder.filePath.setText(audioFile.getFilePath());
                holder.duration.setText(Utils.getDurationString(audioFile.length));

                if (mainViewModel.isEqualsToCurrentTrack(audioFile) && mainViewModel.isPlaying()) {
                    holder.playEq.setVisibility(View.VISIBLE);

                    AnimationDrawable animation = (AnimationDrawable) ContextCompat.
                            getDrawable(getApplicationContext(), R.drawable.ic_equalizer);
                    holder.playEq.setImageDrawable(animation);
                    if (animation != null) {
                        if (mainViewModel.isPlaying()) {
                            animation.start();
                        } else {
                            animation.stop();
                        }
                    }
                } else {
                    holder.playEq.setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return currentTrackList.size();
        }

        public void refreshAdapter(List<AudioFile> audioFiles) {
            currentTrackList.clear();
            currentTrackList.addAll(audioFiles);
            notifyDataSetChanged();
        }

        public void clearAdapter() {
            currentTrackList.clear();
            notifyDataSetChanged();
        }
    }
}
