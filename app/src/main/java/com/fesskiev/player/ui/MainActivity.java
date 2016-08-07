package com.fesskiev.player.ui;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.ui.vk.data.model.User;
import com.fesskiev.player.services.FileObserverService;
import com.fesskiev.player.services.FileSystemIntentService;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.ui.about.AboutActivity;
import com.fesskiev.player.ui.audio.AudioFragment;
import com.fesskiev.player.ui.equalizer.EqualizerActivity;
import com.fesskiev.player.ui.playback.PlaybackActivity;
import com.fesskiev.player.ui.playlist.PlayListActivity;
import com.fesskiev.player.ui.settings.SettingsActivity;
import com.fesskiev.player.ui.video.VideoFragment;
import com.fesskiev.player.ui.vk.VkontakteActivity;
import com.fesskiev.player.ui.vk.data.source.DataRepository;
import com.fesskiev.player.utils.AnimationUtils;
import com.fesskiev.player.utils.AppLog;
import com.fesskiev.player.utils.AppSettingsManager;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.RxUtils;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.dialogs.FetchMediaContentDialog;
import com.fesskiev.player.widgets.dialogs.effects.BassBoostDialog;
import com.fesskiev.player.widgets.dialogs.effects.VirtualizerDialog;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends PlaybackActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSION_REQ = 0;

    private Subscription subscription;
    private FetchMediaContentDialog mediaContentDialog;
    private NavigationView navigationViewEffects;
    private NavigationView navigationViewMain;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private AppSettingsManager settingsManager;
    private SwitchCompat eqSwitch;
    private SwitchCompat bassSwitch;
    private SwitchCompat virtualizerSwitch;
    private ImageView userPhoto;
    private ImageView logoutButton;
    private ImageView headerAnimation;
    private TextView firstName;
    private TextView lastName;
    private boolean finish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AnimationUtils.setupSlideWindowAnimations(this);

        settingsManager = AppSettingsManager.getInstance(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        animateToolbar();

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
                ((AnimationDrawable) headerAnimation.getDrawable()).start();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                ((AnimationDrawable) headerAnimation.getDrawable()).stop();
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        toggle.syncState();

        setEffectsNavView();
        setMainNavView();

        registerBroadcastReceiver();
        if (!settingsManager.isAuthTokenEmpty()) {
            setUserInfo();
        } else {
            setEmptyUserInfo();
        }

        checkPermission();
        checkAudioContentItem();
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
                String[] vkScope = new String[]{VKScope.DIRECT, VKScope.AUDIO};
                VKSdk.login(MainActivity.this, vkScope);
            } else {
                startActivity(new Intent(getApplicationContext(), VkontakteActivity.class));
            }
        });
        firstName = (TextView) headerLayout.findViewById(R.id.firstName);
        lastName = (TextView) headerLayout.findViewById(R.id.lastName);
    }

    private void setEffectsNavView() {
        navigationViewEffects = (NavigationView) findViewById(R.id.nav_view_effects);
        navigationViewEffects.setNavigationItemSelectedListener(this);
        View effectsHeaderLayout =
                navigationViewEffects.inflateHeaderView(R.layout.nav_header_effects);

        headerAnimation = (ImageView) effectsHeaderLayout.findViewById(R.id.effectHeaderAnimation);


        eqSwitch = (SwitchCompat) navigationViewEffects.getMenu().
                findItem(R.id.equalizer).getActionView().findViewById(R.id.eq_switch);
        eqSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            PlaybackService.changeEQState(getApplicationContext());
            settingsManager.setEQState(isChecked);
        });

        bassSwitch = (SwitchCompat) navigationViewEffects.getMenu().
                findItem(R.id.bass).getActionView().findViewById(R.id.bass_switch);
        bassSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            PlaybackService.changeBassBoostState(getApplicationContext());
            settingsManager.setBassBoostState(isChecked);
        });

        virtualizerSwitch = (SwitchCompat) navigationViewEffects.getMenu().
                findItem(R.id.virtualizer).getActionView().findViewById(R.id.virtualizer_switch);
        virtualizerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            PlaybackService.changeVirtualizerState(getApplicationContext());
            settingsManager.setVirtualizerState(isChecked);
        });
    }

    private void setUserInfo() {
        BitmapHelper.loadBitmapAvatar(this, BitmapHelper.getUserPhoto(), userPhoto);

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
        BitmapHelper.loadEmptyAvatar(this, userPhoto);

        firstName.setText(getString(R.string.empty_first_name));
        lastName.setText(getString(R.string.empty_last_name));
    }

    private void clearUserInfo() {
        settingsManager.setUserFirstName("");
        settingsManager.setUserLastName("");
        settingsManager.setAuthToken("");
        settingsManager.setAuthSecret("");
        settingsManager.setUserId("");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");

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
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class),
                        AnimationUtils.createBundle(this));
                break;
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class),
                        AnimationUtils.createBundle(this));
                break;
            case R.id.equalizer:
                startActivity(new Intent(this, EqualizerActivity.class));
                break;
            case R.id.bass:
                if (settingsManager.isBassBoostOn()) {
                    BassBoostDialog.getInstance(this);
                }
                break;
            case R.id.virtualizer:
                if (settingsManager.isVirtualizerOn()) {
                    VirtualizerDialog.getInstance(this);
                }
                break;
            case R.id.audio_content:
                checkAudioContentItem();
                addAudioFragment();
                break;
            case R.id.video_content:
                checkVideoContentItem();
                addVideoFragment();
                break;
            case R.id.playlist:
                startActivity(new Intent(this, PlayListActivity.class),
                        AnimationUtils.createBundle(this));
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


    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlaybackService.ACTION_PLAYBACK_BASS_BOOST_STATE);
        filter.addAction(PlaybackService.ACTION_PLAYBACK_BASS_BOOST_SUPPORT);
        filter.addAction(PlaybackService.ACTION_PLAYBACK_VIRTUALIZER_STATE);
        filter.addAction(PlaybackService.ACTION_PLAYBACK_VIRTUALIZER_SUPPORT);
        filter.addAction(PlaybackService.ACTION_PLAYBACK_EQ_STATE);
        filter.addAction(FileSystemIntentService.ACTION_START_FETCH_MEDIA_CONTENT);
        filter.addAction(FileSystemIntentService.ACTION_END_FETCH_MEDIA_CONTENT);
        filter.addAction(FileSystemIntentService.ACTION_AUDIO_FOLDER_NAME);
        filter.addAction(FileSystemIntentService.ACTION_AUDIO_TRACK_NAME);
        filter.addAction(FileSystemIntentService.ACTION_VIDEO_FILE);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                filter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcastReceiver();
        PlaybackService.destroyPlayer(getApplicationContext());
        resetAudioPlayer();
        FileObserverService.stopFileObserverService(getApplicationContext());

        RxUtils.unsubscribe(subscription);
    }

    private void resetAudioPlayer() {
        AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        audioPlayer.isPlaying = false;
        audioPlayer.currentAudioFolder = null;
        audioPlayer.currentAudioFile = null;
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {
            switch (intent.getAction()) {
                case PlaybackService.ACTION_PLAYBACK_BASS_BOOST_STATE:
                    setBassBoostState(intent);
                    break;
                case PlaybackService.ACTION_PLAYBACK_BASS_BOOST_SUPPORT:
                    setBassBoostSupport(intent);
                    break;
                case PlaybackService.ACTION_PLAYBACK_VIRTUALIZER_STATE:
                    setVirtualizerState(intent);
                    break;
                case PlaybackService.ACTION_PLAYBACK_VIRTUALIZER_SUPPORT:
                    setVirtualizertSupport(intent);
                    break;
                case PlaybackService.ACTION_PLAYBACK_EQ_STATE:
                    setEQState(intent);
                    break;
                case FileSystemIntentService.ACTION_START_FETCH_MEDIA_CONTENT:
                    mediaContentDialog = FetchMediaContentDialog.newInstance(MainActivity.this);
                    mediaContentDialog.show();
                    break;
                case FileSystemIntentService.ACTION_END_FETCH_MEDIA_CONTENT:
                    if (mediaContentDialog != null) {
                        mediaContentDialog.hide();
                    }
                    if (settingsManager.isFirstStartApp()) {
                        settingsManager.setFirstStartApp();
                        FileObserverService.startFileObserverService(getApplicationContext());
                    }
                    updateMediaContent();
                    break;
                case FileSystemIntentService.ACTION_AUDIO_FOLDER_NAME:
                    String folderName =
                            intent.getStringExtra(FileSystemIntentService.EXTRA_AUDIO_FOLDER_NAME);
                    if (mediaContentDialog != null) {
                        mediaContentDialog.setFolderName(folderName);
                    }
                    break;
                case FileSystemIntentService.ACTION_AUDIO_TRACK_NAME:
                    String trackName =
                            intent.getStringExtra(FileSystemIntentService.EXTRA_AUDIO_TRACK_NAME);
                    if (mediaContentDialog != null) {
                        mediaContentDialog.setFileName(trackName);
                    }
                    break;
                case FileSystemIntentService.ACTION_VIDEO_FILE:
                    String videoFileName =
                            intent.getStringExtra(FileSystemIntentService.EXTRA_VIDEO_FILE_NAME);
                    if (mediaContentDialog != null) {
                        mediaContentDialog.setFileName(videoFileName);
                    }
                    break;
            }
        }
    };


    private void updateMediaContent() {
        if (isAudioFragmentShow()) {
            AudioFragment audioFragment = (AudioFragment) getSupportFragmentManager().
                    findFragmentByTag(AudioFragment.class.getName());
            if (audioFragment != null) {
                audioFragment.fetchAudioContent();
            }
        } else if (isVideoFragmentShow()) {
            VideoFragment videoFragment = (VideoFragment) getSupportFragmentManager().
                    findFragmentByTag(VideoFragment.class.getName());
            videoFragment.fetchVideoContent();
        }
    }

    private void setEQState(Intent intent) {
        boolean eqState =
                intent.getBooleanExtra(PlaybackService.PLAYBACK_EXTRA_EQ_STATE, false);
        eqSwitch.setChecked(eqState);
    }

    private void setVirtualizertSupport(Intent intent) {
        boolean virtualizerSupport =
                intent.getBooleanExtra(PlaybackService.PLAYBACK_EXTRA_VIRTUALIZER_SUPPORT, false);
        visibleVirtualizerMenu(virtualizerSupport);
    }

    private void setVirtualizerState(Intent intent) {
        boolean virtualizerState =
                intent.getBooleanExtra(PlaybackService.PLAYBACK_EXTRA_VIRTUALIZER_STATE, false);
        virtualizerSwitch.setChecked(virtualizerState);
    }

    private void setBassBoostSupport(Intent intent) {
        boolean bassBoostSupport =
                intent.getBooleanExtra(PlaybackService.PLAYBACK_EXTRA_BASS_BOOST_SUPPORT, false);
        visibleBassBoostMenu(bassBoostSupport);
    }

    private void setBassBoostState(Intent intent) {
        boolean bassBoostState =
                intent.getBooleanExtra(PlaybackService.PLAYBACK_EXTRA_BASS_BOOST_STATE, false);
        bassSwitch.setChecked(bassBoostState);
    }

    private void visibleBassBoostMenu(boolean visible) {
        navigationViewEffects.getMenu().findItem(R.id.bass).setVisible(visible);
    }

    private void visibleVirtualizerMenu(boolean visible) {
        navigationViewEffects.getMenu().findItem(R.id.virtualizer).setVisible(visible);
    }


    private void checkPermission() {
        if (!checkPermissions()) {
            showPermissionSnackbar();
        } else {
            checkAppFirstStart();
            PlaybackService.startPlaybackService(this);
        }
    }

    public boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                        this, Manifest.permission.MODIFY_AUDIO_SETTINGS) &&
                PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                        this, Manifest.permission.RECORD_AUDIO);
    }

    private void checkAppFirstStart() {
        if (settingsManager == null) {
            settingsManager = AppSettingsManager.getInstance(getApplication());
        }
        if (settingsManager.isFirstStartApp()) {
            BitmapHelper.saveDownloadFolderIcon(getApplicationContext());
            addAudioFragment();
            FileSystemIntentService.startFetchMedia(getApplicationContext());
        } else {
            addAudioFragment();
            FileObserverService.startFileObserverService(getApplicationContext());
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

    private void showPermissionSnackbar() {
        Utils.showCustomSnackbar(findViewById(R.id.content),
                getApplicationContext(),
                getString(R.string.permission_read_external_storage),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.button_ok, view -> requestPermissions())
                .show();
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQ);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkAppFirstStart();
                    PlaybackService.startPlaybackService(this);
                } else {
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Log.d(TAG, "auth success: " + res.accessToken);

                settingsManager.setAuthToken(res.accessToken);
                settingsManager.setAuthSecret(res.secret);
                settingsManager.setUserId(res.userId);

                logoutShow();

                makeRequestUserProfile();
            }

            @Override
            public void onError(VKError error) {
                Log.d(TAG, "auth fail: " + error.errorMessage);
                Utils.showCustomSnackbar(getCurrentFocus(),
                        getApplicationContext(),
                        getString(R.string.snackbar_vk_auth_error),
                        Snackbar.LENGTH_SHORT).show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void makeRequestUserProfile() {
        DataRepository repository = DataRepository.getInstance();
        subscription = repository.getUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userResponse -> {
                    setUserProfile(userResponse.getUser());
                    startActivity(new Intent(MainActivity.this, VkontakteActivity.class));
                }, throwable -> {AppLog.ERROR(throwable.getMessage());});
    }

    private void setUserProfile(User user) {
        if (user != null) {

            AppLog.ERROR(user.toString());

            firstName.setText(user.getFirstName());
            lastName.setText(user.getLastName());

            settingsManager.setUserFirstName(user.getFirstName());
            settingsManager.setUserLastName(user.getLastName());

            BitmapHelper.loadURLAvatar(getApplicationContext(),
                    user.getPhotoUrl(), userPhoto, new BitmapHelper.OnBitmapLoadListener() {
                        @Override
                        public void onLoaded(Bitmap bitmap) {
                            BitmapHelper.saveUserPhoto(bitmap);
                        }

                        @Override
                        public void onFailed() {

                        }
                    });
        }
    }

    private void animateToolbar() {
        View view = toolbar.getChildAt(0);
        if (view != null && view instanceof TextView) {
            TextView title = (TextView) view;
            title.setAlpha(0f);
            title.setScaleX(0.6f);
            title.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .setStartDelay(300)
                    .setDuration(900)
                    .setInterpolator(new FastOutSlowInInterpolator());
        }
    }

}
