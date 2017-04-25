package com.fesskiev.mediacenter.utils;


import android.os.Environment;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class CacheManager {

    public final static String EXTERNAL_STORAGE = Environment.getExternalStorageDirectory().toString();
    private final static String DOWNLOADS_FOLDER_PATH = EXTERNAL_STORAGE + "/MediaCenter/Downloads/";
    private final static String USER_PHOTO_PATH = EXTERNAL_STORAGE + "/MediaCenter/UserPhoto/";
    private final static String TEMP_PATH = EXTERNAL_STORAGE + "/MediaCenter/Temp/";
    public final static String RECORDER_DEST_PATH = EXTERNAL_STORAGE + "/MediaCenter/Records/";
    public final static String IMAGES_AUDIO_CACHE_PATH = EXTERNAL_STORAGE + "/MediaCenter/Images/Audio/";
    public final static String IMAGES_VIDEO_CACHE_PATH = EXTERNAL_STORAGE + "/MediaCenter/Images/Video/";
    public final static String CHECK_DOWNLOADS_FOLDER_PATH = EXTERNAL_STORAGE + "/MediaCenter/Downloads";

    public static void clearAudioImagesCache() {
        File folder = new File(IMAGES_AUDIO_CACHE_PATH);
        if (!folder.exists()) {
            return;
        }
        try {
            FileUtils.cleanDirectory(folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearVideoImagesCache() {
        File folder = new File(IMAGES_VIDEO_CACHE_PATH);
        if (!folder.exists()) {
            return;
        }
        try {
            FileUtils.cleanDirectory(folder);
        } catch (IOException e) {
            e.printStackTrace();
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
        File temp = new File(TEMP_PATH);
        if (!temp.exists()) {
            temp.mkdirs();
        }
        return new File(temp.getAbsolutePath(), "record_temp.wav");
    }

    public static File getRecordDestPath() {
        File dest = new File(AppSettingsManager.getInstance().getRecordPath());
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

    public static boolean deleteDirectoryWithFiles(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            boolean containDirs = false;
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    } else {
                        containDirs = true;
                    }
                }
            }
            return containDirs || directory.delete();
        }
        return false;
    }


    public static void clearTempDir() {
        File folder = new File(TEMP_PATH);
        if (!folder.exists()) {
            return;
        }
        try {
            FileUtils.cleanDirectory(folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
