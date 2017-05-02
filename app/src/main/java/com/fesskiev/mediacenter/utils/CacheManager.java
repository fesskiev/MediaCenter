package com.fesskiev.mediacenter.utils;


import android.os.Environment;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class CacheManager {

    public final static String EXTERNAL_STORAGE = Environment.getExternalStorageDirectory().toString();
    private final static String TEMP_PATH = EXTERNAL_STORAGE + "/MediaCenter/Temp/";
    public final static String RECORDER_DEST_PATH = EXTERNAL_STORAGE + "/MediaCenter/Records/";
    public final static String CONVERT_DEST_PATH = EXTERNAL_STORAGE + "/MediaCenter/Convert/";
    public final static String CUT_DEST_PATH = EXTERNAL_STORAGE + "/MediaCenter/Cut/";
    public final static String IMAGES_AUDIO_CACHE_PATH = EXTERNAL_STORAGE + "/MediaCenter/Images/Audio/";
    public final static String IMAGES_VIDEO_CACHE_PATH = EXTERNAL_STORAGE + "/MediaCenter/Images/Video/";

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

    public static File getCutFolderPath() {
        File dest = new File(AppSettingsManager.getInstance().getCutFolderPath());
        if (!dest.exists()) {
            dest.mkdirs();
        }
        return dest;
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
