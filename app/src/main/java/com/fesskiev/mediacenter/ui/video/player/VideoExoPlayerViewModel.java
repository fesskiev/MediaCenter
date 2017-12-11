package com.fesskiev.mediacenter.ui.video.player;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.video.RendererState;
import com.fesskiev.mediacenter.players.VideoPlayer;
import com.fesskiev.mediacenter.services.VideoPlaybackService;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.RxBus;
import com.fesskiev.mediacenter.utils.events.SingleLiveEvent;

import java.util.Set;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;


public class VideoExoPlayerViewModel extends ViewModel {

    private final MutableLiveData<VideoFile> currentVideoFileLiveData = new MutableLiveData<>();

    private final MutableLiveData<Long> positionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Long> progressLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> playingLiveData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> firstVideoFileLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> lastVideoFileLiveData = new MutableLiveData<>();

    private final SingleLiveEvent<String> errorMessageLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> tracksInitLiveData = new SingleLiveEvent<>();

    @Inject
    AppSettingsManager settingsManager;
    @Inject
    VideoPlayer videoPlayer;
    @Inject
    RxBus rxBus;

    private CompositeDisposable disposables;

    public VideoExoPlayerViewModel() {
        MediaApplication.getInstance().getAppComponent().inject(this);
        disposables = new CompositeDisposable();
        subscribeToEvents();
    }

    private void subscribeToEvents() {
        disposables.add(rxBus.toCurrentVideoFileObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(videoFile -> {
                    notifyCurrentVideoFile(videoFile);
                    notifyFirstOtLastTack();
                }, Throwable::printStackTrace));

        disposables.add(rxBus.toVideoPlaybackObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::notifyPlayback, Throwable::printStackTrace));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
    }

    private void notifyPlayback(VideoPlaybackService playbackService) {
        VideoPlaybackService.VIDEO_PLAYBACK videoPlayback = playbackService.getVideoPlaybackState();
        switch (videoPlayback) {
            case ERROR:
                notifyError(playbackService.getErrorMessage());
                break;
            case TRACKS:
                notifyTracksInit();
                break;
            case READY:
                updatePlayback(playbackService);
                break;
        }
    }

    private void notifyTracksInit() {
        tracksInitLiveData.call();
    }

    private void updatePlayback(VideoPlaybackService playbackService) {

        long position = playbackService.getPosition();
        long lastPosition = getPosition();
        if (lastPosition != position) {
            positionLiveData.setValue(position);
        }

        long progress = playbackService.getProgress();
        long lastProgress = getProgress();
        if (lastProgress != progress) {
            progressLiveData.setValue(progress);
        }

        boolean playing = playbackService.isPlaying();
        boolean lastPlaying = isPlaying();
        if (lastPlaying != playing) {
            playingLiveData.setValue(playing);
        }
    }

    private void notifyError(String errorMessage) {
        errorMessageLiveData.setValue(errorMessage);
    }

    private void notifyFirstOtLastTack() {
        if (videoPlayer.first()) {
            firstVideoFileLiveData.setValue(true);
        } else {
            firstVideoFileLiveData.setValue(false);
        }
        if (videoPlayer.last()) {
            lastVideoFileLiveData.setValue(true);
        } else {
            lastVideoFileLiveData.setValue(false);
        }
    }

    private void notifyCurrentVideoFile(VideoFile videoFile) {
        currentVideoFileLiveData.setValue(videoFile);
    }

    public void next() {
        videoPlayer.next();
    }

    public void previous() {
        videoPlayer.previous();
    }

    public void setNeedVideoPlayerActivityGuide(boolean need) {
        settingsManager.setNeedVideoPlayerActivityGuide(need);
    }

    public boolean isNeedVideoPlayerActivityGuide() {
        return settingsManager.isNeedVideoPlayerActivityGuide();
    }

    public void saveRendererState(Set<RendererState> states) {
        settingsManager.setRendererState(states);
    }

    public Set<RendererState> getRendererState() {
        return settingsManager.getRendererState();
    }

    public void clearRendererState() {
        settingsManager.clearRendererState();
    }

    public boolean isProUser() {
        return settingsManager.isUserPro();
    }

    public MutableLiveData<VideoFile> getCurrentVideoFileLiveData() {
        return currentVideoFileLiveData;
    }

    public MutableLiveData<Boolean> getFirstVideoFileLiveData() {
        return firstVideoFileLiveData;
    }

    public MutableLiveData<Boolean> getLastVideoFileLiveData() {
        return lastVideoFileLiveData;
    }

    public MutableLiveData<Long> getPositionLiveData() {
        return positionLiveData;
    }

    public MutableLiveData<Long> getProgressLiveData() {
        return progressLiveData;
    }

    public MutableLiveData<Boolean> getPlayingLiveData() {
        return playingLiveData;
    }

    public SingleLiveEvent<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public SingleLiveEvent<Void> getTracksInitLiveData() {
        return tracksInitLiveData;
    }

    public long getPosition() {
        Long position = positionLiveData.getValue();
        if (position == null) {
            return -1;
        }
        return position;
    }

    private long getProgress() {
        Long progress = progressLiveData.getValue();
        if (progress == null) {
            return -1;
        }
        return progress;
    }

    public boolean isPlaying() {
        Boolean playing = playingLiveData.getValue();
        if (playing == null) {
            return false;
        }
        return playing;
    }
}
