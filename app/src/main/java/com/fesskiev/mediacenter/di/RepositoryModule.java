package com.fesskiev.mediacenter.di;


import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.data.source.local.LocalDataSource;
import com.fesskiev.mediacenter.data.source.remote.RemoteDataSource;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RepositoryModule {

    @Provides
    @Singleton
    public DataRepository provideRepository(RemoteDataSource remoteDataSource, LocalDataSource localDataSource) {
        return new DataRepository(remoteDataSource, localDataSource);
    }
}
