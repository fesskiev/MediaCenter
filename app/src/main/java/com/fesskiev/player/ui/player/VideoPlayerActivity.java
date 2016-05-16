package com.fesskiev.player.ui.player;


import android.animation.TimeAnimator;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.analytics.AnalyticsActivity;
import com.fesskiev.player.model.VideoFile;
import com.fesskiev.player.ui.gl.VideoGLSurfaceView;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public class VideoPlayerActivity extends AnalyticsActivity implements SurfaceHolder.Callback {


    private VideoGLSurfaceView glSurfaceView;
    private VideoFile videoFile;

    private MediaCodec.CryptoInfo cryptoInfo;
    private MediaExtractor mediaExtractor;
    private MediaCodec mediaCodec;
    private TimeAnimator timeAnimator;
    private ByteBuffer[] inputBuffers;
    private ByteBuffer[] outputBuffers;
    private Queue<Integer> availableInputBuffers;
    private Queue<Integer> availableOutputBuffers;
    private MediaCodec.BufferInfo[] outputBufferInfo;

    private static final String TAG = VideoPlayerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        videoFile = MediaApplication.getInstance().getVideoPlayer().currentVideoFile;
        glSurfaceView = (VideoGLSurfaceView) findViewById(R.id.videoGLSurfaceView);
        glSurfaceView.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        startPlayback();
        timeAnimator = new TimeAnimator();
        timeAnimator.setTimeListener(new TimeAnimator.TimeListener() {

            @Override
            public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {

                boolean isEos = ((mediaExtractor.getSampleFlags() & MediaCodec
                        .BUFFER_FLAG_END_OF_STREAM) == MediaCodec.BUFFER_FLAG_END_OF_STREAM);

                if (!isEos) {
                    boolean result = writeSample(false,
                            mediaExtractor.getSampleTime(), mediaExtractor.getSampleFlags());
                    if (result) {
                        mediaExtractor.advance();
                    }
                }

                MediaCodec.BufferInfo out_bufferInfo = new MediaCodec.BufferInfo();
                peekSample(out_bufferInfo);

                if (out_bufferInfo.size <= 0 && isEos) {
                    timeAnimator.end();
                    stopAndRelease();
                    mediaExtractor.release();
                } else if (out_bufferInfo.presentationTimeUs / 1000 < totalTime) {
                    // Pop the sample off the queue and send it to {@link Surface}
                    popSample(true);
                }

            }
        });
        timeAnimator.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public boolean peekSample(MediaCodec.BufferInfo out_bufferInfo) {
        update();
        boolean result = false;
        if (!availableOutputBuffers.isEmpty()) {
            int index = availableOutputBuffers.peek();
            MediaCodec.BufferInfo info = outputBufferInfo[index];
            // metadata of the sample
            out_bufferInfo.set(
                    info.offset,
                    info.size,
                    info.presentationTimeUs,
                    info.flags);
            result = true;
        }
        return result;
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }

    public void popSample(boolean render) {
        update();
        if (!availableOutputBuffers.isEmpty()) {
            int index = availableOutputBuffers.remove();

            if (render) {
                ByteBuffer buffer = outputBuffers[index];
                MediaCodec.BufferInfo info = outputBufferInfo[index];
                Log.d(TAG, "render pop sample: " + info.presentationTimeUs / 1000000);
            }

            mediaCodec.releaseOutputBuffer(index, render);
        }
    }


    public boolean writeSample(final boolean isSecure, final long presentationTimeUs, int flags) {
        boolean result = false;

        if (!availableInputBuffers.isEmpty()) {
            int index = availableInputBuffers.remove();
            ByteBuffer buffer = inputBuffers[index];

            int size = mediaExtractor.readSampleData(buffer, 0);
            if (size <= 0) {
                flags |= MediaCodec.BUFFER_FLAG_END_OF_STREAM;
            }

            if (!isSecure) {
                mediaCodec.queueInputBuffer(index, 0, size, presentationTimeUs, flags);
            } else {
                mediaExtractor.getSampleCryptoInfo(cryptoInfo);
                mediaCodec.queueSecureInputBuffer(index, 0, cryptoInfo, presentationTimeUs, flags);
            }

            result = true;
        }

        return result;
    }

    private void update() {

        int index;
        // Get valid input buffers from the codec to fill later in the same order they were
        // made available by the codec.
        while ((index = mediaCodec.dequeueInputBuffer(0)) != MediaCodec.INFO_TRY_AGAIN_LATER) {
            availableInputBuffers.add(index);
        }

        // Likewise with output buffers. If the output buffers have changed, start using the
        // new set of output buffers. If the output format has changed, notify listeners.
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        while ((index = mediaCodec.dequeueOutputBuffer(info, 0)) != MediaCodec.INFO_TRY_AGAIN_LATER) {
            switch (index) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    outputBuffers = mediaCodec.getOutputBuffers();
                    outputBufferInfo = new MediaCodec.BufferInfo[outputBuffers.length];
                    availableOutputBuffers.clear();
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.d(TAG, "output format changed: " + mediaCodec.getOutputFormat().toString());
                    break;
                default:
                    if (index >= 0) {
                        outputBufferInfo[index] = info;
                        availableOutputBuffers.add(index);
                    } else {
                        throw new IllegalStateException("Unknown status from dequeueOutputBuffer");
                    }
                    break;
            }
        }
    }

    private void startPlayback() {
        cryptoInfo = new MediaCodec.CryptoInfo();
        mediaExtractor = new MediaExtractor();

        try {
            mediaExtractor.setDataSource(videoFile.filePath);
            int tracks = mediaExtractor.getTrackCount();

            Log.d(TAG, "tracks: " + tracks);

            for (int i = 0; i < tracks; i++) {
                MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
                final String mimeType = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.contains("video/")) {

                    mediaCodec = MediaCodec.createDecoderByType(mimeType);
                    mediaCodec.configure(mediaFormat,
                            new Surface(glSurfaceView.getSurfaceTexture()), null, 0);
                    mediaCodec.start();

                    inputBuffers = mediaCodec.getInputBuffers();
                    outputBuffers = mediaCodec.getOutputBuffers();
                    outputBufferInfo = new MediaCodec.BufferInfo[outputBuffers.length];
                    availableInputBuffers = new ArrayDeque<>(outputBuffers.length);
                    availableOutputBuffers = new ArrayDeque<>(inputBuffers.length);

                    if (mediaCodec != null) {
                        mediaExtractor.selectTrack(i);
                    }
                    Log.d(TAG, "contains video");
                } else if (mimeType.contains("audio/")) {
                    Log.d(TAG, "contains audio");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopAndRelease() {
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaExtractor.release();
            mediaExtractor.release();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timeAnimator != null && timeAnimator.isRunning()) {
            timeAnimator.end();
        }
        stopAndRelease();
    }

}
