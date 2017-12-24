package com.fesskiev.mediacenter.ui.audio;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.graphics.Bitmap;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.events.SingleLiveEvent;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class AudioFoldersViewModel extends ViewModel {

    private final MutableLiveData<List<AudioFolder>> audioFoldersLiveData = new MutableLiveData<>();
    private final SingleLiveEvent<Void> emptyFoldersLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Integer> deletedFolderLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> clearPlaybackLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> updatedFolderIndexesLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> notExistFolderLiveData = new SingleLiveEvent<>();

    @Inject
    DataRepository repository;
    @Inject
    AppSettingsManager settingsManager;
    @Inject
    AudioPlayer audioPlayer;
    @Inject
    BitmapHelper bitmapHelper;

    private CompositeDisposable disposables;

    public AudioFoldersViewModel() {
        MediaApplication.getInstance().getAppComponent().inject(this);
        disposables = new CompositeDisposable();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
    }

    public void getAudioFolders() {
        disposables.add(repository.getAudioFolders()
                .subscribeOn(Schedulers.io())
                .flatMap(Observable::fromIterable)
                .filter(folder -> settingsManager.isShowHiddenFiles() || !folder.isHidden)
                .toList()
                .toObservable()
                .flatMap(audioFolders -> {
                    if (audioFolders != null) {
                        if (!audioFolders.isEmpty()) {
                            Collections.sort(audioFolders);
                        }
                    }
                    return audioFolders != null ? Observable.just(audioFolders) : Observable.empty();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::notifyAudioFolders));
    }

    public void deleteAudioFolder(AudioFolder audioFolder, int position) {
        disposables.add(CacheManager.deleteDirectoryWithFiles(audioFolder.folderPath)
                .subscribeOn(Schedulers.io())
                .flatMap(result -> repository.deleteAudioFolder(audioFolder))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    notifyDeletedFolder(position);
                    clearPlayBackIfNeed(audioFolder);
                }));
    }

    public void updateAudioFolderIndexes(List<AudioFolder> audioFolders) {
        disposables.add(repository.updateAudioFoldersIndex(audioFolders)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> notifyUpdatedIndexes()));
    }

    public Observable<Bitmap> getAudioFolderArtwork(AudioFolder audioFolder) {
        return bitmapHelper.getAudioFolderArtwork(audioFolder)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<BitmapHelper.PaletteColor> getAudioFolderPalette(AudioFolder audioFolder) {
        return bitmapHelper.getAudioFolderPalette(audioFolder)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public boolean checkAudioFolderExist(AudioFolder audioFolder) {
        if (audioFolder.exists()) {
            return true;
        }
        notExistFolderLiveData.call();
        return false;
    }

    private void clearPlayBackIfNeed(AudioFolder audioFolder) {
        if (audioPlayer.isDeletedFolderSelect(audioFolder)) {
            clearPlaybackLiveData.call();
        }
    }

    private void notifyAudioFolders(List<AudioFolder> audioFolders) {
        if (audioFolders != null && !audioFolders.isEmpty()) {
            audioFoldersLiveData.setValue(audioFolders);
        } else {
            emptyFoldersLiveData.call();
        }
    }

    private void notifyDeletedFolder(int position) {
        deletedFolderLiveData.setValue(position);
    }

    private void notifyUpdatedIndexes() {
        updatedFolderIndexesLiveData.call();
    }

    public MutableLiveData<List<AudioFolder>> getAudioFoldersLiveData() {
        return audioFoldersLiveData;
    }

    public SingleLiveEvent<Void> getEmptyFoldersLiveData() {
        return emptyFoldersLiveData;
    }

    public SingleLiveEvent<Integer> getDeletedFolderLiveData() {
        return deletedFolderLiveData;
    }

    public SingleLiveEvent<Void> getClearPlaybackLiveData() {
        return clearPlaybackLiveData;
    }

    public SingleLiveEvent<Void> getUpdatedFolderIndexesLiveData() {
        return updatedFolderIndexesLiveData;
    }

    public SingleLiveEvent<Void> getNotExistFolderLiveData() {
        return notExistFolderLiveData;
    }
}
