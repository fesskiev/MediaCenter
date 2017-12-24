package com.fesskiev.mediacenter.ui.video;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.graphics.Bitmap;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.VideoFolder;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.events.SingleLiveEvent;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class VideoFoldersViewModel extends ViewModel {

    private final MutableLiveData<List<VideoFolder>> videoFoldersLiveData = new MutableLiveData<>();
    private final SingleLiveEvent<Void> emptyFoldersLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Integer> deletedFolderLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> updatedFolderIndexesLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> notExistFolderLiveData = new SingleLiveEvent<>();

    @Inject
    DataRepository repository;
    @Inject
    AppSettingsManager settingsManager;
    @Inject
    BitmapHelper bitmapHelper;

    private CompositeDisposable disposables;

    public VideoFoldersViewModel() {
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

    public void fetchVideoFolders() {
        disposables.add(repository.getVideoFolders()
                .subscribeOn(Schedulers.io())
                .flatMap(Observable::fromIterable)
                .filter(folder -> settingsManager.isShowHiddenFiles() || !folder.isHidden)
                .toList()
                .toObservable()
                .flatMap(videoFolders -> {
                    if (videoFolders != null && !videoFolders.isEmpty()) {
                        Collections.sort(videoFolders);
                    }
                    return videoFolders != null ? Observable.just(videoFolders) : Observable.empty();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::notifyVideoFolders));
    }

    public void deleteVideoFolder(VideoFolder videoFolder, int position) {
        disposables.add(CacheManager.deleteDirectoryWithFiles(videoFolder.folderPath)
                .subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    if (result) {
                        return repository.deleteVideoFolder(videoFolder);
                    }
                    return Observable.empty();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> notifyDeletedFolder(position)));
    }

    public void updateVideoFoldersIndexes(List<VideoFolder> videoFolders) {
        disposables.add(repository.updateVideoFoldersIndex(videoFolders)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> notifyUpdatedIndexes()));

    }

    public Observable<List<Bitmap>> getVideoFilesFrame(VideoFolder videoFolder) {
        return repository.getVideoFilesFrame(videoFolder.id)
                .flatMap(Observable::fromIterable)
                .flatMap(path -> bitmapHelper.loadVideoFolderFrame(path))
                .toList()
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .take(4);
    }


    public boolean checkVideoFolderExist(VideoFolder videoFolder) {
        if (videoFolder.exists()) {
            return true;
        }
        emptyFoldersLiveData.call();
        return false;
    }

    private void notifyVideoFolders(List<VideoFolder> videoFolders) {
        if (videoFolders != null && !videoFolders.isEmpty()) {
            videoFoldersLiveData.setValue(videoFolders);
        } else {
            emptyFoldersLiveData.call();
        }
    }

    private void notifyUpdatedIndexes() {
        updatedFolderIndexesLiveData.call();
    }

    private void notifyDeletedFolder(int position) {
        deletedFolderLiveData.setValue(position);
    }

    public MutableLiveData<List<VideoFolder>> getVideoFoldersLiveData() {
        return videoFoldersLiveData;
    }

    public SingleLiveEvent<Void> getEmptyFoldersLiveData() {
        return emptyFoldersLiveData;
    }

    public SingleLiveEvent<Integer> getDeletedFolderLiveData() {
        return deletedFolderLiveData;
    }

    public SingleLiveEvent<Void> getUpdatedFolderIndexesLiveData() {
        return updatedFolderIndexesLiveData;
    }

    public SingleLiveEvent<Void> getNotExistFolderLiveData() {
        return notExistFolderLiveData;
    }
}
