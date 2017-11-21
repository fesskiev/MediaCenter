package com.fesskiev.mediacenter.di;


import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.ui.MainViewModel;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {
        AppModule.class,
        RxBusModule.class})

@Singleton
public interface AppComponent {
    void inject(MainViewModel viewModel);
    void inject(PlaybackService service);
    void inject(FileSystemService service);
}
