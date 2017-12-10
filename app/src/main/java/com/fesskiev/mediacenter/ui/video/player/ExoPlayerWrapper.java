package com.fesskiev.mediacenter.ui.video.player;


import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.fesskiev.mediacenter.R;
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
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

public class ExoPlayerWrapper implements Player.EventListener {

    public interface OnErrorListener {
        void onError(String message);
    }

    public interface OnPlaybackStateChangedListener {
        void onPlaybackStateChanged(int playbackState);
    }

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private static final int DEFAULT_MIN_BUFFER_MS = 3000;
    private static final int DEFAULT_MAX_BUFFER_MS = 5000;
    private static final int DEFAULT_BUFFER_FOR_PLAYBACK_MS = 1000;
    private static final int DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 2000;

    private Context context;
    private OnErrorListener errorListener;
    private OnPlaybackStateChangedListener playbackStateChangedListener;

    private SimpleExoPlayer exoPlayer;

    private DataSource.Factory mediaDataSourceFactory;
    private TrackSelection.Factory trackSelectionFactory;
    private EventLogger eventLogger;
    private DefaultTrackSelector trackSelector;

    private boolean shouldAutoPlay;

    public ExoPlayerWrapper(Context context, boolean shouldAutoPlay) {
        this.context = context;
        this.shouldAutoPlay = shouldAutoPlay;
        mediaDataSourceFactory = buildDataSourceFactory(true);
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(context, bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
    }

    private HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(context, "ExoPlayer"),
                bandwidthMeter);
    }

    public SimpleExoPlayer initializePlayer(Uri uri, String subtitlePath) {
        trackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);

        trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        trackSelector.setTunnelingAudioSessionId(C.generateAudioSessionIdV21(context));

        exoPlayer = ExoPlayerFactory.newSimpleInstance(
                new CustomRenderersFactory(context),
                trackSelector,
                new DefaultLoadControl(
                        new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE),
                        DEFAULT_MIN_BUFFER_MS,
                        DEFAULT_MAX_BUFFER_MS,
                        DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                        DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
                )
        );

        exoPlayer.addListener(this);
        eventLogger = new EventLogger(trackSelector);
        exoPlayer.addListener(eventLogger);
        exoPlayer.setAudioDebugListener(eventLogger);
        exoPlayer.setVideoDebugListener(eventLogger);
        exoPlayer.setPlayWhenReady(shouldAutoPlay);

        MediaSource videoSource = buildMediaSource(uri);
        if (subtitlePath != null) {
            Format textFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, null, Format.NO_VALUE,
                    Format.NO_VALUE, "external", null, 0);
            MediaSource textMediaSource = new SingleSampleMediaSource(Uri.fromFile(new File(subtitlePath)),
                    mediaDataSourceFactory, textFormat, C.TIME_UNSET);

            MediaSource mergedSource = new MergingMediaSource(videoSource, textMediaSource);

            exoPlayer.prepare(mergedSource);
        } else {
            exoPlayer.prepare(videoSource);
        }
        return exoPlayer;
    }

    public void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
            trackSelector = null;
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackStateChangedListener != null) {
            playbackStateChangedListener.onPlaybackStateChanged(playbackState);
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        int type = Util.inferContentType(uri.getLastPathSegment());
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), new Handler(), eventLogger);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), new Handler(), eventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, new Handler(), eventLogger);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        new Handler(), eventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }


    @Override
    public void onRepeatModeChanged(int repeatMode) {

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
                        errorString = context.getString(R.string.error_querying_decoders);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = context.getString(R.string.error_no_secure_decoder,
                                decoderInitializationException.mimeType);
                    } else {
                        errorString = context.getString(R.string.error_no_decoder,
                                decoderInitializationException.mimeType);
                    }
                } else {
                    errorString = context.getString(R.string.error_instantiating_decoder,
                            decoderInitializationException.decoderName);
                }
            }
        }
        if (errorString != null && errorListener != null) {
            errorListener.onError(errorString);
        }
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    public void setErrorListener(OnErrorListener listener) {
        this.errorListener = listener;
    }

    public void setPlaybackStateChangedListener(OnPlaybackStateChangedListener listener) {
        this.playbackStateChangedListener = listener;
    }

    public void seekTo(long seek) {
        exoPlayer.seekTo(seek);
    }

    public void setPlayWhenReady(boolean isPlaying) {
        exoPlayer.setPlayWhenReady(isPlaying);
    }

    public long getDuration() {
        return exoPlayer.getDuration();
    }

    public long getCurrentPosition() {
        return exoPlayer.getCurrentPosition();
    }

    public boolean getPlayWhenReady() {
        return exoPlayer.getPlayWhenReady();
    }

    public TrackSelection.Factory getTrackSelectionFactory() {
        return trackSelectionFactory;
    }

    public DefaultTrackSelector getTrackSelector() {
        return trackSelector;
    }

    public SimpleExoPlayer getExoPlayer() {
        return exoPlayer;
    }
}
