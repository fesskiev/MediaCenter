package com.fesskiev.mediacenter.ui.playlist;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.graphics.Bitmap;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.MediaFile;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.events.SingleLiveEvent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class PlayListViewModel extends ViewModel {

    private final MutableLiveData<List<MediaFile>> playListFilesLiveData = new MutableLiveData<>();
    private final SingleLiveEvent<Void> emptyPlaListLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> notExistsMediaFileLiveData = new SingleLiveEvent<>();

    @Inject
    DataRepository repository;
    @Inject
    AudioPlayer audioPlayer;
    @Inject
    BitmapHelper bitmapHelper;

    private CompositeDisposable disposables;

    public PlayListViewModel() {
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

    public void fetchPlayListFiles() {
        disposables.add(Observable.zip(repository.getAudioFilePlaylist(),
                repository.getVideoFilePlaylist(),
                (audioFiles, videoFiles) -> {
                    List<MediaFile> mediaFiles = new ArrayList<>();
                    mediaFiles.addAll(audioFiles);
                    mediaFiles.addAll(videoFiles);
                    return mediaFiles;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::notifyPlayListFiles));
    }

    public void clearPlaylist() {
        disposables.add(repository.clearPlaylist()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> notifyEmptyList()));
    }

    public void removeFromPlayList(MediaFile mediaFile) {
        mediaFile.setToPlayList(false);
        switch (mediaFile.getMediaType()) {
            case VIDEO:
                disposables.add(repository.updateVideoFile((VideoFile) mediaFile)
                        .subscribeOn(Schedulers.io())
                        .flatMap(integer -> Observable.zip(repository.getAudioFilePlaylist(),
                                repository.getVideoFilePlaylist(),
                                (audioFiles, videoFiles) -> {
                                    List<MediaFile> mediaFiles = new ArrayList<>();
                                    mediaFiles.addAll(audioFiles);
                                    mediaFiles.addAll(videoFiles);
                                    return mediaFiles;
                                }))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::notifyPlayListFiles));
                break;
            case AUDIO:
                disposables.add(repository.updateAudioFile((AudioFile) mediaFile)
                        .subscribeOn(Schedulers.io())
                        .flatMap(integer -> Observable.zip(repository.getAudioFilePlaylist(),
                                repository.getVideoFilePlaylist(),
                                (audioFiles, videoFiles) -> {
                                    List<MediaFile> mediaFiles = new ArrayList<>();
                                    mediaFiles.addAll(audioFiles);
                                    mediaFiles.addAll(videoFiles);
                                    return mediaFiles;
                                }))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::notifyPlayListFiles));
                break;
        }
    }

    public Observable<Bitmap> getPlayListArtwork(MediaFile mediaFile) {
        return bitmapHelper.getTrackListArtwork(mediaFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void setCurrentAudioFileAndPlay(AudioFile audioFile) {
        audioPlayer.setCurrentAudioFileAndPlay(audioFile);
    }

    public boolean checkMediaFileExist(MediaFile mediaFile) {
        if (mediaFile.exists()) {
            return true;
        }
        notExistsMediaFileLiveData.call();
        return false;
    }

    private void notifyPlayListFiles(List<MediaFile> mediaFiles) {
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            playListFilesLiveData.setValue(mediaFiles);
        } else {
            notifyEmptyList();
        }
    }

    private void notifyEmptyList() {
        emptyPlaListLiveData.call();
    }

    public MutableLiveData<List<MediaFile>> getPlayListFilesLiveData() {
        return playListFilesLiveData;
    }

    public SingleLiveEvent<Void> getEmptyPlaListLiveData() {
        return emptyPlaListLiveData;
    }

    public SingleLiveEvent<Void> getNotExistsMediaFileLiveData() {
        return notExistsMediaFileLiveData;
    }

}
