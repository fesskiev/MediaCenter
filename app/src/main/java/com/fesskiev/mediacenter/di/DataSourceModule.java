package com.fesskiev.mediacenter.di;


import android.content.Context;


import com.fesskiev.mediacenter.data.source.local.LocalDataSource;
import com.fesskiev.mediacenter.data.source.remote.RemoteDataSource;
import com.squareup.sqlbrite2.BriteDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

@Module
public class DataSourceModule {

    @Provides
    @Singleton
    public RemoteDataSource provideRemoteDataSource(Context context, Retrofit retrofit) {
        return new RemoteDataSource(context, retrofit);
    }

    @Provides
    @Singleton
    public LocalDataSource provideLocalDataSource(BriteDatabase database) {
        return new LocalDataSource(database);
    }

}
