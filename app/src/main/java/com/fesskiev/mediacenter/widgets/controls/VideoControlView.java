package com.fesskiev.mediacenter.widgets.controls;


import android.content.Context;
import android.graphics.drawable.Animatable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.AnimationUtils;
import com.fesskiev.mediacenter.widgets.buttons.PlayPauseButton;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.util.MimeTypes;

import java.util.Locale;

import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH;

public class VideoControlView extends FrameLayout {


    public interface OnVideoPlayerControlListener {

        void playPauseButtonClick(boolean isPlaying);

        void seekVideo(int progress);

        void nextVideo();

        void previousVideo();

        void resizeModeChanged(int mode);
    }

    private OnVideoPlayerControlListener listener;
    private View videoControlPanel;
    private PlayPauseButton playPauseButton;
    private SeekBar seekVideo;
    private TextView videoTimeCount;
    private TextView videoTimeTotal;
    private TextView resizeModeState;
    private TextView videoName;
    private boolean isPlaying;
    private int resizeMode;
    private boolean showPanel;
    private boolean animatePanel;
    private int heightPanel;
    private boolean showControl;
    private boolean animateControl;

    private MappingTrackSelector selector;
    private TrackSelection.Factory adaptiveVideoTrackSelectionFactory;
    private MappingTrackSelector.SelectionOverride override;


    public VideoControlView(Context context) {
        super(context);
        init(context);
    }

    public VideoControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.video_player_control, this, true);

        showControl = true;
        showPanel = true;
        videoControlPanel = view.findViewById(R.id.videoControlPanel);
        videoControlPanel.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        heightPanel = videoControlPanel.getHeight();
                        hidePanel(100);

                        getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                });

        videoTimeCount = (TextView) view.findViewById(R.id.videoTimeCount);
        videoTimeTotal = (TextView) view.findViewById(R.id.videoTimeTotal);

        view.findViewById(R.id.resizeModeState).setOnClickListener(v -> changeResizeMode());

        resizeModeState = (TextView) view.findViewById(R.id.resizeModeState);
        videoName = (TextView) view.findViewById(R.id.videoName);


        ImageView settingsButton = (ImageView) view.findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            AnimationUtils.getInstance().rotateAnimation(settingsButton);
            togglePanel();
        });

        ImageView nextVideo = (ImageView) findViewById(R.id.nextVideo);
        nextVideo.setOnClickListener(v -> {
            ((Animatable) nextVideo.getDrawable()).start();
            if (listener != null) {
                listener.nextVideo();
            }
        });

        ImageView previousVideo = (ImageView) findViewById(R.id.previousVideo);
        previousVideo.setOnClickListener(v -> {
            ((Animatable) previousVideo.getDrawable()).start();
            if (listener != null) {
                listener.previousVideo();
            }
        });
        playPauseButton = (PlayPauseButton) view.findViewById(R.id.playPauseButton);
        playPauseButton.setColor(ContextCompat.getColor(context, R.color.primary));
        playPauseButton.setOnClickListener(v -> {
            isPlaying = !isPlaying;
            playPauseButton.setPlay(isPlaying);
            if (listener != null) {
                listener.playPauseButtonClick(isPlaying);
            }

        });

        seekVideo = (SeekBar) findViewById(R.id.seekVideo);
        seekVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.progress = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (listener != null) {
                    listener.seekVideo(progress);
                }
            }
        });
    }

    private void changeResizeMode() {
        switch (resizeMode) {
            case RESIZE_MODE_FIT:
                resizeModeState.setText(getResources().getString(R.string.resize_mode_fill));
                resizeMode = RESIZE_MODE_FILL;
                break;
            case RESIZE_MODE_FILL:
                resizeModeState.setText(getResources().getString(R.string.resize_mode_fixed_width));
                resizeMode = RESIZE_MODE_FIXED_WIDTH;
                break;
            case RESIZE_MODE_FIXED_HEIGHT:
                resizeModeState.setText(getResources().getString(R.string.resize_mode_fit));
                resizeMode = RESIZE_MODE_FIT;
                break;
            case RESIZE_MODE_FIXED_WIDTH:
                resizeModeState.setText(getResources().getString(R.string.resize_mode_fixed_height));
                resizeMode = RESIZE_MODE_FIXED_HEIGHT;
                break;

        }

        if (listener != null) {
            listener.resizeModeChanged(resizeMode);
        }
    }

    public void setResizeModeState(@AspectRatioFrameLayout.ResizeMode int resizeMode) {
        switch (resizeMode) {
            case RESIZE_MODE_FIT:
                resizeModeState.setText(getResources().getString(R.string.resize_mode_fit));
                this.resizeMode = RESIZE_MODE_FIT;
                break;
            case RESIZE_MODE_FILL:
                resizeModeState.setText(getResources().getString(R.string.resize_mode_fill));
                this.resizeMode = RESIZE_MODE_FILL;
                break;
            case RESIZE_MODE_FIXED_HEIGHT:
                resizeModeState.setText(getResources().getString(R.string.resize_mode_fixed_height));
                this.resizeMode = RESIZE_MODE_FIXED_HEIGHT;
                break;
            case RESIZE_MODE_FIXED_WIDTH:
                resizeModeState.setText(getResources().getString(R.string.resize_mode_fixed_width));
                this.resizeMode = RESIZE_MODE_FIXED_WIDTH;
                break;
        }
    }


    public void setOnVideoPlayerControlListener(OnVideoPlayerControlListener l) {
        this.listener = l;
    }

    public void setVideoTimeTotal(String time) {
        videoTimeTotal.setText(time);
    }

    public void setVideoTimeCount(String time) {
        videoTimeCount.setText(time);
    }

    public void setProgress(int progress) {
        seekVideo.setProgress(progress);
    }

    public void setVideoName(String name) {
        videoName.setText(name);
    }

    public void setPlay(boolean play) {
        this.isPlaying = play;
        playPauseButton.setPlay(play);
    }

    public void resetIndicators() {
        videoTimeTotal.setText("0:00");
        videoTimeCount.setText("0:00");
        seekVideo.setProgress(0);
    }


    public void setVideoTrackInfo(SimpleExoPlayer player, MappingTrackSelector selector,
                                  TrackSelection.Factory adaptiveVideoTrackSelectionFactory) {
        this.selector = selector;
        this.adaptiveVideoTrackSelectionFactory = adaptiveVideoTrackSelectionFactory;

        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = selector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo == null) {
            return;
        }

        Log.e("test_", "setVideoTrackInfo");

        for (int i = 0; i < mappedTrackInfo.length; i++) {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
            if (trackGroups.length != 0) {
                switch (player.getRendererType(i)) {
                    case C.TRACK_TYPE_AUDIO:
                        setTrack(i);
                        break;
                    case C.TRACK_TYPE_VIDEO:
                        setTrack(i);
                        break;
                    case C.TRACK_TYPE_TEXT:
                        setTrack(i);
                        break;
                    default:
                }
            }
        }
    }

    private void setTrack(int rendererIndex) {
        Log.e("test_", "setTrack: " + rendererIndex);

        MappingTrackSelector.MappedTrackInfo trackInfo = selector.getCurrentMappedTrackInfo();

        TrackGroupArray trackGroups = trackInfo.getTrackGroups(rendererIndex);
        boolean[] trackGroupsAdaptive = new boolean[trackGroups.length];
        for (int i = 0; i < trackGroups.length; i++) {
            trackGroupsAdaptive[i] = adaptiveVideoTrackSelectionFactory != null
                    && trackInfo.getAdaptiveSupport(rendererIndex, i, false)
                    != RendererCapabilities.ADAPTIVE_NOT_SUPPORTED
                    && trackGroups.get(i).length > 1;
        }
        boolean isDisabled = selector.getRendererDisabled(rendererIndex);
        override = selector.getSelectionOverride(rendererIndex, trackGroups);

        boolean haveSupportedTracks = false;
        boolean haveAdaptiveTracks = false;
        for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
            TrackGroup group = trackGroups.get(groupIndex);
            boolean groupIsAdaptive = trackGroupsAdaptive[groupIndex];
            haveAdaptiveTracks |= groupIsAdaptive;
            for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
                if (trackIndex == 0) {

                }
                Log.e("test_", "track name: " + buildTrackName(group.getFormat(trackIndex)) + " is disabled: " + isDisabled);

                if (trackInfo.getTrackFormatSupport(rendererIndex, groupIndex, trackIndex)
                        == RendererCapabilities.FORMAT_HANDLED) {
                    haveSupportedTracks = true;

                    Log.e("test_", "haveSupportedTracks");
                }
            }
        }
    }

    private static String buildTrackName(Format format) {
        String trackName;
        if (MimeTypes.isVideo(format.sampleMimeType)) {
            trackName = joinWithSeparator(joinWithSeparator(buildResolutionString(format),
                    buildBitrateString(format)), buildTrackIdString(format));
        } else if (MimeTypes.isAudio(format.sampleMimeType)) {
            trackName = joinWithSeparator(joinWithSeparator(joinWithSeparator(buildLanguageString(format),
                    buildAudioPropertyString(format)), buildBitrateString(format)),
                    buildTrackIdString(format));
        } else {
            trackName = joinWithSeparator(joinWithSeparator(buildLanguageString(format),
                    buildBitrateString(format)), buildTrackIdString(format));
        }
        return trackName.length() == 0 ? "unknown" : trackName;
    }

    private static String buildResolutionString(Format format) {
        return format.width == Format.NO_VALUE || format.height == Format.NO_VALUE
                ? "" : format.width + "x" + format.height;
    }

    private static String buildAudioPropertyString(Format format) {
        return format.channelCount == Format.NO_VALUE || format.sampleRate == Format.NO_VALUE
                ? "" : format.channelCount + "ch, " + format.sampleRate + "Hz";
    }

    private static String buildLanguageString(Format format) {
        return TextUtils.isEmpty(format.language) || "und".equals(format.language) ? ""
                : format.language;
    }

    private static String buildBitrateString(Format format) {
        return format.bitrate == Format.NO_VALUE ? ""
                : String.format(Locale.US, "%.2fMbit", format.bitrate / 1000000f);
    }

    private static String joinWithSeparator(String first, String second) {
        return first.length() == 0 ? second : (second.length() == 0 ? first : first + ", " + second);
    }

    private static String buildTrackIdString(Format format) {
        return format.id == null ? "" : ("id:" + format.id);
    }


    private void hidePanel(int duration) {
        if (!animatePanel) {
            animatePanel = true;
            ViewCompat.animate(videoControlPanel)
                    .translationY(-heightPanel)
                    .setDuration(duration)
                    .setInterpolator(new DecelerateInterpolator(1.2f))
                    .setListener(new ViewPropertyAnimatorListener() {
                        @Override
                        public void onAnimationStart(View view) {
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            showPanel = false;
                            animatePanel = false;
                        }

                        @Override
                        public void onAnimationCancel(View view) {

                        }
                    }).start();
        }

    }

    private void showPanel() {
        if (!animatePanel) {
            animatePanel = true;
            ViewCompat.animate(videoControlPanel)
                    .translationY(0)
                    .setDuration(800)
                    .setInterpolator(new DecelerateInterpolator(1.2f))
                    .setListener(new ViewPropertyAnimatorListener() {
                        @Override
                        public void onAnimationStart(View view) {

                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            showPanel = true;
                            animatePanel = false;
                        }

                        @Override
                        public void onAnimationCancel(View view) {

                        }
                    }).start();
        }
    }

    private void togglePanel() {
        if (showPanel) {
            hidePanel(800);
        } else {
            showPanel();
        }
    }

    public void toggleControl() {
        if (showControl) {
            hideControl();
        } else {
            showControl();
        }
    }

    private void hideControl() {
        if (!animateControl) {
            animateControl = true;
            ViewCompat.animate(this)
                    .alpha(0f)
                    .setDuration(800)
                    .setInterpolator(new DecelerateInterpolator(1.2f))
                    .setListener(new ViewPropertyAnimatorListener() {
                        @Override
                        public void onAnimationStart(View view) {
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            showControl = false;
                            animateControl = false;
                        }

                        @Override
                        public void onAnimationCancel(View view) {

                        }
                    }).start();
        }
    }

    private void showControl() {
        if (!animateControl) {
            animateControl = true;
            ViewCompat.animate(this)
                    .alpha(0.9f)
                    .setDuration(800)
                    .setInterpolator(new DecelerateInterpolator(1.2f))
                    .setListener(new ViewPropertyAnimatorListener() {
                        @Override
                        public void onAnimationStart(View view) {
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            showControl = true;
                            animateControl = false;
                        }

                        @Override
                        public void onAnimationCancel(View view) {

                        }
                    }).start();
        }
    }
}
