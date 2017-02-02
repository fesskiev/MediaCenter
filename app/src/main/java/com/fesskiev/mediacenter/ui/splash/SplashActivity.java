package com.fesskiev.mediacenter.ui.splash;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.services.FileSystemIntentService;
import com.fesskiev.mediacenter.ui.MainActivity;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.FetchMediaFilesManager;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.dialogs.PermissionDialog;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        repository = MediaApplication.getInstance().getRepository();
        settingsManager = AppSettingsManager.getInstance();

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
    protected void onDestroy() {
        super.onDestroy();
        fetchMediaFilesManager.unregister();
        RxUtils.unsubscribe(subscription);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @Nullable String permissions[], @Nullable int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        fetchAudioContent();

                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale =
                                ActivityCompat.shouldShowRequestPermissionRationale(this,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(getString(R.string.dialog_permission_title));
        builder.setMessage(R.string.dialog_permission_message);
        builder.setPositiveButton(R.string.dialog_permission_ok,
                (dialog, which) -> Utils.startSettingsActivity(getApplicationContext()));
        builder.setNegativeButton(R.string.dialog_permission_cancel,
                (dialog, which) -> finish());
        builder.setOnCancelListener(dialog -> finish());
        builder.show();
    }

    private void permissionsDenied() {
        Utils.showCustomSnackbar(findViewById(R.id.splashRoot), getApplicationContext(),
                getString(R.string.snackbar_permission_title), Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.snackbar_permission_button, v -> checkRuntimePermission())
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

    private void checkRuntimePermission() {
        if (!checkPermissions()) {
            showPermissionDialog();
        } else {
            fetchAudioContent();
        }
    }

    private void showPermissionDialog() {
        PermissionDialog permissionDialog = PermissionDialog.newInstance(SplashActivity.this);
        permissionDialog.setOnPermissionDialogListener(new PermissionDialog.OnPermissionDialogListener() {
            @Override
            public void onPermissionGranted() {
                requestPermissions();
            }

            @Override
            public void onPermissionCancel() {
                permissionsDenied();
            }
        });
        permissionDialog.show();
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
                .translationY(-Utils.dipToPixels(getApplicationContext(), 100))
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
                            startFetchLogic();

                        }
                    }

                    @Override
                    public void onAnimationCancel(View view) {

                    }
                }).start();

        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            ViewCompat.animate(v)
                    .translationY(50)
                    .alpha(1)
                    .setStartDelay((ITEM_DELAY * i) + 500)
                    .setDuration(1000)
                    .start();
        }
    }

    private void startFetchLogic() {
        if (Utils.isMarshmallow()) {
            checkRuntimePermission();
        } else {
            fetchAudioContent();
        }
    }

    private void fetchAudioContent() {
        if (settingsManager.isFirstStartApp()) {
            BitmapHelper.getInstance().saveDownloadFolderIcon();
            FileSystemIntentService.startFetchMedia(getApplicationContext());
        } else {
            loadMediaFiles();
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
