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
import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.data.model.Artist;
import com.fesskiev.player.data.model.AudioFile;
import com.fesskiev.player.data.model.AudioFolder;
import com.fesskiev.player.data.model.Genre;
import com.fesskiev.player.data.model.MediaFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapHelper {

    public interface OnBitmapLoadListener {
        void onLoaded(Bitmap bitmap);

        void onFailed();
    }

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


    public void loadURLAvatar(String url, final ImageView into, final OnBitmapLoadListener listener) {
        Glide.with(context).load(url).asBitmap().
                centerCrop().into(new BitmapImageViewTarget(into) {
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

    private String findNotificationArtworkPath(AudioFile audioFile) {
        String mediaArtworkPath = findMediaFileArtworkPath(audioFile);
        if (mediaArtworkPath != null) {
            return mediaArtworkPath;
        }
        return null;
    }

    public void loadNotificationArtwork(AudioFile audioFile, final OnBitmapLoadListener listener) {
        Glide.with(context)
                .load(findNotificationArtworkPath(audioFile))
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
                        if (listener != null) {
                            listener.onFailed();
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

    public void loadURIBitmap(String uri, ImageView into) {
        Glide.with(context).
                load(uri).
                fitCenter().
                crossFade().
                into(into);
    }

    public void loadCircleURIBitmap(String uri, ImageView into) {
        Glide.with(context).
                load(uri).
                fitCenter().
                transform(new CircleTransform(context)).
                crossFade().
                into(into);
    }


    public boolean loadAudioPlayerArtwork(AudioFile audioFile, ImageView imageView) {
        String mediaArtworkPath = findMediaFileArtworkPath(audioFile);
        if (mediaArtworkPath != null) {
            Glide.with(context).
                    load(mediaArtworkPath).
                    crossFade().
                    fitCenter().
                    into(imageView);
            return true;
        }

        return false;
    }


    public void loadTrackListArtwork(MediaFile mediaFile, ImageView imageView) {

        String mediaArtworkPath = findMediaFileArtworkPath(mediaFile);
        if (mediaArtworkPath != null) {
            Glide.with(context).
                    load(mediaArtworkPath).
                    crossFade().
                    fitCenter().
                    transform(new CircleTransform(context)).
                    into(imageView);
            return;
        }

        if (mediaFile instanceof AudioFile) {
            Glide.with(context).
                    load(R.drawable.no_cover_track_icon).
                    crossFade().
                    fitCenter().
                    transform(new CircleTransform(context)).
                    into(imageView);
        }
    }

    private String findMediaFileArtworkPath(MediaFile mediaFile) {
        String artworkPath = mediaFile.getArtworkPath();
        if (artworkPath != null) {
            return artworkPath;
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


    public void loadAudioArtistsFolderArtwork(Artist artist, ImageView imageView) {

        String path = artist.artworkPath;
        if (path != null) {
            Glide.with(context).
                    load(path).
                    crossFade().
                    fitCenter().
                    into(imageView);
        } else {
            Glide.with(context).
                    load(R.drawable.no_cover_folder_icon).
                    fitCenter().
                    crossFade().
                    into(imageView);
        }

    }

    public void loadAudioGenresFolderArtwork(Genre genre, ImageView imageView) {

        String path = genre.artworkPath;
        if (path != null) {
            Glide.with(context).
                    load(path).
                    crossFade().
                    fitCenter().
                    into(imageView);
        } else {
            Glide.with(context).
                    load(R.drawable.no_cover_folder_icon).
                    fitCenter().
                    crossFade().
                    into(imageView);
        }
    }

    public void loadAudioFolderArtwork(AudioFolder audioFolder, ImageView placeholder) {

        String coverFile = findAudioFolderArtworkPath(audioFolder);
        if (coverFile != null) {
            Glide.with(context).
                    load(coverFile).
                    crossFade().
                    fitCenter().
                    into(placeholder);
        } else {
            Glide.with(context).
                    load(R.drawable.no_cover_folder_icon).
                    fitCenter().
                    crossFade().
                    into(placeholder);
        }
    }

    public void saveBitmap(Bitmap bitmap, File path) {
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

    public void saveBitmap(byte[] data, File path) {
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
        saveBitmap(getBitmapFromResource(R.drawable.icon_folder_download),
                CacheManager.getDownloadFolderIconPath());
    }


}
