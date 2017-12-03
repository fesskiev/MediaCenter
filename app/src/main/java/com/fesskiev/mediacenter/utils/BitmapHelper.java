package com.fesskiev.mediacenter.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.MediaFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.Observable;

public class BitmapHelper {

    private static final int WIDTH = 140;
    private static final int HEIGHT = 140;

    private Context context;

    public BitmapHelper(Context context) {
        this.context = context;
    }

    public Observable<Bitmap> getCoverBitmapFromURL(String url) {
        return Observable.create(e -> {
            if (url != null) {
                FutureTarget<Bitmap> target = Glide.with(context)
                        .load(url)
                        .asBitmap()
                        .into(WIDTH * 3, HEIGHT * 3);
                Bitmap bitmap = target.get();
                e.onNext(bitmap);
                Glide.clear(target);
            } else {
                FutureTarget<Bitmap> target = Glide.with(context)
                        .load(R.drawable.no_cover_track_icon)
                        .asBitmap()
                        .into(WIDTH * 3, HEIGHT * 3);
                Bitmap bitmap = target.get();
                e.onNext(bitmap);
                Glide.clear(target);
            }
            e.onComplete();
        });
    }

    public Observable<Bitmap> loadBitmap(MediaFile mediaFile) {
        String path = findPath(mediaFile);
        return Observable.create(e -> {
            if (path != null) {
                FutureTarget<Bitmap> target = Glide.with(context)
                        .load(path)
                        .asBitmap()
                        .into(WIDTH, HEIGHT);
                Bitmap bitmap = target.get();
                e.onNext(bitmap);
                Glide.clear(target);
            } else {
                FutureTarget<Bitmap> target = Glide.with(context)
                        .load(R.drawable.no_cover_track_icon)
                        .asBitmap()
                        .error(R.drawable.no_cover_track_icon)
                        .into(WIDTH, HEIGHT);
                Bitmap bitmap = target.get();
                e.onNext(bitmap);
                Glide.clear(target);
            }
            e.onComplete();
        });

    }

    public Observable<Bitmap> loadAudioPlayerArtwork(AudioFile audioFile) {
        String mediaArtworkPath = findMediaFileArtworkPath(audioFile);
        return Observable.create(e -> {
            if (mediaArtworkPath != null) {
                FutureTarget<Bitmap> target = Glide.with(context)
                        .load(mediaArtworkPath)
                        .asBitmap()
                        .into(WIDTH, HEIGHT);
                Bitmap bitmap = target.get();
                e.onNext(bitmap);
                Glide.clear(target);
            } else {
                FutureTarget<Bitmap> target = Glide.with(context)
                        .load(R.drawable.no_cover_player)
                        .asBitmap()
                        .into(WIDTH, HEIGHT);
                Bitmap bitmap = target.get();
                e.onNext(bitmap);
                Glide.clear(target);
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


    public Observable<Bitmap> getTrackListArtwork(MediaFile mediaFile) {
        String mediaArtworkPath = findMediaFileArtworkPath(mediaFile);
        return Observable.create(e -> {
            if (mediaArtworkPath != null) {
                FutureTarget<Bitmap> target = Glide.with(context)
                        .load(mediaArtworkPath)
                        .asBitmap()
                        .transform(new CircleTransform(context))
                        .into(WIDTH * 3, HEIGHT * 3);
                Bitmap bitmap = target.get();
                e.onNext(bitmap);
                Glide.clear(target);
            } else {
                FutureTarget<Bitmap> target = Glide.with(context)
                        .load(R.drawable.no_cover_track_icon)
                        .asBitmap()
                        .transform(new CircleTransform(context))
                        .into(WIDTH * 3, HEIGHT * 3);
                Bitmap bitmap = target.get();
                e.onNext(bitmap);
                Glide.clear(target);
            }
            e.onComplete();
        });
    }

    public Observable<Bitmap> getAudioFolderArtwork(AudioFolder audioFolder) {
        return Observable.create(e -> {
            String coverFile = findAudioFolderArtworkPath(audioFolder);
            if (coverFile != null) {
                FutureTarget<Bitmap> target = Glide.with(context)
                        .load(coverFile)
                        .asBitmap()
                        .into(WIDTH * 3, HEIGHT * 3);
                Bitmap bitmap = target.get();
                e.onNext(bitmap);
                Glide.clear(target);
            } else {
                FutureTarget<Bitmap> target = Glide.with(context)
                        .load(R.drawable.no_cover_folder_icon)
                        .asBitmap()
                        .into(WIDTH * 3, HEIGHT * 3);
                Bitmap bitmap = target.get();
                e.onNext(bitmap);
                Glide.clear(target);
            }
            e.onComplete();
        });
    }

    public Observable<Bitmap> loadVideoFileFrame(String path) {
        return Observable.create(e -> {
            if (path != null) {
                FutureTarget<Bitmap> target = Glide.with(context)
                        .load(path)
                        .asBitmap()
                        .into(WIDTH * 3, HEIGHT * 3);
                Bitmap bitmap = target.get();
                e.onNext(bitmap);
                Glide.clear(target);
            } else {
                FutureTarget<Bitmap> target = Glide.with(context)
                        .load(R.drawable.no_cover_track_icon)
                        .asBitmap()
                        .into(WIDTH * 3, HEIGHT * 3);
                Bitmap bitmap = target.get();
                e.onNext(bitmap);
                Glide.clear(target);
            }
            e.onComplete();
        });

    }

    public Observable<Bitmap> loadVideoFolderFrame(String path) {
        return Observable.create(e -> {
            if (path != null) {
                FutureTarget<Bitmap> target = Glide.with(context)
                        .load(path)
                        .asBitmap()
                        .into(WIDTH * 3, HEIGHT * 3);
                Bitmap bitmap = target.get();
                e.onNext(bitmap);
                Glide.clear(target);
            } else {
                FutureTarget<Bitmap> target = Glide.with(context)
                        .load(R.drawable.no_cover_folder_icon)
                        .asBitmap()
                        .into(WIDTH * 3, HEIGHT * 3);
                Bitmap bitmap = target.get();
                e.onNext(bitmap);
                Glide.clear(target);
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
