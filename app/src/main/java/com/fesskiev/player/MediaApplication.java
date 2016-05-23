package com.fesskiev.player;

import android.app.Application;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.model.VideoPlayer;
import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

import java.util.List;


public class MediaApplication extends Application {

    private static MediaApplication application;
    private RequestQueue requestQueue;
    private AudioPlayer audioPlayer;
    private VideoPlayer videoPlayer;

    private Tracker tracker;

    VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                Log.e(MediaApplication.class.getName(), "VK TOKEN INVALID");
            } else {
                Log.e(MediaApplication.class.getName(), "VK TOKEN VALID");
            }
        }
    };

    static {
        System.loadLibrary("khronos-media");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        audioPlayer = new AudioPlayer(getApplicationContext());
        videoPlayer = new VideoPlayer();

        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(this);

        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new ExceptionReporter(
                getDefaultTracker(),
                Thread.getDefaultUncaughtExceptionHandler(),
                getApplicationContext());
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
    }


    public static synchronized MediaApplication getInstance() {
        return application;
    }

    public synchronized Tracker getDefaultTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(R.xml.global_tracker);
            tracker.enableAutoActivityTracking(true);
            tracker.enableExceptionReporting(true);
        }
        return tracker;
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
