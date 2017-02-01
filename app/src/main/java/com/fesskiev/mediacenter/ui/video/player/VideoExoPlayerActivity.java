package com.fesskiev.mediacenter.ui.video.player;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.fesskiev.extensions.player.SimpleMediaCenterPlayer;
import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.players.VideoPlayer;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.controls.VideoControlView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.StreamingDrmSessionManager;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class VideoExoPlayerActivity extends AppCompatActivity implements ExoPlayer.EventListener {

    private static final String TAG = VideoExoPlayerActivity.class.getSimpleName();

    private static final int REQUEST_CODE_SUBTITLE = 1337;

    public static final String BUNDLE_PLAYER_POSITION = "com.fesskiev.player.BUNDLE_PLAYER_POSITION";

    public static final String ACTION_VIEW_URI = "com.fesskiev.player.action.VIEW_LIST";
    public static final String URI_EXTRA = "com.fesskiev.player.URI_EXTRA";
    public static final String VIDEO_NAME_EXTRA = "com.fesskiev.player.VIDEO_NAME_EXTRA";
    public static final String SUB_EXTRA = "com.fesskiev.player.SUB_EXTRA";

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private GestureDetector gestureDetector;
    private VideoPlayer videoPlayer;
    private VideoControlView videoControlView;
    private DataSource.Factory mediaDataSourceFactory;
    private TrackSelection.Factory videoTrackSelectionFactory;
    private EventLogger eventLogger;
    private Timeline.Window window;
    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleMediaCenterPlayer player;
    private DefaultTrackSelector trackSelector;
    private Timer timer;

    private boolean isTimelineStatic;
    private boolean playerNeedsSource;
    private boolean shouldAutoPlay;
    private int playerWindow;
    private long playerPosition;
    private long durationScale;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            updateProgressControls();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_exo_player);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        videoPlayer = MediaApplication.getInstance().getVideoPlayer();

        if (savedInstanceState != null) {
            playerPosition = savedInstanceState.getLong(BUNDLE_PLAYER_POSITION);
        }

        gestureDetector = new GestureDetector(getApplicationContext(), new GestureListener());

        shouldAutoPlay = true;

        window = new Timeline.Window();
        mediaDataSourceFactory = buildDataSourceFactory(true);

        videoControlView = (VideoControlView) findViewById(R.id.videoPlayerControl);
        videoControlView.setOnVideoPlayerControlListener(new VideoControlView.OnVideoPlayerControlListener() {
            @Override
            public void playPauseButtonClick(boolean isPlaying) {
                player.setPlayWhenReady(isPlaying);
            }

            @Override
            public void addSubButtonClick() {
                performSubSearch();
            }

            @Override
            public void seekVideo(int progress) {
                player.seekTo(progress * durationScale);
            }

            @Override
            public void nextVideo() {
                next();
            }

            @Override
            public void previousVideo() {
                previous();
            }

            @Override
            public void resizeModeChanged(int mode) {
                simpleExoPlayerView.setResizeMode(mode);
            }
        });

        videoControlView.setPlay(shouldAutoPlay);

        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.videoView);
        simpleExoPlayerView.setUseController(false);
        simpleExoPlayerView.requestFocus();

        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
        videoControlView.setResizeModeState(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);

        EventBus.getDefault().register(this);
    }

    private void setFullScreen() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void performSubSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers, it would be
        // "*/*".
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_SUBTITLE);
    }


    private void previous() {
        if (videoPlayer.first()) {
            Utils.showCustomSnackbar(findViewById(R.id.videoPlayerRoot),
                    getApplicationContext(),
                    getString(R.string.snackbar_first_video),
                    Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }
        videoPlayer.previous();
    }

    private void next() {
        if (videoPlayer.last()) {
            Utils.showCustomSnackbar(findViewById(R.id.videoPlayerRoot),
                    getApplicationContext(),
                    getString(R.string.snackbar_last_video),
                    Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }
        videoPlayer.next();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_CODE_SUBTITLE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    Intent intent = new Intent();
                    intent.putExtra(VideoExoPlayerActivity.URI_EXTRA, currentVideoPath);
                    intent.putExtra(VideoExoPlayerActivity.VIDEO_NAME_EXTRA, currentVideoName);
                    intent.putExtra(VideoExoPlayerActivity.SUB_EXTRA, Environment.getExternalStorageDirectory() + "/" + split[1]);
                    intent.setAction(VideoExoPlayerActivity.ACTION_VIEW_URI);
                    setIntent(intent);
                }

            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            videoControlView.setResizeModeState(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
            videoControlView.setResizeModeState(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        out.putLong(BUNDLE_PLAYER_POSITION, playerPosition);
        super.onSaveInstanceState(out);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setFullScreen();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
        stopUpdateTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCurrentVideoFileEvent(VideoFile videoFile) {
        Log.e("video", "PLAYER onCurrentVideoFileEvent: " + videoFile.toString());
        videoControlView.resetIndicators();
        isTimelineStatic = false;

        releasePlayer();

        Intent intent = new Intent();
        intent.putExtra(VideoExoPlayerActivity.URI_EXTRA, videoFile.getFilePath());
        intent.putExtra(VideoExoPlayerActivity.VIDEO_NAME_EXTRA, videoFile.getFileName());
        intent.setAction(VideoExoPlayerActivity.ACTION_VIEW_URI);
        setIntent(intent);

        initializePlayer();

    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        isTimelineStatic = !timeline.isEmpty()
                && !timeline.getWindow(timeline.getWindowCount() - 1, window).isDynamic;
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
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
            case ExoPlayer.STATE_READY:
                Log.wtf(TAG, "ready");
                videoControlView.setVideoTimeTotal(Utils.getTimeFromMillisecondsString(player.getDuration()));
                updateProgressControls();
                startUpdateTimer();
                videoControlView.setVideoTrackInfo(player, trackSelector, videoTrackSelectionFactory);
                break;
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        String errorString = null;
        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
            Exception cause = e.getRendererException();
            if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                        (MediaCodecRenderer.DecoderInitializationException) cause;
                if (decoderInitializationException.decoderName == null) {
                    if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                        errorString = getString(R.string.error_querying_decoders);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = getString(R.string.error_no_secure_decoder,
                                decoderInitializationException.mimeType);
                    } else {
                        errorString = getString(R.string.error_no_decoder,
                                decoderInitializationException.mimeType);
                    }
                } else {
                    errorString = getString(R.string.error_instantiating_decoder,
                            decoderInitializationException.decoderName);
                }
            }
        }
        if (errorString != null) {
            showErrorShackBar(errorString);
        }
        playerNeedsSource = true;
    }

    private void showErrorShackBar(String errorString) {
        Utils.showCustomSnackbar(findViewById(R.id.videoPlayerRoot), getApplicationContext(),
                errorString, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    String currentVideoPath;
    String currentVideoName;

    private void initializePlayer() {
        Intent intent = getIntent();
        if (player == null) {

            videoTrackSelectionFactory = new AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

            AudioCapabilities audioCapabilities = AudioCapabilities.getCapabilities(this);
            boolean passthrough = audioCapabilities.supportsEncoding(AudioFormat.ENCODING_AC3);

            Log.i(TAG, "audio passthrough: " + (passthrough ? "enabled" : "disabled"));

            player = new SimpleMediaCenterPlayer(this, trackSelector, true);


            player.addListener(this);
            eventLogger = new EventLogger(trackSelector);
            player.addListener(eventLogger);
            player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);
            player.setId3Output(eventLogger);


//            VideoTextureView videoTextureView = new VideoTextureView(getApplicationContext());
//            videoTextureView.setOnVideoTextureListener(new VideoTextureView.OnVideoTextureListener() {
//                @Override
//                public void onZoom() {
//                    Log.d("test", "onZoom()");
//                }
//
//                @Override
//                public void onDrag() {
//                    Log.d("test", "onDrag()");
//                }
//
//                @Override
//                public void onTouch() {
//                    Log.d("test", "onTouch()");
//                }
//            });
//            player.setVideoTextureView(videoTextureView);

            simpleExoPlayerView.setPlayer(player);
            if (isTimelineStatic) {
                if (playerPosition == C.TIME_UNSET) {
                    player.seekToDefaultPosition(playerWindow);
                } else {
                    player.seekTo(playerWindow, playerPosition);
                }
            }
            player.setPlayWhenReady(shouldAutoPlay);
            playerNeedsSource = true;
        }
        if (playerNeedsSource) {
            String action = intent.getAction();
            Uri uri;
            if (ACTION_VIEW_URI.equals(action)) {
                currentVideoPath = intent.getStringExtra(URI_EXTRA);
                uri = Uri.parse(currentVideoPath);

                currentVideoName = intent.getStringExtra(VIDEO_NAME_EXTRA);
                videoControlView.setVideoName(currentVideoName);
            } else {
                return;
            }
            MediaSource videoSource = buildMediaSource(uri);

            if (intent.hasExtra(SUB_EXTRA)) {
                String subPath = intent.getStringExtra(SUB_EXTRA);

                Format textFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, null, Format.NO_VALUE,
                        Format.NO_VALUE, "external", null, 0);
                MediaSource textMediaSource = new SingleSampleMediaSource(Uri.fromFile(new File(subPath)),
                        mediaDataSourceFactory, textFormat, C.TIME_UNSET);

                MediaSource mergedSource = new MergingMediaSource(videoSource, textMediaSource);

                player.prepare(mergedSource, !isTimelineStatic, !isTimelineStatic);
            } else {
                player.prepare(videoSource, !isTimelineStatic, !isTimelineStatic);
            }

            playerNeedsSource = false;


        }
    }

    private void releasePlayer() {
        if (player != null) {
            shouldAutoPlay = player.getPlayWhenReady();
            playerWindow = player.getCurrentWindowIndex();
            playerPosition = C.TIME_UNSET;
            Timeline timeline = player.getCurrentTimeline();
            if (!timeline.isEmpty() && timeline.getWindow(playerWindow, window).isSeekable) {
                playerPosition = player.getCurrentPosition();
            }
            player.release();
            player = null;
            trackSelector = null;
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        int type = Util.inferContentType(uri.getLastPathSegment());
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), handler, eventLogger);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), handler, eventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, handler, eventLogger);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        handler, eventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return MediaApplication.getInstance().buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        return MediaApplication.getInstance().buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }


    private DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(UUID uuid,
                                                                           String licenseUrl, Map<String, String> keyRequestProperties) throws UnsupportedDrmException {
        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl,
                buildHttpDataSourceFactory(false), keyRequestProperties);
        return new StreamingDrmSessionManager<>(uuid,
                FrameworkMediaDrm.newInstance(uuid), drmCallback, null, handler, eventLogger);
    }

    private void updateProgressControls() {
        if (player != null) {
            videoControlView.setVideoTimeCount(Utils.getTimeFromMillisecondsString(player.getCurrentPosition()));
            playerPosition = player.getCurrentPosition();
            durationScale = player.getDuration() / 100;
            if (durationScale != 0) {
                videoControlView.setProgress((int) (playerPosition / durationScale));
            }
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
        if (timer != null) {
            timer.cancel();
        }
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            videoControlView.toggleControl();
            return true;
        }
    }
}
