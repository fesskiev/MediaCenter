package com.fesskiev.mediacenter.utils;


import android.support.v4.content.ContextCompat;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.widgets.fetch.FetchContentView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

import static com.fesskiev.mediacenter.services.FileSystemService.FetchFolderCreate.AUDIO;
import static com.fesskiev.mediacenter.services.FileSystemService.FetchFolderCreate.VIDEO;


public class FetchMediaFilesManager {

    private final static int DELAY = 3;

    public interface OnFetchMediaFilesListener {

        void onFetchMediaPrepare();

        void onFetchAudioContentStart();

        void onFetchVideoContentStart();

        void onFetchMediaContentFinish();

        void onAudioFolderCreated();

        void onVideoFolderCreated();
    }


    private static FetchMediaFilesManager INSTANCE;

    public static FetchMediaFilesManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FetchMediaFilesManager();
        }
        return INSTANCE;
    }

    private OnFetchMediaFilesListener listener;

    private WeakReference<FetchContentView> fetchContentRef;

    private boolean fetchStart;
    private boolean fetchComplete;
    private int folderAudioCount;
    private int folderVideoCount;

    private FetchMediaFilesManager() {

    }

    public void setFetchContentView(FetchContentView fetchContentView) {
        this.fetchContentRef = new WeakReference<>(fetchContentView);
    }

    public void setOnFetchMediaFilesListener(OnFetchMediaFilesListener l) {
        this.listener = l;
    }

    public void unregister() {
        EventBus.getDefault().unregister(this);
    }

    public void register() {
        EventBus.getDefault().register(this);
        FileSystemService.requestFetchState(MediaApplication.getInstance().getApplicationContext());

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFetchPercent(Float percent) {
        FetchContentView fetchContentView = fetchContentRef.get();
        if (fetchContentView != null) {
            fetchContentView.setProgress(percent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFetchObjectEvent(FileSystemService.FetchDescription fetchDescription) {
        FetchContentView fetchContentView = fetchContentRef.get();
        if (fetchContentView != null) {
            String folderName = fetchDescription.getFolderName();
            if (folderName != null) {
                fetchContentView.setFolderName(folderName);
            }

            String fileName = fetchDescription.getFileName();
            if (fileName != null) {
                fetchContentView.setFileName(fileName);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFetchFolderCreate(FileSystemService.FetchFolderCreate fetchFolderCreate) {
        int type = fetchFolderCreate.getType();
        switch (type) {
            case AUDIO:
                folderAudioCount++;
                if (listener != null && folderAudioCount == DELAY) {
                    listener.onAudioFolderCreated();
                    folderAudioCount = 0;
                }
                break;
            case VIDEO:
                folderVideoCount++;
                if (listener != null && folderVideoCount == DELAY) {
                    listener.onVideoFolderCreated();
                    folderVideoCount = 0;
                }
                break;
        }

    }

    public void visibleContent() {
        FetchContentView fetchContentView = fetchContentRef.get();
        if (fetchContentView != null) {
            fetchContentView.setVisibleContent();
            fetchContentView.showTimer();
        }
    }

    private void scanning(FileSystemService.SCAN_TYPE scanType) {
        visibleContent();
        fetchStart = true;
        fetchComplete = false;

        switch (scanType) {
            case AUDIO:
                if (listener != null) {
                    listener.onFetchAudioContentStart();
                }
                break;
            case VIDEO:
                if (listener != null) {
                    listener.onFetchVideoContentStart();
                }
                break;
            case BOTH:
                if (listener != null) {
                    listener.onFetchAudioContentStart();
                    listener.onFetchVideoContentStart();
                }
                break;
        }
    }

    private void finish() {
        if (listener != null) {
            listener.onFetchMediaContentFinish();
            listener.onAudioFolderCreated();
            listener.onVideoFolderCreated();
        }

        FetchContentView fetchContentView = fetchContentRef.get();
        if (fetchContentView != null) {
            fetchContentView.setInvisibleContent();
            fetchContentView.hideTimer();
            fetchContentView.clear();
        }
        fetchStart = false;
        fetchComplete = true;
        folderVideoCount = 0;
        folderAudioCount = 0;
    }

    private void prepare() {
        if (listener != null) {
            listener.onFetchMediaPrepare();
        }
    }

    public boolean isFetchStart() {
        return fetchStart;
    }

    public boolean isFetchComplete() {
        return fetchComplete;
    }

    public void setTextWhite() {
        FetchContentView fetchContentView = fetchContentRef.get();
        if (fetchContentView != null) {
            fetchContentView.setTextColor(ContextCompat.getColor(MediaApplication.getInstance().getApplicationContext(),
                    R.color.white));
        }
    }

}
