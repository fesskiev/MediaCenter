package com.fesskiev.mediacenter.utils.download;


import android.app.Activity;

import com.fesskiev.mediacenter.vk.data.model.Audio;

import java.util.ArrayList;
import java.util.List;

public class DownloadGroupAudioFile implements DownloadManager.OnDownloadListener {

    private Activity activity;
    private Audio audio;
    private DownloadManager downloadManager;

    public interface OnDownloadAudioListener {
        void onProgress(DownloadManager downloadManager);
    }

    private OnDownloadAudioListener listener;

    public DownloadGroupAudioFile(Activity activity, Audio audio) {
        this.activity = activity;
        this.audio = audio;
    }

    public void setOnDownloadAudioListener(OnDownloadAudioListener listener) {
        this.listener = listener;
    }

    public void startDownload() {
        String fileName = audio.getArtist() + "-" + audio.getTitle();
        downloadManager = new DownloadManager(audio.getUrl(), fileName);
        downloadManager.setOnDownloadListener(this);
        progressOnUiThread();
    }

    public static List<DownloadGroupAudioFile> getDownloadGroupAudioFiles(Activity activity, List<Audio> audios) {
        List<DownloadGroupAudioFile> downloadGroupAudioFiles = new ArrayList<>();
        for (Audio audio : audios) {
            downloadGroupAudioFiles.add(new DownloadGroupAudioFile(activity, audio));
        }
        return downloadGroupAudioFiles;
    }

    @Override
    public void onStatusChanged() {
        progressOnUiThread();

    }

    @Override
    public void onProgress() {
        progressOnUiThread();
    }

    private void progressOnUiThread() {
        activity.runOnUiThread(this::progress);
    }

    private void progress() {
        if (listener != null) {
            listener.onProgress(downloadManager);
        }
    }

    public Audio getAudio() {
        return audio;
    }

    public DownloadManager getDownloadManager() {
        return downloadManager;
    }
}
