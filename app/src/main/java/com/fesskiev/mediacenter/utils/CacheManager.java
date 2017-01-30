package com.fesskiev.mediacenter.utils;


import android.os.Environment;

import java.io.File;

public class CacheManager {

    private final static String EXTERNAL_STORAGE = Environment.getExternalStorageDirectory().toString();
    private final static String DOWNLOADS_FOLDER_PATH = EXTERNAL_STORAGE + "/MediaCenter/Downloads/";
    private final static String USER_PHOTO_PATH = EXTERNAL_STORAGE + "/MediaCenter/UserPhoto/";
    public final static String IMAGES_AUDIO_CACHE_PATH = EXTERNAL_STORAGE + "/MediaCenter/Images/Audio/";
    public final static String IMAGES_VIDEO_CACHE_PATH = EXTERNAL_STORAGE + "/MediaCenter/Images/Video/";
    public final static String CHECK_DOWNLOADS_FOLDER_PATH = EXTERNAL_STORAGE + "/MediaCenter/Downloads";

    public static void clearAudioImagesCache() {
        File folder = new File(IMAGES_AUDIO_CACHE_PATH);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public static void clearVideoImagesCache() {
        File folder = new File(IMAGES_VIDEO_CACHE_PATH);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public static File getUserPhotoPath() {
        File folder = new File(USER_PHOTO_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return new File(folder.getAbsolutePath(), "user_photo.png");
    }


    public static File getDownloadsFilePath(String fileName) {
        File folder = new File(DOWNLOADS_FOLDER_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return new File(folder.getAbsolutePath(), fileName + ".mp3");
    }

    public static File getDownloadFolderIconPath() {
        File folder = new File(IMAGES_AUDIO_CACHE_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return new File(folder.getAbsolutePath(), "download_folder_icon.png");
    }
}
