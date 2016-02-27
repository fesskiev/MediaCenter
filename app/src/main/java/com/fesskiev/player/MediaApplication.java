package com.fesskiev.player;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.model.VideoPlayer;
import com.fesskiev.player.utils.RecursiveFileObserver;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.vk.sdk.VKSdk;


public class MediaApplication extends Application {

    private static MediaApplication application;
    private RequestQueue requestQueue;
    private AudioPlayer audioPlayer;
    private VideoPlayer videoPlayer;

    private RefWatcher refWatcher;

    static {
        System.loadLibrary("khronos-media");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        audioPlayer = new AudioPlayer();
        videoPlayer = new VideoPlayer();
        VKSdk.initialize(this);
        refWatcher = LeakCanary.install(this);

//        createFileObserver();
    }

    private void createFileObserver() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                RecursiveFileObserver fileObserver =
                        new RecursiveFileObserver(Environment.getExternalStorageDirectory().toString());
                fileObserver.startWatching();
            }
        }).start();
    }

    public static synchronized MediaApplication getInstance() {
        return application;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return requestQueue;
    }


    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(MediaApplication.class.getSimpleName());
        getRequestQueue().add(req);
    }


    public void cancelPendingRequests() {
        if (requestQueue != null) {
            requestQueue.cancelAll(MediaApplication.class.getSimpleName());
        }
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public VideoPlayer getVideoPlayer() {
        return videoPlayer;
    }

    public RefWatcher getRefWatcher() {
        return refWatcher;
    }
}
