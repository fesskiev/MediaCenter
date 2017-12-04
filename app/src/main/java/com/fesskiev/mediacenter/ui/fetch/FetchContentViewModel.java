package com.fesskiev.mediacenter.ui.fetch;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.RxBus;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.events.SingleLiveEvent;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.fesskiev.mediacenter.services.FileSystemService.FetchFolderCreated.AUDIO;
import static com.fesskiev.mediacenter.services.FileSystemService.FetchFolderCreated.VIDEO;


public class FetchContentViewModel extends ViewModel {

    private final MutableLiveData<Float> percentLiveData = new MutableLiveData<>();
    private final MutableLiveData<FileSystemService.FetchDescription> fetchDescriptionLiveData = new MutableLiveData<>();
    private final SingleLiveEvent<Void> prepareFetchLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> finishFetchLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> fetchAudioStartLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> fetchVideoStartLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<FileSystemService.FetchFolderCreated> audioFoldersCreatedLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<FileSystemService.FetchFolderCreated> videoFoldersCreatedLiveData = new SingleLiveEvent<>();

    private final static int DELAY = 3;

    @Inject
    RxBus rxBus;

    private Disposable disposable;

    private int folderAudioCount;
    private int folderVideoCount;


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
                    prepare(fileSystemService);
                    break;
                case SCANNING:
                    scanning(fileSystemService);
                    break;
                case FINISHED:
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

        FileSystemService.FetchFolderCreated fetchFolderCreated = fileSystemService.getFolderCreated();
        if (fetchFolderCreated != null) {
            int type = fetchFolderCreated.getType();
            switch (type) {
                case AUDIO:
                    FileSystemService.FetchFolderCreated lastAudioFetchFolderCreated = audioFoldersCreatedLiveData.getValue();
                    if (lastAudioFetchFolderCreated == null || lastAudioFetchFolderCreated != fetchFolderCreated) {
                        audioFoldersCreatedLiveData.setValue(fetchFolderCreated);
                        folderAudioCount++;
                        if (folderAudioCount == DELAY) {
                            folderAudioCount = 0;
                            audioFoldersCreatedLiveData.call();
                        }
                    }
                    break;
                case VIDEO:
                    FileSystemService.FetchFolderCreated lastVideoFetchFolderCreated = audioFoldersCreatedLiveData.getValue();
                    if (lastVideoFetchFolderCreated == null || lastVideoFetchFolderCreated != fetchFolderCreated) {
                        videoFoldersCreatedLiveData.setValue(fetchFolderCreated);
                        folderVideoCount++;
                        if (folderVideoCount == DELAY) {
                            folderVideoCount = 0;
                            videoFoldersCreatedLiveData.call();
                        }
                    }
                    break;
            }
        }

        float progress = fileSystemService.getProgress();
        float lastProgress = getProgress();
        if (lastProgress != progress) {
            percentLiveData.setValue(progress);
        }
    }

    private void finish() {
        folderVideoCount = 0;
        folderAudioCount = 0;
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

    public SingleLiveEvent<FileSystemService.FetchFolderCreated> getAudioFoldersCreatedLiveData() {
        return audioFoldersCreatedLiveData;
    }

    public SingleLiveEvent<FileSystemService.FetchFolderCreated> getVideoFoldersCreatedLiveData() {
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
