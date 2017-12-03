package com.fesskiev.mediacenter.di;


import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.ui.MainActivity;
import com.fesskiev.mediacenter.ui.MainViewModel;

import com.fesskiev.mediacenter.ui.audio.AudioFoldersFragment;
import com.fesskiev.mediacenter.ui.audio.AudioFoldersViewModel;
import com.fesskiev.mediacenter.ui.audio.AudioGroupsFragment;
import com.fesskiev.mediacenter.ui.audio.AudioGroupsViewModel;
import com.fesskiev.mediacenter.ui.audio.player.AudioPlayerViewModel;
import com.fesskiev.mediacenter.ui.audio.tracklist.PlayerTrackListViewModel;
import com.fesskiev.mediacenter.ui.audio.tracklist.TrackListActivity;
import com.fesskiev.mediacenter.ui.audio.tracklist.TrackListViewModel;
import com.fesskiev.mediacenter.ui.billing.InAppBillingActivity;
import com.fesskiev.mediacenter.ui.converter.ConverterActivity;
import com.fesskiev.mediacenter.ui.converter.ConverterAudioFragment;
import com.fesskiev.mediacenter.ui.converter.ConverterFragment;
import com.fesskiev.mediacenter.ui.converter.ConverterVideoFragment;
import com.fesskiev.mediacenter.ui.cue.CueActivity;
import com.fesskiev.mediacenter.ui.cut.CutMediaActivity;
import com.fesskiev.mediacenter.ui.effects.EqualizerFragment;
import com.fesskiev.mediacenter.ui.effects.OtherEffectsFragment;
import com.fesskiev.mediacenter.ui.effects.ReverbFragment;
import com.fesskiev.mediacenter.ui.fetch.FetchContentViewModel;
import com.fesskiev.mediacenter.ui.playlist.PlayListViewModel;
import com.fesskiev.mediacenter.ui.search.AlbumSearchViewModel;
import com.fesskiev.mediacenter.ui.search.SearchViewModel;
import com.fesskiev.mediacenter.ui.settings.SettingsFragment;
import com.fesskiev.mediacenter.ui.splash.SplashActivity;
import com.fesskiev.mediacenter.ui.video.VideoFilesActivity;
import com.fesskiev.mediacenter.ui.video.VideoFilesViewModel;
import com.fesskiev.mediacenter.ui.video.VideoFoldersFragment;
import com.fesskiev.mediacenter.ui.video.VideoFoldersViewModel;
import com.fesskiev.mediacenter.ui.video.player.VideoExoPlayerViewModel;
import com.fesskiev.mediacenter.ui.walkthrough.ProUserFragment;
import com.fesskiev.mediacenter.ui.walkthrough.WalkthroughFragment;
import com.fesskiev.mediacenter.widgets.dialogs.AudioFolderDetailsDialog;
import com.fesskiev.mediacenter.widgets.dialogs.EditTrackDialog;
import com.fesskiev.mediacenter.widgets.dialogs.MediaFolderDetailsDialog;
import com.fesskiev.mediacenter.widgets.dialogs.VideoFileDetailsDialog;
import com.fesskiev.mediacenter.widgets.dialogs.VideoFolderDetailsDialog;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {
        AppModule.class,
        RxBusModule.class,

        RepositoryModule.class,
        DataSourceModule.class,
        NetworkModule.class,
        DatabaseModule.class,

        PlayersModule.class,

        UtilsModule.class,

        AppSettingsModule.class})


@Singleton
public interface AppComponent {

    void inject(MainViewModel viewModel);
    void inject(AudioFoldersViewModel viewModel);
    void inject(AudioGroupsViewModel viewModel);
    void inject(AudioPlayerViewModel viewModel);
    void inject(TrackListViewModel viewModel);
    void inject(PlayerTrackListViewModel viewModel);
    void inject(VideoFoldersViewModel viewModel);
    void inject(VideoFilesViewModel viewModel);
    void inject(VideoExoPlayerViewModel viewModel);
    void inject(FetchContentViewModel viewModel);
    void inject(PlayListViewModel viewModel);
    void inject(AlbumSearchViewModel viewModel);
    void inject(SearchViewModel viewModel);

    void inject(WalkthroughFragment fragment);
    void inject(ProUserFragment fragment);
    void inject(EqualizerFragment fragment);
    void inject(OtherEffectsFragment fragment);
    void inject(ReverbFragment fragment);
    void inject(SettingsFragment fragment);
    void inject(AudioFoldersFragment fragment);
    void inject(AudioGroupsFragment fragment);
    void inject(ConverterVideoFragment fragment);
    void inject(ConverterAudioFragment fragment);
    void inject(ConverterFragment fragment);
    void inject(VideoFoldersFragment fragment);

    void inject(InAppBillingActivity activity);
    void inject(CueActivity activity);
    void inject(CutMediaActivity activity);
    void inject(SplashActivity activity);
    void inject(ConverterActivity activity);
    void inject(MainActivity activity);
    void inject(TrackListActivity activity);
    void inject(VideoFilesActivity activity);

    void inject(MediaFolderDetailsDialog dialog);
    void inject(VideoFileDetailsDialog dialog);
    void inject(EditTrackDialog dialog);

    void inject(PlaybackService service);
    void inject(FileSystemService service);

    void inject(MediaApplication application);

}
