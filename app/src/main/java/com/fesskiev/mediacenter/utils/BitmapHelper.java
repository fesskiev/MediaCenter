package com.fesskiev.mediacenter.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.Artist;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.Genre;
import com.fesskiev.mediacenter.data.model.MediaFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class BitmapHelper {

    private static LoggingListener loggingListener = new LoggingListener();

    public interface OnBitmapLoadListener {
        void onLoaded(Bitmap bitmap);

        void onFailed();
    }

    private static final int WIDTH = 140;
    private static final int HEIGHT = 140;

    private static BitmapHelper INSTANCE;
    private Context context;

    public static BitmapHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BitmapHelper();
        }
        return INSTANCE;
    }

    private BitmapHelper() {
        context = MediaApplication.getInstance().getApplicationContext();
    }

    public void loadURLBitmap(String url, final OnBitmapLoadListener listener) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .override(WIDTH * 3, HEIGHT * 3)
                .centerCrop()
                .listener(loggingListener)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        if (listener != null) {
                            listener.onLoaded(resource);
                        }
                    }
                });
    }


    public void loadBitmap(MediaFile mediaFile, AudioFolder audioFolder, final OnBitmapLoadListener listener) {
        String path = findPath(mediaFile, audioFolder);
        if (path != null) {
            Glide.with(context)
                    .load(path)
                    .asBitmap()
                    .override(WIDTH, HEIGHT)
                    .centerCrop()
                    .listener(loggingListener)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            if (listener != null) {
                                listener.onLoaded(resource);
                            }
                        }
                    });
        } else {
            Glide.with(context)
                    .load(R.drawable.no_cover_track_icon)
                    .asBitmap()
                    .override(WIDTH, HEIGHT)
                    .centerCrop()
                    .listener(loggingListener)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            if (listener != null) {
                                listener.onLoaded(resource);
                            }
                        }
                    });
        }
    }

    public boolean loadAudioPlayerArtwork(AudioFolder audioFolder, AudioFile audioFile, ImageView imageView) {
        if (audioFile != null) {
            String mediaArtworkPath = findMediaFileArtworkPath(audioFile);
            if (mediaArtworkPath != null) {
                Glide.with(context)
                        .load(mediaArtworkPath)
                        .override(WIDTH * 3, HEIGHT * 3)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(imageView);
                return true;
            }
        }

        if (audioFolder != null) {
            String folderPath = findAudioFolderArtworkPath(audioFolder);
            if (folderPath != null) {
                Glide.with(context)
                        .load(folderPath)
                        .override(WIDTH * 3, HEIGHT * 3)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(imageView);
                return true;
            }
        }

        Glide.with(context)
                .load(R.drawable.no_cover_player)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(imageView);

        return false;
    }

    private String findPath(MediaFile mediaFile, AudioFolder audioFolder) {
        if (mediaFile != null) {
            String mediaArtworkPath = findMediaFileArtworkPath(mediaFile);
            if (mediaArtworkPath != null) {
                return mediaArtworkPath;
            }
        }

        if (audioFolder != null) {
            String folderPath = findAudioFolderArtworkPath(audioFolder);
            if (folderPath != null) {
                return folderPath;
            }
        }
        return null;
    }

    public void loadTrackListArtwork(MediaFile mediaFile, AudioFolder audioFolder, ImageView imageView) {

        String mediaArtworkPath = findMediaFileArtworkPath(mediaFile);
        if (mediaArtworkPath != null) {
            Glide.with(context)
                    .load(mediaArtworkPath)
                    .override(WIDTH, HEIGHT)
                    .crossFade()
                    .centerCrop()
                    .listener(loggingListener)
                    .error(R.drawable.no_cover_track_icon)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .transform(new CircleTransform(context))
                    .into(imageView);
            return;
        }

        if (audioFolder != null) {
            String folderPath = findAudioFolderArtworkPath(audioFolder);
            if (folderPath != null) {
                Glide.with(context)
                        .load(folderPath)
                        .override(WIDTH, HEIGHT)
                        .crossFade()
                        .centerCrop()
                        .listener(loggingListener)
                        .error(R.drawable.no_cover_track_icon)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .transform(new CircleTransform(context))
                        .into(imageView);
                return;
            }
        }

        if (mediaFile instanceof AudioFile) {
            Glide.with(context)
                    .load(R.drawable.no_cover_track_icon)
                    .override(WIDTH, HEIGHT)
                    .crossFade()
                    .fitCenter()
                    .listener(loggingListener)
                    .error(R.drawable.no_cover_track_icon)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .transform(new CircleTransform(context))
                    .into(imageView);
        }
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


    public void loadAudioArtistsFolderArtwork(Artist artist, ImageView placeholder) {

        String path = artist.artworkPath;
        if (path != null) {
            Glide.with(context)
                    .load(path)
                    .override(WIDTH * 3, HEIGHT * 3)
                    .crossFade()
                    .centerCrop()
                    .listener(loggingListener)
                    .error(R.drawable.no_cover_folder_icon)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(placeholder);
        } else {
            Glide.with(context)
                    .load(R.drawable.no_cover_folder_icon)
                    .override(WIDTH * 3, HEIGHT * 3)
                    .crossFade()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(placeholder);
        }

    }

    public void loadAudioGenresFolderArtwork(Genre genre, ImageView placeholder) {

        String path = genre.artworkPath;
        if (path != null) {
            Glide.with(context)
                    .load(path)
                    .override(WIDTH * 3, HEIGHT * 3)
                    .crossFade()
                    .centerCrop()
                    .listener(loggingListener)
                    .error(R.drawable.no_cover_folder_icon)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(placeholder);
        } else {
            Glide.with(context)
                    .load(R.drawable.no_cover_folder_icon)
                    .override(WIDTH * 3, HEIGHT * 3)
                    .crossFade()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(placeholder);
        }
    }

    public void loadAudioFolderArtwork(AudioFolder audioFolder, ImageView placeholder) {

        String coverFile = findAudioFolderArtworkPath(audioFolder);
        if (coverFile != null) {
            Glide.with(context)
                    .load(coverFile)
                    .override(WIDTH * 3, HEIGHT * 3)
                    .crossFade()
                    .centerCrop()
                    .listener(loggingListener)
                    .error(R.drawable.no_cover_folder_icon)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(placeholder);
        } else {
            Glide.with(context)
                    .load(R.drawable.no_cover_folder_icon)
                    .override(WIDTH * 3, HEIGHT * 3)
                    .crossFade()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(placeholder);
        }
    }

    public void loadVideoFileCover(String path, ImageView placeholder) {
        Glide.with(context)
                .load(path)
                .override(WIDTH * 3, HEIGHT * 3)
                .crossFade()
                .centerCrop()
                .listener(loggingListener)
                .error(R.drawable.no_cover_track_icon)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(placeholder);
    }

    public void loadVideoFolderFrame(String path, ImageView placeholder) {
        Glide.with(context)
                .load(path)
                .override(WIDTH, HEIGHT)
                .crossFade()
                .centerCrop()
                .listener(loggingListener)
                .error(R.drawable.no_cover_folder_icon)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(placeholder);
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

    public void saveBitmapPng(Bitmap bitmap, File path) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
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

    public void saveBitmap(Bitmap bitmap, File path) {
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

    public void saveBitmap(byte[] data, File path) {
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

}
