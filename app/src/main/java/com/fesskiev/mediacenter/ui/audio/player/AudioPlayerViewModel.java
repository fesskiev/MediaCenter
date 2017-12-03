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

        setCurrentTrack(audioPlayer.getCurrentTrack());
        PlaybackService.requestPlaybackStateIfNeed(context);
    }

    private void subscribeToEvents() {
        disposables.add(rxBus.toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object -> {
                    if (object instanceof PlaybackService) {
                        notifyPlayback((PlaybackService) object);
                    } else if (object instanceof AudioFile) {
                        setCurrentTrack((AudioFile) object);
                    }
                }));

    }

    private void setCurrentTrack(AudioFile audioFile) {
        notifyCurrentTrack(audioFile);
        notifyFirstOtLastTack();
        notifyCoverImage(audioFile);
        notifyPalette(audioFile);
    }

    private void notifyPalette(AudioFile audioFile) {
        disposables.add(bitmapHelper.getAudioFilePalette(audioFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(paletteLiveData::setValue));
    }

    private void notifyCoverImage(AudioFile audioFile) {
        disposables.add(bitmapHelper.loadAudioPlayerArtwork(audioFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
        boolean lastConverting = isConverting();
        if (lastConverting != converting) {
            convertingLiveData.setValue(converting);
        }

        boolean loadingSuccess = playbackService.isLoadSuccess();
        boolean lastLoadingSuccess = isLoadingSuccess();
        if (lastLoadingSuccess != loadingSuccess) {
            loadingSuccessLiveData.setValue(loadingSuccess);
        }

        boolean loadingError = playbackService.isLoadError();
        boolean lastLoadingError = isLoadingError();
        if (lastLoadingError != loadingError) {
            loadingErrorLiveData.setValue(loadingError);
        }

        boolean playing = playbackService.isPlaying();
        boolean lastPlaying = isPlaying();
        if (lastPlaying != playing) {
            playingLiveData.setValue(playing);
        }

        boolean looping = playbackService.isLooping();
        boolean lastLooping = isLooping();
        if (lastLooping != looping) {
            loopingLiveData.setValue(looping);
        }

        int duration = playbackService.getDuration();
        int lastDuration = getDuration();
        if (lastDuration != duration) {
            durationLiveData.setValue(duration);
        }

        int position = playbackService.getPosition();
        int lastPosition = getPosition();
        if (lastPosition != position) {
            positionLiveData.setValue(position);
        }

        float positionPercent = playbackService.getPositionPercent();
        float lastPositionPercent = getPositionPercent();
        if (lastPositionPercent != positionPercent) {
            positionPercentLiveData.setValue(positionPercent);
        }

        float volume = playbackService.getVolume();
        float lastVolume = getVolume();
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
        if (!isConverting()) {
            if (isPlaying()) {
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
        if (!isLooping() && !isConverting()) {
            audioPlayer.next();
            nextTrackLiveData.setValue(null);
        }
    }

    public void previous() {
        if (!isLooping() && !isConverting()) {
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

    public boolean isLooping() {
        Boolean looping = loopingLiveData.getValue();
        if (looping == null) {
            return false;
        }
        return looping;
    }

    public float getPositionPercent() {
        Float position = positionPercentLiveData.getValue();
        if (position == null) {
            return -1;
        }
        return position;
    }

    public int getPosition() {
        Integer position = positionLiveData.getValue();
        if (position == null) {
            return -1;
        }
        return position;
    }

    public int getDuration() {
        Integer duration = durationLiveData.getValue();
        if (duration == null) {
            return -1;
        }
        return duration;
    }

    public float getVolume() {
        Float volume = volumeLiveData.getValue();
        if (volume == null) {
            return -1;
        }
        return volume;
    }

    public boolean isLoadingSuccess() {
        Boolean success = loadingSuccessLiveData.getValue();
        if (success == null) {
            return false;
        }
        return success;
    }

    public boolean isLoadingError() {
        Boolean error = loadingErrorLiveData.getValue();
        if (error == null) {
            return false;
        }
        return error;
    }

}
