package com.fesskiev.player;

import android.app.Application;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.fesskiev.player.model.MusicPlayer;
import com.vk.sdk.VKSdk;


public class MusicApplication extends Application {

    private static MusicApplication application;
    private RequestQueue requestQueue;
    private MusicPlayer musicPlayer;


    @Override

    public void onCreate() {
        super.onCreate();
        application = this;
        musicPlayer = new MusicPlayer();

        VKSdk.initialize(this);

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

    public MusicPlayer getMusicPlayer() {
        return musicPlayer;
    }
}
