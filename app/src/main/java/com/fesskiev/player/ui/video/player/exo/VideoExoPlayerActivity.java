package com.fesskiev.player.ui.video.player.exo;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.SeekBar;

import com.fesskiev.player.R;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.buttons.PlayPauseFloatingButton;
import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.util.PlayerControl;
import com.google.android.exoplayer.util.Util;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class VideoExoPlayerActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        MediaExoPlayer.Listener, MediaExoPlayer.CaptionListener, MediaExoPlayer.Id3MetadataListener {

    private static final String TAG = VideoExoPlayerActivity.class.getSimpleName();

    private MediaExoPlayer player;
    private PlayerControl control;
    private EventLogger eventLogger;
    private AspectRatioFrameLayout videoFrame;
    private SurfaceView surfaceView;
    private PlayPauseFloatingButton playPauseFloatingButton;
    private SeekBar seekVideo;
    private View shutterView;
    private Uri contentUri;
    private long playerPosition;
    private boolean isPlaying;
    private boolean playerNeedsPrepare;
    private int durationScale;

    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_exo_player);

        contentUri = getIntent().getData();

        shutterView = findViewById(R.id.shutter);
        videoFrame = (AspectRatioFrameLayout) findViewById(R.id.video_frame);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);

        playPauseFloatingButton = (PlayPauseFloatingButton) findViewById(R.id.playPauseFAB);
        playPauseFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPauseFloatingButton.setPlay(isPlaying);
                isPlaying = !isPlaying;
                if (isPlaying) {
                    control.pause();
                } else {
                    control.start();
                }

            }
        });

        seekVideo = (SeekBar) findViewById(R.id.seekVideo);
        seekVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                control.seekTo(durationScale * progress * 1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        findViewById(R.id.rootScreen).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    showControlsVisibility();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.performClick();
                }
                return true;
            }
        });
        hideControlsVisibility();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void hideControlsVisibility() {
        Log.d(TAG, "hideControlsVisibility");
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(2000);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d(TAG, "onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                playPauseFloatingButton.setVisibility(View.INVISIBLE);
                seekVideo.setVisibility(View.INVISIBLE);
                Log.d(TAG, "onAnimationEnd");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        playPauseFloatingButton.startAnimation(animation);
        seekVideo.startAnimation(animation);

    }

    private void createTick() {
        subscription = Observable
                .interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "tick onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "tick onError: " + e.getMessage());
                    }

                    @Override
                    public void onNext(Long number) {
                        Log.d(TAG, "tick onNext: " + number.toString());
                        updateProgressControls();
                    }
                });
    }

    private void updateProgressControls(){
        playerPosition = control.getCurrentPosition();
        durationScale = 100 / control.getDuration();
        Log.d(TAG, "duration scale: " + durationScale);
        if(durationScale != 0) {
            seekVideo.setProgress((int) playerPosition / durationScale);
        }
    }

    private void startTimer() {
        Observable.timer(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted");
                        hideControlsVisibility();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError");
                    }

                    @Override
                    public void onNext(Long number) {
                        Log.d(TAG, "onNext: " + number.toString());
                    }
                });
    }

    private void showControlsVisibility() {
        Log.d(TAG, "showControlsVisibility");
        AlphaAnimation animation = new AlphaAnimation(0.1f, 1.0f);
        animation.setDuration(2000);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d(TAG, "onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                playPauseFloatingButton.setVisibility(View.VISIBLE);
                seekVideo.setVisibility(View.VISIBLE);
                Log.d(TAG, "onAnimationEnd");
                startTimer();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        playPauseFloatingButton.startAnimation(animation);
        seekVideo.startAnimation(animation);

    }

    @Override
    protected void onStart() {
        super.onStart();
        preparePlayer(true);
    }

    private MediaExoPlayer.RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(this, "MediaExoPlayer");
        return new ExtractorRendererBuilder(this, userAgent, contentUri);
    }

    private void preparePlayer(boolean playWhenReady) {
        if (player == null) {
            player = new MediaExoPlayer(getRendererBuilder());
            player.addListener(this);
            player.setCaptionListener(this);
            player.setMetadataListener(this);
            player.seekTo(playerPosition);
            playerNeedsPrepare = true;
            eventLogger = new EventLogger();
            eventLogger.startSession();
            player.addListener(eventLogger);
            player.setInfoListener(eventLogger);
            player.setInternalErrorListener(eventLogger);
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
        }

        player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(playWhenReady);
    }

    private void releasePlayer() {
        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.release();
            eventLogger.endSession();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            Log.d(TAG, "unsubscribe");
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (player != null) {
            player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null) {
            player.blockingClearSurface();
        }
    }

    @Override
    public void onCues(List<Cue> cues) {

    }

    @Override
    public void onId3Metadata(List<Id3Frame> id3Frames) {

    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {

        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                Log.wtf(TAG, "buffering");
                break;
            case ExoPlayer.STATE_ENDED:
                Log.wtf(TAG, "ended");
                break;
            case ExoPlayer.STATE_IDLE:
                Log.wtf(TAG, "idle");
                break;
            case ExoPlayer.STATE_PREPARING:
                Log.wtf(TAG, "preparing");
                break;
            case ExoPlayer.STATE_READY:
                control = player.getPlayerControl();
                Log.wtf(TAG, "ready duration: " +
                        Utils.getTimeFromMillisecondsString(control.getDuration()));
                createTick();
                break;
        }
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                   float pixelWidthAspectRatio) {
        videoFrame.setAspectRatio(height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
        shutterView.setVisibility(View.GONE);
    }

}
