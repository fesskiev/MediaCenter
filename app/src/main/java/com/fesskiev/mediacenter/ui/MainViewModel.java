package com.fesskiev.mediacenter.ui;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.utils.AppSettingsManager;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.fesskiev.mediacenter.players.AudioPlayer.sortAudioFiles;


public class MainViewModel extends ViewModel {

    private final MutableLiveData<AudioFile> currentTrackLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<AudioFile>> currentTrackListLiveData = new MutableLiveData<>();

    private DataRepository repository;

    private AudioPlayer audioPlayer;

    private AppSettingsManager appSettingsManager;

    private Disposable disposable;


    public MainViewModel() {
        repository = MediaApplication.getInstance().getRepository();
        audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        appSettingsManager = AppSettingsManager.getInstance();

        disposable = repository.getSelectedFolderAudioFiles()
                .subscribeOn(Schedulers.io())
                .flatMap(audioFiles -> Observable.just(sortAudioFiles(appSettingsManager.getSortType(), audioFiles)))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(currentTrackListLiveData::setValue)
                .subscribeOn(Schedulers.io())
                .flatMap(audioFiles -> repository.getSelectedAudioFile())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(currentTrackLiveData::setValue)
                .firstOrError()
                .subscribe(object -> {
                }, Throwable::printStackTrace);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public void pause() {
        audioPlayer.pause();
    }

    public void play() {
        audioPlayer.play();
    }

    public void setCurrentAudioFileAndPlay(AudioFile audioFile) {
        audioPlayer.setCurrentAudioFileAndPlay(audioFile);
        currentTrackLiveData.setValue(audioFile);
    }

    public boolean isTrackSelected() {
        return currentTrackLiveData.getValue() != null;
    }

    public boolean isEqualsToCurrentTrack(AudioFile audioFile) {
        return currentTrackLiveData.getValue() != null && currentTrackLiveData.getValue().equals(audioFile);
    }

    public boolean isUserPro() {
        return appSettingsManager.isUserPro();
    }

    public void dropEffects() {
        appSettingsManager.setEQEnable(false);
        appSettingsManager.setReverbEnable(false);
        appSettingsManager.setWhooshEnable(false);
        appSettingsManager.setEchoEnable(false);
    }

    public boolean isNeedMainActivityGuide() {
        return appSettingsManager.isNeedMainActivityGuide();
    }

    public void setMainActivityGuideWatched() {
        appSettingsManager.setNeedMainActivityGuide(false);
    }


    public MutableLiveData<AudioFile> getCurrentTrackLiveData() {
        return currentTrackLiveData;
    }

    public MutableLiveData<List<AudioFile>> getCurrentTrackListLiveData() {
        return currentTrackListLiveData;
    }

}
