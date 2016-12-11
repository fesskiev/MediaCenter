package com.fesskiev.player.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.data.source.DataRepository;
import com.fesskiev.player.data.source.local.db.LocalDataSource;
import com.fesskiev.player.data.model.AudioFile;
import com.fesskiev.player.data.model.AudioFolder;
import com.fesskiev.player.data.model.VideoFile;
import com.fesskiev.player.utils.CacheManager;
import com.fesskiev.player.utils.Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;


public class FileSystemIntentService extends IntentService {

    private static final String TAG = FileSystemIntentService.class.getSimpleName();


    public static final String ACTION_VIDEO_FILE = "com.fesskiev.player.action.VIDEO_FILE";

    private static final String ACTION_START_FETCH_MEDIA_SERVICE =
            "com.fesskiev.player.action.ACTION_FETCH_MEDIA_SERVICE";
    private static final String ACTION_START_FETCH_AUDIO_SERVICE =
            "com.fesskiev.player.action.ACTION_START_FETCH_AUDIO_SERVICE";
    private static final String ACTION_START_FETCH_VIDEO_SERVICE =
            "com.fesskiev.player.action.ACTION_START_FETCH_VIDEO_SERVICE";

    private static final String ACTION_CHECK_DOWNLOAD_FOLDER_SERVICE =
            "com.fesskiev.player.action.ACTION_CHECK_DOWNLOAD_FOLDER_SERVICE";

    public static final String ACTION_START_FETCH_MEDIA_CONTENT
            = "com.fesskiev.player.action.ACTION_START_FETCH_MEDIA_CONTENT";
    public static final String ACTION_END_FETCH_MEDIA_CONTENT
            = "com.fesskiev.player.action.ACTION_END_FETCH_MEDIA_CONTENT";

    public static final String ACTION_AUDIO_FOLDER_NAME = "com.fesskiev.player.action.ACTION_AUDIO_FOLDER_NAME";
    public static final String ACTION_AUDIO_TRACK_NAME = "com.fesskiev.player.action.ACTION_AUDIO_TRACK_NAME";

    public static final String EXTRA_AUDIO_FOLDER_NAME = "com.fesskiev.player.action.EXTRA_AUDIO_FOLDER_NAME";
    public static final String EXTRA_AUDIO_TRACK_NAME = "com.fesskiev.player.action.EXTRA_AUDIO_TRACK_NAME";

    public static final String EXTRA_VIDEO_FILE_NAME = "com.fesskiev.player.action.EXTRA_VIDEO_FILE_NAME";


    public FileSystemIntentService() {
        super(FileSystemIntentService.class.getName());
    }


    public static void startFetchMedia(Context context) {
        Intent intent = new Intent(context, FileSystemIntentService.class);
        intent.setAction(ACTION_START_FETCH_MEDIA_SERVICE);
        context.startService(intent);
    }

    public static void startFetchAudio(Context context) {
        Intent intent = new Intent(context, FileSystemIntentService.class);
        intent.setAction(ACTION_START_FETCH_AUDIO_SERVICE);
        context.startService(intent);
    }

    public static void startFetchVideo(Context context) {
        Intent intent = new Intent(context, FileSystemIntentService.class);
        intent.setAction(ACTION_START_FETCH_VIDEO_SERVICE);
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
                case ACTION_START_FETCH_MEDIA_SERVICE:
                    getMediaContent();
                    break;
                case ACTION_START_FETCH_VIDEO_SERVICE:
                    getVideoContent();
                    break;
                case ACTION_START_FETCH_AUDIO_SERVICE:
                    getAudioContent();
                    break;
                case ACTION_CHECK_DOWNLOAD_FOLDER_SERVICE:
                    checkAudioFolderService();
                    break;
            }
        }
    }

    private void checkAudioFolderService() {
        DataRepository repository = MediaApplication.getInstance().getRepository();
        String folderId = null;
        File root = new File(CacheManager.CHECK_DOWNLOADS_FOLDER_PATH);
        File[] list = root.listFiles();
        for (File child : list) {

            if (!repository.containAudioTrack(child.getAbsolutePath())) {
                if (folderId == null) {
                    folderId = repository.getDownloadFolderID();
                }
                new Thread(new FetchDownloadAudioInfo(child, folderId)).start();
            }
        }
    }

    private void getAudioContent() {
        String sdCardState = Environment.getExternalStorageState();
        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            sendStartFetchMediaBroadcast();

            File root = Environment.getExternalStorageDirectory();
            walkAudio(root.getAbsolutePath());

            sendEndFetchMediaBroadcast();

        } else {
            Log.wtf(TAG, "NO SD CARD!");
        }

    }

    private void getVideoContent() {
        String sdCardState = Environment.getExternalStorageState();
        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            sendStartFetchMediaBroadcast();

            File root = Environment.getExternalStorageDirectory();
            walkVideo(root.getAbsolutePath());

            sendEndFetchMediaBroadcast();

        } else {
            Log.wtf(TAG, "NO SD CARD!");
        }
    }

    private void getMediaContent() {
        String sdCardState = Environment.getExternalStorageState();
        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            sendStartFetchMediaBroadcast();

            File root = Environment.getExternalStorageDirectory();
            walk(root.getAbsolutePath());

            sendEndFetchMediaBroadcast();

        } else {
            Log.wtf(TAG, "NO SD CARD!");
        }
    }

    public void walkAudio(String path) {
        File root = new File(path);
        File[] list = root.listFiles();
        if (list == null) {
            Log.w(TAG, "Root is null");
            return;
        }
        for (File child : list) {
            if (child.isDirectory()) {
                checkAudioFilesFolder(child);
                walkAudio(child.getAbsolutePath());
            }
        }
    }

    public void walkVideo(String path) {
        File root = new File(path);
        File[] list = root.listFiles();
        if (list == null) {
            Log.w(TAG, "Root is null");
            return;
        }
        for (File child : list) {
            checkMediaFile(child);
            walkVideo(child.getAbsolutePath());
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
                walk(child.getAbsolutePath());
            } else if (child.isFile()) {
                checkMediaFile(child);
            }
        }
    }


    public boolean checkDownloadFolder(File file) {
        return file.getAbsolutePath().equals(CacheManager.CHECK_DOWNLOADS_FOLDER_PATH);
    }

    private void checkMediaFile(File file) {
        String path = file.getAbsolutePath().toLowerCase();
        if (path.endsWith(".mp4") ||
                path.endsWith(".avi") ||
                path.endsWith(".ts") ||
                path.endsWith(".mkv")) {

            VideoFile videoFile = new VideoFile(file);
            Log.w(TAG, "create video file!: " + file.getAbsolutePath());
            LocalDataSource.getInstance().insertVideoFile(videoFile);
            sendVideoFileBroadcast(videoFile.description);
        }
    }


    private void checkAudioFilesFolder(File child) {
        File[] directoryFiles = child.listFiles();

        if (directoryFiles != null) {
            for (File directoryFile : directoryFiles) {
                File[] audioFiles = directoryFile.listFiles(audioFilter());
                if (audioFiles != null && audioFiles.length > 0) {
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
                    File correctPath = new File(Utils.replaceSymbols(directoryFile.getName()));

                    audioFolder.folderPath = correctPath;
                    audioFolder.folderName = correctPath.getName();
                    audioFolder.id = UUID.randomUUID().toString();

                    sendAudioFolderNameBroadcast(audioFolder.folderName);

                    CountDownLatch latch = new CountDownLatch(audioFiles.length);

                    for (File file : audioFiles) {
                        new Thread(new FetchAudioInfo(audioFolder, file, latch)).start();
                    }

                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    MediaApplication.getInstance().getRepository().insertAudioFolder(audioFolder);

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
                    audioFile -> {
                        audioFile.id = id;
                        MediaApplication.getInstance().getRepository().insertAudioFile(audioFile);

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

            final AudioFile audioFile = new AudioFile(getApplicationContext(), file, audioFile1 -> {
                audioFile1.id = audioFolder.id;

                MediaApplication.getInstance().getRepository().insertAudioFile(audioFile1);

                sendAudioTrackNameBroadcast(audioFile1.artist + "-" + audioFile1.title);
                latch.countDown();
            });

            audioFolder.audioFiles.add(audioFile);
        }
    }

    public void sendStartFetchMediaBroadcast() {
        Intent intent = new Intent(ACTION_START_FETCH_MEDIA_CONTENT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void sendEndFetchMediaBroadcast() {
        Intent intent = new Intent(ACTION_END_FETCH_MEDIA_CONTENT);
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

    public void sendVideoFileBroadcast(String fileName) {
        Intent intent = new Intent(ACTION_VIDEO_FILE);
        intent.putExtra(EXTRA_VIDEO_FILE_NAME, fileName);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    private FilenameFilter audioFilter() {
        return (dir, name) -> {
            String lowercaseName = name.toLowerCase();
            return lowercaseName.endsWith(".mp3") || lowercaseName.endsWith(".flac");
        };
    }

    private FilenameFilter folderImageFilter() {
        return (dir, name) -> {
            String lowercaseName = name.toLowerCase();
            return (lowercaseName.endsWith(".png") || lowercaseName.endsWith(".jpg"));
        };
    }
}
