package com.fesskiev.mediacenter.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
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


    public void loadBitmap(MediaFile mediaFile, AudioFolder audioFolder, final OnBitmapLoadListener listener){
        String path = findPath(mediaFile, audioFolder);
        if (path != null) {
            Glide.with(context)
                    .load(path)
                    .asBitmap()
                    .override(WIDTH, HEIGHT)
                    .centerCrop()
                    .listener(loggingListener)
                    .error(R.drawable.icon_error_load_cover)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
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
                    .error(R.drawable.icon_error_load_cover)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
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


    public void loadURLAvatar(String url, final ImageView into, final OnBitmapLoadListener listener) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .centerCrop()
                .into(new BitmapImageViewTarget(into) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);

                        into.setImageDrawable(circularBitmapDrawable);
                        if (listener != null) {
                            listener.onLoaded(resource);
                        }
                    }
                });
    }

    public void loadBitmapAvatar(Bitmap bitmap, final ImageView into) {
        RoundedBitmapDrawable circularBitmapDrawable =
                RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
        circularBitmapDrawable.setCircular(true);
        into.setImageDrawable(circularBitmapDrawable);
    }

    public void loadEmptyAvatar(final ImageView into) {
        Glide.with(context)
                .load(R.drawable.icon_no_avatar)
                .asBitmap()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new BitmapImageViewTarget(into) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);

                        into.setImageDrawable(circularBitmapDrawable);

                    }
                });
    }

    public void loadURIBitmap(String uri, ImageView into) {
        Glide.with(context)
                .load(uri)
                .fitCenter()
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(into);
    }

    public void loadCircleURIBitmap(String uri, ImageView into) {
        Glide.with(context)
                .load(uri)
                .fitCenter()
                .transform(new CircleTransform(context))
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(into);
    }


    public boolean loadAudioPlayerArtwork(AudioFolder audioFolder, AudioFile audioFile, ImageView imageView) {
        if (audioFile != null) {
            String mediaArtworkPath = findMediaFileArtworkPath(audioFile);
            if (mediaArtworkPath != null) {
                Glide.with(context)
                        .load(mediaArtworkPath)
                        .override(WIDTH * 3, HEIGHT * 3)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
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
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(imageView);
                return true;
            }
        }

        Glide.with(context)
                .load(R.drawable.no_cover_player)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
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
                    .error(R.drawable.icon_error_load_cover)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
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
                        .error(R.drawable.icon_error_load_cover)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
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
                    .error(R.drawable.icon_error_load_cover)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
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
                    .override(WIDTH * 2, HEIGHT * 2)
                    .crossFade()
                    .centerCrop()
                    .listener(loggingListener)
                    .error(R.drawable.icon_error_load_cover)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(placeholder);
        } else {
            Glide.with(context)
                    .load(R.drawable.no_cover_folder_icon)
                    .override(WIDTH * 2, HEIGHT * 2)
                    .crossFade()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(placeholder);
        }

    }

    public void loadAudioGenresFolderArtwork(Genre genre, ImageView placeholder) {

        String path = genre.artworkPath;
        if (path != null) {
            Glide.with(context)
                    .load(path)
                    .override(WIDTH * 2, HEIGHT * 2)
                    .crossFade()
                    .centerCrop()
                    .listener(loggingListener)
                    .error(R.drawable.icon_error_load_cover)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(placeholder);
        } else {
            Glide.with(context)
                    .load(R.drawable.no_cover_folder_icon)
                    .override(WIDTH, HEIGHT)
                    .crossFade()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(placeholder);
        }
    }

    public void loadAudioFolderArtwork(AudioFolder audioFolder, ImageView placeholder) {

        String coverFile = findAudioFolderArtworkPath(audioFolder);
        if (coverFile != null) {
            Glide.with(context)
                    .load(coverFile)
                    .override(WIDTH * 2, HEIGHT * 2)
                    .crossFade()
                    .centerCrop()
                    .listener(loggingListener)
                    .error(R.drawable.icon_error_load_cover)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(placeholder);
        } else {
            Glide.with(context)
                    .load(R.drawable.no_cover_folder_icon)
                    .override(WIDTH * 2, HEIGHT * 2)
                    .crossFade()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(placeholder);
        }
    }

    public void loadVideoFileCover(String path, ImageView placeholder) {
        Glide.with(context)
                .load(path)
                .override(WIDTH * 2, HEIGHT * 2)
                .crossFade()
                .centerCrop()
                .listener(loggingListener)
                .error(R.drawable.icon_error_load_cover)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(placeholder);
    }

    public void loadVideoFolderFrame(String path, ImageView placeholder) {
        Glide.with(context)
                .load(path)
                .override(WIDTH, HEIGHT)
                .crossFade()
                .centerCrop()
                .listener(loggingListener)
                .error(R.drawable.icon_error_load_cover)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
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

    public void saveUserPhoto(Bitmap bitmap) {
        saveBitmap(bitmap, CacheManager.getUserPhotoPath());
    }

    public Bitmap getUserPhoto() {
        return getBitmapFromPath(CacheManager.getUserPhotoPath().getAbsolutePath());
    }

    public void saveDownloadFolderIcon() {
        saveBitmapPng(getBitmapFromResource(R.drawable.icon_folder_download),
                CacheManager.getDownloadFolderIconPath());
    }
}
