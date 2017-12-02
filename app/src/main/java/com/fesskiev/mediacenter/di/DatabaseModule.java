package com.fesskiev.mediacenter.di;

import android.content.Context;

import com.fesskiev.mediacenter.data.source.local.db.DatabaseHelper;
import com.squareup.sqlbrite2.BriteDatabase;
import com.squareup.sqlbrite2.SqlBrite;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;

@Module
public class DatabaseModule {

    @Provides
    @Singleton
    public BriteDatabase provideRoomDatabase(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SqlBrite sqlBrite = new SqlBrite.Builder().build();
        return sqlBrite.wrapDatabaseHelper(dbHelper, Schedulers.io());
    }

}
