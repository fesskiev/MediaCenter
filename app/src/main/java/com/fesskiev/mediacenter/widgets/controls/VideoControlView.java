package com.fesskiev.mediacenter.widgets.controls;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.video.RendererState;
import com.fesskiev.mediacenter.ui.cut.CutMediaActivity;
import com.fesskiev.mediacenter.utils.AppAnimationUtils;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.buttons.PlayPauseButton;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.RandomTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.util.MimeTypes;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH;

public class VideoControlView extends FrameLayout {

    public interface OnVideoPlayerControlListener {

        void playPauseButtonClick(boolean isPlaying);

        void addSubButtonClick();

        void seekVideo(int progress);

        void nextVideo();

        void previousVideo();

        void resizeModeChanged(int mode);

        void pictureInPictureModeChanged(boolean enable);
    }

    private OnVideoPlayerControlListener listener;

    private View trackSelectionPanel;
    private View videoControlPanel;
    private PlayPauseButton playPauseButton;
    private SeekBar seekVideo;
    private TextView videoTimeCount;
    private TextView videoTimeTotal;
    private TextView resizeModeState;
    private TextView videoName;
    private ImageView videoLockScreen;
    private ImageView addSubButton;
    private ImageView settingsButton;
    private ImageView pipButton;
    private ImageView nextVideo;
    private ImageView previousVideo;
    private TextView audioTrackView;
    private TextView videoTrackView;
    private TextView subTrackView;

    private boolean isPictureInPicture;
    private boolean isPlaying;
    private int resizeMode;
    private boolean showPanel;
    private boolean animatePanel;
    private boolean showControl;
    private boolean animateControl;

    private static final TrackSelection.Factory FIXED_FACTORY = new FixedTrackSelection.Factory();
    private static final TrackSelection.Factory RANDOM_FACTORY = new RandomTrackSelection.Factory();

    private MappingTrackSelector selector;
    private TrackSelection.Factory adaptiveVideoTrackSelectionFactory;
    private MappingTrackSelector.SelectionOverride override;
    private int rendererIndex;
    private TrackGroupArray trackGroups;
    private boolean[] trackGroupsAdaptive;
    private boolean isDisabled;

    private CheckedTextView disableView;
    private CheckedTextView defaultView;
    private CheckedTextView enableRandomAdaptationView;
    private CheckedTextView[][] trackViews;

    private Set<RendererState> rendererStates;
    private boolean restoreRenderer;
    private boolean lockScreen;


    public VideoControlView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public VideoControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public VideoControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.VideoControlView, defStyle, 0);

        String orientation = a.getString(R.styleable.VideoControlView_orientation);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;
        if (orientation.equals("portrait")) {
            view = inflater.inflate(R.layout.video_player_control, this, true);
        } else {
            view = inflater.inflate(R.layout.video_player_control_landscape, this, true);
        }

        a.recycle();

        rendererStates = new TreeSet<>();

        showControl = true;
        showPanel = false;
        lockScreen = false;
        restoreRenderer = true;

        videoControlPanel = view.findViewById(R.id.videoControlPanel);

        trackSelectionPanel = view.findViewById(R.id.trackSelectionPanel);

        videoTimeCount = view.findViewById(R.id.videoTimeCount);
        videoTimeTotal = view.findViewById(R.id.videoTimeTotal);

        videoName = view.findViewById(R.id.videoName);

        audioTrackView = view.findViewById(R.id.audioTrackButton);
        audioTrackView.setOnClickListener(v -> setAudioTrack((int) v.getTag()));

        videoTrackView = view.findViewById(R.id.videoTrackButton);
        videoTrackView.setOnClickListener(v -> setVideoTrack((int) v.getTag()));

        subTrackView = view.findViewById(R.id.subTrackButton);
        subTrackView.setOnClickListener(v -> setSubTrack((int) v.getTag()));

        resizeModeState = view.findViewById(R.id.resizeModeState);
        resizeModeState.setOnClickListener(v -> changeResizeMode());

        addSubButton = view.findViewById(R.id.addSubButton);
        addSubButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.addSubButtonClick();
            }
        });

        videoLockScreen = view.findViewById(R.id.videoLockScreen);
        videoLockScreen.setOnClickListener(v -> toggleLockScreen());

        ImageView cutVideoButton = view.findViewById(R.id.cutVideoButton);
        cutVideoButton.setOnClickListener(v -> startCutActivity());
        boolean isProUser = AppSettingsManager.getInstance().isUserPro();
        if (!isProUser) {
            cutVideoButton.setVisibility(INVISIBLE);
        }

        pipButton = view.findViewById(R.id.pipButton);
        pipButton.setOnClickListener(v -> {
            isPictureInPicture = !isPictureInPicture;
            if (listener != null) {
                listener.pictureInPictureModeChanged(isPictureInPicture);
            }
        });
        if (Utils.isOreo()) {
            pipButton.setVisibility(VISIBLE);
        }

        settingsButton = view.findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> togglePanel(settingsButton));

        nextVideo = findViewById(R.id.nextVideo);
        nextVideo.setOnClickListener(v -> {
            ((Animatable) nextVideo.getDrawable()).start();
            if (listener != null) {
                listener.nextVideo();
            }
        });

        previousVideo = findViewById(R.id.previousVideo);
        previousVideo.setOnClickListener(v -> {
            ((Animatable) previousVideo.getDrawable()).start();
            if (listener != null) {
                listener.previousVideo();
            }
        });
        playPauseButton = view.findViewById(R.id.playPauseButton);
        playPauseButton.setColor(ContextCompat.getColor(context, R.color.player_primary));
        playPauseButton.setOnClickListener(v -> {
            isPlaying = !isPlaying;
            playPauseButton.setPlay(isPlaying);
            if (listener != null) {
                listener.playPauseButtonClick(isPlaying);
            }
        });

        seekVideo = findViewById(R.id.seekVideo);
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


    private void startCutActivity() {
        CutMediaActivity.startCutMediaActivity((Activity) getContext(), CutMediaActivity.CUT_VIDEO);
    }


    private void setSubTrack(int index) {
        AppAnimationUtils.getInstance().scaleToSmallViews(audioTrackView, videoTrackView);
        AppAnimationUtils.getInstance().scaleToOriginalView(subTrackView);
        rendererIndex = index;
        setTrack();
    }

    private void setVideoTrack(int index) {
        AppAnimationUtils.getInstance().scaleToSmallViews(audioTrackView, subTrackView);
        AppAnimationUtils.getInstance().scaleToOriginalView(videoTrackView);
        rendererIndex = index;
        setTrack();
    }

    private void setAudioTrack(int index) {
        AppAnimationUtils.getInstance().scaleToSmallViews(subTrackView, videoTrackView);
        AppAnimationUtils.getInstance().scaleToOriginalView(audioTrackView);
        rendererIndex = index;
        setTrack();
    }

    private void changeResizeMode() {
        AppAnimationUtils.getInstance().errorAnimation(resizeModeState);
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

        MappingTrackSelector.MappedTrackInfo trackInfo = selector.getCurrentMappedTrackInfo();
        if (trackInfo == null) {
            return;
        }
        for (int i = 0; i < trackInfo.length; i++) {
            TrackGroupArray trackGroups = trackInfo.getTrackGroups(i);
            if (trackGroups.length != 0) {
                switch (player.getRendererType(i)) {
                    case C.TRACK_TYPE_AUDIO:
                        audioTrackView.setVisibility(VISIBLE);
                        audioTrackView.setTag(i);
                        break;
                    case C.TRACK_TYPE_VIDEO:
                        videoTrackView.setVisibility(VISIBLE);
                        videoTrackView.setTag(i);
                        break;
                    case C.TRACK_TYPE_TEXT:
                        subTrackView.setVisibility(VISIBLE);
                        subTrackView.setTag(i);
                        break;
                    default:
                }
            }
        }

        restoreTracksState();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (isPointInsideView(e.getRawX(), e.getRawY(), videoLockScreen)) {
            return false;
        }
        return lockScreen;
    }

    private boolean isPointInsideView(float x, float y, View view) {
        int location[] = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];
        return (x > viewX && x < (viewX + view.getWidth())) &&
                (y > viewY && y < (viewY + view.getHeight()));
    }

    private OnClickListener onClickListener = this::handleTrackClick;

    private void handleTrackClick(View view) {
        if (view == disableView) {
            isDisabled = true;
            override = null;
        } else if (view == defaultView) {
            isDisabled = false;
            override = null;
        } else if (view == enableRandomAdaptationView) {
            setOverride(override.groupIndex, override.tracks, !enableRandomAdaptationView.isChecked());
        } else {
            isDisabled = false;
            @SuppressWarnings("unchecked")
            Pair<Integer, Integer> tag = (Pair<Integer, Integer>) view.getTag();
            int groupIndex = tag.first;
            int trackIndex = tag.second;
            if (!trackGroupsAdaptive[groupIndex] || override == null
                    || override.groupIndex != groupIndex) {
                override = new MappingTrackSelector.SelectionOverride(FIXED_FACTORY, groupIndex, trackIndex);
            } else {
                // The group being modified is adaptive and we already have a non-null override.
                boolean isEnabled = ((CheckedTextView) view).isChecked();
                int overrideLength = override.length;
                if (isEnabled) {
                    // Remove the track from the override.
                    if (overrideLength == 1) {
                        // The last track is being removed, so the override becomes empty.
                        override = null;
                        isDisabled = true;
                    } else {
                        setOverride(groupIndex, getTracksRemoving(override, trackIndex),
                                enableRandomAdaptationView.isChecked());
                    }
                } else {
                    // Add the track to the override.
                    setOverride(groupIndex, getTracksAdding(override, trackIndex),
                            enableRandomAdaptationView.isChecked());
                }
            }
        }

        updateViews();
    }

    private void restoreTracksState() {

        Set<RendererState> rendererStates = AppSettingsManager.getInstance().getRendererState();

        if (restoreRenderer) {
            MappingTrackSelector.MappedTrackInfo trackInfo = selector.getCurrentMappedTrackInfo();
            if (rendererStates != null && !rendererStates.isEmpty()) {

                for (RendererState rendererState : rendererStates) {

                    Log.e("test", "restore renderer state!" + rendererState.toString());

                    boolean disable = rendererState.isDisabled();
                    selector.setRendererDisabled(rendererState.getIndex(), disable);
                    if (disable) {
                        selector.clearSelectionOverrides(rendererState.getIndex());
                    } else {
                        TrackGroupArray trackGroups = trackInfo.getTrackGroups(rendererState.getIndex());

                        MappingTrackSelector.SelectionOverride override = new MappingTrackSelector.SelectionOverride(FIXED_FACTORY,
                                rendererState.getGroupIndex(), rendererState.getTrackIndex());

                        selector.setSelectionOverride(rendererState.getIndex(), trackGroups, override);
                    }
                }
            } else {
                /**
                 *  set default audio renderer
                 */
                Log.e("test", "set default audio renderer state");

                TrackGroupArray trackGroups = trackInfo.getTrackGroups(C.TRACK_TYPE_AUDIO);

                selector.setRendererDisabled(C.TRACK_TYPE_AUDIO, false);
                MappingTrackSelector.SelectionOverride override
                        = new MappingTrackSelector.SelectionOverride(FIXED_FACTORY, 0, 0);
                selector.setSelectionOverride(C.TRACK_TYPE_AUDIO, trackGroups, override);
            }

            restoreRenderer = false;
        }
    }

    private void setTrack() {

        MappingTrackSelector.MappedTrackInfo trackInfo = selector.getCurrentMappedTrackInfo();

        trackGroups = trackInfo.getTrackGroups(rendererIndex);
        trackGroupsAdaptive = new boolean[trackGroups.length];
        for (int i = 0; i < trackGroups.length; i++) {
            trackGroupsAdaptive[i] = adaptiveVideoTrackSelectionFactory != null
                    && trackInfo.getAdaptiveSupport(rendererIndex, i, false)
                    != RendererCapabilities.ADAPTIVE_NOT_SUPPORTED
                    && trackGroups.get(i).length > 1;
        }
        isDisabled = selector.getRendererDisabled(rendererIndex);
        override = selector.getSelectionOverride(rendererIndex, trackGroups);

        buildView(getContext());

    }

    @SuppressLint("InflateParams")
    private void buildView(Context context) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup root = trackSelectionPanel.findViewById(R.id.trackSelectionRoot);
        root.removeAllViews();

        root.addView(inflater.inflate(R.layout.item_video_list_divider, root, false));

        TypedArray attributeArray = context.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.selectableItemBackground});
        int selectableItemBackgroundResourceId = attributeArray.getResourceId(0, 0);
        attributeArray.recycle();

        // View for disabling the renderer.
        disableView = (CheckedTextView) inflater.inflate(
                R.layout.item_video_track_single_layout, root, false);
        disableView.setBackgroundResource(selectableItemBackgroundResourceId);
        disableView.setText(getResources().getString(R.string.video_track_disable));
        disableView.setFocusable(true);
        disableView.setOnClickListener(onClickListener);
        root.addView(disableView);

        // View for clearing the override to allow the selector to use its default selection logic.
        defaultView = (CheckedTextView) inflater.inflate(R.layout.item_video_track_single_layout, root, false);
        defaultView.setBackgroundResource(selectableItemBackgroundResourceId);
        defaultView.setText(getResources().getString(R.string.video_track_default));
        defaultView.setFocusable(true);
        defaultView.setOnClickListener(onClickListener);
        root.addView(inflater.inflate(R.layout.item_video_list_divider, root, false));
        root.addView(defaultView);

        // Per-track views.
        boolean haveSupportedTracks = false;
        boolean haveAdaptiveTracks = false;
        trackViews = new CheckedTextView[trackGroups.length][];
        for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
            TrackGroup group = trackGroups.get(groupIndex);
            boolean groupIsAdaptive = trackGroupsAdaptive[groupIndex];
            haveAdaptiveTracks |= groupIsAdaptive;
            trackViews[groupIndex] = new CheckedTextView[group.length];
            for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
                if (trackIndex == 0) {
                    root.addView(inflater.inflate(R.layout.item_video_list_divider, root, false));
                }
                int trackViewLayoutId = groupIsAdaptive ? R.layout.item_video_track_multiple_layout
                        : R.layout.item_video_track_single_layout;
                CheckedTextView trackView = (CheckedTextView) inflater.inflate(
                        trackViewLayoutId, root, false);
                trackView.setBackgroundResource(selectableItemBackgroundResourceId);
                trackView.setText(buildTrackName(group.getFormat(trackIndex)));
                trackView.setFocusable(true);
                trackView.setTag(Pair.create(groupIndex, trackIndex));
                trackView.setOnClickListener(onClickListener);
                haveSupportedTracks = true;

                trackViews[groupIndex][trackIndex] = trackView;
                root.addView(trackView);
            }
        }

        if (!haveSupportedTracks) {
            // Indicate that the default selection will be nothing.
            defaultView.setText(getResources().getString(R.string.video_track_default_none));
        } else if (haveAdaptiveTracks) {
            // View for using random adaptation.
            enableRandomAdaptationView = (CheckedTextView) inflater.inflate(
                    R.layout.item_video_track_multiple_layout, root, false);
            enableRandomAdaptationView.setBackgroundResource(selectableItemBackgroundResourceId);
            enableRandomAdaptationView.setText(getResources().getString(R.string.video_track_adaptation));
            enableRandomAdaptationView.setOnClickListener(onClickListener);
            root.addView(inflater.inflate(R.layout.item_video_list_divider, root, false));
            root.addView(enableRandomAdaptationView);
        }

        root.addView(inflater.inflate(R.layout.item_video_list_divider, root, false));

        View buttonRoot = inflater.inflate(R.layout.item_video_track_select_button, root, false);
        buttonRoot.findViewById(R.id.buttonSelectTrack).setOnClickListener(v -> selectTrack());
        root.addView(buttonRoot);

        updateViews();
    }

    private void selectTrack() {
        Log.e("test", "selectTrack disabled? " + isDisabled);

        selector.setRendererDisabled(rendererIndex, isDisabled);

        RendererState rendererState;
        if (override != null) {
            selector.setSelectionOverride(rendererIndex, trackGroups, override);

            rendererState = new RendererState(rendererIndex, override.groupIndex, override.tracks[0], isDisabled);

        } else {
            selector.clearSelectionOverrides(rendererIndex);

            rendererState = new RendererState(rendererIndex, -1, -1, isDisabled);
        }


        if (rendererStates.contains(rendererState)) {
            rendererStates.remove(rendererState);
            Log.e("test", "remove old state");
        }
        Log.e("test", "add renderer stat" + rendererState.toString());
        rendererStates.add(rendererState);
    }

    public void saveRendererState() {
        AppSettingsManager.getInstance().setRendererState(rendererStates);
        Log.e("test", "save renderer state");
        restoreRenderer = true;
    }

    public void clearRendererState() {
        AppSettingsManager.getInstance().clearRendererState();
        Log.e("test", "clear renderer state");
    }

    private void updateViews() {
        disableView.setChecked(isDisabled);
        defaultView.setChecked(!isDisabled && override == null);
        for (int i = 0; i < trackViews.length; i++) {
            for (int j = 0; j < trackViews[i].length; j++) {
                trackViews[i][j].setChecked(override != null && override.groupIndex == i
                        && override.containsTrack(j));
            }
        }
        if (enableRandomAdaptationView != null) {
            boolean enableView = !isDisabled && override != null && override.length > 1;
            enableRandomAdaptationView.setEnabled(enableView);
            enableRandomAdaptationView.setFocusable(enableView);
            if (enableView) {
                enableRandomAdaptationView.setChecked(!isDisabled
                        && override.factory instanceof RandomTrackSelection.Factory);
            }
        }
    }

    private void setOverride(int group, int[] tracks, boolean enableRandomAdaptation) {
        TrackSelection.Factory factory = tracks.length == 1 ? FIXED_FACTORY
                : (enableRandomAdaptation ? RANDOM_FACTORY : adaptiveVideoTrackSelectionFactory);
        override = new MappingTrackSelector.SelectionOverride(factory, group, tracks);
    }

    private static int[] getTracksRemoving(MappingTrackSelector.SelectionOverride override, int removedTrack) {
        int[] tracks = new int[override.length - 1];
        int trackCount = 0;
        for (int i = 0; i < tracks.length + 1; i++) {
            int track = override.tracks[i];
            if (track != removedTrack) {
                tracks[trackCount++] = track;
            }
        }
        return tracks;
    }

    private static int[] getTracksAdding(MappingTrackSelector.SelectionOverride override, int addedTrack) {
        int[] tracks = override.tracks;
        tracks = Arrays.copyOf(tracks, tracks.length + 1);
        tracks[tracks.length - 1] = addedTrack;
        return tracks;
    }

    private static String buildTrackName(Format format) {
        String trackName;
        if (MimeTypes.isVideo(format.sampleMimeType)) {
            trackName = joinWithSeparator(buildResolutionString(format), buildBitrateString(format));
        } else if (MimeTypes.isAudio(format.sampleMimeType)) {
            trackName = joinWithSeparator(joinWithSeparator(buildLanguageString(format),
                    buildAudioPropertyString(format)), buildBitrateString(format));
        } else {
            trackName = joinWithSeparator(buildLanguageString(format), buildBitrateString(format));
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

    private void hidePanel() {
        if (!animatePanel) {
            animatePanel = true;
            ViewCompat.animate(videoControlPanel)
                    .translationY(-videoControlPanel.getHeight())
                    .setDuration(800)
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

    private void togglePanel(ImageView settingsButton) {
        AppAnimationUtils.getInstance().rotateAnimation(settingsButton);
        if (showPanel) {
            hidePanel();
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

    private void toggleLockScreen() {
        AppAnimationUtils.getInstance().errorAnimation(videoLockScreen);
        lockScreen = !lockScreen;
        if (lockScreen) {
            videoLockScreen.setImageResource(R.drawable.icon_video_lock_screen);
        } else {
            videoLockScreen.setImageResource(R.drawable.icon_video_unlock_screen);
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
                            lockScreen = true;
                        }

                        @Override
                        public void onAnimationCancel(View view) {

                        }
                    }).start();
        }
    }

    public void hideControls() {
        TransitionManager.beginDelayedTransition(this);
        trackSelectionPanel.setVisibility(INVISIBLE);
        videoControlPanel.setVisibility(INVISIBLE);
        playPauseButton.setVisibility(INVISIBLE);
        seekVideo.setVisibility(INVISIBLE);
        videoTimeCount.setVisibility(INVISIBLE);
        videoTimeTotal.setVisibility(INVISIBLE);
        resizeModeState.setVisibility(INVISIBLE);
        videoName.setVisibility(INVISIBLE);
        videoLockScreen.setVisibility(INVISIBLE);
        addSubButton.setVisibility(INVISIBLE);
        settingsButton.setVisibility(INVISIBLE);
        pipButton.setVisibility(INVISIBLE);
        nextVideo.setVisibility(INVISIBLE);
        previousVideo.setVisibility(INVISIBLE);
        audioTrackView.setVisibility(INVISIBLE);
        videoTrackView.setVisibility(INVISIBLE);
        subTrackView.setVisibility(INVISIBLE);
    }

    public void showControls() {
        TransitionManager.beginDelayedTransition(this);
        trackSelectionPanel.setVisibility(VISIBLE);
        videoControlPanel.setVisibility(VISIBLE);
        playPauseButton.setVisibility(VISIBLE);
        seekVideo.setVisibility(VISIBLE);
        videoTimeCount.setVisibility(VISIBLE);
        videoTimeTotal.setVisibility(VISIBLE);
        resizeModeState.setVisibility(VISIBLE);
        videoName.setVisibility(VISIBLE);
        videoLockScreen.setVisibility(VISIBLE);
        addSubButton.setVisibility(VISIBLE);
        settingsButton.setVisibility(VISIBLE);
        pipButton.setVisibility(VISIBLE);
        nextVideo.setVisibility(VISIBLE);
        previousVideo.setVisibility(VISIBLE);
        audioTrackView.setVisibility(VISIBLE);
        videoTrackView.setVisibility(VISIBLE);
        subTrackView.setVisibility(VISIBLE);
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
                            lockScreen = false;
                            videoLockScreen.setImageResource(R.drawable.icon_video_unlock_screen);
                        }

                        @Override
                        public void onAnimationCancel(View view) {

                        }
                    }).start();
        }
    }

    public void disablePreviousVideoButton() {
        previousVideo.setAlpha(0.5f);
        previousVideo.setEnabled(false);
        previousVideo.setClickable(false);
    }

    public void enablePreviousVideoButton() {
        previousVideo.setAlpha(1f);
        previousVideo.setEnabled(true);
        previousVideo.setClickable(true);
    }

    public void enableNextVideoButton() {
        nextVideo.setAlpha(1f);
        nextVideo.setEnabled(true);
        nextVideo.setClickable(true);
    }

    public void disableNextVideoButton() {
        nextVideo.setAlpha(0.5f);
        nextVideo.setEnabled(false);
        nextVideo.setClickable(false);
    }

    public TextView getResizeModeState() {
        return resizeModeState;
    }

    public ImageView getVideoLockScreen() {
        return videoLockScreen;
    }

    public ImageView getAddSubButton() {
        return addSubButton;
    }

    public ImageView getSettingsButton() {
        return settingsButton;
    }

    public ImageView getNextVideo() {
        return nextVideo;
    }

    public ImageView getPreviousVideo() {
        return previousVideo;
    }

    public PlayPauseButton getPlayPauseButton() {
        return playPauseButton;
    }

    public void setPictureInPicture(boolean pictureInPicture) {
        isPictureInPicture = pictureInPicture;
    }
}
