package com.fesskiev.mediacenter.di;


import android.content.Context;

import com.fesskiev.mediacenter.utils.AppSettingsManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppSettingsModule {

    @Provides
    @Singleton
    public AppSettingsManager provideAppSettingsManager(Context context){
        return new AppSettingsManager(context);
    }
}
