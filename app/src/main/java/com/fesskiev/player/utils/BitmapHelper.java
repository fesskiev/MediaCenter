package com.fesskiev.player.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.fesskiev.player.R;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.model.MediaFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapHelper {

    public interface OnBitmapLoadListener {
        void onLoaded(Bitmap bitmap);

        void onFailed();
    }

    public static void loadURLAvatar(final Context context, String url, final ImageView into, final OnBitmapLoadListener listener) {
        Glide.with(context.getApplicationContext()).load(url).asBitmap().
                centerCrop().into(new BitmapImageViewTarget(into) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(context.getApplicationContext().getResources(), resource);
                circularBitmapDrawable.setCircular(true);

                into.setImageDrawable(circularBitmapDrawable);
                if (listener != null) {
                    listener.onLoaded(resource);
                }
            }
        });
    }

    public static void loadBitmap(final Context context, String url, final OnBitmapLoadListener listener) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(100, 100) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        if (listener != null) {
                            listener.onLoaded(resource);
                        }
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                       if(listener != null){
                            listener.onFailed();
                        }
                    }
                });
    }

    public static void loadBitmapAvatar(final Context context, Bitmap bitmap, final ImageView into) {
        RoundedBitmapDrawable circularBitmapDrawable =
                RoundedBitmapDrawableFactory.create(context.getApplicationContext().getResources(), bitmap);
        circularBitmapDrawable.setCircular(true);
        into.setImageDrawable(circularBitmapDrawable);
    }

    public static void loadEmptyAvatar(final Context context, final ImageView into) {
        Glide.with(context.getApplicationContext()).load(R.drawable.icon_no_avatar).asBitmap().
                centerCrop().into(new BitmapImageViewTarget(into) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(context.getApplicationContext().getResources(), resource);
                circularBitmapDrawable.setCircular(true);

                into.setImageDrawable(circularBitmapDrawable);

            }
        });
    }

    public static void loadURLBitmap(Context context, String url, ImageView into) {
        Glide.with(context.getApplicationContext()).load(url).into(into);
    }

    public static void loadAudioPlayerArtwork(Context context, AudioPlayer audioPlayer, ImageView placeholder) {
        String path = findAudioPlayerArtworkPath(audioPlayer);
        if (path != null) {
            Glide.with(context.getApplicationContext()).
                    load(path).
                    crossFade().
                    into(placeholder);
        } else {
            Glide.with(context.getApplicationContext()).
                    load(R.drawable.no_cover_icon).
                    crossFade().
                    into(placeholder);
        }
    }

    private static String findAudioPlayerArtworkPath(AudioPlayer audioPlayer) {
        String artworkPath = audioPlayer.currentAudioFile.artworkPath;
        if (artworkPath != null) {
            return artworkPath;
        }
        AudioFolder audioFolder = audioPlayer.currentAudioFolder;
        if (audioFolder != null) {
            File coverFile = audioFolder.folderImage;
            if (coverFile != null) {
                return coverFile.getAbsolutePath();
            }
        }
        return null;
    }

    public static void loadArtwork(Context context, AudioFolder audioFolder,
                                   MediaFile mediaFile, ImageView placeholder) {

        String path = findArtworkPath(audioFolder, mediaFile);
        if (path != null) {
            Glide.with(context.getApplicationContext()).
                    load(path).
                    crossFade().
                    transform(new CircleTransform(context.getApplicationContext())).
                    into(placeholder);
        } else {
            Glide.with(context.getApplicationContext()).
                    load(R.drawable.no_cover_icon).
                    crossFade().
                    transform(new CircleTransform(context.getApplicationContext())).
                    into(placeholder);
        }
    }

    private static String findArtworkPath(AudioFolder audioFolder, MediaFile mediaFile) {
        String artworkPath = mediaFile.getArtworkPath();
        if (artworkPath != null) {
            return artworkPath;
        }
        if (audioFolder != null) {
            File coverFile = audioFolder.folderImage;
            if (coverFile != null) {
                return coverFile.getAbsolutePath();
            }
        }
        return null;
    }

    public static void loadAudioFolderArtwork(Context context, AudioFolder audioFolder, ImageView placeholder) {
        File coverFile = audioFolder.folderImage;
        if (coverFile != null) {
            Glide.with(context.getApplicationContext()).
                    load(coverFile).
                    crossFade().
                    into(placeholder);
        } else {
            Glide.with(context.getApplicationContext()).
                    load(R.drawable.no_cover_icon).
                    crossFade().
                    into(placeholder);
        }
    }

    public static void saveBitmap(Bitmap bitmap, File path) {
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

    public static void saveBitmap(byte[] data, File path) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(data);
            out.flush();
            out.close();

        } catch (IOException e) {
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

    public static Bitmap getBitmapFromPath(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap getBitmapFromResource(Context context, int resource) {
        return BitmapFactory.decodeResource(context.getResources(), resource);
    }

    public static void saveUserPhoto(Bitmap bitmap) {
        BitmapHelper.saveBitmap(bitmap, CacheManager.getUserPhotoPath());
    }

    public static Bitmap getUserPhoto() {
        return BitmapHelper.getBitmapFromPath(CacheManager.getUserPhotoPath().getAbsolutePath());
    }

    public static void saveDownloadFolderIcon(Context context) {
        BitmapHelper.saveBitmap(
                BitmapHelper.getBitmapFromResource(context, R.drawable.icon_folder_download),
                CacheManager.getDownloadFolderIconPath());
    }

}
