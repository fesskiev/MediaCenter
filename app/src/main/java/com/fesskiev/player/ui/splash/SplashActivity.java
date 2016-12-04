package com.fesskiev.player.ui.splash;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.data.source.DataRepository;
import com.fesskiev.player.services.FileObserverService;
import com.fesskiev.player.services.FileSystemIntentService;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.ui.MainActivity;
import com.fesskiev.player.utils.AppSettingsManager;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.FetchMediaFilesManager;
import com.fesskiev.player.utils.RxUtils;
import com.fesskiev.player.widgets.dialogs.PermissionDialog;


import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class SplashActivity extends AppCompatActivity {

    private static final int STARTUP_DELAY = 300;
    private static final int ANIM_ITEM_DURATION = 1000;
    private static final int ITEM_DELAY = 300;

    private static final int PERMISSION_REQ = 0;

    private AppSettingsManager settingsManager;
    private DataRepository repository;
    private Subscription subscription;
    private FetchMediaFilesManager fetchMediaFilesManager;
    private PermissionDialog permissionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        repository = MediaApplication.getInstance().getRepository();
        settingsManager = AppSettingsManager.getInstance(this);

        fetchMediaFilesManager = new FetchMediaFilesManager(this);
        fetchMediaFilesManager.setOnFetchMediaFilesListener(
                new FetchMediaFilesManager.OnFetchMediaFilesListener() {
                    @Override
                    public void onFetchContentStart() {

                    }

                    @Override
                    public void onFetchContentFinish() {
                        settingsManager.setFirstStartApp();
                        loadMediaFiles();
                    }
                });
        animate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        fetchMediaFilesManager.unregister();
        RxUtils.unsubscribe(subscription);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (permissionDialog != null) {
                        permissionDialog.hide();
                    }
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

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQ);
    }

    private void checkPermission() {
        if (!checkPermissions()) {
            showPermissionDialog();
        } else {
            checkAppFirstStart();
            loadMediaFiles();
            PlaybackService.startPlaybackService(this);
        }
    }

    //TODO add cancel granted permission later
    private void showPermissionDialog() {
        permissionDialog = PermissionDialog.newInstance(SplashActivity.this);
        permissionDialog.setOnPermissionDialogListener(new PermissionDialog.OnPermissionDialogListener() {
            @Override
            public void onPermissionGranted() {
                requestPermissions();
            }

            @Override
            public void onPermissionCancel() {
                permissionDialog.hide();
                finish();
            }
        });
        permissionDialog.show();
    }

    private void checkAppFirstStart() {
        if (settingsManager == null) {
            settingsManager = AppSettingsManager.getInstance(getApplication());
        }
        if (settingsManager.isFirstStartApp()) {
            BitmapHelper.getInstance().saveDownloadFolderIcon();
            FileSystemIntentService.startFetchMedia(getApplicationContext());
        } else {
            FileObserverService.startFileObserverService(getApplicationContext());
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


    private void animate() {
        ImageView appLogo = (ImageView) findViewById(R.id.appLogo);
        ViewGroup container = (ViewGroup) findViewById(R.id.container);

        ViewCompat.animate(appLogo)
                .translationY(-250)
                .setStartDelay(STARTUP_DELAY + 500)
                .setDuration(ANIM_ITEM_DURATION)
                .setInterpolator(new DecelerateInterpolator(1.2f))
                .setListener(new ViewPropertyAnimatorListener() {

                    private boolean ended = true;

                    @Override
                    public void onAnimationStart(View view) {

                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        if (ended) {
                            ended = false;
                            checkPermission();
                        }
                    }

                    @Override
                    public void onAnimationCancel(View view) {

                    }
                }).start();

        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            ViewCompat.animate(v)
                    .translationY(50).alpha(1)
                    .setStartDelay((ITEM_DELAY * i) + 500)
                    .setDuration(1000)
                    .start();
        }
    }

    private void loadMediaFiles() {
        subscription = Observable.zip(
                repository.getAudioFolders(),
                repository.getGenres(),
                repository.getArtists(),
                (audioFolders, genres, artists) -> Observable.just(null))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });

    }
}
