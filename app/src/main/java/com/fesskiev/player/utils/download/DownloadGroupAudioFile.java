package com.fesskiev.player.utils.download;


import android.app.Activity;

import com.fesskiev.player.model.vk.VKMusicFile;

import java.util.ArrayList;
import java.util.List;

public class DownloadGroupAudioFile implements DownloadManager.OnDownloadListener {

    private Activity activity;
    private VKMusicFile vkMusicFile;
    private DownloadManager downloadManager;

    public interface OnDownloadAudioListener {
        void onProgress(DownloadManager downloadManager);
    }

    private OnDownloadAudioListener listener;

    public DownloadGroupAudioFile(Activity activity, VKMusicFile vkMusicFile) {
        this.activity = activity;
        this.vkMusicFile = vkMusicFile;
    }

    public void setOnDownloadAudioListener(OnDownloadAudioListener listener) {
        this.listener = listener;
    }

    public void startDownload() {
        String fileName = vkMusicFile.artist + "-" + vkMusicFile.title;
        downloadManager = new DownloadManager(vkMusicFile.url, fileName);
        downloadManager.setOnDownloadListener(this);
    }

    public static List<DownloadGroupAudioFile> getDownloadGroupAudioFiles(Activity activity, List<VKMusicFile> vkMusicFiles) {
        List<DownloadGroupAudioFile> downloadGroupAudioFiles = new ArrayList<>();
        for (VKMusicFile vkMusicFile : vkMusicFiles) {
            DownloadGroupAudioFile downloadGroupAudioFile = new DownloadGroupAudioFile(activity, vkMusicFile);
            downloadGroupAudioFiles.add(downloadGroupAudioFile);
        }
        return downloadGroupAudioFiles;
    }

    @Override
    public void onStatusChanged() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress();
            }
        });

    }

    @Override
    public void onProgress() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress();
            }
        });
    }

    private void progress() {
        if (listener != null) {
            listener.onProgress(downloadManager);
        }
    }

    public VKMusicFile getVkMusicFile() {
        return vkMusicFile;
    }

    public DownloadManager getDownloadManager() {
        return downloadManager;
    }
}
