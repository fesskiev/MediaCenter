package com.fesskiev.player.utils.media;


import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import com.fesskiev.player.utils.Utils;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ExtractMediaInfo {

    private static final String TAG = ExtractMediaInfo.class.getSimpleName();

    private static final int NUM_FRAME_DECODED = 100;
    private static final int MAX_NUM_IMAGES = 1;

    private static final long DEFAULT_TIMEOUT_US = 10000;
    private static final long WAIT_FOR_IMAGE_TIMEOUT_MS = 1000;

    private Surface mReaderSurface;
    private ImageListener mImageListener;
    private ImageReader mReader;
    private HandlerThread mHandlerThread;
    private Handler mHandler;


    public class MediaInfo {

        public Bitmap frame;
        public String description;

        @Override
        public String toString() {
            return "MediaInfo{" +
                    "frame=" + frame +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

    public ExtractMediaInfo() {
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mImageListener = new ImageListener();
    }

    private static class ImageListener implements ImageReader.OnImageAvailableListener {
        private final LinkedBlockingQueue<Image> queue = new LinkedBlockingQueue<>();

        @Override
        public void onImageAvailable(ImageReader reader) {
            try {
                queue.put(reader.acquireNextImage());
            } catch (InterruptedException e) {
                throw new UnsupportedOperationException(
                        "Can't handle InterruptedException in onImageAvailable");
            }
        }

        public Image getImage(long timeout) throws InterruptedException {
            return queue.poll(timeout, TimeUnit.MILLISECONDS);
        }
    }


    public MediaInfo extractFileInfo(File filePath) {
        MediaInfo mediaInfo = new MediaInfo();
        MediaExtractor extractor = null;
        MediaCodec decoder = null;

        try {

            extractor = new MediaExtractor();
            extractor.setDataSource(filePath.toString());
            int trackIndex = selectTrack(extractor);
            if (trackIndex < 0) {
                throw new RuntimeException("No video track found in " + filePath);
            }
            extractor.selectTrack(trackIndex);

            MediaFormat format = extractor.getTrackFormat(trackIndex);
            String videoSize = "Video size is " + format.getInteger(MediaFormat.KEY_WIDTH) + "x" +
                    format.getInteger(MediaFormat.KEY_HEIGHT);
            Log.d(TAG, videoSize);

            mediaInfo.description = videoSize;

            String mime = format.getString(MediaFormat.KEY_MIME);
            decoder = MediaCodec.createDecoderByType(mime);

            decodeFramesToImageReader(480, 360, ImageFormat.YUV_420_888, decoder, extractor, format, mediaInfo);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (decoder != null) {
                decoder.stop();
                decoder.release();
            }
            if (extractor != null) {
                extractor.release();
            }
        }

        return mediaInfo;
    }


    /**
     * Decode video frames to image reader.
     */
    private void decodeFramesToImageReader(int width, int height, int imageFormat,
                                           MediaCodec decoder, MediaExtractor extractor, MediaFormat mediaFmt, MediaInfo mediaInfo) throws Exception {
        ByteBuffer[] decoderInputBuffers;

        createImageReader(width, height, imageFormat, MAX_NUM_IMAGES, mImageListener);

        decoder.configure(mediaFmt, mReaderSurface, null, 0);
        decoder.start();
        decoderInputBuffers = decoder.getInputBuffers();
        extractor.selectTrack(0);
        // Start decoding and get Image, only test the first NUM_FRAME_DECODED frames.
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        boolean sawInputEOS = false;
        boolean sawOutputEOS = false;
        int outputFrameCount = 0;
        while (!sawOutputEOS && outputFrameCount < NUM_FRAME_DECODED) {
            Log.v(TAG, "loop:" + outputFrameCount);
            if (!sawInputEOS) {
                int inputBufIndex = decoder.dequeueInputBuffer(DEFAULT_TIMEOUT_US);
                if (inputBufIndex >= 0) {
                    ByteBuffer dstBuf = decoderInputBuffers[inputBufIndex];
                    int sampleSize =
                            extractor.readSampleData(dstBuf, 0 /* offset */);
                    Log.v(TAG, "queue a input buffer, idx/size: "
                            + inputBufIndex + "/" + sampleSize);
                    long presentationTimeUs = 0;
                    if (sampleSize < 0) {
                        Log.v(TAG, "saw input EOS.");
                        sawInputEOS = true;
                        sampleSize = 0;
                    } else {
                        presentationTimeUs = extractor.getSampleTime();
                    }
                    decoder.queueInputBuffer(
                            inputBufIndex,
                            0 /* offset */,
                            sampleSize,
                            presentationTimeUs,
                            sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
                    if (!sawInputEOS) {
                        extractor.advance();
                    }
                }
            }
            // Get output frame
            int res = decoder.dequeueOutputBuffer(info, DEFAULT_TIMEOUT_US);
            Log.v(TAG, "got a buffer: " + info.size + "/" + res);
            if (res == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                Log.v(TAG, "no output frame available");
            } else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // decoder output buffers changed, need update.
                Log.v(TAG, "decoder output buffers changed");
            } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // this happens before the first frame is returned.
                MediaFormat outFormat = decoder.getOutputFormat();
                Log.v(TAG, "decoder output format changed: " + outFormat);
            } else if (res < 0) {
                // Should be decoding error.
                Log.v(TAG, "unexpected result from deocder.dequeueOutputBuffer: " + res);
            } else {
                // res >= 0: normal decoding case, copy the output buffer.
                // Will use it as reference to valid the ImageReader output
                // Some decoders output a 0-sized buffer at the end. Ignore those.
                outputFrameCount++;
                boolean doRender = (info.size != 0);
                decoder.releaseOutputBuffer(res, doRender);
                if (doRender) {
                    // Read image and verify
                    Image image = mImageListener.getImage(WAIT_FOR_IMAGE_TIMEOUT_MS);
                    Image.Plane[] imagePlanes = image.getPlanes();

                    byte[] data = getDataFromImage(image);
                    Log.e(TAG, "Data is null: " + (data == null));

                    mediaInfo.frame = Utils.getBitmap(data);

                    Log.v(TAG, "Image " + outputFrameCount + " Info:");
                    Log.v(TAG, "first plane pixelstride " + imagePlanes[0].getPixelStride());
                    Log.v(TAG, "first plane rowstride " + imagePlanes[0].getRowStride());
                    Log.v(TAG, "Image timestamp:" + image.getTimestamp());
                    image.close();
                }
            }
        }
    }

    private void createImageReader(int width, int height, int format, int maxNumImages,
                                   ImageReader.OnImageAvailableListener listener) throws Exception {
        closeImageReader();
        mReader = ImageReader.newInstance(width, height, format, maxNumImages);
        mReaderSurface = mReader.getSurface();
        mReader.setOnImageAvailableListener(listener, mHandler);
        Log.v(TAG, String.format("Created ImageReader size (%dx%d), format %d", width, height,
                format));
    }

    private static byte[] getDataFromImage(Image image) {
        int format = image.getFormat();
        int width = image.getWidth();
        int height = image.getHeight();
        int rowStride, pixelStride;
        byte[] data;
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer;
        int offset = 0;
        data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        Log.v(TAG, "get data from " + planes.length + " planes");
        for (int i = 0; i < planes.length; i++) {
            buffer = planes[i].getBuffer();

            rowStride = planes[i].getRowStride();
            pixelStride = planes[i].getPixelStride();

            Log.v(TAG, "pixelStride " + pixelStride);
            Log.v(TAG, "rowStride " + rowStride);
            Log.v(TAG, "width " + width);
            Log.v(TAG, "height " + height);

            // For multi-planar yuv images, assuming yuv420 with 2x2 chroma subsampling.
            int w = (i == 0) ? width : width / 2;
            int h = (i == 0) ? height : height / 2;
            for (int row = 0; row < h; row++) {
                int bytesPerPixel = ImageFormat.getBitsPerPixel(format) / 8;
                if (pixelStride == bytesPerPixel) {
                    // Special case: optimized read of the entire row
                    int length = w * bytesPerPixel;
                    buffer.get(data, offset, length);
                    // Advance buffer the remainder of the row stride
                    buffer.position(buffer.position() + rowStride - length);
                    offset += length;
                } else {
                    // Generic case: should work for any pixelStride but slower.
                    // Use intermediate buffer to avoid read byte-by-byte from
                    // DirectByteBuffer, which is very bad for performance
                    buffer.get(rowData, 0, rowStride);
                    for (int col = 0; col < w; col++) {
                        data[offset++] = rowData[col * pixelStride];
                    }
                }
            }
            Log.v(TAG, "Finished reading data from plane " + i);
        }
        return data;
    }


    private int selectTrack(MediaExtractor extractor) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                Log.d(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                return i;
            }
        }
        return -1;
    }

    private void closeImageReader() {
        if (mReader != null) {
            try {
                // Close all possible pending images first.
                Image image = mReader.acquireLatestImage();
                if (image != null) {
                    image.close();
                }
            } finally {
                mReader.close();
                mReader = null;
            }
        }
    }

}
