package com.fesskiev.player;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.model.VideoPlayer;
import com.flurry.android.FlurryAgent;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

public class MediaApplication extends Application {

    private static final String TAG = MediaApplication.class.getSimpleName();

    private static MediaApplication application;
    private RequestQueue requestQueue;
    private AudioPlayer audioPlayer;
    private VideoPlayer videoPlayer;



    private VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
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

        createFlurryAgent();

        audioPlayer = new AudioPlayer(getApplicationContext());
        videoPlayer = new VideoPlayer();

        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(this);

    }

    private void createFlurryAgent() {
        new FlurryAgent.Builder()
                .withLogEnabled(false)
                .build(this, getString(R.string.flurry_key));
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                Log.e(TAG, "TRIM_MEMORY_UI_HIDDEN");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
                Log.e(TAG, "TRIM_MEMORY_BACKGROUND");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
                Log.e(TAG, "TRIM_MEMORY_MODERATE");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
                Log.e(TAG, "TRIM_MEMORY_RUNNING_MODERATE");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
                Log.e(TAG, "TRIM_MEMORY_RUNNING_LOW");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                Log.e(TAG, "TRIM_MEMORY_RUNNING_CRITICAL");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                Log.e(TAG, "TRIM_MEMORY_COMPLETE");
                break;
        }
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
