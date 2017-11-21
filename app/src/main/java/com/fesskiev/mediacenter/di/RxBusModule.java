package com.fesskiev.mediacenter.di;


import com.fesskiev.mediacenter.utils.RxBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class RxBusModule {

    @Provides
    @Singleton
    RxBus provideApplication() {
        return new RxBus();
    }
}
