package com.fesskiev.mediacenter.di;

import android.content.Context;

import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.players.VideoPlayer;
import com.fesskiev.mediacenter.utils.RxBus;
import com.fesskiev.mediacenter.utils.ffmpeg.FFmpegHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PlayersModule {

    @Provides
    @Singleton
    AudioPlayer provideAudioPlayer(Context context, RxBus rxBus,
                                   DataRepository dataRepository, FFmpegHelper fFmpegHelper) {
        return new AudioPlayer(context, rxBus, dataRepository, fFmpegHelper);
    }

    @Provides
    @Singleton
    VideoPlayer provideVideoPlayer(RxBus rxBus) {
        return new VideoPlayer(rxBus);
    }
}
