package com.fesskiev.player.ui.video.player;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.VideoPlayer;
import com.fesskiev.player.ui.playback.Playable;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.controls.VideoControlView;
import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.PlayerControl;
import com.google.android.exoplayer.util.Util;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class VideoExoPlayerActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        MediaExoPlayer.Listener,
        MediaExoPlayer.CaptionListener,
        MediaExoPlayer.Id3MetadataListener,
        AudioCapabilitiesReceiver.Listener, Playable {

    private static final String TAG = VideoExoPlayerActivity.class.getSimpleName();

    private static final int INTERVAL_SECONDS = 2;

    private MediaExoPlayer player;
    private PlayerControl control;
    private EventLogger eventLogger;
    private VideoControlView videoControlView;
    private AspectRatioFrameLayout videoFrame;
    private SurfaceView surfaceView;
    private View shutterView;
    private VideoPlayer videoPlayer;
    private Uri contentUri;
    private long playerPosition;
    private int durationScale;
    private int interval;
    private boolean playerNeedsPrepare;

    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_exo_player);

        videoPlayer = MediaApplication.getInstance().getVideoPlayer();

        contentUri = getIntent().getData();

        shutterView = findViewById(R.id.shutter);
        videoFrame = (AspectRatioFrameLayout) findViewById(R.id.video_frame);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);

        videoControlView = (VideoControlView) findViewById(R.id.videoPlayerControl);
        videoControlView.setOnVideoPlayerControlListener(new VideoControlView.OnVideoPlayerControlListener() {
            @Override
            public void playPauseButtonClick(boolean isPlaying) {
                if (isPlaying) {
                    pause();
                } else {
                    play();
                }
            }

            @Override
            public void seekVideo(int progress) {
                control.seekTo(progress * durationScale);
            }

            @Override
            public void nextVideo() {
                next();
            }

            @Override
            public void previousVideo() {
                previous();
            }
        });

        findViewById(R.id.rootScreen).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    videoControlView.showControlsVisibility();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.performClick();
                }
                return true;
            }
        });

        audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(this, this);
        audioCapabilitiesReceiver.register();

        videoControlView.resetIndicators();
        videoControlView.hideControlsVisibility();
    }


    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            onShown();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || player == null) {
            onShown();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            onHidden();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            onHidden();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopUpdateTimer();
        audioCapabilitiesReceiver.unregister();
        releasePlayer();
    }


    @Override
    public void createPlayer() {
        preparePlayer(true);
    }

    @Override
    public void play() {
        control.start();
    }

    @Override
    public void pause() {
        control.pause();
    }

    @Override
    public void next() {
        videoPlayer.next();

    }

    @Override
    public void previous() {
        videoPlayer.previous();
    }

    private void onShown() {
        if (player == null) {
            createPlayer();
        } else {
            player.setBackgrounded(false);
        }
    }

    private void onHidden() {
        player.setBackgrounded(true);
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

    private void startUpdateTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.obtainMessage(1).sendToTarget();
            }
        }, 0, 1000);
    }

    private void stopUpdateTimer() {
        timer.cancel();
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            updateProgressControls();
            if (videoControlView.isShowControls()) {
                interval += 1;
                if (interval == INTERVAL_SECONDS) {
                    interval = 0;
                    videoControlView.setShowControls(false);
                    videoControlView.hideControlsVisibility();
                }
            }
        }
    };

    private void updateProgressControls() {
        videoControlView.
                setVideoTimeCount(Utils.getTimeFromMillisecondsString(control.getCurrentPosition()));
        playerPosition = control.getCurrentPosition();
        durationScale = control.getDuration() / 100;
        if (durationScale != 0) {
            videoControlView.setProgress((int) playerPosition / durationScale);
        }
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

    private void configureTracks(int trackType) {
        if (player == null) {
            Log.e(TAG, "player null");
            return;
        }
        int trackCount = player.getTrackCount(trackType);
        if (trackCount == 0) {
            Log.e(TAG, "track count 0");
            return;
        }

        for (int i = 0; i < trackCount; i++) {
            Log.e(TAG, "track: " + buildTrackName(player.getTrackFormat(trackType, i)));
        }
    }

    private static String buildTrackName(MediaFormat format) {
        if (format.adaptive) {
            return "auto";
        }
        String trackName;
        if (MimeTypes.isVideo(format.mimeType)) {
            trackName = joinWithSeparator(joinWithSeparator(buildResolutionString(format),
                    buildBitrateString(format)), buildTrackIdString(format));
        } else if (MimeTypes.isAudio(format.mimeType)) {
            trackName = joinWithSeparator(joinWithSeparator(joinWithSeparator(buildLanguageString(format),
                    buildAudioPropertyString(format)), buildBitrateString(format)),
                    buildTrackIdString(format));
        } else {
            trackName = joinWithSeparator(joinWithSeparator(buildLanguageString(format),
                    buildBitrateString(format)), buildTrackIdString(format));
        }
        return trackName.length() == 0 ? "unknown" : trackName;
    }

    private static String buildResolutionString(MediaFormat format) {
        return format.width == MediaFormat.NO_VALUE || format.height == MediaFormat.NO_VALUE
                ? "" : format.width + "x" + format.height;
    }

    private static String buildAudioPropertyString(MediaFormat format) {
        return format.channelCount == MediaFormat.NO_VALUE || format.sampleRate == MediaFormat.NO_VALUE
                ? "" : format.channelCount + "ch, " + format.sampleRate + "Hz";
    }

    private static String buildLanguageString(MediaFormat format) {
        return TextUtils.isEmpty(format.language) || "und".equals(format.language) ? ""
                : format.language;
    }

    private static String buildBitrateString(MediaFormat format) {
        return format.bitrate == MediaFormat.NO_VALUE ? ""
                : String.format(Locale.US, "%.2fMbit", format.bitrate / 1000000f);
    }

    private static String joinWithSeparator(String first, String second) {
        return first.length() == 0 ? second : (second.length() == 0 ? first : first + ", " + second);
    }

    private static String buildTrackIdString(MediaFormat format) {
        return format.trackId == null ? "" : " (" + format.trackId + ")";
    }

    private void releasePlayer() {
        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.release();
            eventLogger.endSession();
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
                Log.wtf(TAG, "ready");
                videoControlView.
                        setVideoTimeTotal(Utils.getTimeFromMillisecondsString(control.getDuration()));
                startUpdateTimer();
                configureTracks(MediaExoPlayer.TYPE_AUDIO);
                configureTracks(MediaExoPlayer.TYPE_VIDEO);
                configureTracks(MediaExoPlayer.TYPE_TEXT);
                break;
        }
    }

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        if (player == null) {
            return;
        }
        boolean backgrounded = player.getBackgrounded();
        boolean playWhenReady = player.getPlayWhenReady();
        releasePlayer();
        preparePlayer(playWhenReady);
        player.setBackgrounded(backgrounded);
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
