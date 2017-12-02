package com.fesskiev.mediacenter.ui.video.player;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.video.RendererState;
import com.fesskiev.mediacenter.players.VideoPlayer;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.RxBus;

import java.util.Set;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;


public class VideoExoPlayerViewModel extends ViewModel {

    private final MutableLiveData<VideoFile> currentVideoFileLiveData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> firstVideoFileLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> lastVideoFileLiveData = new MutableLiveData<>();

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
        disposables.add(rxBus.toObservable()
                .subscribe(object -> {
                    if (object instanceof VideoFile) {
                        VideoFile videoFile = (VideoFile) object;
                        notifyCurrentVideoFile(videoFile);
                        notifyFirstOtLastTack();
                    }
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
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

    public void saveRendererState(Set<RendererState> states){
        settingsManager.setRendererState(states);
    }

    public Set<RendererState> getRendererState(){
        return settingsManager.getRendererState();
    }

    public void clearRendererState(){
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
}
