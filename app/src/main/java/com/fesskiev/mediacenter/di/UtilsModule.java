package com.fesskiev.mediacenter.di;

import android.content.Context;

import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.utils.AppAnimationUtils;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.NotificationHelper;
import com.fesskiev.mediacenter.utils.ffmpeg.FFmpegHelper;
import com.fesskiev.mediacenter.utils.schedulers.BaseSchedulerProvider;
import com.fesskiev.mediacenter.utils.schedulers.SchedulerProvider;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class UtilsModule {

    @Provides
    @Singleton
    public BitmapHelper provideBitmapHelper(Context context, OkHttpClient okHttpClient) {
        return new BitmapHelper(context, okHttpClient);
    }

    @Provides
    @Singleton
    public NotificationHelper provideNotificationHelper(Context context) {
        return new NotificationHelper(context);
    }

    @Provides
    @Singleton
    public FFmpegHelper provideFFmpegHelper(Context context, DataRepository repository) {
        return new FFmpegHelper(context, repository);
    }

    @Provides
    @Singleton
    public AppAnimationUtils provideAppAnimationUtils(Context context) {
        return new AppAnimationUtils(context);
    }

    @Provides
    @Singleton
    public SchedulerProvider schedulerProvider(){
        return new SchedulerProvider();
    }

}
