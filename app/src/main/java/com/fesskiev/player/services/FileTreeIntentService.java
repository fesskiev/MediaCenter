package com.fesskiev.player.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fesskiev.player.MusicApplication;
import com.fesskiev.player.model.MusicFolder;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;


public class FileTreeIntentService extends IntentService {

    private static final String TAG = FileTreeIntentService.class.getSimpleName();
    private static final String ACTION_START_FILE_TREE_SERVICE =
            "com.fesskiev.player.action.START_FILE_TREE_SERVICE";

    public static final String ACTION_MUSIC_FOLDER
            = "com.fesskiev.player.action.MUSIC_FOLDER";

    private ArrayList<MusicFolder> musicFolders;

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
            Log.d(TAG, "HANDLE INTENT: "  + action);
            if (ACTION_START_FILE_TREE_SERVICE.equals(action)) {
                getMusicFolders();
            }
        }
    }




    private void getMusicFolders() {
        musicFolders = new ArrayList<>();
        String sdCardState = Environment.getExternalStorageState();
        if (!sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            Log.wtf(TAG, "NO SD CARD");
        } else {
            File root = Environment.getExternalStorageDirectory();
            walk(root.getAbsolutePath());
        }

//        musicFolderToString();

    }

    public void walk(String path) {
        File root = new File(path);
        File[] list = root.listFiles();
        if (list == null) {
            return;
        }
        for (File child : list) {
            if (child.isDirectory()) {
                checkMusicFilesFolder(child);
                walk(child.getAbsolutePath());
            }
        }
    }

    private void checkMusicFilesFolder(File child) {
        File[] directoryFiles = child.listFiles();
        for (File directoryFile : directoryFiles) {
            File[] filterFiles = directoryFile.listFiles(musicFilter());
            if (filterFiles != null && filterFiles.length > 0) {
                MusicFolder musicFolder = new MusicFolder();
                musicFolder.folderName = directoryFile.getName();
                for (File file : filterFiles) {
                    Log.wtf(TAG, "sound file: " + file);
                    musicFolder.musicFiles.add(file);
                }

                File[] filterImages = directoryFile.listFiles(folderImageFilter());
                if (filterImages != null) {
                    for (File file : filterImages) {
//                        Log.wtf(TAG, "image File: " + file);
                        musicFolder.folderImages.add(file);
                    }
                }
                musicFolders.add(musicFolder);
                ((MusicApplication) getApplication()).setMusicFolders(musicFolders);
                sendMusicFoldersBroadcast();
            }
        }
    }

    public void sendMusicFoldersBroadcast() {
        Intent intent = new Intent(ACTION_MUSIC_FOLDER);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void musicFolderToString() {
        for (MusicFolder musicFolder : musicFolders) {
            Log.d(TAG, "folder: " + musicFolder.toString());
        }
    }

    private FilenameFilter musicFilter() {
        return new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                return lowercaseName.endsWith(".mp3");
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
