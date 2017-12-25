package com.fesskiev.mediacenter.ui.fetch;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.utils.RxBus;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.events.SingleLiveEvent;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class FetchContentViewModel extends ViewModel {

    private final MutableLiveData<Float> percentLiveData = new MutableLiveData<>();
    private final MutableLiveData<FileSystemService.FetchDescription> fetchDescriptionLiveData = new MutableLiveData<>();
    private final SingleLiveEvent<Void> prepareFetchLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> finishFetchLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> fetchAudioStartLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> fetchVideoStartLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> audioFoldersCreatedLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> videoFoldersCreatedLiveData = new SingleLiveEvent<>();

    @Inject
    RxBus rxBus;

    private Disposable disposable;
    private Disposable timerDisposable;

    public FetchContentViewModel() {
        MediaApplication.getInstance().getAppComponent().inject(this);
        observeEvents();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        RxUtils.unsubscribe(disposable);
    }

    private void observeEvents() {
        disposable = rxBus.toFileSystemObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::notifyFetchState, Throwable::printStackTrace);
    }

    private void notifyFetchState(FileSystemService fileSystemService) {
        FileSystemService.SCAN_STATE scanState = fileSystemService.getScanState();
        if (scanState != null) {
            switch (scanState) {
                case PREPARE:
                    addTimerCheckFolders(fileSystemService);
                    prepare(fileSystemService);
                    break;
                case SCANNING:
                    scanning(fileSystemService);
                    break;
                case FINISHED:
                    clearTimer();
                    finish();
                    break;
            }
        }
    }

    private void scanning(FileSystemService fileSystemService) {
        FileSystemService.FetchDescription fetchDescription = fileSystemService.getFetchDescription();
        if (fetchDescription != null) {
            FileSystemService.FetchDescription lastFetchDescription = fetchDescriptionLiveData.getValue();
            if (lastFetchDescription == null || lastFetchDescription != fetchDescription) {
                fetchDescriptionLiveData.setValue(fetchDescription);
            }
        }

        float progress = fileSystemService.getProgress();
        float lastProgress = getProgress();
        if (lastProgress != progress) {
            percentLiveData.setValue(progress);
        }
    }

    private void finish() {
        finishFetchLiveData.call();
    }

    private void prepare(FileSystemService fileSystemService) {
        prepareFetchLiveData.call();
        FileSystemService.SCAN_TYPE scanType = fileSystemService.getScanType();
        switch (scanType) {
            case AUDIO:
                fetchAudioStartLiveData.call();
                break;
            case VIDEO:
                fetchVideoStartLiveData.call();
                break;
            case BOTH:
                fetchVideoStartLiveData.call();
                fetchAudioStartLiveData.call();
                break;
        }
    }

    private void addTimerCheckFolders(FileSystemService fileSystemService) {
        timerDisposable = Observable.interval(5, 5, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    FileSystemService.SCAN_TYPE scanType = fileSystemService.getScanType();
                    switch (scanType) {
                        case AUDIO:
                            audioFoldersCreatedLiveData.call();
                            break;
                        case VIDEO:
                            videoFoldersCreatedLiveData.call();
                            break;
                    }
                }, Throwable::printStackTrace);
    }

    private void clearTimer() {
        if (timerDisposable != null && !timerDisposable.isDisposed()) {
            timerDisposable.dispose();
        }
    }

    public MutableLiveData<Float> getPercentLiveData() {
        return percentLiveData;
    }

    public MutableLiveData<FileSystemService.FetchDescription> getFetchDescriptionLiveData() {
        return fetchDescriptionLiveData;
    }

    public SingleLiveEvent<Void> getPrepareFetchLiveData() {
        return prepareFetchLiveData;
    }

    public SingleLiveEvent<Void> getFinishFetchLiveData() {
        return finishFetchLiveData;
    }

    public SingleLiveEvent<Void> getFetchAudioStartLiveData() {
        return fetchAudioStartLiveData;
    }

    public SingleLiveEvent<Void> getFetchVideoStartLiveData() {
        return fetchVideoStartLiveData;
    }

    public SingleLiveEvent<Void> getAudioFoldersCreatedLiveData() {
        return audioFoldersCreatedLiveData;
    }

    public SingleLiveEvent<Void> getVideoFoldersCreatedLiveData() {
        return videoFoldersCreatedLiveData;
    }

    private float getProgress() {
        Float progress = percentLiveData.getValue();
        if (progress == null) {
            return 0f;
        }
        return progress;
    }
}
