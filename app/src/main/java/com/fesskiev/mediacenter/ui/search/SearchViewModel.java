package com.fesskiev.mediacenter.ui.search;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.graphics.Bitmap;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.events.SingleLiveEvent;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class SearchViewModel extends ViewModel {

    private final MutableLiveData<List<AudioFile>> resultAudioFilesLiveData = new MutableLiveData<>();
    private final SingleLiveEvent<List<AudioFile>> searchAudioFilesLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> notExistsAudioFileLiveData = new SingleLiveEvent<>();

    @Inject
    DataRepository repository;
    @Inject
    AudioPlayer audioPlayer;
    @Inject
    BitmapHelper bitmapHelper;

    private CompositeDisposable disposables;

    public SearchViewModel() {
        MediaApplication.getInstance().getAppComponent().inject(this);
        disposables = new CompositeDisposable();
    }

    public void querySearch(String query, boolean search) {
        disposables.add(repository
                .getSearchAudioFiles(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioFiles -> notifyAudioFiles(audioFiles, search)));
    }

    public boolean checkAudioFileExist(AudioFile audioFile) {
        if (audioFile.exists()) {
            return true;
        }
        notExistsAudioFileLiveData.call();
        return false;
    }

    public void setCurrentAudioFileAndPlay(AudioFile audioFile) {
        audioPlayer.setCurrentAudioFileAndPlay(audioFile);
    }

    private void notifyAudioFiles(List<AudioFile> audioFiles, boolean search) {
        if (search) {
            searchAudioFilesLiveData.setValue(audioFiles);
        } else {
            resultAudioFilesLiveData.setValue(audioFiles);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
    }

    public MutableLiveData<List<AudioFile>> getResultAudioFilesLiveData() {
        return resultAudioFilesLiveData;
    }

    public SingleLiveEvent<List<AudioFile>> getSearchAudioFilesLiveData() {
        return searchAudioFilesLiveData;
    }

    public SingleLiveEvent<Void> getNotExistsAudioFileLiveData() {
        return notExistsAudioFileLiveData;
    }

    public Observable<Bitmap> getTrackListArtwork(AudioFile audioFile) {
        return bitmapHelper.getTrackListArtwork(audioFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
