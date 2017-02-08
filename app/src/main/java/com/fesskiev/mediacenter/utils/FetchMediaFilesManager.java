package com.fesskiev.mediacenter.utils;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.widgets.fetch.FetchContentView;


public class FetchMediaFilesManager {

    public interface OnFetchMediaFilesListener {

        void onFetchContentStart();

        void onFetchContentFinish();

    }

    private OnFetchMediaFilesListener listener;
    private FetchContentView fetchContentView;
    private boolean fetchStart;
    private boolean needTimer;

    public FetchMediaFilesManager(FetchContentView fetchContentView) {
        this.fetchContentView = fetchContentView;
        registerBroadcastReceiver();
    }

    public void setOnFetchMediaFilesListener(OnFetchMediaFilesListener l) {
        this.listener = l;
    }

    public void unregister() {
        unregisterBroadcastReceiver();
    }


    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(FileSystemService.ACTION_START_FETCH_MEDIA_CONTENT);
        filter.addAction(FileSystemService.ACTION_END_FETCH_MEDIA_CONTENT);
        filter.addAction(FileSystemService.ACTION_AUDIO_FOLDER_NAME);
        filter.addAction(FileSystemService.ACTION_AUDIO_TRACK_NAME);
        filter.addAction(FileSystemService.ACTION_VIDEO_FILE);
        LocalBroadcastManager.getInstance(MediaApplication.getInstance().getApplicationContext())
                .registerReceiver(broadcastReceiver, filter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(MediaApplication.getInstance().getApplicationContext())
                .unregisterReceiver(broadcastReceiver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {
            switch (intent.getAction()) {
                case FileSystemService.ACTION_START_FETCH_MEDIA_CONTENT:
                    if (listener != null) {
                        listener.onFetchContentStart();
                    }
                    fetchContentView.setVisibleContent();
                    if (needTimer) {
                        fetchContentView.showTimer();
                    }
                    fetchStart = true;
                    break;
                case FileSystemService.ACTION_END_FETCH_MEDIA_CONTENT:
                    if (listener != null) {
                        listener.onFetchContentFinish();
                    }
                    fetchContentView.setInvisibleContent();
                    if (needTimer) {
                        fetchContentView.hideTimer();
                    }
                    fetchStart = false;
                    break;
                case FileSystemService.ACTION_AUDIO_FOLDER_NAME:
                    String folderName =
                            intent.getStringExtra(FileSystemService.EXTRA_AUDIO_FOLDER_NAME);
                    if (fetchContentView != null) {
                        fetchContentView.setAudioFolderName(folderName);
                    }
                    break;
                case FileSystemService.ACTION_AUDIO_TRACK_NAME:
                    String trackName =
                            intent.getStringExtra(FileSystemService.EXTRA_AUDIO_TRACK_NAME);
                    if (fetchContentView != null) {
                        fetchContentView.setAudioFileName(trackName);
                    }
                    break;
                case FileSystemService.ACTION_VIDEO_FILE:
                    String videoFileName =
                            intent.getStringExtra(FileSystemService.EXTRA_VIDEO_FILE_NAME);
                    if (fetchContentView != null) {
                        fetchContentView.setVideoFileName(videoFileName);
                    }
                    break;
            }
        }
    };

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
