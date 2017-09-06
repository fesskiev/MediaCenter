package com.fesskiev.mediacenter.ui.splash;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.ui.MainActivity;
import com.fesskiev.mediacenter.ui.walkthrough.WalkthroughActivity;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;

import java.io.File;
import java.util.UUID;

import io.reactivex.Observable;;
import io.reactivex.android.schedulers.AndroidSchedulers;;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class SplashActivity extends AppCompatActivity {

    public static final String EXTRA_OPEN_FROM_ACTION = "com.fesskiev.mediacenter.extra.ACTION_VIEW";

    private static final int STARTUP_DELAY = 300;
    private static final int ANIM_ITEM_DURATION = 1000;
    private static final int ITEM_DELAY = 300;

    private DataRepository repository;
    private Disposable subscription;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AppSettingsManager settingsManager = AppSettingsManager.getInstance();
        if (settingsManager.isFirstStartApp()) {
            startWalkthroughActivity();
        } else {
            startApplication();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void startApplication() {
        repository = MediaApplication.getInstance().getRepository();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_VIEW.equals(action) && type != null) {
            if (type.startsWith("audio/")) {
                Uri uri = intent.getData();
                if (uri != null) {
                    parseActionViewPath(uri.getPath());
                }
            }
        } else {
            animateAndFetchData(false);
        }
    }

    private void startWalkthroughActivity() {
        startActivity(new Intent(this, WalkthroughActivity.class));
        finish();
    }

    private void parseActionViewPath(String path) {
        subscription = Observable.just(repository.containAudioTrack(path))
                .subscribeOn(Schedulers.io())
                .flatMap(contain -> {
                    if (contain) {
                        return selectAudioFolderAndFile(path);
                    }
                    return parseAudioFolder(path);
                })
                .firstOrError()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(v -> animateAndFetchData(true));
    }


    private Observable<Void> selectAudioFolderAndFile(String path) {
        return repository.getAudioFileByPath(path)
                .flatMap(audioFile -> {
                    if (audioFile != null) {
                        audioFile.isSelected = true;
                        repository.updateSelectedAudioFile(audioFile);
                    }
                    return audioFile != null ? Observable.just(audioFile) : Observable.empty();
                })
                .flatMap(audioFile -> repository.getAudioFolderByPath(new File(path).getParent()))
                .flatMap(audioFolder -> {
                    if (audioFolder != null) {
                        audioFolder.isSelected = true;
                        repository.updateSelectedAudioFolder(audioFolder);
                    }
                    return Observable.empty();
                });
    }

    private Observable<Void> parseAudioFolder(String path) {
        return Observable.just(new File(path))
                .flatMap(file -> {
                    File dir = file.getParentFile();
                    if (dir.isDirectory()) {
                        createParsingFolderSnackBar(dir.getName());
                        return Observable.just(dir);
                    }
                    return Observable.empty();

                }).flatMap(dir -> {
                    if (dir != null) {
                        parseAudioFolderAndFile(dir, path);
                    }
                    return Observable.empty();
                });
    }

    private void parseAudioFolderAndFile(File dir, String path) {
        AudioFolder audioFolder = new AudioFolder();

        audioFolder.folderPath = dir;
        audioFolder.folderName = dir.getName();
        audioFolder.id = UUID.randomUUID().toString();
        audioFolder.timestamp = System.currentTimeMillis();

        File[] filterImages = dir.listFiles(FileSystemService.folderImageFilter());
        if (filterImages != null && filterImages.length > 0) {
            audioFolder.folderImage = filterImages[0];
        }

        File[] audioPaths = dir.listFiles(FileSystemService.audioFilter());

        if (audioPaths != null && audioPaths.length > 0) {
            for (File p : audioPaths) {
                AudioFile audioFile = new AudioFile(getApplicationContext(), p, audioFolder.id);
                repository.insertAudioFile(audioFile);
                if (p.getAbsolutePath().equals(path)) {
                    Log.d("test", "parse select file: " + audioFile.toString());
                    audioFile.isSelected = true;
                    repository.updateSelectedAudioFile(audioFile);
                }
            }
        }

        repository.insertAudioFolder(audioFolder);

        Log.d("test", "parse select folder: " + audioFolder.toString());
        audioFolder.isSelected = true;
        repository.updateSelectedAudioFolder(audioFolder);
    }

    private void createParsingFolderSnackBar(String name) {
        Utils.showCustomSnackbar(findViewById(R.id.splashRoot), getApplicationContext(),
                String.format("%1s %2s", getString(R.string.splash_folder_parsing), name),
                Snackbar.LENGTH_LONG).show();
    }

    private void animateAndFetchData(boolean fromAction) {
        ImageView appLogo = findViewById(R.id.appLogo);
        ViewGroup container = findViewById(R.id.container);

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
                            loadMediaFiles(fromAction);
                        }
                    }

                    @Override
                    public void onAnimationCancel(View view) {

                    }
                }).start();

        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            ViewCompat.animate(v)
                    .alpha(1)
                    .setStartDelay((ITEM_DELAY * i) + 500)
                    .setDuration(1000)
                    .start();
        }
    }

    private void loadMediaFiles(boolean fromAction) {
        subscription = Observable.zip(
                repository.getAudioFolders(),
                repository.getGenresList(),
                repository.getArtistsList(),
                repository.getVideoFolders(),
                (audioFolders, genres, artists, videoFolders) -> Observable.empty())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> startMainActivity(fromAction));
    }

    private void startMainActivity(boolean fromAction) {
        Intent intent = new Intent(this, MainActivity.class);
        if (fromAction) {
            intent.putExtra(EXTRA_OPEN_FROM_ACTION, true);
        }
        startActivity(intent);
        finish();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
    }

}
