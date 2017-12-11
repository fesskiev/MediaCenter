package com.fesskiev.mediacenter.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.ui.video.player.ExoPlayerWrapper;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.RxBus;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class VideoPlaybackService extends Service {

    public enum VIDEO_PLAYBACK {
        ERROR, IDLE, BUFFERING, INIT, READY, ENDED
    }

    private IBinder binder = new VideoPlaybackServiceBinder();

    @Inject
    AppSettingsManager settingsManager;
    @Inject
    RxBus rxBus;

    private ExoPlayerWrapper player;

    private Disposable disposable;

    private VIDEO_PLAYBACK videoPlaybackState;

    private long durationScale;

    private boolean playing;
    private long position;
    private long progress;

    private String errorMessage;

    @Override
    public void onCreate() {
        super.onCreate();
        clearErrorMessage();
        MediaApplication.getInstance().getAppComponent().inject(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    public SimpleExoPlayer initPlayer(Uri uri, String subtitlePath) {
        player = new ExoPlayerWrapper(getApplicationContext(), true);
        player.setErrorListener(this::sendErrorMessage);
        player.setPlaybackStateChangedListener(playbackState -> {
            switch (playbackState) {
                case Player.STATE_BUFFERING:
                    videoPlaybackState = VIDEO_PLAYBACK.BUFFERING;
                    AppLog.WTF("STATE_BUFFERING");
                    break;
                case Player.STATE_ENDED:
                    videoPlaybackState = VIDEO_PLAYBACK.ENDED;
                    AppLog.WTF("STATE_ENDED");
                    break;
                case Player.STATE_IDLE:
                    videoPlaybackState = VIDEO_PLAYBACK.IDLE;
                    AppLog.WTF("STATE_IDLE");
                    break;
                case Player.STATE_READY:
                    videoPlaybackState = VIDEO_PLAYBACK.INIT;
                    updateProgressControls();
                    videoPlaybackState = VIDEO_PLAYBACK.READY;
                    AppLog.WTF("STATE_READY");
                    updateProgressControls();
                    addTimer();
                    break;
            }
            clearErrorMessage();
            sendPlaybackEvent();
        });
        return player.initializePlayer(uri, subtitlePath);
    }

    private void sendErrorMessage(String message) {
        setErrorMessage(message);
        videoPlaybackState = VIDEO_PLAYBACK.ERROR;
        sendPlaybackEvent();
    }

    private void updateProgressControls() {
        if (player != null) {
            position = player.getCurrentPosition();
            durationScale = player.getDuration() / 100;
            if (durationScale != 0) {
                progress = (position / durationScale);
            }
            playing = player.getPlayWhenReady();

            sendPlaybackEvent();
//            AppLog.ERROR("video playback: " + VideoPlaybackService.this.toString());
        }
    }

    public void releasePlayer() {
        player.releasePlayer();
        player = null;
        stopTimer();
    }

    public void seekTo(int progress) {
        player.seekTo(progress * durationScale);
    }

    public void setPlaying(boolean playing) {
        player.setPlayWhenReady(playing);
    }

    private void addTimer() {
        disposable = Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                .subscribe(interval -> updateProgressControls(), Throwable::printStackTrace);
    }

    private void stopTimer() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    private void sendPlaybackEvent() {
        rxBus.sendVideoPlaybackEvent(VideoPlaybackService.this);
    }

    public void stopPlayback() {
        if (player.getPlayWhenReady()) {
            player.setPlayWhenReady(false);
        }
    }

    public void setPlayback(boolean playing) {
        player.setPlayWhenReady(playing);
    }

    public boolean getPlayback() {
        return player.getPlayWhenReady();
    }

    private void clearErrorMessage() {
        errorMessage = "";
    }

    private void setErrorMessage(String message) {
        errorMessage = message;
    }

    public VIDEO_PLAYBACK getVideoPlaybackState() {
        return videoPlaybackState;
    }

    public ExoPlayerWrapper getExoPlayerWrapper() {
        return player;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isPlaying() {
        return playing;
    }

    public long getPosition() {
        return position;
    }

    public long getProgress() {
        return progress;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class VideoPlaybackServiceBinder extends Binder {
        public VideoPlaybackService getService() {
            return VideoPlaybackService.this;
        }
    }

    @Override
    public String toString() {
        return "VideoPlaybackService{" +
                ", playing=" + playing +
                ", position=" + position +
                ", durationScale=" + durationScale +
                ", progress=" + progress +
                '}';
    }
}
