package com.fesskiev.mediacenter.di;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.fesskiev.mediacenter.data.source.local.room.MediaCenterDb;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DatabaseModule {
    private String databaseName;

    public DatabaseModule(String databaseName) {
        this.databaseName = databaseName;
    }

    @Provides
    @Singleton
    public MediaCenterDb provideRoomDatabase(Context context) {
        return Room.databaseBuilder(context, MediaCenterDb.class, databaseName).build();
    }

}
