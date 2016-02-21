package com.fesskiev.player.utils.download;


import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import com.fesskiev.player.model.vk.VKMusicFile;

import java.util.ArrayList;
import java.util.List;

public class DownloadAudioFile implements DownloadManager.OnDownloadListener {


    private RecyclerView.Adapter adapter;
    private Activity activity;
    private VKMusicFile vkMusicFile;
    private DownloadManager downloadManager;
    private int position;

    public DownloadAudioFile(Activity activity, RecyclerView.Adapter adapter, VKMusicFile vkMusicFile) {
        this.activity = activity;
        this.adapter = adapter;
        this.vkMusicFile = vkMusicFile;
    }

    @Override
    public void onStatusChanged() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemChanged(position);

            }
        });
    }

    @Override
    public void onProgress() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemChanged(position);

            }
        });
    }

    public static List<DownloadAudioFile> getDownloadAudioFiles(Activity activity,
                                                                RecyclerView.Adapter adapter, List<VKMusicFile> vkMusicFiles) {

        List<DownloadAudioFile> downloadAudioFiles = new ArrayList<>();
        for (VKMusicFile vkMusicFile : vkMusicFiles) {
            DownloadAudioFile downloadAudioFile
                    = new DownloadAudioFile(activity, adapter, vkMusicFile);
            downloadAudioFiles.add(downloadAudioFile);
        }
        return downloadAudioFiles;
    }

    public void downloadMusicFile(int position) {
        this.position = position;
        String fileName = vkMusicFile.artist + "-" + vkMusicFile.title;
        downloadManager = new DownloadManager(vkMusicFile.url, fileName);
        downloadManager.setOnDownloadListener(this);
    }

    public DownloadManager getDownloadManager() {
        return downloadManager;
    }

    public VKMusicFile getVkMusicFile() {
        return vkMusicFile;
    }
}