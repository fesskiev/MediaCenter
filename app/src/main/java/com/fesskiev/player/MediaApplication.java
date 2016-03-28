package com.fesskiev.player;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.model.VideoPlayer;
import com.fesskiev.player.utils.RecursiveFileObserver;
import com.vk.sdk.VKSdk;


public class MediaApplication extends Application {

    private static MediaApplication application;
    private RequestQueue requestQueue;
    private AudioPlayer audioPlayer;
    private VideoPlayer videoPlayer;

    static {
        System.loadLibrary("khronos-media");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        audioPlayer = new AudioPlayer(getApplicationContext());
        videoPlayer = new VideoPlayer();
        VKSdk.initialize(this);

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

}
