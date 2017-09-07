package com.fesskiev.mediacenter.ui.video.player;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Rational;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.players.VideoPlayer;
import com.fesskiev.mediacenter.utils.AppGuide;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.controls.VideoControlView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
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
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

@TargetApi(Build.VERSION_CODES.O)
public class VideoExoPlayerActivity extends AppCompatActivity implements Player.EventListener {

    private static final String TAG = VideoExoPlayerActivity.class.getSimpleName();

    private static final int DEFAULT_MIN_BUFFER_MS = 3000;
    private static final int DEFAULT_MAX_BUFFER_MS = 5000;
    private static final int DEFAULT_BUFFER_FOR_PLAYBACK_MS = 1000;
    private static final int DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 2000;

    private static final int CONTROL_TYPE_PLAY = 1;
    private static final int CONTROL_TYPE_PAUSE = 2;
    private static final int REQUEST_PLAY = 1;
    private static final int REQUEST_PAUSE = 2;
    private static final int REQUEST_CODE_SUBTITLE = 1337;

    private static final String ACTION_MEDIA_CONTROL = "com.fesskiev.player.ACTION_MEDIA_CONTROL";
    private static final String EXTRA_CONTROL_TYPE = "com.fesskiev.player.EXTRA_CONTROL_TYPE";

    public static final String BUNDLE_PLAYER_POSITION = "com.fesskiev.player.BUNDLE_PLAYER_POSITION";
    public static final String BUNDLE_AUTO_PLAY = "com.fesskiev.player.BUNDLE_AUTO_PLAY";

    public static final String ACTION_VIEW_URI = "com.fesskiev.player.action.VIEW_LIST";
    public static final String URI_EXTRA = "com.fesskiev.player.URI_EXTRA";
    public static final String VIDEO_NAME_EXTRA = "com.fesskiev.player.VIDEO_NAME_EXTRA";
    public static final String SUB_EXTRA = "com.fesskiev.player.SUB_EXTRA";


    private AppSettingsManager settingsManager;
    private VideoPlayer videoPlayer;

    private AppGuide appGuide;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private GestureDetector gestureDetector;
    private VideoControlView videoControlView;
    private DataSource.Factory mediaDataSourceFactory;
    private TrackSelection.Factory trackSelectionFactory;
    private EventLogger eventLogger;
    private Timeline.Window window;
    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private Timer timer;

    private String currentVideoPath;
    private String currentVideoName;
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

    private BroadcastReceiver pictureInPictureReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_exo_player);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        settingsManager = AppSettingsManager.getInstance();
        videoPlayer = MediaApplication.getInstance().getVideoPlayer();

        if (savedInstanceState != null) {
            playerPosition = savedInstanceState.getLong(BUNDLE_PLAYER_POSITION);
            shouldAutoPlay = savedInstanceState.getBoolean(BUNDLE_AUTO_PLAY);
            isTimelineStatic = true;
        } else {
            shouldAutoPlay = true;
        }

        gestureDetector = new GestureDetector(getApplicationContext(), new GestureListener());


        window = new Timeline.Window();
        mediaDataSourceFactory = buildDataSourceFactory(true);

        videoControlView = findViewById(R.id.videoPlayerControl);
        videoControlView.setOnVideoPlayerControlListener(new VideoControlView.OnVideoPlayerControlListener() {
            @Override
            public void playPauseButtonClick(boolean isPlaying) {
                player.setPlayWhenReady(isPlaying);
                updatePictureInPictureState(isPlaying);
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

            @Override
            public void pictureInPictureModeChanged(boolean enable) {
                if (enable) {
                    minimize();
                }
            }
        });

        videoControlView.setPlay(shouldAutoPlay);

        simpleExoPlayerView = findViewById(R.id.videoView);
        simpleExoPlayerView.setUseController(false);
        simpleExoPlayerView.requestFocus();

        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
        videoControlView.setResizeModeState(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);

        EventBus.getDefault().register(this);

        checkFirstOrLastVideoFile();
    }

    private void updatePictureInPictureState(boolean isPlaying) {
        if (Utils.isOreo()) {
            if (isPlaying) {
                updatePictureInPictureActions(R.drawable.ic_pause_24dp,
                        "Pause", CONTROL_TYPE_PAUSE, REQUEST_PAUSE);
            } else {
                updatePictureInPictureActions(R.drawable.ic_play_24dp,
                        "Play", CONTROL_TYPE_PLAY, REQUEST_PLAY);
            }
        }
    }

    private void minimize() {
        videoControlView.hideControls();
        videoControlView.setPlay(false);
        if (player.getPlayWhenReady()) {
            player.setPlayWhenReady(false);
        }

        Rational aspectRatio = new Rational(videoControlView.getHeight(), videoControlView.getWidth());

        final PictureInPictureParams.Builder pictureInPictureParamsBuilder = new PictureInPictureParams.Builder();
        pictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build();

        enterPictureInPictureMode(pictureInPictureParamsBuilder.build());
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        videoControlView.setPictureInPicture(isInPictureInPictureMode);
        if (isInPictureInPictureMode) {
            if (player != null) {
                updatePictureInPictureState(player.getPlayWhenReady());
            }

            pictureInPictureReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent == null || !ACTION_MEDIA_CONTROL.equals(intent.getAction())) {
                        return;
                    }
                    final int controlType = intent.getIntExtra(EXTRA_CONTROL_TYPE, 0);
                    switch (controlType) {
                        case CONTROL_TYPE_PLAY:
                            player.setPlayWhenReady(true);
                            updatePictureInPictureActions(R.drawable.ic_pause_24dp,
                                    "Pause", CONTROL_TYPE_PAUSE, REQUEST_PAUSE);
                            break;
                        case CONTROL_TYPE_PAUSE:
                            player.setPlayWhenReady(false);
                            updatePictureInPictureActions(R.drawable.ic_play_24dp,
                                    "Play", CONTROL_TYPE_PLAY, REQUEST_PLAY);
                            break;
                    }
                }
            };
            registerReceiver(pictureInPictureReceiver, new IntentFilter(ACTION_MEDIA_CONTROL));
        } else {
            if (player != null) {
                shouldAutoPlay = player.getPlayWhenReady();
                videoControlView.setPlay(shouldAutoPlay);
                updatePictureInPictureState(shouldAutoPlay);
            }
            videoControlView.showControls();

            unregisterReceiver(pictureInPictureReceiver);
            pictureInPictureReceiver = null;
        }
    }


    private void updatePictureInPictureActions(int iconId, String title, int controlType, int requestCode) {
        final ArrayList<RemoteAction> actions = new ArrayList<>();

        final PendingIntent intent = PendingIntent.getBroadcast(VideoExoPlayerActivity.this,
                requestCode, new Intent(ACTION_MEDIA_CONTROL)
                        .putExtra(EXTRA_CONTROL_TYPE, controlType), 0);
        final Icon icon = Icon.createWithResource(VideoExoPlayerActivity.this, iconId);
        actions.add(new RemoteAction(icon, title, title, intent));

        final PictureInPictureParams.Builder pictureInPictureParamsBuilder = new PictureInPictureParams.Builder();
        pictureInPictureParamsBuilder.setActions(actions).build();
        setPictureInPictureParams(pictureInPictureParamsBuilder.build());
    }

    @Override
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
        videoPlayer.previous();
    }

    private void next() {
        videoPlayer.next();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        videoControlView.resetIndicators();
        isTimelineStatic = false;

        releasePlayer();

        setIntent(intent);
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
        out.putBoolean(BUNDLE_AUTO_PLAY, shouldAutoPlay);
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
        videoControlView.postDelayed(this::makeGuideIfNeed, 1000);
    }

    private void makeGuideIfNeed() {
        if (settingsManager.isNeedVideoPlayerActivityGuide()) {
            appGuide = new AppGuide(this, 5);
            appGuide.OnAppGuideListener(new AppGuide.OnAppGuideListener() {
                @Override
                public void next(int count) {
                    switch (count) {
                        case 1:
                            appGuide.makeGuide(videoControlView.getSettingsButton(),
                                    getString(R.string.app_guide_settings_title),
                                    getString(R.string.app_guide_settings_desc));
                            break;
                        case 2:
                            appGuide.makeGuide(videoControlView.getVideoLockScreen(),
                                    getString(R.string.app_guide_lock_title),
                                    getString(R.string.app_guide_lock_desc));
                            break;
                        case 3:
                            appGuide.makeGuide(videoControlView.getPreviousVideo(),
                                    getString(R.string.app_guide_video_prev_title),
                                    getString(R.string.app_guide_video_prev_desc));
                            break;
                        case 4:
                            appGuide.makeGuide(videoControlView.getNextVideo(),
                                    getString(R.string.app_guide_video_next_title),
                                    getString(R.string.app_guide_video_next_desc));
                            break;
                    }
                }

                @Override
                public void watched() {
                    settingsManager.setNeedVideoPlayerActivityGuide(false);
                }
            });
            appGuide.makeGuide(videoControlView.getAddSubButton(),
                    getString(R.string.app_guide_subtitle_title),
                    getString(R.string.app_guide_subtitle_desc));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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
        if (appGuide != null) {
            appGuide.clear();
        }
        shouldAutoPlay = false;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
        stopUpdateTimer();
        videoControlView.saveRendererState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        videoControlView.clearRendererState();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCurrentVideoFileEvent(VideoFile videoFile) {
        Log.e(TAG, "PLAYER onCurrentVideoFileEvent: " + videoFile.toString());
        videoControlView.resetIndicators();
        isTimelineStatic = false;

        releasePlayer();

        Intent intent = new Intent();
        intent.putExtra(VideoExoPlayerActivity.URI_EXTRA, videoFile.getFilePath());
        intent.putExtra(VideoExoPlayerActivity.VIDEO_NAME_EXTRA, videoFile.getFileName());
        intent.setAction(VideoExoPlayerActivity.ACTION_VIEW_URI);
        setIntent(intent);

        initializePlayer();

        checkFirstOrLastVideoFile();

    }

    private void checkFirstOrLastVideoFile() {
        if (videoPlayer.first()) {
            videoControlView.disablePreviousVideoButton();
        } else {
            videoControlView.enablePreviousVideoButton();
        }

        if (videoPlayer.last()) {
            videoControlView.disableNextVideoButton();
        } else {
            videoControlView.enableNextVideoButton();
        }
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
            case Player.STATE_BUFFERING:
                Log.wtf(TAG, "buffering");
                break;
            case Player.STATE_ENDED:
                Log.wtf(TAG, "ended");
                break;
            case Player.STATE_IDLE:
                Log.wtf(TAG, "idle");
                break;
            case Player.STATE_READY:
                Log.wtf(TAG, "ready");
                videoControlView.setVideoTimeTotal(Utils.getVideoFileTimeFormat(player.getDuration()));
                updateProgressControls();
                startUpdateTimer();
                videoControlView.setVideoTrackInfo(player, trackSelector, trackSelectionFactory);
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


    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    private void initializePlayer() {
        Intent intent = getIntent();
        if (player == null) {

            trackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);

            trackSelector = new DefaultTrackSelector(trackSelectionFactory);
            trackSelector.setTunnelingAudioSessionId(C.generateAudioSessionIdV21(getApplicationContext()));


            player = ExoPlayerFactory.newSimpleInstance(
                    new CustomRenderersFactory(getApplicationContext()),
                    trackSelector,
                    new DefaultLoadControl(
                            new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE),
                            DEFAULT_MIN_BUFFER_MS,
                            DEFAULT_MAX_BUFFER_MS,
                            DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                            DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
                    )
            );

            player.addListener(this);
            eventLogger = new EventLogger(trackSelector);
            player.addListener(eventLogger);
            player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);

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
            Uri uri = null;
            if (ACTION_VIEW_URI.equals(action)) {
                currentVideoPath = intent.getStringExtra(URI_EXTRA);
                uri = Uri.parse(currentVideoPath);

                currentVideoName = intent.getStringExtra(VIDEO_NAME_EXTRA);
                videoControlView.setVideoName(currentVideoName);
            } else if (Intent.ACTION_VIEW.equals(action)) {
                String type = intent.getType();
                if (type != null) {
                    if (type.startsWith("video/")) {
                        uri = intent.getData();
                        videoControlView.setVideoName(uri.getLastPathSegment());
                    }
                }
                videoControlView.disableNextVideoButton();
                videoControlView.disablePreviousVideoButton();
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

    private void updateProgressControls() {
        if (player != null) {
            videoControlView.setVideoTimeCount(Utils.getVideoFileTimeFormat(player.getCurrentPosition()));
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
