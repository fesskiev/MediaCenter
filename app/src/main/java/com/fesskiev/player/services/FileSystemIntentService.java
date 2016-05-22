package com.fesskiev.player.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.db.DatabaseHelper;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.model.VideoFile;
import com.fesskiev.player.utils.CacheManager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;


public class FileSystemIntentService extends IntentService {

    private static final String TAG = FileSystemIntentService.class.getSimpleName();


    public static final String ACTION_VIDEO_FILE = "com.fesskiev.player.action.VIDEO_FILE";

    private static final String ACTION_START_FILE_SYSTEM_SERVICE =
            "com.fesskiev.player.action.ACTION_START_FILE_SYSTEM_SERVICE";
    private static final String ACTION_CHECK_DOWNLOAD_FOLDER_SERVICE =
            "com.fesskiev.player.action.ACTION_CHECK_DOWNLOAD_FOLDER_SERVICE";

    public static final String ACTION_START_FETCH_AUDIO = "com.fesskiev.player.action.ACTION_START_FETCH_AUDIO";
    public static final String ACTION_END_FETCH_AUDIO = "com.fesskiev.player.action.ACTION_END_FETCH_AUDIO";
    public static final String ACTION_AUDIO_FOLDER_NAME = "com.fesskiev.player.action.ACTION_AUDIO_FOLDER_NAME";
    public static final String ACTION_AUDIO_TRACK_NAME = "com.fesskiev.player.action.ACTION_AUDIO_TRACK_NAME";

    public static final String EXTRA_AUDIO_FOLDER_NAME = "com.fesskiev.player.action.EXTRA_AUDIO_FOLDER_NAME";
    public static final String EXTRA_AUDIO_TRACK_NAME = "com.fesskiev.player.action.EXTRA_AUDIO_TRACK_NAME";


    public FileSystemIntentService() {
        super(FileSystemIntentService.class.getName());
    }


    public static void startFileTreeService(Context context) {
        Intent intent = new Intent(context, FileSystemIntentService.class);
        intent.setAction(ACTION_START_FILE_SYSTEM_SERVICE);
        context.startService(intent);
    }

    public static void startCheckDownloadFolderService(Context context) {
        Intent intent = new Intent(context, FileSystemIntentService.class);
        intent.setAction(ACTION_CHECK_DOWNLOAD_FOLDER_SERVICE);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.w(TAG, "HANDLE INTENT: " + action);
            switch (action) {
                case ACTION_START_FILE_SYSTEM_SERVICE:
                    getAudioFolders();
                    break;
                case ACTION_CHECK_DOWNLOAD_FOLDER_SERVICE:
                    checkAudioFolderService();
                    break;
            }
        }
    }

    private void checkAudioFolderService() {
        String folderId = null;
        File root = new File(CacheManager.CHECK_DOWNLOADS_FOLDER_PATH);
        File[] list = root.listFiles();
        for (File child : list) {
            if (!DatabaseHelper.containAudioTrack(getApplicationContext(), child.getAbsolutePath())) {
                if (folderId == null) {
                    folderId = DatabaseHelper.getDownloadFolderID(getApplicationContext());
                }
                new Thread(new FetchDownloadAudioInfo(child, folderId)).start();
            }
        }
    }

    private void getAudioFolders() {
        MediaApplication.getInstance().getAudioPlayer().audioFolders.clear();
        MediaApplication.getInstance().getVideoPlayer().videoFiles.clear();
        String sdCardState = Environment.getExternalStorageState();
        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            sendStartFetchAudioBroadcast();
            Log.w(TAG, "sendStartFetchAudioBroadcast");

            File root = Environment.getExternalStorageDirectory();
            walk(root.getAbsolutePath());

            sendEndFetchAudioBroadcast();
            Log.w(TAG, "sendEndFetchAudioBroadcast");

        } else {
            Log.wtf(TAG, "NO SD CARD!");
        }
    }

    public void walk(String path) {
        File root = new File(path);
        File[] list = root.listFiles();
        if (list == null) {
            Log.w(TAG, "Root is null");
            return;
        }
        for (File child : list) {
            if (child.isDirectory()) {
                checkAudioFilesFolder(child);
//                checkVideoFiles(child);
                walk(child.getAbsolutePath());
            }
        }
    }

    private void checkVideoFiles(File child) {
        File[] moviesFiles = child.listFiles(videoFilter());
        if (moviesFiles != null) {
            for (File movieFile : moviesFiles) {
                VideoFile videoFile = new VideoFile(movieFile.getAbsolutePath());
                MediaApplication.getInstance().getVideoPlayer().videoFiles.add(videoFile);
                sendVideoFoldersBroadcast();
            }
        }
    }

    public boolean checkDownloadFolder(File file) {
        return file.getAbsolutePath().equals(CacheManager.CHECK_DOWNLOADS_FOLDER_PATH);
    }


    private void checkAudioFilesFolder(File child) {
        File[] directoryFiles = child.listFiles();
        if (directoryFiles != null) {
            for (File directoryFile : directoryFiles) {
                File[] filterFiles = directoryFile.listFiles(audioFilter());
                if (filterFiles != null && filterFiles.length > 0) {
                    Log.w(TAG, "audio folder created");

                    AudioFolder audioFolder = new AudioFolder();

                    if (checkDownloadFolder(directoryFile)) {
                        audioFolder.folderImage = CacheManager.getDownloadFolderIconPath();
                    } else {
                        File[] filterImages = directoryFile.listFiles(folderImageFilter());
                        if (filterImages != null && filterImages.length > 0) {
                            audioFolder.folderImage = filterImages[0];
                        }
                    }

                    audioFolder.folderPath = directoryFile;
                    audioFolder.folderName = directoryFile.getName();
                    audioFolder.id = UUID.randomUUID().toString();

                    sendAudioFolderNameBroadcast(audioFolder.folderName);

                    CountDownLatch latch = new CountDownLatch(filterFiles.length);

                    for (File file : filterFiles) {
                        new Thread(new FetchAudioInfo(audioFolder, file, latch)).start();
                    }

                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    DatabaseHelper.insertAudioFolder(getApplicationContext(), audioFolder);

                }
            }
        }
    }

    private class FetchDownloadAudioInfo implements Runnable {

        private File file;
        private String id;

        public FetchDownloadAudioInfo(File file, String id) {
            this.file = file;
            this.id = id;
        }

        @Override
        public void run() {

            new AudioFile(getApplicationContext(), file,
                    new AudioFile.OnMp3TagListener() {
                        @Override
                        public void onFetchCompleted(AudioFile file) {
                            file.id = id;
                            DatabaseHelper.insertAudioFile(getApplicationContext(), file);
                        }
                    });
        }
    }

    private class FetchAudioInfo implements Runnable {

        private CountDownLatch latch;
        private AudioFolder audioFolder;
        private File file;

        public FetchAudioInfo(AudioFolder audioFolder, File file, CountDownLatch latch) {
            this.audioFolder = audioFolder;
            this.file = file;
            this.latch = latch;
        }

        @Override
        public void run() {

            final AudioFile audioFile = new AudioFile(getApplicationContext(), file,
                    new AudioFile.OnMp3TagListener() {
                        @Override
                        public void onFetchCompleted(AudioFile file) {
                            file.id = audioFolder.id;

                            DatabaseHelper.insertAudioFile(getApplicationContext(), file);

                            sendAudioTrackNameBroadcast(file.artist + "-" + file.title);
                            latch.countDown();

                        }
                    });

            audioFolder.audioFiles.add(audioFile);
        }
    }

    public void sendStartFetchAudioBroadcast() {
        Intent intent = new Intent(ACTION_START_FETCH_AUDIO);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void sendEndFetchAudioBroadcast() {
        Intent intent = new Intent(ACTION_END_FETCH_AUDIO);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void sendAudioTrackNameBroadcast(String trackName) {
        Intent intent = new Intent(ACTION_AUDIO_TRACK_NAME);
        intent.putExtra(EXTRA_AUDIO_TRACK_NAME, trackName);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void sendAudioFolderNameBroadcast(String folderName) {
        Intent intent = new Intent(ACTION_AUDIO_FOLDER_NAME);
        intent.putExtra(EXTRA_AUDIO_FOLDER_NAME, folderName);
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
