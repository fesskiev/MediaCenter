package com.fesskiev.mediacenter;

import android.app.Application;
import android.content.ComponentCallbacks2;

import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.data.source.local.db.LocalDataSource;
import com.fesskiev.mediacenter.data.source.memory.MemoryDataSource;
import com.fesskiev.mediacenter.data.source.remote.RemoteDataSource;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.players.VideoPlayer;
import com.fesskiev.mediacenter.utils.AppLog;

import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;

public class MediaApplication extends Application {


    private static MediaApplication INSTANCE;
    private AudioPlayer audioPlayer;
    private VideoPlayer videoPlayer;
    private DataRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

        repository = DataRepository.getInstance(RemoteDataSource.getInstance(),
                LocalDataSource.getInstance(), MemoryDataSource.getInstance());

        audioPlayer = new AudioPlayer(repository);
        videoPlayer = new VideoPlayer();

        RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler() {
            @Override
            public void handleError(Throwable e) {
                AppLog.ERROR("ERROR: " + e.toString());
                super.handleError(e);
            }
        });

    }

    public static synchronized MediaApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                AppLog.INFO("TRIM_MEMORY_UI_HIDDEN");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
                AppLog.INFO("TRIM_MEMORY_BACKGROUND");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
                AppLog.INFO("TRIM_MEMORY_MODERATE");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
                AppLog.INFO("TRIM_MEMORY_RUNNING_MODERATE");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
                AppLog.INFO("TRIM_MEMORY_RUNNING_LOW");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                AppLog.INFO("TRIM_MEMORY_RUNNING_CRITICAL");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                AppLog.INFO("TRIM_MEMORY_COMPLETE");
                break;
        }
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public VideoPlayer getVideoPlayer() {
        return videoPlayer;
    }

    public DataRepository getRepository() {
        return repository;
    }
}
