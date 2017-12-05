package com.fesskiev.mediacenter.utils;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.support.v7.graphics.Palette;
import android.util.LruCache;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.MediaFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BitmapHelper {

    private static final String KEY_NO_COVER_FOLDER_ICON = "KEY_FOLDER";
    private static final String KEY_NO_COVER_TRACK_ICON = "KEY_TRACK";
    private static final String KEY_NO_COVER_PLAYER_ICON = "KEY_PLAYER";

    private static final int WIDTH = 140 * 3;
    private static final int HEIGHT = 140 * 3;

    private Context context;
    private Resources resources;
    private LruCache<String, Bitmap> bitmapLruCache;
    private OkHttpClient okHttpClient;

    public BitmapHelper(Context context, OkHttpClient okHttpClient) {
        this.context = context;
        this.okHttpClient = okHttpClient;
        this.resources = context.getResources();

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 4;
        bitmapLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private Bitmap getNoCoverTrackBitmap() {
        Bitmap bitmap = getBitmapFromMemCache(KEY_NO_COVER_TRACK_ICON);
        if (bitmap == null) {
            Bitmap decodeBitmap = decodeBitmapFromResource(R.drawable.no_cover_track_icon);
            addBitmapToMemoryCache(KEY_NO_COVER_TRACK_ICON, decodeBitmap);
            return decodeBitmap;
        }
        return bitmap;
    }

    private Bitmap getNoCoverFolderBitmap() {
        Bitmap bitmap = getBitmapFromMemCache(KEY_NO_COVER_FOLDER_ICON);
        if (bitmap == null) {
            Bitmap decodeBitmap = decodeBitmapFromResource(R.drawable.no_cover_folder_icon);
            addBitmapToMemoryCache(KEY_NO_COVER_FOLDER_ICON, decodeBitmap);
            return decodeBitmap;
        }
        return bitmap;
    }

    private Bitmap getNoCoverAudioPlayer() {
        Bitmap bitmap = getBitmapFromMemCache(KEY_NO_COVER_PLAYER_ICON);
        if (bitmap == null) {
            Bitmap decodeBitmap = decodeBitmapFromResource(R.drawable.no_cover_player);
            addBitmapToMemoryCache(KEY_NO_COVER_PLAYER_ICON, decodeBitmap);
            return decodeBitmap;
        }
        return bitmap;
    }

    private Bitmap getBitmapFromPath(String path) {
        Bitmap bitmap = getBitmapFromMemCache(path);
        if (bitmap == null) {
            Bitmap decodeBitmap = decodeBitmapFromFile(path);
            addBitmapToMemoryCache(path, decodeBitmap);
            return decodeBitmap;
        }
        return bitmap;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            bitmapLruCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return bitmapLruCache.get(key);
    }

    public Observable<Bitmap> getCoverBitmapFromURL(String url) {
        return Observable.create(e -> {
            if (url != null) {
                Request request = new Request.Builder().url(url).build();
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException ex) {
                        e.onError(ex);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Bitmap bitmap = decodeBitmapFromInputStream(response.body().byteStream());
                        e.onNext(bitmap);
                    }
                });
            } else {
                Bitmap bitmap = getNoCoverTrackBitmap();
                e.onNext(bitmap);
            }
            e.onComplete();
        });
    }

    public Observable<Bitmap> loadBitmap(MediaFile mediaFile) {
        String path = findPath(mediaFile);
        return Observable.create(e -> {
            if (path != null) {
                Bitmap bitmap = getBitmapFromPath(path);
                e.onNext(bitmap);
            } else {
                Bitmap bitmap = getNoCoverTrackBitmap();
                e.onNext(bitmap);
            }
            e.onComplete();
        });

    }

    public Observable<Bitmap> loadAudioPlayerArtwork(AudioFile audioFile) {
        String mediaArtworkPath = findMediaFileArtworkPath(audioFile);
        return Observable.create(e -> {
            if (mediaArtworkPath != null) {
                Bitmap bitmap = getBitmapFromPath(mediaArtworkPath);
                e.onNext(bitmap);
            } else {
                Bitmap bitmap = getNoCoverAudioPlayer();
                e.onNext(bitmap);
            }
            e.onComplete();
        });

    }

    public Observable<Bitmap> getTrackListArtwork(MediaFile mediaFile) {
        String mediaArtworkPath = findMediaFileArtworkPath(mediaFile);
        return Observable.create(e -> {
            if (mediaArtworkPath != null) {
                Bitmap bitmap = getCircularBitmap(getBitmapFromPath(mediaArtworkPath));
                e.onNext(bitmap);
            } else {
                Bitmap bitmap = getCircularBitmap(getNoCoverTrackBitmap());
                e.onNext(bitmap);
            }
            e.onComplete();
        });
    }

    public Observable<Bitmap> getAudioFolderArtwork(AudioFolder audioFolder) {
        return Observable.create(e -> {
            String coverFile = findAudioFolderArtworkPath(audioFolder);
            if (coverFile != null) {
                Bitmap bitmap = getBitmapFromPath(coverFile);
                e.onNext(bitmap);
            } else {
                Bitmap bitmap = getNoCoverFolderBitmap();
                e.onNext(bitmap);
            }
            e.onComplete();
        });
    }

    public Observable<Bitmap> loadVideoFileFrame(String path) {
        return Observable.create(e -> {
            if (path != null) {
                Bitmap bitmap = getBitmapFromPath(path);
                e.onNext(bitmap);
            } else {
                Bitmap bitmap = getNoCoverTrackBitmap();
                e.onNext(bitmap);
            }
            e.onComplete();
        });

    }

    public Observable<Bitmap> loadVideoFolderFrame(String path) {
        return Observable.create(e -> {
            if (path != null) {
                Bitmap bitmap = getBitmapFromPath(path);
                e.onNext(bitmap);
            } else {
                Bitmap bitmap = getNoCoverFolderBitmap();
                e.onNext(bitmap);
            }
            e.onComplete();
        });
    }

    private String findMediaFileArtworkPath(MediaFile mediaFile) {
        if (mediaFile != null) {
            String artworkPath = mediaFile.getArtworkPath();
            if (artworkPath != null) {
                return artworkPath;
            }
        }
        return null;
    }

    private String findAudioFolderArtworkPath(AudioFolder audioFolder) {
        File artworkFile = audioFolder.folderImage;
        if (artworkFile != null) {
            return artworkFile.getAbsolutePath();
        }
        return null;
    }

    private String findPath(MediaFile mediaFile) {
        if (mediaFile != null) {
            String mediaArtworkPath = findMediaFileArtworkPath(mediaFile);
            if (mediaArtworkPath != null) {
                return mediaArtworkPath;
            }
        }
        return null;
    }

    public static void saveBitmap(Bitmap bitmap, File path) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveBitmap(byte[] data, File path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        saveBitmap(bitmap, path);

    }

    public Observable<PaletteColor> getAudioFilePalette(AudioFile audioFile) {
        return Observable.create(e -> {
            String coverPath = findMediaFileArtworkPath(audioFile);
            if (coverPath != null) {
                e.onNext(new PaletteColor(context, Palette
                        .from(decodeBitmapFromFile(coverPath))
                        .generate()));
            } else {
                Bitmap bitmap = getNoCoverAudioPlayer();
                e.onNext(new PaletteColor(context, Palette
                        .from(bitmap)
                        .generate()));
            }
            e.onComplete();
        });
    }

    public Observable<PaletteColor> getAudioFolderPalette(AudioFolder audioFolder) {
        return Observable.create(e -> {
            String coverPath = findAudioFolderArtworkPath(audioFolder);
            if (coverPath != null) {
                e.onNext(new PaletteColor(context, Palette
                        .from(decodeBitmapFromFile(coverPath))
                        .generate()));
            } else {
                Bitmap bitmap = getNoCoverFolderBitmap();
                e.onNext(new PaletteColor(context, Palette
                        .from(bitmap)
                        .generate()));
            }
            e.onComplete();
        });
    }

    public Bitmap decodeBitmapFromInputStream(InputStream inputStream) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);

        options.inSampleSize = calculateInSampleSize(options, WIDTH, HEIGHT);

        options.inMutable = true;
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeStream(inputStream, null, options);
    }

    public Bitmap decodeBitmapFromResource(int drawable) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, drawable, options);

        options.inSampleSize = calculateInSampleSize(options, WIDTH, HEIGHT);

        options.inMutable = true;
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(resources, drawable, options);
    }

    public static Bitmap decodeBitmapFromFile(String path) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, WIDTH, HEIGHT);

        options.inMutable = true;
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            long totalPixels = width * height / inSampleSize;

            // Anything more than 2x the requested pixels we'll sample down further
            final long totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels > totalReqPixelsCap) {
                inSampleSize *= 2;
                totalPixels /= 2;
            }
        }
        return inSampleSize;
    }


    public class PaletteColor {

        private int vibrant;
        private int vibrantLight;
        private int vibrantDark;
        private int muted;
        private int mutedLight;
        private int mutedDark;

        public PaletteColor(Context context, Palette palette) {
            int defaultValue = context.getResources().getColor(R.color.secondary_text);
            vibrant = palette.getVibrantColor(defaultValue);
            vibrantLight = palette.getLightVibrantColor(defaultValue);
            vibrantDark = palette.getDarkVibrantColor(defaultValue);
            muted = palette.getMutedColor(defaultValue);
            mutedLight = palette.getLightMutedColor(defaultValue);
            mutedDark = palette.getDarkMutedColor(defaultValue);
        }

        public int getVibrant() {
            return vibrant;
        }

        public int getVibrantLight() {
            return vibrantLight;
        }

        public int getVibrantDark() {
            return vibrantDark;
        }

        public int getMuted() {
            return muted;
        }

        public int getMutedLight() {
            return mutedLight;
        }

        public int getMutedDark() {
            return mutedDark;
        }

        @Override
        public String toString() {
            return "PaletteColor{" +
                    "vibrant=" + vibrant +
                    ", vibrantLight=" + vibrantLight +
                    ", vibrantDark=" + vibrantDark +
                    ", muted=" + muted +
                    ", mutedLight=" + mutedLight +
                    ", mutedDark=" + mutedDark +
                    '}';
        }
    }

    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final Path path = new Path();
        path.addCircle((float) (width / 2), (float) (height / 2), (float) Math.min(width, (height / 2)), Path.Direction.CCW);

        final Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return outputBitmap;
    }

    public void clearCache() {
        bitmapLruCache.evictAll();
    }
}
