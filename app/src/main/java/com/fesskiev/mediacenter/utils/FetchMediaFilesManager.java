package com.fesskiev.mediacenter.utils;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.fesskiev.mediacenter.services.FileSystemIntentService;
import com.fesskiev.mediacenter.widgets.dialogs.FetchMediaContentDialog;


public class FetchMediaFilesManager {

    private FetchMediaContentDialog mediaContentDialog;

    public interface OnFetchMediaFilesListener {

        void onFetchContentStart();

        void onFetchContentFinish();
    }

    private Activity activity;
    private OnFetchMediaFilesListener listener;

    public FetchMediaFilesManager(Activity activity) {
        this.activity = activity;
        registerBroadcastReceiver();
    }

    public void setOnFetchMediaFilesListener(OnFetchMediaFilesListener l) {
        this.listener = l;
    }

    public void unregister() {
        unregisterBroadcastReceiver();
        hideDialog();
    }


    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(FileSystemIntentService.ACTION_START_FETCH_MEDIA_CONTENT);
        filter.addAction(FileSystemIntentService.ACTION_END_FETCH_MEDIA_CONTENT);
        filter.addAction(FileSystemIntentService.ACTION_AUDIO_FOLDER_NAME);
        filter.addAction(FileSystemIntentService.ACTION_AUDIO_TRACK_NAME);
        filter.addAction(FileSystemIntentService.ACTION_VIDEO_FILE);
        LocalBroadcastManager.getInstance(activity).registerReceiver(broadcastReceiver,
                filter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(broadcastReceiver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {
            switch (intent.getAction()) {
                case FileSystemIntentService.ACTION_START_FETCH_MEDIA_CONTENT:
                    mediaContentDialog = FetchMediaContentDialog.newInstance(activity);
                    mediaContentDialog.show();
                    if (listener != null) {
                        listener.onFetchContentStart();
                    }
                    break;
                case FileSystemIntentService.ACTION_END_FETCH_MEDIA_CONTENT:
                    if (mediaContentDialog != null) {
                        mediaContentDialog.hide();
                        mediaContentDialog.dismiss();
                    }
                    if (listener != null) {
                        listener.onFetchContentFinish();
                    }
                    break;
                case FileSystemIntentService.ACTION_AUDIO_FOLDER_NAME:
                    String folderName =
                            intent.getStringExtra(FileSystemIntentService.EXTRA_AUDIO_FOLDER_NAME);
                    if (mediaContentDialog != null) {
                        mediaContentDialog.setFolderName(folderName);
                    }
                    break;
                case FileSystemIntentService.ACTION_AUDIO_TRACK_NAME:
                    String trackName =
                            intent.getStringExtra(FileSystemIntentService.EXTRA_AUDIO_TRACK_NAME);
                    if (mediaContentDialog != null) {
                        mediaContentDialog.setFileName(trackName);
                    }
                    break;
                case FileSystemIntentService.ACTION_VIDEO_FILE:
                    String videoFileName =
                            intent.getStringExtra(FileSystemIntentService.EXTRA_VIDEO_FILE_NAME);
                    if (mediaContentDialog != null) {
                        mediaContentDialog.setFileName(videoFileName);
                    }
                    break;
            }
        }
    };

    private void hideDialog() {
        if (mediaContentDialog != null && mediaContentDialog.isShowing()) {
            mediaContentDialog.hide();
            mediaContentDialog.dismiss();
        }
    }

}
