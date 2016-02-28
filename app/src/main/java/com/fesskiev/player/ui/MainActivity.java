package com.fesskiev.player.ui;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.User;
import com.fesskiev.player.services.FileTreeIntentService;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.services.RESTService;
import com.fesskiev.player.ui.player.PlaybackActivity;
import com.fesskiev.player.ui.settings.SettingsActivity;
import com.fesskiev.player.ui.soundcloud.SoundCloudActivity;
import com.fesskiev.player.ui.vk.MusicVKActivity;
import com.fesskiev.player.utils.AppSettingsManager;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


public class MainActivity extends PlaybackActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSION_REQ = 0;
    private AppSettingsManager appSettingsManager;
    private Handler handler;
    private ImageView userPhoto;
    private TextView firstName;
    private TextView lastName;
    private boolean finish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();
        appSettingsManager = AppSettingsManager.getInstance(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        View headerLayout =
                navigationView.inflateHeaderView(R.layout.nav_header_main);

        userPhoto = (ImageView) headerLayout.findViewById(R.id.photo);
        firstName = (TextView) headerLayout.findViewById(R.id.firstName);
        lastName = (TextView) headerLayout.findViewById(R.id.lastName);


        if (savedInstanceState == null) {
            FragmentTransaction transaction =
                    getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, MainFragment.newInstance(),
                    MainFragment.class.getName());
            transaction.addToBackStack(null);
            transaction.commit();
        }

        registerBroadcastReceiver();
        if (!appSettingsManager.isAuthTokenEmpty()) {
            setUserInfo();
        } else {
            setEmptyUserInfo();
        }
        checkPermission();
    }

    private void setUserInfo() {
        userPhoto.setImageBitmap(appSettingsManager.getUserPhoto());
        firstName.setText(appSettingsManager.getUserFirstName());
        lastName.setText(appSettingsManager.getUserLastName());
    }

    private void setEmptyUserInfo() {
        userPhoto.setImageResource(R.drawable.no_cover_icon);
        firstName.setText(getString(R.string.empty_first_name));
        lastName.setText(getString(R.string.empty_last_name));
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.vk_music:
                startActivity(new Intent(this, MusicVKActivity.class));
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.soundcloud_music:
                startActivity(new Intent(this, SoundCloudActivity.class));
                break;
        }


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        }, 500);

        return true;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            hidePlayback();
            View view = findViewById(R.id.content);
            if (view != null) {
                Snackbar.make(view, getString(R.string.snack_exit_text), Snackbar.LENGTH_LONG)
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
        LocalBroadcastManager.getInstance(this).registerReceiver(userProfileReceiver,
                filter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userProfileReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcastReceiver();
        PlaybackService.destroyPlayer(this);
        MediaApplication.getInstance().getAudioPlayer().isPlaying = false;
    }

    private BroadcastReceiver userProfileReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case RESTService.ACTION_USER_PROFILE_RESULT:

                    User user = intent.getParcelableExtra(RESTService.EXTRA_USER_PROFILE_RESULT);
                    if (user != null) {

                        firstName.setText(user.firstName);
                        lastName.setText(user.lastName);


                        appSettingsManager.setUserFirstName(user.firstName);
                        appSettingsManager.setUserLastName(user.lastName);

                        Picasso.with(getApplicationContext()).
                                load(user.photoUrl).
                                resize(128, 128).
                                into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        userPhoto.setImageBitmap(bitmap);
                                        appSettingsManager.saveUserPhoto(bitmap);
                                    }

                                    @Override
                                    public void onBitmapFailed(Drawable errorDrawable) {

                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                                    }
                                });
                    }
                    break;
            }
        }
    };

    private void checkPermission() {
        if (!checkPermissions()) {
            showPermissionSnackbar();
        } else {
            FileTreeIntentService.startFileTreeService(this);
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

    private void showPermissionSnackbar() {
        Snackbar.make(findViewById(R.id.content), R.string.permission_read_external_storage,
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
                    FileTreeIntentService.startFileTreeService(this);
                    PlaybackService.startPlaybackService(this);
                } else {
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
