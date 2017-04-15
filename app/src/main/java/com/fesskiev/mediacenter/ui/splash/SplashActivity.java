package com.fesskiev.mediacenter.ui.splash;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.ui.MainActivity;
import com.fesskiev.mediacenter.ui.walkthrough.WalkthroughActivity;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;

import java.io.File;
import java.util.List;

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
                        return repository.getAudioFileByPath(path);
                    }
                    return parseAudioFile(path);
                })
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::openPlayURIAudioFile);

    }

    private void openPlayURIAudioFile(AudioFile audioFile) {
        if (audioFile != null) {
            audioFile.isSelected = true;
            repository.updateSelectedAudioFile(audioFile);
            animateAndFetchData(true);
        }
    }

    private Observable<AudioFile> parseAudioFile(String path) {
        return repository.getAudioFolders()
                .flatMap(audioFolders -> Observable.just(getAudioFolderByPath(audioFolders, new File(path).getParent())))
                .flatMap(audioFolder -> Observable.just(new AudioFile(getApplicationContext(), new File(path),
                        audioFolder == null ? "" : audioFolder.id, null)))
                .flatMap(audioFile -> {
                    if (!TextUtils.isEmpty(audioFile.id)) {

                        repository.insertAudioFile(audioFile);

                        repository.getMemorySource().setCacheArtistsDirty(true);
                        repository.getMemorySource().setCacheGenresDirty(true);
                        repository.getMemorySource().setCacheFoldersDirty(true);
                    }
                    return Observable.just(audioFile);
                });


    }

    private AudioFolder getAudioFolderByPath(List<AudioFolder> audioFolders, String path) {
        for (AudioFolder audioFolder : audioFolders) {
            if (audioFolder.folderPath.getAbsolutePath().equals(path)) {
                return audioFolder;
            }
        }
        return null;
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
