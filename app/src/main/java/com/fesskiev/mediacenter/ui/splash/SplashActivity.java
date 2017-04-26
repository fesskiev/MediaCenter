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

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class SplashActivity extends AppCompatActivity {

    public static final String EXTRA_OPEN_FROM_ACTION = "com.fesskiev.mediacenter.extra.ACTION_VIEW";

    private static final int STARTUP_DELAY = 300;
    private static final int ANIM_ITEM_DURATION = 1000;
    private static final int ITEM_DELAY = 300;

    private DataRepository repository;
    private Subscription subscription;

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

    private void startApplication() {
        repository = MediaApplication.getInstance().getRepository();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_VIEW.equals(action) && type != null) {
            if (type.startsWith("audio/")) {
                Uri uri = intent.getData();

                parseActionViewPath(uri.getPath());
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
                        return selectAudioFolderAnFile(path);
                    }
                    return parseAudioFolder(path);
                })
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(v -> animateAndFetchData(true));

    }


    private Observable<Void> selectAudioFolderAnFile(String path) {
        return repository.getAudioFileByPath(path)
                .flatMap(audioFile -> {
                    if (audioFile != null) {
                        audioFile.isSelected = true;
                        repository.updateSelectedAudioFile(audioFile);
                    }
                    return Observable.just(audioFile);
                })
                .flatMap(audioFile -> repository.getAudioFolderByPath(new File(path).getParent()))
                .flatMap(audioFolder -> {
                    if (audioFolder != null) {
                        audioFolder.isSelected = true;
                        repository.updateSelectedAudioFolder(audioFolder);
                    }
                    return Observable.just(null);
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
                    return Observable.just(null);

                }).flatMap(dir -> {
                    if (dir != null) {
                        parseAudioFolderAndFile(dir, path);
                    }
                    return Observable.just(null);
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
                new AudioFile(getApplicationContext(), p, audioFolder.id, audioFile -> {

                    MediaApplication.getInstance().getRepository().insertAudioFile(audioFile);

                    if (p.getAbsolutePath().equals(path)) {
                        Log.d("test", "parse select file: " + audioFile.toString());
                        audioFile.isSelected = true;
                        repository.updateSelectedAudioFile(audioFile);
                    }
                });
            }
        }

        MediaApplication.getInstance().getRepository().insertAudioFolder(audioFolder);

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
                repository.getGenres(),
                repository.getArtists(),
                (audioFolders, genres, artists) -> Observable.just(null))
                .subscribeOn(Schedulers.io())
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
