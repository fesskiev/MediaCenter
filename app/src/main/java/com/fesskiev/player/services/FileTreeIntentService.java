package com.fesskiev.player.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fesskiev.player.MusicApplication;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.model.VideoFile;

import java.io.File;
import java.io.FilenameFilter;


public class FileTreeIntentService extends IntentService {

    private static final String TAG = FileTreeIntentService.class.getSimpleName();

    private static final String ACTION_START_FILE_TREE_SERVICE = "com.fesskiev.player.action.START_FILE_TREE_SERVICE";
    public static final String ACTION_AUDIO_FOLDER = "com.fesskiev.player.action.AUDIO_FOLDER";
    public static final String ACTION_VIDEO_FILE = "com.fesskiev.player.action.VIDEO_FILE";

    public FileTreeIntentService() {
        super(FileTreeIntentService.class.getName());
    }


    public static void startFileTreeService(Context context) {
        Intent intent = new Intent(context, FileTreeIntentService.class);
        intent.setAction(ACTION_START_FILE_TREE_SERVICE);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.d(TAG, "HANDLE INTENT: " + action);
            if (ACTION_START_FILE_TREE_SERVICE.equals(action)) {
                getMusicFolders();
            }
        }
    }


    private void getMusicFolders() {
        String sdCardState = Environment.getExternalStorageState();
        if (!sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            Log.wtf(TAG, "NO SD CARD");
        } else {
            File root = Environment.getExternalStorageDirectory();
            walk(root.getAbsolutePath());
        }
    }

    public void walk(String path) {
        File root = new File(path);
        File[] list = root.listFiles();
        if (list == null) {
            return;
        }
        for (File child : list) {
            if (child.isDirectory()) {
                checkAudioFilesFolder(child);
                checkVideoFiles(child);
                walk(child.getAbsolutePath());
            }
        }
    }

    private void checkVideoFiles(File child) {
        File[] moviesFiles = child.listFiles(videoFilter());
        if (moviesFiles != null) {
            for (File movieFile : moviesFiles) {
                VideoFile videoFile = new VideoFile();
                videoFile.filePath = movieFile.getAbsolutePath();

                MusicApplication.getInstance().getVideoPlayer().videoFiles.add(videoFile);
                sendVideoFoldersBroadcast();
            }
        }
    }


    private void checkAudioFilesFolder(File child) {
        File[] directoryFiles = child.listFiles();
        if (directoryFiles != null) {
            for (File directoryFile : directoryFiles) {
                File[] filterFiles = directoryFile.listFiles(audioFilter());
                if (filterFiles != null && filterFiles.length > 0) {
                    AudioFolder audioFolder = new AudioFolder();
                    audioFolder.folderName = directoryFile.getName();

                    for (File file : filterFiles) {
//                        Log.wtf(TAG, "sound file: " + file);
                        audioFolder.musicFiles.add(file);
                    }

                    File[] filterImages = directoryFile.listFiles(folderImageFilter());
                    if (filterImages != null) {
                        for (File file : filterImages) {
//                        Log.wtf(TAG, "image File: " + file);
                            audioFolder.folderImages.add(file);
                        }
                    }
                    MusicApplication.getInstance().getAudioPlayer().audioFolders.add(audioFolder);
                    sendAudioFoldersBroadcast();
                }
            }
        }
    }

    public void sendAudioFoldersBroadcast() {
        Intent intent = new Intent(ACTION_AUDIO_FOLDER);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void sendVideoFoldersBroadcast() {
        Intent intent = new Intent(ACTION_VIDEO_FILE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    private FilenameFilter videoFilter() {
        return new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                return lowercaseName.endsWith(".mp4") || lowercaseName.endsWith(".ts");
            }
        };
    }

    private FilenameFilter audioFilter() {
        return new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                return lowercaseName.endsWith(".mp3") || lowercaseName.endsWith(".flac");
            }
        };
    }

    private FilenameFilter folderImageFilter() {
        return new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                return (lowercaseName.endsWith(".png") || lowercaseName.endsWith(".jpg"));
            }
        };
    }

}
