package com.fesskiev.player.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.db.DatabaseHelper;
import com.fesskiev.player.db.MediaCenterProvider;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.model.VideoFile;

import java.io.File;
import java.io.FilenameFilter;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;


public class FileSystemIntentService extends IntentService {

    private static final String TAG = FileSystemIntentService.class.getSimpleName();


    public static final String ACTION_VIDEO_FILE = "com.fesskiev.player.action.VIDEO_FILE";

    private static final String ACTION_START_FILE_SYSTEM_SERVICE = "com.fesskiev.player.action.ACTION_START_FILE_SYSTEM_SERVICE";

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


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.d(TAG, "HANDLE INTENT: " + action);
            if (ACTION_START_FILE_SYSTEM_SERVICE.equals(action)) {
//                DatabaseHelper.getAudioFolders(getApplicationContext());

                getMusicFolders();
            }
        }
    }


    private void getMusicFolders() {
        MediaApplication.getInstance().getAudioPlayer().audioFolders.clear();
        MediaApplication.getInstance().getVideoPlayer().videoFiles.clear();
        String sdCardState = Environment.getExternalStorageState();
        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {

            sendStartFetchAudioBroadcast();

            File root = Environment.getExternalStorageDirectory();
            walk(root.getAbsolutePath());

            sendEndFetchAudioBroadcast();

        } else {
            Log.wtf(TAG, "NO SD CARD");
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


    private void checkAudioFilesFolder(File child) {
        File[] directoryFiles = child.listFiles();
        if (directoryFiles != null) {
            for (File directoryFile : directoryFiles) {
                File[] filterFiles = directoryFile.listFiles(audioFilter());
                if (filterFiles != null && filterFiles.length > 0) {
                    AudioFolder audioFolder = new AudioFolder();

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

                    File[] filterImages = directoryFile.listFiles(folderImageFilter());
                    if (filterImages != null && filterImages.length > 0) {
                        audioFolder.folderImage = filterImages[0];
                    }

//                    DatabaseHelper.insertAudioFolder(getApplicationContext(), audioFolder);


                    MediaApplication.getInstance().getAudioPlayer().audioFolders.add(audioFolder);
                }
            }
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

            AudioFile audioFile = new AudioFile(getApplicationContext(), file,
                    new AudioFile.OnMp3TagListener() {
                        @Override
                        public void onFetchCompleted(AudioFile file) {
                            file.id = audioFolder.id;
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
