package com.fesskiev.mediacenter.ui;

import android.annotation.SuppressLint;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.graphics.Bitmap;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.RxBus;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.events.SingleLiveEvent;
import com.fesskiev.mediacenter.utils.ffmpeg.FFmpegHelper;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.fesskiev.mediacenter.players.AudioPlayer.sortAudioFiles;


public class MainViewModel extends ViewModel {

    private final MutableLiveData<AudioFile> currentTrackLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<AudioFile>> currentTrackListLiveData = new MutableLiveData<>();

    private final MutableLiveData<Integer> positionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> playingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> convertingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingSuccessLiveData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> reverbLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> EQLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> whooshLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> echoLiveData = new MutableLiveData<>();

    private final MutableLiveData<Bitmap> coverLiveData = new MutableLiveData<>();
    private final SingleLiveEvent<Void> finishPlaybackLiveData = new SingleLiveEvent<>();

    @Inject
    AudioPlayer audioPlayer;
    @Inject
    AppSettingsManager settingsManager;
    @Inject
    RxBus rxBus;
    @Inject
    FFmpegHelper fFmpegHelper;
    @Inject
    DataRepository repository;
    @Inject
    BitmapHelper bitmapHelper;
    @SuppressLint("StaticFieldLeak")
    @Inject
    Context context;

    private CompositeDisposable disposables;

    public MainViewModel() {
        MediaApplication.getInstance().getAppComponent().inject(this);
        disposables = new CompositeDisposable();
        subscribeToPlayback();
        getCurrentTrackAndTrackList();
    }

    private void getCurrentTrackAndTrackList() {
        disposables.add(repository.getSelectedFolderAudioFiles()
                .subscribeOn(Schedulers.io())
                .flatMap(audioFiles -> Observable.just(sortAudioFiles(settingsManager.getSortType(), audioFiles)))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(this::notifyCurrentTrackList)
                .subscribeOn(Schedulers.io())
                .flatMap(audioFiles -> repository.getSelectedAudioFile())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(this::notifyCurrentTrack)
                .doOnNext(this::notifyCoverImage)
                .subscribe(object -> {
                }, Throwable::printStackTrace));

    }

    private void subscribeToPlayback() {
        disposables.add(rxBus.toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object -> {
                    if (object instanceof PlaybackService) {
                        AppLog.ERROR("observeEvents main PlaybackService");
                        notifyPlayback((PlaybackService) object);
                    } else if (object instanceof AudioFile) {
                        AudioFile audioFile = (AudioFile) object;
                        notifyCurrentTrack(audioFile);
                        notifyCoverImage(audioFile);
                    } else if (object instanceof List<?>) {
                        List<?> list = (List<?>) object;
                        if (!list.isEmpty() && list.get(0) instanceof AudioFile) {
                            notifyCurrentTrackList((List<AudioFile>) object);
                        }
                    }
                }, Throwable::printStackTrace));
    }

    public void fetchFileSystemAudio() {
        disposables.add(RxUtils.fromCallable(repository.resetAudioContentDatabase())
                .subscribeOn(Schedulers.io())
                .doOnNext(integer -> CacheManager.clearAudioImagesCache())
                .subscribe(integer -> FileSystemService.startFetchAudio(context)));
    }

    public void fetchFileSystemVideo() {
        disposables.add(RxUtils.fromCallable(repository.resetVideoContentDatabase())
                .subscribeOn(Schedulers.io())
                .doOnNext(integer -> CacheManager.clearVideoImagesCache())
                .subscribe(aVoid -> FileSystemService.startFetchVideo(context)));
    }

    private void notifyCoverImage(AudioFile audioFile) {
        disposables.add(bitmapHelper.getTrackListArtwork(audioFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(coverLiveData::setValue));
    }

    private void notifyPlayback(PlaybackService playbackService) {
        if (playbackService.isFinish()) {
            finishPlaybackLiveData.call();
            return;
        }

        boolean converting = playbackService.isConvertStart();
        boolean lastConverting = isConverting();
        if (lastConverting != converting) {
            convertingLiveData.setValue(converting);
        }

        boolean loadingSuccess = playbackService.isLoadSuccess();
        boolean lastLoadingSuccess = isLoadingSuccess();
        if (lastLoadingSuccess != loadingSuccess) {
            loadingSuccessLiveData.setValue(loadingSuccess);
        }

        boolean playing = playbackService.isPlaying();
        boolean lastPlaying = isPlaying();
        if (lastPlaying != playing) {
            playingLiveData.setValue(playing);
        }

        int position = playbackService.getPosition();
        int lastPosition = getPosition();
        if (lastPosition != position) {
            positionLiveData.setValue(position);
        }

        boolean enableEq = playbackService.isEnableEQ();
        boolean lastEnableEQ = isEQEnable();
        if (lastEnableEQ != enableEq) {
            EQLiveData.setValue(enableEq);
        }

        boolean enableReverb = playbackService.isEnableReverb();
        boolean lastEnableReverb = isReverbEnable();
        if (lastEnableReverb != enableReverb) {
            reverbLiveData.setValue(enableReverb);
        }

        boolean enableWhoosh = playbackService.isEnableWhoosh();
        boolean lastEnableWhoosh = isWhooshEnable();
        if (lastEnableWhoosh != enableWhoosh) {
            whooshLiveData.setValue(enableWhoosh);
        }

        boolean enableEcho = playbackService.isEnableEcho();
        boolean lastEnableEcho = isEchoEnable();
        if (lastEnableEcho != enableEcho) {
            echoLiveData.setValue(enableEcho);
        }
    }

    public void killFFmpeg() {
        if (fFmpegHelper.isCommandRunning()) {
            fFmpegHelper.killRunningProcesses();
        }
    }

    private void notifyCurrentTrack(AudioFile audioFile) {
        AppLog.ERROR("notifyCurrentTrack: " + (audioFile == null));
        audioPlayer.setCurrentAudioFile(audioFile);
        currentTrackLiveData.setValue(audioFile);
    }

    private void notifyCurrentTrackList(List<AudioFile> audioFiles) {
        AppLog.ERROR("notifyCurrentTrackList: " + (audioFiles == null));
        audioPlayer.setCurrentTrackList(audioFiles);
        currentTrackListLiveData.setValue(audioFiles);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
    }

    public void playStateChanged() {
        AppLog.ERROR("playStateChanged()");
        if (!isConverting()) {
            if (isPlaying()) {
                AppLog.ERROR("pause");
                pause();
            } else {
                AppLog.ERROR("play");
                play();
            }
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
        return settingsManager.isUserPro();
    }

    public void dropEffects() {
        settingsManager.setEQEnable(false);
        settingsManager.setReverbEnable(false);
        settingsManager.setWhooshEnable(false);
        settingsManager.setEchoEnable(false);
    }

    public boolean isNeedMainActivityGuide() {
        return settingsManager.isNeedMainActivityGuide();
    }

    public void setMainActivityGuideWatched() {
        settingsManager.setNeedMainActivityGuide(false);
    }

    public MutableLiveData<AudioFile> getCurrentTrackLiveData() {
        return currentTrackLiveData;
    }

    public MutableLiveData<List<AudioFile>> getCurrentTrackListLiveData() {
        return currentTrackListLiveData;
    }

    public void setWhooshEnable(boolean enable) {
        PlaybackService.changeWhooshEnable(context, enable);
        settingsManager.setWhooshEnable(enable);
    }

    public void setEQEnable(boolean enable) {
        PlaybackService.changeEQEnable(context, enable);
        settingsManager.setEQEnable(enable);
    }

    public void setReverbEnable(boolean enable) {
        PlaybackService.changeReverbEnable(context, enable);
        settingsManager.setReverbEnable(enable);
    }

    public void setEchoEnable(boolean enable) {
        PlaybackService.changeEchoEnable(context, enable);
        settingsManager.setEchoEnable(enable);
    }

    public MutableLiveData<Integer> getPositionLiveData() {
        return positionLiveData;
    }

    public MutableLiveData<Boolean> getPlayingLiveData() {
        return playingLiveData;
    }

    public MutableLiveData<Boolean> getConvertingLiveData() {
        return convertingLiveData;
    }

    public MutableLiveData<Boolean> getLoadingSuccessLiveData() {
        return loadingSuccessLiveData;
    }

    public MutableLiveData<Boolean> getReverbLiveData() {
        return reverbLiveData;
    }

    public MutableLiveData<Boolean> getEQLiveData() {
        return EQLiveData;
    }

    public MutableLiveData<Boolean> getWhooshLiveData() {
        return whooshLiveData;
    }

    public MutableLiveData<Boolean> getEchoLiveData() {
        return echoLiveData;
    }

    public MutableLiveData<Void> getFinishPlaybackLiveData() {
        return finishPlaybackLiveData;
    }

    public MutableLiveData<Bitmap> getCoverLiveData() {
        return coverLiveData;
    }


    public boolean isPlaying() {
        Boolean playing = playingLiveData.getValue();
        if (playing == null) {
            return false;
        }
        return playing;
    }

    public boolean isConverting() {
        Boolean converting = convertingLiveData.getValue();
        if (converting == null) {
            return false;
        }
        return converting;
    }

    public boolean isLoadingSuccess() {
        Boolean success = loadingSuccessLiveData.getValue();
        if (success == null) {
            return false;
        }
        return success;
    }

    public boolean isEQEnable() {
        Boolean enable = EQLiveData.getValue();
        if (enable == null) {
            return false;
        }
        return enable;
    }

    public boolean isReverbEnable() {
        Boolean enable = reverbLiveData.getValue();
        if (enable == null) {
            return false;
        }
        return enable;
    }

    public boolean isWhooshEnable() {
        Boolean enable = whooshLiveData.getValue();
        if (enable == null) {
            return false;
        }
        return enable;
    }

    public boolean isEchoEnable() {
        Boolean enable = echoLiveData.getValue();
        if (enable == null) {
            return false;
        }
        return enable;
    }

    public int getPosition() {
        Integer position = positionLiveData.getValue();
        if (position == null) {
            return -1;
        }
        return position;
    }
}
