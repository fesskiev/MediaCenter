package com.fesskiev.mediacenter.ui.video.player;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Rational;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.services.VideoPlaybackService;
import com.fesskiev.mediacenter.utils.AppGuide;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.controls.VideoControlView;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import java.util.ArrayList;

@TargetApi(Build.VERSION_CODES.O)
public class VideoExoPlayerActivity extends AppCompatActivity {

    private static final String TAG = VideoExoPlayerActivity.class.getSimpleName();

    private static final int CONTROL_TYPE_PLAY = 1;
    private static final int CONTROL_TYPE_PAUSE = 2;
    private static final int REQUEST_PLAY = 1;
    private static final int REQUEST_PAUSE = 2;
    private static final int REQUEST_CODE_SUBTITLE = 1337;

    private static final String ACTION_MEDIA_CONTROL = "com.fesskiev.player.ACTION_MEDIA_CONTROL";
    private static final String EXTRA_CONTROL_TYPE = "com.fesskiev.player.EXTRA_CONTROL_TYPE";

    public static final String ACTION_VIEW_URI = "com.fesskiev.player.action.VIEW_LIST";
    public static final String URI_EXTRA = "com.fesskiev.player.URI_EXTRA";
    public static final String VIDEO_NAME_EXTRA = "com.fesskiev.player.VIDEO_NAME_EXTRA";
    public static final String SUB_EXTRA = "com.fesskiev.player.SUB_EXTRA";

    private VideoExoPlayerViewModel viewModel;

    private AppGuide appGuide;

    private GestureDetector gestureDetector;
    private VideoControlView videoControlView;

    private SimpleExoPlayerView simpleExoPlayerView;

    private String currentVideoPath;
    private String currentVideoName;

    private BroadcastReceiver pictureInPictureReceiver;

    private VideoPlaybackService playbackService;
    private boolean serviceBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_exo_player);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        gestureDetector = new GestureDetector(getApplicationContext(), new GestureListener());

        videoControlView = findViewById(R.id.videoPlayerControl);
        videoControlView.setPlay(true);
        videoControlView.setOnVideoPlayerControlListener(new VideoControlView.OnVideoPlayerControlListener() {
            @Override
            public void playPauseButtonClick(boolean isPlaying) {
                setPlayback(isPlaying);
                updatePictureInPictureState(isPlaying);
            }

            @Override
            public void addSubButtonClick() {
                performSubSearch();
            }

            @Override
            public void seekVideo(int progress) {
                seekVideoFile(progress);
            }

            @Override
            public void nextVideo() {
                viewModel.next();
            }

            @Override
            public void previousVideo() {
                viewModel.previous();
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

        simpleExoPlayerView = findViewById(R.id.videoView);
        simpleExoPlayerView.setUseController(false);
        simpleExoPlayerView.requestFocus();
        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);


        videoControlView.setResizeModeState(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
        observeData();
        videoControlView.setProFutures(viewModel.isProUser());
    }

    private void seekVideoFile(int progress) {
        if (playbackService != null && serviceBound) {
            playbackService.seekTo(progress);
        }
    }

    private void stopPlayback() {
        if (playbackService != null && serviceBound) {
            playbackService.stopPlayback();
        }
    }

    private void setPlayback(boolean playing) {
        if (playbackService != null && serviceBound) {
            playbackService.setPlayback(playing);
        }
    }

    private boolean getPlayback() {
        if (playbackService != null && serviceBound) {
            return playbackService.getPlayback();
        }
        return false;
    }

    private void bindVideoPlaybackService() {
        Intent intent = new Intent(this, VideoPlaybackService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindVideoPlaybackService() {
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            VideoPlaybackService.VideoPlaybackServiceBinder binder =
                    (VideoPlaybackService.VideoPlaybackServiceBinder) service;
            playbackService = binder.getService();
            serviceBound = true;
            initializePlayer();
        }
    };

    private void observeData() {
        viewModel = ViewModelProviders.of(this).get(VideoExoPlayerViewModel.class);
        viewModel.getCurrentVideoFileLiveData().observe(this, this::setCurrentVideoFile);
        viewModel.getFirstVideoFileLiveData().observe(this, this::setFirstVideoFile);
        viewModel.getLastVideoFileLiveData().observe(this, this::setLastVideoFile);
        viewModel.getPositionLiveData().observe(this, this::setPosition);
        viewModel.getProgressLiveData().observe(this, this::setProgress);
        viewModel.getPlayingLiveData().observe(this, this::setPlaying);
        viewModel.getErrorMessageLiveData().observe(this, this::showErrorShackBar);
        viewModel.getTracksInitLiveData().observe(this, Void -> initTracksInfo());
    }

    private void initTracksInfo() {
        ExoPlayerWrapper player = playbackService.getExoPlayerWrapper();
        videoControlView.setVideoTimeTotal(Utils.getVideoFileTimeFormat(player.getDuration()));

        videoControlView.setVideoTrackInfo(player, viewModel.getRendererState());
    }

    private void setPlaying(boolean playing) {
        videoControlView.setPlay(playing);
    }

    private void setProgress(long progress) {
        videoControlView.setProgress((int) progress);
    }

    private void setPosition(long position) {
        videoControlView.setVideoTimeCount(Utils.getVideoFileTimeFormat(position));
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
        stopPlayback();

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
            updatePictureInPictureState(getPlayback());


            pictureInPictureReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent == null || !ACTION_MEDIA_CONTROL.equals(intent.getAction())) {
                        return;
                    }
                    final int controlType = intent.getIntExtra(EXTRA_CONTROL_TYPE, 0);
                    switch (controlType) {
                        case CONTROL_TYPE_PLAY:
                            setPlayback(true);
                            updatePictureInPictureActions(R.drawable.ic_pause_24dp,
                                    "Pause", CONTROL_TYPE_PAUSE, REQUEST_PAUSE);
                            break;
                        case CONTROL_TYPE_PAUSE:
                            setPlayback(false);
                            updatePictureInPictureActions(R.drawable.ic_play_24dp,
                                    "Play", CONTROL_TYPE_PLAY, REQUEST_PLAY);
                            break;
                    }
                }
            };
            registerReceiver(pictureInPictureReceiver, new IntentFilter(ACTION_MEDIA_CONTROL));
        } else {

            boolean shouldAutoPlay = getPlayback();
            videoControlView.setPlay(shouldAutoPlay);
            updatePictureInPictureState(shouldAutoPlay);

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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        videoControlView.resetIndicators();
        releasePlayer();
        setIntent(intent);
        initializePlayer();
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
                    releasePlayer();
                    Intent intent = new Intent();
                    intent.putExtra(VideoExoPlayerActivity.URI_EXTRA, currentVideoPath);
                    intent.putExtra(VideoExoPlayerActivity.VIDEO_NAME_EXTRA, currentVideoName);
                    intent.putExtra(VideoExoPlayerActivity.SUB_EXTRA, Environment.getExternalStorageDirectory() + "/" + split[1]);
                    intent.setAction(VideoExoPlayerActivity.ACTION_VIEW_URI);
                    setIntent(intent);
                    initializePlayer();
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
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public void onStart() {
        super.onStart();
        bindVideoPlaybackService();
        videoControlView.postDelayed(this::makeGuideIfNeed, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (appGuide != null) {
            appGuide.clear();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        unbindVideoPlaybackService();
        viewModel.saveRendererState(videoControlView.getRendererState());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.clearRendererState();
    }

    private void initializePlayer() {
        Intent intent = getIntent();
        String action = intent.getAction();

        Uri uri = null;
        String subPath = null;
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
        }
        if (intent.hasExtra(SUB_EXTRA)) {
            subPath = intent.getStringExtra(SUB_EXTRA);
        }
        if (playbackService != null && serviceBound) {
            simpleExoPlayerView.setPlayer(playbackService.initPlayer(uri, subPath));
        }
    }

    private void releasePlayer() {
        if (playbackService != null && serviceBound) {
            playbackService.releasePlayer();
        }
    }

    private void setCurrentVideoFile(VideoFile videoFile) {
        videoControlView.resetIndicators();

        releasePlayer();
        Intent intent = new Intent();
        intent.putExtra(VideoExoPlayerActivity.URI_EXTRA, videoFile.getFilePath());
        intent.putExtra(VideoExoPlayerActivity.VIDEO_NAME_EXTRA, videoFile.getFileName());
        intent.setAction(VideoExoPlayerActivity.ACTION_VIEW_URI);
        setIntent(intent);
        initializePlayer();
    }

    private void setLastVideoFile(boolean last) {
        if (last) {
            videoControlView.disableNextVideoButton();
        } else {
            videoControlView.enableNextVideoButton();
        }
    }

    private void setFirstVideoFile(boolean first) {
        if (first) {
            videoControlView.disablePreviousVideoButton();
        } else {
            videoControlView.enablePreviousVideoButton();
        }
    }

    private void showErrorShackBar(String message) {
        Utils.showCustomSnackbar(findViewById(R.id.videoPlayerRoot), getApplicationContext(),
                message, Snackbar.LENGTH_LONG).show();
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

    private void makeGuideIfNeed() {
        if (viewModel.isNeedVideoPlayerActivityGuide()) {
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
                    viewModel.setNeedVideoPlayerActivityGuide(false);
                }
            });
            appGuide.makeGuide(videoControlView.getAddSubButton(),
                    getString(R.string.app_guide_subtitle_title),
                    getString(R.string.app_guide_subtitle_desc));
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
