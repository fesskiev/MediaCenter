package com.fesskiev.mediacenter.utils.download;


import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import com.fesskiev.mediacenter.data.model.vk.Audio;

import java.util.ArrayList;
import java.util.List;

public class DownloadFile implements DownloadManager.OnDownloadListener {

    private RecyclerView.Adapter adapter;
    private Activity activity;
    private Audio audio;
    private DownloadManager downloadManager;
    private int position;

    public DownloadFile(Activity activity, RecyclerView.Adapter adapter, Audio audio) {
        this.activity = activity;
        this.adapter = adapter;
        this.audio = audio;
    }

    @Override
    public void onStatusChanged() {
        updateAdapter();
    }

    @Override
    public void onProgress() {
        updateAdapter();
    }

    public void updateAdapter() {
        activity.runOnUiThread(() -> adapter.notifyItemChanged(position));
    }

    public static List<DownloadFile> getDownloadFiles(Activity activity, RecyclerView.Adapter adapter,
                                                      List<Audio> audios) {

        List<DownloadFile> downloadFiles = new ArrayList<>();
        for (Audio audio : audios) {
            DownloadFile downloadFile
                    = new DownloadFile(activity, adapter, audio);
            downloadFiles.add(downloadFile);
        }
        return downloadFiles;
    }

    public void downloadMusicFile(int position) {
        this.position = position;
        String fileName = audio.getArtist() + "-" + audio.getTitle();
        downloadManager = new DownloadManager(audio.getUrl(), fileName);
        downloadManager.setOnDownloadListener(this);
    }

    public DownloadManager getDownloadManager() {
        return downloadManager;
    }

    public Audio getAudio() {
        return audio;
    }
}