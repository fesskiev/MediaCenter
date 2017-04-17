package com.fesskiev.mediacenter.utils;


import android.support.v4.content.ContextCompat;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.widgets.fetch.FetchContentView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.fesskiev.mediacenter.services.FileSystemService.FetchFolderCreate.AUDIO;
import static com.fesskiev.mediacenter.services.FileSystemService.FetchFolderCreate.VIDEO;


public class FetchMediaFilesManager {

    private final static int DELAY = 3;

    public interface OnFetchMediaFilesListener {

        void onFetchMediaPrepare();

        void onFetchAudioContentStart(boolean clear);

        void onFetchVideoContentStart(boolean clear);

        void onFetchMediaContentFinish();

        void onAudioFolderCreated();

        void onVideoFolderCreated();
    }

    private OnFetchMediaFilesListener listener;
    private FetchContentView fetchContentView;
    private boolean fetchStart;
    private boolean needTimer;
    private int folderAudioCount;
    private int folderVideoCount;

    public FetchMediaFilesManager(FetchContentView fetchContentView) {
        this.fetchContentView = fetchContentView;
        EventBus.getDefault().register(this);
    }

    public void setOnFetchMediaFilesListener(OnFetchMediaFilesListener l) {
        this.listener = l;
    }

    public void unregister() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlaybackStateEvent(FileSystemService fileSystemService) {
        FileSystemService.SCAN_STATE scanState = fileSystemService.getScanState();
        switch (scanState) {
            case PREPARE:
                prepare();
                break;
            case SCANNING_ALL:
                scanning(fileSystemService.getScanType(), true);
                break;
            case SCANNING_FOUND:
                scanning(fileSystemService.getScanType(), false);
                break;
            case FINISHED:
                finish();
                break;
        }
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFetchObjectEvent(FileSystemService.FetchDescription fetchDescription) {
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

    private void scanning(FileSystemService.SCAN_TYPE scanType, boolean clear) {
        if (fetchContentView != null) {
            fetchContentView.setVisibleContent();
            if (needTimer) {
                fetchContentView.showTimer();
            }
        }
        fetchStart = true;

        switch (scanType) {
            case AUDIO:
                if (listener != null) {
                    listener.onFetchAudioContentStart(clear);
                }
                break;
            case VIDEO:
                if (listener != null) {
                    listener.onFetchVideoContentStart(clear);
                }
                break;
            case BOTH:
                if (listener != null) {
                    listener.onFetchAudioContentStart(clear);
                    listener.onFetchVideoContentStart(clear);
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

        if (fetchContentView != null) {
            fetchContentView.setInvisibleContent();
            if (needTimer) {
                fetchContentView.hideTimer();
                fetchContentView.clear();
            }
        }
        fetchStart = false;
        folderVideoCount = 0;
        folderAudioCount = 0;
    }

    private void prepare() {
        if (listener != null) {
            listener.onFetchMediaPrepare();
        }
    }

    public void setFetchContentView(FetchContentView fetchContentView) {
        this.fetchContentView = fetchContentView;
    }

    public boolean isFetchStart() {
        return fetchStart;
    }

    public void setTextWhite() {
        fetchContentView.setTextColor(ContextCompat.getColor(MediaApplication.getInstance().getApplicationContext(),
                R.color.white));
    }

    public void setTextPrimary() {
        fetchContentView.setTextColor(ContextCompat.getColor(MediaApplication.getInstance().getApplicationContext(),
                R.color.primary));
    }

    public void isNeedTimer(boolean needTimer) {
        this.needTimer = needTimer;
    }
}
