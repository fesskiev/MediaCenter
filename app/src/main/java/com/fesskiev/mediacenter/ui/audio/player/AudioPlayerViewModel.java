package com.fesskiev.mediacenter.ui.audio.player;


import android.annotation.SuppressLint;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.graphics.Bitmap;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.RxBus;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AudioPlayerViewModel extends ViewModel {

    private final MutableLiveData<AudioFile> currentTrackLiveData = new MutableLiveData<>();

    private final MutableLiveData<Float> volumeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> positionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Float> positionPercentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> durationLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loopingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> playingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> convertingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingErrorLiveData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> firstTrackLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> lastTrackLiveData = new MutableLiveData<>();

    private final MutableLiveData<Bitmap> coverLiveData = new MutableLiveData<>();
    private final MutableLiveData<BitmapHelper.PaletteColor> paletteLiveData = new MutableLiveData<>();

    private final MutableLiveData<Void> nextTrackLiveData = new MutableLiveData<>();
    private final MutableLiveData<Void> previousTrackLiveData = new MutableLiveData<>();

    @Inject
    AudioPlayer audioPlayer;
    @Inject
    AppSettingsManager settingsManager;
    @Inject
    RxBus rxBus;
    @Inject
    BitmapHelper bitmapHelper;
    @SuppressLint("StaticFieldLeak")
    @Inject
    Context context;

    private CompositeDisposable disposables;

    public AudioPlayerViewModel() {
        MediaApplication.getInstance().getAppComponent().inject(this);
        disposables = new CompositeDisposable();
        subscribeToEvents();

        currentTrackLiveData.setValue(audioPlayer.getCurrentTrack());
        PlaybackService.requestPlaybackStateIfNeed(context);
    }

    private void subscribeToEvents() {
        disposables.add(rxBus.toObservable()
                .subscribe(object -> {
                    if (object instanceof PlaybackService) {
                        notifyPlayback((PlaybackService) object);
                    } else if (object instanceof AudioFile) {
                        AudioFile audioFile = (AudioFile) object;
                        notifyCurrentTrack(audioFile);
                        notifyFirstOtLastTack();
                        notifyCoverImage(audioFile);
                        notifyPalette(audioFile);
                    }
                }));

    }

    private void notifyPalette(AudioFile audioFile) {
        disposables.add(bitmapHelper.getAudioFilePalette(audioFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(paletteLiveData::setValue));
    }

    private void notifyCoverImage(AudioFile audioFile) {
        disposables.add(bitmapHelper.loadAudioPlayerArtwork(audioFile)
                .subscribe(coverLiveData::setValue));
    }

    private void notifyCurrentTrack(AudioFile audioFile) {
        currentTrackLiveData.setValue(audioFile);
    }

    private void notifyFirstOtLastTack() {
        if (audioPlayer.first()) {
            firstTrackLiveData.setValue(true);
        } else {
            firstTrackLiveData.setValue(false);
        }
        if (audioPlayer.last()) {
            lastTrackLiveData.setValue(true);
        } else {
            lastTrackLiveData.setValue(false);
        }
    }

    private void notifyPlayback(PlaybackService playbackService) {
        boolean converting = playbackService.isConvertStart();
        boolean lastConverting = convertingLiveData.getValue();
        if (lastConverting != converting) {
            convertingLiveData.setValue(converting);
        }

        boolean loadingSuccess = playbackService.isLoadSuccess();
        boolean lastLoadingSuccess = loadingSuccessLiveData.getValue();
        if (lastLoadingSuccess != loadingSuccess) {
            loadingSuccessLiveData.setValue(loadingSuccess);
        }

        boolean loadingError = playbackService.isLoadError();
        boolean lastLoadingError = loadingErrorLiveData.getValue();
        if (lastLoadingError != loadingError) {
            loadingErrorLiveData.setValue(loadingError);
        }

        boolean playing = playbackService.isPlaying();
        boolean lastPlaying = playingLiveData.getValue();
        if (lastPlaying != playing) {
            playingLiveData.setValue(lastPlaying);
        }

        boolean looping = playbackService.isLooping();
        boolean lastLooping = loopingLiveData.getValue();
        if (lastLooping != looping) {
            loopingLiveData.setValue(lastLooping);
        }

        int duration = playbackService.getDuration();
        int lastDuration = durationLiveData.getValue();
        if (lastDuration != duration) {
            durationLiveData.setValue(duration);
        }

        int position = playbackService.getPosition();
        int lastPosition = positionLiveData.getValue();
        if (lastPosition != position) {
            positionLiveData.setValue(position);
        }

        float positionPercent = playbackService.getPositionPercent();
        float lastPositionPercent = positionPercentLiveData.getValue();
        if (lastPositionPercent != positionPercent) {
            positionPercentLiveData.setValue(positionPercent);
        }

        float volume = playbackService.getVolume();
        float lastVolume = volumeLiveData.getValue();
        if (lastVolume != volume) {
            volumeLiveData.setValue(volume);
        }
    }

    public void volumeStateChanged(int volume) {
        PlaybackService.volumePlayback(context, volume);
    }

    public void seekPlayback(int seek) {
        PlaybackService.seekPlayback(context, seek);
    }

    public void changeLoopingState(boolean repeat) {
        PlaybackService.changeLoopingState(context, repeat);
    }

    public void startLooping(int start, int end) {
        PlaybackService.startLooping(context, start, end);
    }

    public void playStateChanged() {
        if (!convertingLiveData.getValue()) {
            if (playingLiveData.getValue()) {
                pause();
            } else {
                play();
            }
        }
    }

    private void play() {
        audioPlayer.play();
    }

    private void pause() {
        audioPlayer.pause();
    }

    public void next() {
        if (!loopingLiveData.getValue() && !convertingLiveData.getValue()) {
            audioPlayer.next();
            nextTrackLiveData.setValue(null);
        }
    }

    public void previous() {
        if (!loopingLiveData.getValue() && !convertingLiveData.getValue()) {
            audioPlayer.previous();
            previousTrackLiveData.setValue(null);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
    }

    public boolean isFullScreenMode() {
        return settingsManager.isFullScreenMode();
    }

    public boolean isUserPro() {
        return settingsManager.isUserPro();
    }

    public boolean isNeedAudioPlayerActivityGuide() {
        return settingsManager.isNeedAudioPlayerActivityGuide();
    }

    public void setNeedAudioPlayerActivityGuide(boolean need) {
        settingsManager.setNeedAudioPlayerActivityGuide(need);
    }

    public MutableLiveData<AudioFile> getCurrentTrackLiveData() {
        return currentTrackLiveData;
    }

    public MutableLiveData<Float> getVolumeLiveData() {
        return volumeLiveData;
    }

    public MutableLiveData<Integer> getPositionLiveData() {
        return positionLiveData;
    }

    public MutableLiveData<Integer> getDurationLiveData() {
        return durationLiveData;
    }

    public MutableLiveData<Boolean> getLoopingLiveData() {
        return loopingLiveData;
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

    public MutableLiveData<Boolean> getLoadingErrorLiveData() {
        return loadingErrorLiveData;
    }

    public MutableLiveData<Float> getPositionPercentLiveData() {
        return positionPercentLiveData;
    }

    public MutableLiveData<Boolean> getFirstTrackLiveData() {
        return firstTrackLiveData;
    }

    public MutableLiveData<Boolean> getLastTrackLiveData() {
        return lastTrackLiveData;
    }

    public MutableLiveData<Bitmap> getCoverLiveData() {
        return coverLiveData;
    }

    public MutableLiveData<BitmapHelper.PaletteColor> getPaletteLiveData() {
        return paletteLiveData;
    }

    public MutableLiveData<Void> getNextTrackLiveData() {
        return nextTrackLiveData;
    }

    public MutableLiveData<Void> getPreviousTrackLiveData() {
        return previousTrackLiveData;
    }

}
