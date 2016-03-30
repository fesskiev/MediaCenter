package com.fesskiev.player.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.fesskiev.player.R;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.model.AudioPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapHelper {

    public interface OnBitmapLoad {
        void onLoaded(Bitmap bitmap);
    }

    public static void loadURLAvatar(final Context context, String url, final ImageView into, final OnBitmapLoad onBitmapLoad) {
        Glide.with(context).load(url).asBitmap().
                centerCrop().into(new BitmapImageViewTarget(into) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                circularBitmapDrawable.setCircular(true);

                into.setImageDrawable(circularBitmapDrawable);
                onBitmapLoad.onLoaded(resource);
            }
        });
    }

    public static void loadBitmapAvatar(final Context context, Bitmap bitmap, final ImageView into) {
        RoundedBitmapDrawable circularBitmapDrawable =
                RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
        circularBitmapDrawable.setCircular(true);
        into.setImageDrawable(circularBitmapDrawable);
    }

    public static void loadEmptyAvatar(final Context context, final ImageView into) {
        Glide.with(context).load(R.drawable.icon_no_avatar).asBitmap().
                centerCrop().into(new BitmapImageViewTarget(into) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                circularBitmapDrawable.setCircular(true);

                into.setImageDrawable(circularBitmapDrawable);

            }
        });
    }

    public static void loadURLBitmap(Context context, String url, ImageView into) {
        Glide.with(context).load(url).into(into);
    }

    public static void loadAudioPlayerArtwork(Context context, AudioPlayer audioPlayer, ImageView placeholder) {
        String artworkPath = audioPlayer.currentAudioFile.artworkPath;
        if (artworkPath != null) {
            Glide.with(context).load(artworkPath).into(placeholder);
        } else {
            AudioFolder audioFolder = audioPlayer.currentAudioFolder;
            if (audioFolder != null) {
                File coverFile = audioFolder.folderImage;
                if (coverFile != null) {
                    Glide.with(context).load(coverFile).into(placeholder);
                } else {
                    Glide.with(context).load(R.drawable.no_cover_icon).into(placeholder);
                }
            }
        }
    }

    public static void loadTrackListArtwork(Context context, AudioFolder audioFolder, AudioFile audioFile, ImageView placeholder) {
        String artworkPath = audioFile.artworkPath;
        if (artworkPath != null) {
            Glide.with(context).load(artworkPath).into(placeholder);
        } else {
            if (audioFolder != null) {
                File coverFile = audioFolder.folderImage;
                if (coverFile != null) {
                    Glide.with(context).load(coverFile).into(placeholder);
                } else {
                    Glide.with(context).load(R.drawable.no_cover_icon).into(placeholder);
                }
            }
        }
    }

    public static void loadAudioFolderArtwork(Context context, AudioFolder audioFolder, ImageView placeholder) {
        File coverFile = audioFolder.folderImage;
        if (coverFile != null) {
            Glide.with(context).load(coverFile).into(placeholder);
        } else {
            Glide.with(context).load(R.drawable.no_cover_icon).into(placeholder);
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

}
