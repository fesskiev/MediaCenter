package com.fesskiev.player.utils;


import android.content.Context;
import android.graphics.Bitmap;
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
        byte[] artworkBinaryData = audioPlayer.currentAudioFile.getArtworkBinaryData();
        if (artworkBinaryData != null) {
            Glide.with(context).load(artworkBinaryData).into(placeholder);
        } else {
            AudioFolder audioFolder = audioPlayer.currentAudioFolder;
            if (audioFolder != null && audioFolder.folderImages.size() > 0) {
                File coverFile = audioFolder.folderImages.get(0);
                if (coverFile != null) {
                    Glide.with(context).load(coverFile).into(placeholder);
                }
            } else {
                Glide.with(context).load(R.drawable.no_cover_icon).into(placeholder);
            }
        }
    }

    public static void loadTrackListArtwork(Context context, AudioFolder audioFolder, AudioFile audioFile, ImageView placeholder) {
        byte[] artworkBinaryData = audioFile.getArtworkBinaryData();
        if (artworkBinaryData != null) {
            Glide.with(context).load(artworkBinaryData).into(placeholder);
        } else {
            if (audioFolder != null && audioFolder.folderImages.size() > 0) {
                File coverFile = audioFolder.folderImages.get(0);
                if (coverFile != null) {
                    Glide.with(context).load(coverFile).into(placeholder);
                }
            } else {
                Glide.with(context).load(R.drawable.no_cover_icon).into(placeholder);
            }
        }
    }

    public static void loadAudioFolderArtwork(Context context, AudioFolder audioFolder, ImageView placeholder) {
        if (audioFolder.folderImages.size() > 0) {
            File coverFile = audioFolder.folderImages.get(0);
            if (coverFile != null) {
                Glide.with(context).load(coverFile).into(placeholder);
            }
        } else {
            Glide.with(context).load(R.drawable.no_cover_icon).into(placeholder);
        }
    }
}
