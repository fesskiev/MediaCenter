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

import static com.fesskiev.mediacenter.services.FileSystemService.FetchFolderCreate.AUDIO;
import static com.fesskiev.mediacenter.services.FileSystemService.FetchFolderCreate.VIDEO;


public class FetchContentViewModel extends ViewModel {

    private final MutableLiveData<Float> percentLiveData = new MutableLiveData<>();
    private final MutableLiveData<FileSystemService.FetchDescription> fetchDescriptionLiveData = new MutableLiveData<>();
    private final SingleLiveEvent<Void> prepareFetchLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> finishFetchLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> fetchAudioStartLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> fetchVideoStartLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> audioFoldersCreatedLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> videoFoldersCreatedLiveData = new SingleLiveEvent<>();

    private final static int DELAY = 2;

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
        disposable = rxBus.toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object -> {
                    if (object instanceof FileSystemService) {
                        onPlaybackStateEvent((FileSystemService) object);
                    } else if (object instanceof Float) {
                        onFetchPercent((Float) object);
                    } else if (object instanceof FileSystemService.FetchDescription) {
                        onFetchObjectEvent((FileSystemService.FetchDescription) object);
                    } else if (object instanceof FileSystemService.FetchFolderCreate) {
                        onFetchFolderCreated((FileSystemService.FetchFolderCreate) object);
                    }
                }, Throwable::printStackTrace);
    }

    public void onPlaybackStateEvent(FileSystemService fileSystemService) {
        FileSystemService.SCAN_STATE scanState = fileSystemService.getScanState();
        if (scanState != null) {
            switch (scanState) {
                case PREPARE:
                    prepare();
                    break;
                case SCANNING_ALL:
                    scanning(fileSystemService.getScanType());
                    break;
                case FINISHED:
                    finish();
                    break;
            }
        }
    }

    public void onFetchPercent(Float percent) {
        percentLiveData.setValue(percent);
    }

    public void onFetchObjectEvent(FileSystemService.FetchDescription fetchDescription) {
        fetchDescriptionLiveData.setValue(fetchDescription);
    }

    public void onFetchFolderCreated(FileSystemService.FetchFolderCreate fetchFolderCreate) {
        int type = fetchFolderCreate.getType();
        switch (type) {
            case AUDIO:
                folderAudioCount++;
                if (folderAudioCount == DELAY) {
                    folderAudioCount = 0;
                    audioFoldersCreatedLiveData.call();
                }
                break;
            case VIDEO:
                folderVideoCount++;
                if (folderVideoCount == DELAY) {
                    folderVideoCount = 0;
                    videoFoldersCreatedLiveData.call();
                }
                break;
        }

    }

    private void scanning(FileSystemService.SCAN_TYPE scanType) {
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

    private void finish() {
        folderVideoCount = 0;
        folderAudioCount = 0;
        finishFetchLiveData.call();
    }

    private void prepare() {
        prepareFetchLiveData.call();
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
}
