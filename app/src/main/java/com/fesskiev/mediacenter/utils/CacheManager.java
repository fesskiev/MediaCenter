package com.fesskiev.mediacenter.utils;


import android.os.Environment;

import java.io.File;

public class CacheManager {

    private final static String EXTERNAL_STORAGE = Environment.getExternalStorageDirectory().toString();
    private final static String DOWNLOADS_FOLDER_PATH = EXTERNAL_STORAGE + "/MediaCenter/Downloads/";
    private final static String USER_PHOTO_PATH = EXTERNAL_STORAGE + "/MediaCenter/UserPhoto/";
    private final static String RECORDER_TEMP_PATH = EXTERNAL_STORAGE + "/MediaCenter/Temp/";
    private final static String RECORDER_DEST_PATH = EXTERNAL_STORAGE + "/MediaCenter/Records/";
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

    public static File getRecordTempPath() {
        File temp = new File(RECORDER_TEMP_PATH);
        if (!temp.exists()) {
            temp.mkdirs();
        }
        return new File(temp.getAbsolutePath(), "record_temp.wav");
    }

    public static File getRecordDestPath() {
        File dest = new File(RECORDER_DEST_PATH);
        if (!dest.exists()) {
            dest.mkdirs();
        }
        return new File(dest.getAbsolutePath(), String.valueOf(System.currentTimeMillis()));
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

    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }
}
