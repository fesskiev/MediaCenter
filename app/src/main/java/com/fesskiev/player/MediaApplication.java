package com.fesskiev.player;

import android.app.Application;
import android.content.ComponentCallbacks2;

import com.fesskiev.player.db.MediaDataSource;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.model.VideoPlayer;
import com.fesskiev.player.utils.AppLog;
import com.flurry.android.FlurryAgent;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

public class MediaApplication extends Application {


    private static MediaApplication INSTANCE;
    private AudioPlayer audioPlayer;
    private VideoPlayer videoPlayer;
    private MediaDataSource mediaDataSource;

    private VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                AppLog.INFO("VK TOKEN INVALID");
            } else {
                AppLog.INFO("VK TOKEN VALID");
            }
        }
    };



    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

        mediaDataSource = MediaDataSource.getInstance();

        audioPlayer = new AudioPlayer();
        videoPlayer = new VideoPlayer();

        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(this);

        createFlurryAgent();
    }

    public static synchronized MediaApplication getInstance() {
        return INSTANCE;
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
                AppLog.INFO("TRIM_MEMORY_UI_HIDDEN");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
                AppLog.INFO("TRIM_MEMORY_BACKGROUND");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
                AppLog.INFO("TRIM_MEMORY_MODERATE");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
                AppLog.INFO("TRIM_MEMORY_RUNNING_MODERATE");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
                AppLog.INFO("TRIM_MEMORY_RUNNING_LOW");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                AppLog.INFO("TRIM_MEMORY_RUNNING_CRITICAL");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                AppLog.INFO("TRIM_MEMORY_COMPLETE");
                break;
        }
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public VideoPlayer getVideoPlayer() {
        return videoPlayer;
    }

    public MediaDataSource getMediaDataSource() {
        return mediaDataSource;
    }
}
