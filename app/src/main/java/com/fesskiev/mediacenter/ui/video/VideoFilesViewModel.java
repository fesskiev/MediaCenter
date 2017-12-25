package com.fesskiev.mediacenter.ui.video;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.graphics.Bitmap;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.VideoFolder;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.players.VideoPlayer;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.events.SingleLiveEvent;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class VideoFilesViewModel extends ViewModel {

    private final MutableLiveData<List<VideoFile>> videoFilesLiveData = new MutableLiveData<>();
    private final SingleLiveEvent<Integer> deletedFileLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> emptyFilesLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> notExistFileLiveData = new SingleLiveEvent<>();

    @Inject
    VideoPlayer videoPlayer;
    @Inject
    DataRepository repository;
    @Inject
    AppSettingsManager settingsManager;
    @Inject
    BitmapHelper bitmapHelper;

    private CompositeDisposable disposables;

    public VideoFilesViewModel() {
        MediaApplication.getInstance().getAppComponent().inject(this);
        disposables = new CompositeDisposable();
    }

    public void fetchVideoFolderFiles(VideoFolder videoFolder) {
        disposables.add(repository.getVideoFiles(videoFolder.id)
                .subscribeOn(Schedulers.io())
                .flatMap(Observable::fromIterable)
                .filter(file -> settingsManager.isShowHiddenFiles() || !file.isHidden)
                .toList()
                .toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::notifyVideoFiles));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
    }

    public Observable<Bitmap> getVideoFileFrame(String path) {
        return bitmapHelper.loadVideoFileFrame(path)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void addVideoFileToPlayList(VideoFile videoFile) {
        videoFile.inPlayList = true;
        repository.updateVideoFile(videoFile);
    }

    public void deleteVideoFile(VideoFile videoFile, int position) {
        disposables.add(CacheManager.deleteFile(videoFile.filePath)
                .subscribeOn(Schedulers.io())
                .flatMap(result -> repository.deleteVideoFile(videoFile))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> notifyDeletedFile(position)));

    }

    public boolean checkVideoFileExist(VideoFile videoFile) {
        if (videoFile.exists()) {
            videoPlayer.setCurrentVideoFiles(videoFilesLiveData.getValue());
            return true;
        }
        notExistFileLiveData.call();
        return false;
    }

    public void updateCurrentVideoFiles(VideoFolder videoFolder, List<VideoFile> videoFiles) {
        videoPlayer.updateCurrentVideoFolders(videoFolder, videoFiles);
    }

    public void updateCurrentVideoFile(VideoFile videoFile) {
        videoPlayer.updateCurrentVideoFile(videoFile);
    }

    private void notifyDeletedFile(int position) {
        deletedFileLiveData.setValue(position);
    }

    private void notifyVideoFiles(List<VideoFile> videoFiles) {
        if (videoFiles != null && !videoFiles.isEmpty()) {
            videoFilesLiveData.setValue(videoFiles);
        } else {
            emptyFilesLiveData.call();
        }
    }

    public MutableLiveData<List<VideoFile>> getVideoFilesLiveData() {
        return videoFilesLiveData;
    }

    public SingleLiveEvent<Integer> getDeletedFileLiveData() {
        return deletedFileLiveData;
    }

    public SingleLiveEvent<Void> getEmptyFilesLiveData() {
        return emptyFilesLiveData;
    }

    public SingleLiveEvent<Void> getNotExistFileLiveData() {
        return notExistFileLiveData;
    }

}
