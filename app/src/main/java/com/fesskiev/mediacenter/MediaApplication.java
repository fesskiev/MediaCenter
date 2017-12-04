package com.fesskiev.mediacenter;


import android.content.ComponentCallbacks2;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.fesskiev.mediacenter.di.AppComponent;
import com.fesskiev.mediacenter.di.AppModule;
import com.fesskiev.mediacenter.di.AppSettingsModule;
import com.fesskiev.mediacenter.di.DaggerAppComponent;
import com.fesskiev.mediacenter.di.DataSourceModule;
import com.fesskiev.mediacenter.di.DatabaseModule;
import com.fesskiev.mediacenter.di.NetworkModule;
import com.fesskiev.mediacenter.di.RepositoryModule;
import com.fesskiev.mediacenter.di.RxBusModule;
import com.fesskiev.mediacenter.di.UtilsModule;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.ffmpeg.FFmpegHelper;
import com.google.firebase.crash.FirebaseCrash;

import javax.inject.Inject;

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
    private AppComponent appComponent;
    @Inject
    FFmpegHelper fFmpegHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        appComponent = buildComponent();

        appComponent.inject(this);

        fFmpegHelper.loadFFmpegLibrary(new FFmpegHelper.OnConverterLibraryLoadListener() {
            @Override
            public void onSuccess() {
                Log.e("ffmpef", "FFMPEG LOAD");
            }

            @Override
            public void onFailure() {
                Log.e("ffmpef", "FFMPEG FAIL");
            }
        });

        addFirebaseCrash();
    }

    private void addFirebaseCrash() {
        if (!BuildConfig.DEBUG) {
            RxJavaPlugins.setErrorHandler(FirebaseCrash::report);
        }
    }

    protected AppComponent buildComponent() {
        return DaggerAppComponent.builder()
                .appModule(new AppModule(getApplicationContext()))
                .repositoryModule(new RepositoryModule())
                .dataSourceModule(new DataSourceModule())
                .databaseModule(new DatabaseModule())
                .networkModule(new NetworkModule("http://ws.audioscrobbler.com/2.0/"))
                .utilsModule(new UtilsModule())
                .rxBusModule(new RxBusModule())
                .appSettingsModule((new AppSettingsModule()))
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
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
}
