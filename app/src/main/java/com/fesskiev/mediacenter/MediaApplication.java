package com.fesskiev.mediacenter;


import android.content.ComponentCallbacks2;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.data.source.local.db.LocalDataSource;
import com.fesskiev.mediacenter.data.source.memory.MemoryDataSource;
import com.fesskiev.mediacenter.data.source.remote.RemoteDataSource;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.players.VideoPlayer;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.ffmpeg.FFmpegHelper;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.crash.FirebaseCrash;

import io.reactivex.plugins.RxJavaPlugins;

public class MediaApplication extends MultiDexApplication {

    static {
        try {
            System.loadLibrary("ffmpeg");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    public static synchronized MediaApplication getInstance() {
        return INSTANCE;
    }

    private static MediaApplication INSTANCE;
    private AudioPlayer audioPlayer;
    private VideoPlayer videoPlayer;
    private DataRepository repository;
    private String userAgent;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

        repository = DataRepository.getInstance(RemoteDataSource.getInstance(),
                LocalDataSource.getInstance(), MemoryDataSource.getInstance());

        audioPlayer = new AudioPlayer(repository);
        videoPlayer = new VideoPlayer();

        FFmpegHelper.getInstance()
                .loadFFmpegLibrary(new FFmpegHelper.OnConverterLibraryLoadListener() {
                    @Override
                    public void onSuccess() {
                        Log.e("ffmpef", "FFMPEG LOAD");
                    }

                    @Override
                    public void onFailure() {
                        Log.e("ffmpef", "FFMPEG FAIL");
                    }
                });


        RxJavaPlugins.setErrorHandler(FirebaseCrash::report);

        userAgent = Util.getUserAgent(this, "ExoPlayer");
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
                Glide.get(getApplicationContext()).trimMemory(ComponentCallbacks2.TRIM_MEMORY_MODERATE);
                break;
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                AppLog.INFO("TRIM_MEMORY_COMPLETE");
                break;
        }
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
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
