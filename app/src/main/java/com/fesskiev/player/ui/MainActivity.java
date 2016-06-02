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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.model.User;
import com.fesskiev.player.services.FileObserverService;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.services.RESTService;
import com.fesskiev.player.ui.about.AboutActivity;
import com.fesskiev.player.ui.audio.AudioFragment;
import com.fesskiev.player.ui.equalizer.EqualizerActivity;
import com.fesskiev.player.ui.playback.PlaybackActivity;
import com.fesskiev.player.ui.playlist.PlaylistActivity;
import com.fesskiev.player.ui.settings.SettingsActivity;
import com.fesskiev.player.ui.video.VideoFilesFragment;
import com.fesskiev.player.ui.vk.MusicVKActivity;
import com.fesskiev.player.utils.AppSettingsManager;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.CacheManager;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.utils.http.URLHelper;
import com.fesskiev.player.widgets.dialogs.BassBoostDialog;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;


public class MainActivity extends PlaybackActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSION_REQ = 0;

    private NavigationView navigationViewEffects;
    private NavigationView navigationViewMain;
    private DrawerLayout drawer;
    private AppSettingsManager settingsManager;
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
        settingsManager = AppSettingsManager.getInstance(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEmptyUserInfo();
                clearUserInfo();
                logoutHide();
            }
        });


        userPhoto = (ImageView) headerLayout.findViewById(R.id.photo);
        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (settingsManager.isAuthTokenEmpty()) {
                    String[] vkScope = new String[]{VKScope.DIRECT, VKScope.AUDIO};
                    VKSdk.login(MainActivity.this, vkScope);
                } else {
                    startActivity(new Intent(MainActivity.this, MusicVKActivity.class));
                }
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


        SwitchCompat eqSwitch = (SwitchCompat)
                navigationViewEffects.getMenu().findItem(R.id.equalizer).getActionView().findViewById(R.id.eq_switch);
        eqSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v(TAG, "EQ " + isChecked);
            }
        });

        SwitchCompat bassSwitch = (SwitchCompat)
                navigationViewEffects.getMenu().findItem(R.id.bass).getActionView().findViewById(R.id.bass_switch);
        if (settingsManager.isBassBoostOn()) {
            bassSwitch.setChecked(true);
        }
        bassSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PlaybackService.changeBassBoostState(MainActivity.this, PlaybackService.BASS_BOOST_ON);
                } else {
                    PlaybackService.changeBassBoostState(MainActivity.this, PlaybackService.BASS_BOOST_OFF);
                }
                settingsManager.setBassBoostState(isChecked);
            }
        });
        visibleBassBoostMenu(false);
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
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
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
                break;
            case R.id.audio_content:
                checkAudioContentItem();
                addAudioFragment(false);
                break;
            case R.id.video_content:
                checkVideoContentItem();
                addVideoFilesFragment();
                break;
            case R.id.playlist:
                startActivity(new Intent(this, PlaylistActivity.class));
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
                        .setAction(getString(R.string.snack_exit_action), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish = true;
                                finish();
                            }
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
        filter.addAction(RESTService.ACTION_USER_PROFILE_RESULT);
        filter.addAction(PlaybackService.ACTION_PLAYBACK_BASS_BOOST_STATE);
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
        PlaybackService.destroyPlayer(this);
        resetAudioPlayer();
        FileObserverService.stopFileObserverService(this);
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
                case RESTService.ACTION_USER_PROFILE_RESULT:
                    setUserProfile(context, intent);
                    break;
                case PlaybackService.ACTION_PLAYBACK_BASS_BOOST_STATE:
                    setBassBoostState(intent);
                    break;
            }
        }
    };

    private void setBassBoostState(Intent intent) {
        int bassBoostState =
                intent.getIntExtra(PlaybackService.PLAYBACK_EXTRA_BASS_BOOST_STATE, -1);
        if (bassBoostState != -1) {
            switch (bassBoostState) {
                case PlaybackService.BASS_BOOST_SUPPORT:
                    visibleBassBoostMenu(true);
                    break;
                case PlaybackService.BASS_BOOST_NOT_SUPPORT:
                    visibleBassBoostMenu(false);
                    break;
            }
        }
    }

    private void visibleBassBoostMenu(boolean visible) {
        navigationViewEffects.getMenu().findItem(R.id.bass).setVisible(visible);
    }

    private void setUserProfile(Context context, Intent intent) {
        User user = intent.getParcelableExtra(RESTService.EXTRA_USER_PROFILE_RESULT);
        if (user != null) {

            firstName.setText(user.firstName);
            lastName.setText(user.lastName);


            settingsManager.setUserFirstName(user.firstName);
            settingsManager.setUserLastName(user.lastName);

            BitmapHelper.loadURLAvatar(context,
                    user.photoUrl, userPhoto, new BitmapHelper.OnBitmapLoad() {
                        @Override
                        public void onLoaded(Bitmap bitmap) {
                            BitmapHelper.saveUserPhoto(bitmap);
                        }
                    });
        }
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

    private void saveDownloadFolderIcon() {
        BitmapHelper.saveBitmap(
                BitmapHelper.getBitmapFromResource(getApplicationContext(), R.drawable.icon_folder_download),
                CacheManager.getDownloadFolderIconPath());
    }

    private void checkAppFirstStart() {
        if (settingsManager == null) {
            settingsManager = AppSettingsManager.getInstance(getApplication());
        }
        if (settingsManager.isFirstStartApp()) {
            settingsManager.setFirstStartApp();
            saveDownloadFolderIcon();
            addAudioFragment(true);
        } else {
            addAudioFragment(false);
        }
    }

    private void addAudioFragment(boolean isFetchAudio) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        hideVisibleFragment(transaction);

        AudioFragment audioFragment = (AudioFragment) getSupportFragmentManager().
                findFragmentByTag(AudioFragment.class.getName());
        if (audioFragment == null) {
            Log.d(TAG, "audio fragment is null");
            transaction.add(R.id.content, AudioFragment.newInstance(isFetchAudio),
                    AudioFragment.class.getName());
            transaction.addToBackStack(AudioFragment.class.getName());
        } else {
            Log.d(TAG, "audio fragment not null");
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
        VideoFilesFragment videoFragment = (VideoFilesFragment) getSupportFragmentManager().
                findFragmentByTag(VideoFilesFragment.class.getName());
        return videoFragment != null && videoFragment.isAdded() && videoFragment.isVisible();
    }

    private void addVideoFilesFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        hideVisibleFragment(transaction);

        VideoFilesFragment videoFilesFragment = (VideoFilesFragment) getSupportFragmentManager().
                findFragmentByTag(VideoFilesFragment.class.getName());
        if (videoFilesFragment == null) {
            Log.d(TAG, "video fragment is null");
            transaction.add(R.id.content, VideoFilesFragment.newInstance(),
                    VideoFilesFragment.class.getName());
            transaction.addToBackStack(VideoFilesFragment.class.getName());
        } else {
            Log.d(TAG, "video fragment not null");
            transaction.show(videoFilesFragment);
        }
        transaction.commit();
    }

    private void hideVisibleFragment(FragmentTransaction transaction) {

        AudioFragment audioFragment = (AudioFragment) getSupportFragmentManager().
                findFragmentByTag(AudioFragment.class.getName());
        if (audioFragment != null && audioFragment.isAdded() && audioFragment.isVisible()) {
            Log.d(TAG, "hide audio fragment");
            transaction.hide(audioFragment);
        }

        VideoFilesFragment videoFilesFragment = (VideoFilesFragment) getSupportFragmentManager().
                findFragmentByTag(VideoFilesFragment.class.getName());
        if (videoFilesFragment != null && videoFilesFragment.isAdded() && videoFilesFragment.isVisible()) {
            Log.d(TAG, "hide video fragment");
            transaction.hide(videoFilesFragment);
        }
    }


    private void showPermissionSnackbar() {
        Utils.showCustomSnackbar(findViewById(R.id.content),
                getApplicationContext(),
                getString(R.string.permission_read_external_storage),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.button_ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestPermissions();
                    }
                })
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
                startActivity(new Intent(MainActivity.this, MusicVKActivity.class));
            }

            @Override
            public void onError(VKError error) {
                Log.d(TAG, "auth fail: " + error.errorMessage);
                finish();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void makeRequestUserProfile() {
        RESTService.fetchUserProfile(this, URLHelper.getUserProfileURL(settingsManager.getUserId()));
    }
}
