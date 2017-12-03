package com.fesskiev.mediacenter.ui.audio.tracklist;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.graphics.Bitmap;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.ui.audio.CONTENT_TYPE;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.RxBus;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.events.SingleLiveEvent;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class TrackListViewModel extends ViewModel {

    private final MutableLiveData<AudioFile> currentTrackLiveData = new MutableLiveData<>();

    private final MutableLiveData<List<AudioFile>> currentTrackListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> playingLiveData = new MutableLiveData<>();
    private final SingleLiveEvent<Void> addToPlayListAudioFileLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> notExistsAudioFileLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Integer> deletedAudioFileLiveData = new SingleLiveEvent<>();

    @Inject
    DataRepository repository;
    @Inject
    AppSettingsManager settingsManager;
    @Inject
    AudioPlayer audioPlayer;
    @Inject
    BitmapHelper bitmapHelper;
    @Inject
    RxBus rxBus;

    private CompositeDisposable disposables;

    public TrackListViewModel() {
        MediaApplication.getInstance().getAppComponent().inject(this);
        disposables = new CompositeDisposable();
        subscribeToEvents();
        notifyCurrentTrack(audioPlayer.getCurrentTrack());
    }

    private void subscribeToEvents() {
        disposables.add(rxBus.toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object -> {
                    if (object instanceof PlaybackService) {
                        notifyPlayback((PlaybackService) object);
                    } else if (object instanceof AudioFile) {
                        AudioFile audioFile = (AudioFile) object;
                        notifyCurrentTrack(audioFile);
                    }
                }));

    }

    private void notifyPlayback(PlaybackService playbackService) {
        boolean playing = playbackService.isPlaying();
        boolean lastPlaying = isPlaying();
        if (lastPlaying != playing) {
            playingLiveData.setValue(playing);
        }
    }

    private void notifyCurrentTrack(AudioFile audioFile) {
        currentTrackLiveData.setValue(audioFile);
    }

    public void fetchContentByType(CONTENT_TYPE contentType, String contentValue, AudioFolder audioFolder) {
        Observable<List<AudioFile>> audioFilesObservable = null;
        switch (contentType) {
            case FOLDERS:
                audioFilesObservable = repository.getAudioTracks(audioFolder.id);
                break;
            case ARTIST:
                audioFilesObservable = repository.getArtistTracks(contentValue);
                break;
            case GENRE:
                audioFilesObservable = repository.getGenreTracks(contentValue);
                break;
        }

        if (audioFilesObservable != null) {
            disposables.add(audioFilesObservable
                    .firstOrError()
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .flatMap(Observable::fromIterable)
                    .filter(audioFile -> settingsManager.isShowHiddenFiles() || !audioFile.isHidden)
                    .toList()
                    .toObservable()
                    .map(unsortedList -> AudioPlayer.sortAudioFiles(settingsManager.getSortType(), unsortedList))
                    .doOnNext(sortedList -> audioPlayer.setSortingTrackList(sortedList))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::notifyTrackList));
        }
    }

    public void sortTracks(int type) {
        disposables.add(Observable.just(audioPlayer.getCurrentTrackList())
                .subscribeOn(Schedulers.io())
                .map(unsortedList -> AudioPlayer.sortAudioFiles(type, unsortedList))
                .doOnNext(sortedList -> audioPlayer.setSortingTrackList(sortedList))
                .doOnNext(sortedList -> settingsManager.setSortType(type))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::notifyTrackList));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
    }

    public Observable<Bitmap> getTrackListArtwork(AudioFile audioFile) {
        return bitmapHelper.getTrackListArtwork(audioFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void deleteAudioFile(AudioFile audioFile, int position) {
        disposables.add(RxUtils.fromCallable(CacheManager.deleteFile(audioFile.filePath))
                .subscribeOn(Schedulers.io())
                .flatMap(result -> RxUtils.fromCallable(repository.deleteAudioFile(audioFile.getFilePath())))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> notifyDeleteAudioFile(position)));
    }


    public void updateAudioFile(AudioFile audioFile) {
        audioFile.inPlayList = true;
        disposables.add(RxUtils.fromCallable(repository.updateAudioFile(audioFile))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> notifyAddAudioFileToPlayList()));
    }

    public boolean checkAudioFileExist(AudioFile audioFile) {
        if (audioFile.exists()) {
            return true;
        }
        notExistsAudioFileLiveData.call();
        return false;
    }

    private void notifyAddAudioFileToPlayList() {
        addToPlayListAudioFileLiveData.call();
    }

    private void notifyTrackList(List<AudioFile> audioFiles) {
        currentTrackListLiveData.setValue(audioFiles);
    }

    private void notifyDeleteAudioFile(int position) {
        deletedAudioFileLiveData.setValue(position);
    }

    public void setCurrentTrackList(AudioFolder audioFolder, List<AudioFile> audioFiles) {
        audioPlayer.setCurrentTrackList(audioFolder, audioFiles);
    }

    public void setCurrentAudioFileAndPlay(AudioFile audioFile) {
        audioPlayer.setCurrentAudioFileAndPlay(audioFile);
    }

    public boolean isNeedTrackListActivityGuide() {
        return settingsManager.isNeedTrackListActivityGuide();
    }

    public void setNeedTrackListActivityGuide(boolean need) {
        settingsManager.setNeedTrackListActivityGuide(need);
    }

    public MutableLiveData<List<AudioFile>> getCurrentTrackListLiveData() {
        return currentTrackListLiveData;
    }

    public MutableLiveData<Boolean> getPlayingLiveData() {
        return playingLiveData;
    }

    public MutableLiveData<AudioFile> getCurrentTrackLiveData() {
        return currentTrackLiveData;
    }

    public SingleLiveEvent<Void> getAddToPlayListAudioFileLiveData() {
        return addToPlayListAudioFileLiveData;
    }

    public SingleLiveEvent<Void> getNotExistsAudioFileLiveData() {
        return notExistsAudioFileLiveData;
    }

    public SingleLiveEvent<Integer> getDeletedAudioFileLiveData() {
        return deletedAudioFileLiveData;
    }
    public boolean isPlaying() {
        Boolean playing = playingLiveData.getValue();
        if (playing == null) {
            return false;
        }
        return playing;
    }

}
