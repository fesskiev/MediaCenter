package com.fesskiev.mediacenter.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.graphics.Palette;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.MediaFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import io.reactivex.Observable;

public class BitmapHelper {

    private static LoggingListener loggingListener = new LoggingListener();

    private static final int WIDTH = 140;
    private static final int HEIGHT = 140;

    private Context context;

    public BitmapHelper(Context context) {
        this.context = context;
    }

    public Observable<Bitmap> getCoverBitmapFromURL(String url) {
        return Observable.create(e -> {
            if (url != null) {
                Glide.with(context)
                        .load(url)
                        .asBitmap()
                        .override(WIDTH * 3, HEIGHT * 3)
                        .centerCrop()
                        .listener(loggingListener)
                        .error(R.drawable.no_cover_track_icon)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                e.onNext(resource);
                            }
                        });
            } else {
                Glide.with(context)
                        .load(R.drawable.no_cover_track_icon)
                        .asBitmap()
                        .override(WIDTH * 3, HEIGHT * 3)
                        .centerCrop()
                        .listener(loggingListener)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                e.onNext(resource);
                            }
                        });
            }
            e.onComplete();
        });
    }

    public Observable<Bitmap> loadBitmap(MediaFile mediaFile) {
        String path = findPath(mediaFile);
        return Observable.create(e -> {
            if (path != null) {
                Glide.with(context)
                        .load(path)
                        .asBitmap()
                        .override(WIDTH, HEIGHT)
                        .centerCrop()
                        .listener(loggingListener)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                e.onNext(resource);
                            }
                        });
            } else {
                Glide.with(context)
                        .load(R.drawable.no_cover_track_icon)
                        .asBitmap()
                        .override(WIDTH, HEIGHT)
                        .centerCrop()
                        .listener(loggingListener)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                e.onNext(resource);
                            }
                        });
            }
            e.onComplete();
        });

    }

    public Observable<Bitmap> loadAudioPlayerArtwork(AudioFile audioFile) {
        String mediaArtworkPath = findMediaFileArtworkPath(audioFile);
        return Observable.create(e -> {
            if (mediaArtworkPath != null) {
                Glide.with(context)
                        .load(mediaArtworkPath)
                        .asBitmap()
                        .override(WIDTH, HEIGHT)
                        .centerCrop()
                        .listener(loggingListener)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                e.onNext(resource);
                            }
                        });
            } else {
                Glide.with(context)
                        .load(R.drawable.no_cover_player)
                        .asBitmap()
                        .override(WIDTH, HEIGHT)
                        .centerCrop()
                        .listener(loggingListener)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                e.onNext(resource);
                            }
                        });
            }
            e.onComplete();
        });

    }

    public Observable<PaletteColor> getAudioFilePalette(AudioFile audioFile) {
        return Observable.create(e -> {
            String coverPath = findMediaFileArtworkPath(audioFile);
            if (coverPath != null) {
                e.onNext(new PaletteColor(context, Palette
                        .from(getBitmapFromPath(coverPath))
                        .generate()));
            } else {
                e.onNext(new PaletteColor(context, Palette
                        .from(getBitmapFromResource(R.drawable.no_cover_player))
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
                        .from(getBitmapFromPath(coverPath))
                        .generate()));
            } else {
                e.onNext(new PaletteColor(context, Palette
                        .from(getBitmapFromResource(R.drawable.no_cover_folder_icon))
                        .generate()));
            }
            e.onComplete();
        });
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

    public Observable<Bitmap> getTrackListArtwork(MediaFile mediaFile) {
        String mediaArtworkPath = findMediaFileArtworkPath(mediaFile);
        return Observable.create(e -> {
            if (mediaArtworkPath != null) {
                Glide.with(context)
                        .load(mediaArtworkPath)
                        .override(WIDTH, HEIGHT)
                        .crossFade()
                        .centerCrop()
                        .listener(loggingListener)
                        .error(R.drawable.no_cover_track_icon)
                        .transform(new CircleTransform(context))
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                e.onNext(resource);
                            }
                        });
            } else {
                Glide.with(context)
                        .load(R.drawable.no_cover_track_icon)
                        .override(WIDTH, HEIGHT)
                        .crossFade()
                        .fitCenter()
                        .listener(loggingListener)
                        .error(R.drawable.no_cover_track_icon)
                        .transform(new CircleTransform(context))
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                e.onNext(resource);
                            }
                        });
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

    public Observable<Bitmap> getAudioFolderArtwork(AudioFolder audioFolder) {
        return Observable.create(e -> {
            String coverFile = findAudioFolderArtworkPath(audioFolder);
            if (coverFile != null) {
                Glide.with(context)
                        .load(coverFile)
                        .override(WIDTH * 3, HEIGHT * 3)
                        .crossFade()
                        .centerCrop()
                        .listener(loggingListener)
                        .error(R.drawable.no_cover_folder_icon)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                e.onNext(resource);
                            }
                        });
            } else {
                Glide.with(context)
                        .load(R.drawable.no_cover_folder_icon)
                        .override(WIDTH * 3, HEIGHT * 3)
                        .crossFade()
                        .centerCrop()
                        .listener(loggingListener)
                        .error(R.drawable.no_cover_folder_icon)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                e.onNext(resource);
                            }
                        });
            }
            e.onComplete();
        });
    }

    public Observable<Bitmap> loadVideoFileFrame(String path) {
        return Observable.create(e -> {
            Glide.with(context)
                    .load(path)
                    .override(WIDTH * 3, HEIGHT * 3)
                    .crossFade()
                    .centerCrop()
                    .listener(loggingListener)
                    .error(R.drawable.no_cover_track_icon)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            e.onNext(resource);
                        }
                    });
            e.onComplete();
        });

    }

    public Observable<Bitmap> loadVideoFolderFrame(String path) {
        return Observable.create(e -> {
            Glide.with(context)
                    .load(path)
                    .override(WIDTH, HEIGHT)
                    .crossFade()
                    .centerCrop()
                    .listener(loggingListener)
                    .error(R.drawable.no_cover_folder_icon)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            e.onNext(resource);
                        }
                    });
            e.onComplete();
        });
    }


    private static class LoggingListener<T, R> implements RequestListener<T, R> {
        @Override
        public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
            Log.d(BitmapHelper.class.getSimpleName(), String.format(Locale.ROOT,
                    "onException(%s, %s, %s, %s)", e, model, target, isFirstResource), e);
            return false;
        }

        @Override
        public boolean onResourceReady(Object resource, Object model, Target target,
                                       boolean isFromMemoryCache, boolean isFirstResource) {
            return false;
        }
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
        Bitmap bitmap =
                BitmapFactory.decodeByteArray(data, 0, data.length, options);
        saveBitmap(bitmap, path);

    }

    public Bitmap getBitmapFromPath(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(path, options);
    }

    public Bitmap getBitmapFromResource(int resource) {
        return BitmapFactory.decodeResource(context.getResources(), resource);
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

}
