package com.fesskiev.mediacenter.di;

import android.content.Context;

import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.players.VideoPlayer;
import com.fesskiev.mediacenter.utils.RxBus;
import com.fesskiev.mediacenter.utils.ffmpeg.FFmpegHelper;
import com.fesskiev.mediacenter.utils.schedulers.SchedulerProvider;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PlayersModule {

    @Provides
    @Singleton
    AudioPlayer provideAudioPlayer(Context context, RxBus rxBus,
                                   DataRepository dataRepository,
                                   SchedulerProvider schedulerProvider,
                                   FFmpegHelper fFmpegHelper) {
        return new AudioPlayer(context, rxBus, dataRepository, schedulerProvider, fFmpegHelper);
    }

    @Provides
    @Singleton
    VideoPlayer provideVideoPlayer(RxBus rxBus,
                                   DataRepository dataRepository,
                                   SchedulerProvider schedulerProvider) {
        return new VideoPlayer(rxBus, dataRepository, schedulerProvider);
    }
}
