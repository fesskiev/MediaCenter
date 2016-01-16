package com.fesskiev.player;

import android.app.Application;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.fesskiev.player.model.MusicFile;
import com.fesskiev.player.model.MusicFolder;
import com.vk.sdk.VKSdk;

import java.util.ArrayList;
import java.util.List;


public class MusicApplication extends Application {

    private static MusicApplication application;
    private RequestQueue requestQueue;
    private List<MusicFolder> musicFolders;
    private MusicFile currentMusicFile;


    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        VKSdk.initialize(this);

        musicFolders = new ArrayList<>();

    }

    public static synchronized MusicApplication getInstance() {
        return application;
    }


    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return requestQueue;
    }


    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(MusicApplication.class.getSimpleName());
        getRequestQueue().add(req);
    }


    public void cancelPendingRequests() {
        if (requestQueue != null) {
            requestQueue.cancelAll(MusicApplication.class.getSimpleName());
        }
    }

    public List<MusicFolder> getMusicFolders() {
        return musicFolders;
    }

    public void setMusicFolders(List<MusicFolder> musicFolders) {
        this.musicFolders = musicFolders;
    }

    public MusicFile getCurrentMusicFile() {
        return currentMusicFile;
    }

    public void setCurrentMusicFile(MusicFile currentMusicFile) {
        this.currentMusicFile = currentMusicFile;
    }
}
