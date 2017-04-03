package com.fesskiev.mediacenter.services;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.data.source.local.db.LocalDataSource;
import com.fesskiev.mediacenter.data.source.memory.MemoryDataSource;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.AudioNotificationHelper;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.RxUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.UUID;

import rx.Observable;


public class FileSystemService extends JobService {

    private static final String TAG = FileSystemService.class.getSimpleName();

    private static Uri MEDIA_URI = Uri.parse("content://" + MediaStore.AUTHORITY + "/");

    private static final int MEDIA_CONTENT_JOB = 31;


    private static final String ACTION_START_FETCH_MEDIA_SERVICE = "com.fesskiev.player.action.FETCH_MEDIA_SERVICE";
    private static final String ACTION_START_FETCH_AUDIO_SERVICE = "com.fesskiev.player.action.START_FETCH_AUDIO_SERVICE";
    private static final String ACTION_START_FETCH_VIDEO_SERVICE = "com.fesskiev.player.action.START_FETCH_VIDEO_SERVICE";

    private static final String ACTION_CHECK_DOWNLOAD_FOLDER_SERVICE = "com.fesskiev.player.action.CHECK_DOWNLOAD_FOLDER_SERVICE";

    public static final String ACTION_START_FETCH_MEDIA_CONTENT = "com.fesskiev.player.action.START_FETCH_MEDIA_CONTENT";
    public static final String ACTION_END_FETCH_MEDIA_CONTENT = "com.fesskiev.player.action.END_FETCH_MEDIA_CONTENT";

    public static final String ACTION_VIDEO_FILE = "com.fesskiev.player.action.VIDEO_FILE";
    public static final String ACTION_AUDIO_FOLDER_NAME = "com.fesskiev.player.action.AUDIO_FOLDER_NAME";
    public static final String ACTION_AUDIO_TRACK_NAME = "com.fesskiev.player.action.AUDIO_TRACK_NAME";
    public static final String ACTION_AUDIO_FOLDER_CREATED = "com.fesskiev.player.action.AUDIO_FOLDER_CREATED";

    public static final String EXTRA_AUDIO_FOLDER_NAME = "com.fesskiev.player.action.EXTRA_AUDIO_FOLDER_NAME";
    public static final String EXTRA_AUDIO_TRACK_NAME = "com.fesskiev.player.action.EXTRA_AUDIO_TRACK_NAME";
    public static final String EXTRA_VIDEO_FILE_NAME = "com.fesskiev.player.action.EXTRA_VIDEO_FILE_NAME";


    private Handler handler;
    private FetchContentThread fetchContentThread;
    private MediaObserver observer;

    public static volatile boolean shouldContinue;

    public static void startFileSystemService(Context context) {
        Intent intent = new Intent(context, FileSystemService.class);
        context.startService(intent);
    }

    public static void stopFileSystemService(Context context) {
        Intent intent = new Intent(context, FileSystemService.class);
        context.stopService(intent);
    }

    public static void startFetchMedia(Context context) {
        Intent intent = new Intent(context, FileSystemService.class);
        intent.setAction(ACTION_START_FETCH_MEDIA_SERVICE);
        context.startService(intent);
    }

    public static void startFetchAudio(Context context) {
        Intent intent = new Intent(context, FileSystemService.class);
        intent.setAction(ACTION_START_FETCH_AUDIO_SERVICE);
        context.startService(intent);
    }

    public static void startFetchVideo(Context context) {
        Intent intent = new Intent(context, FileSystemService.class);
        intent.setAction(ACTION_START_FETCH_VIDEO_SERVICE);
        context.startService(intent);
    }

    public static void startCheckDownloadFolderService(Context context) {
        Intent intent = new Intent(context, FileSystemService.class);
        intent.setAction(ACTION_CHECK_DOWNLOAD_FOLDER_SERVICE);
        context.startService(intent);
    }


    public static void scheduleJob(Context context, int periodic) {

        JobScheduler js = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        JobInfo.Builder builder = new JobInfo.Builder(MEDIA_CONTENT_JOB,
                new ComponentName(context, FileSystemService.class));

        //TODO remove test value
        int testPeriod = 4 * 1000 * 60;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setMinimumLatency(testPeriod);
        } else {
            builder.setPeriodic(testPeriod);
        }
        builder.setOverrideDeadline(testPeriod);
        builder.setRequiresDeviceIdle(false);
        builder.setRequiresCharging(false);

//        builder.addTriggerContentUri(new JobInfo.TriggerContentUri(MEDIA_URI,
//                JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS));
        int jobValue = js.schedule(builder.build());
        if (jobValue == JobScheduler.RESULT_FAILURE) {
            Log.w(TAG, "JobScheduler launch the task failure");
        } else {
            Log.w(TAG, "JobScheduler launch the task success: " + jobValue);
        }

        Log.i(TAG, "JOB SCHEDULED!");
    }

    public static boolean isScheduled(Context context) {
        JobScheduler js = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> jobs = js.getAllPendingJobs();
        for (int i = 0; i < jobs.size(); i++) {
            if (jobs.get(i).getId() == MEDIA_CONTENT_JOB) {
                return true;
            }
        }
        return false;
    }

    public static void cancelJob(Context context) {
        JobScheduler js = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        js.cancel(MEDIA_CONTENT_JOB);

        Log.i(TAG, "JOB SCHEDULED? " + isScheduled(context));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "File System Service created");

        fetchContentThread = new FetchContentThread();
        fetchContentThread.start();

        observer = new MediaObserver(handler);
        getContentResolver().registerContentObserver(
                MEDIA_URI,
                true,
                observer);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "File System Service destroyed");

        fetchContentThread.quitSafely();

        getContentResolver().unregisterContentObserver(observer);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null) {
                Log.w(TAG, "HANDLE INTENT: " + action);
                switch (action) {
                    case ACTION_START_FETCH_MEDIA_SERVICE:
                        handler.sendEmptyMessage(0);
                        break;
                    case ACTION_START_FETCH_VIDEO_SERVICE:
                        handler.sendEmptyMessage(1);
                        break;
                    case ACTION_START_FETCH_AUDIO_SERVICE:
                        handler.sendEmptyMessage(2);
                        break;
                    case ACTION_CHECK_DOWNLOAD_FOLDER_SERVICE:
                        handler.sendEmptyMessage(3);
                        break;
                }
            }
        }
        return START_NOT_STICKY;
    }

    private class MediaObserver extends ContentObserver {

        public MediaObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Cursor cursor = null;
            try {
                String[] projection = {MediaStore.Audio.Media.DATA, MediaStore.Audio.Media._ID};
                cursor = getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToLast()) {
                        String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));

                        Log.w("test_", "OBSERVER CHANGED!: " + path + " id: " + id + " self: " + selfChange);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    private class FetchContentThread extends HandlerThread {

        public FetchContentThread() {
            super(FetchContentThread.class.getName(), Process.THREAD_PRIORITY_BACKGROUND);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            handler = new Handler(getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 0:
                            getMediaContent(msg);
                            break;
                        case 1:
                            getVideoContent();
                            break;
                        case 2:
                            getAudioContent();
                            break;
                        case 3:
                            checkDownloadFolder();
                            break;
                    }
                }
            };
        }
    }

    private void getMediaContent(Message msg) {
        DataRepository repository = MediaApplication.getInstance().getRepository();

        Observable.zip(RxUtils.fromCallable(repository.resetAudioContentDatabase()),
                RxUtils.fromCallable(repository.resetVideoContentDatabase()),
                (integer, integer2) -> Observable.empty())
                .doOnNext(observable -> refreshRepository(repository))
                .doOnNext(observable -> clearImagesCache())
                .subscribe(observable -> {
                    JobParameters jobParameters = (JobParameters) msg.obj;

                    if (jobParameters != null) {
                        AudioNotificationHelper.getInstance(getApplicationContext()).createFetchNotification();
                    }

                    getMediaContent();

                    if (jobParameters != null) {
                        scheduleJob(getApplicationContext(),
                                (int) AppSettingsManager.getInstance().getMediaContentUpdateTime());
                        jobFinished(jobParameters, false);
                    }
                });
    }

    private void clearImagesCache() {
        CacheManager.clearAudioImagesCache();
        BitmapHelper.getInstance().saveDownloadFolderIcon();
    }

    private void refreshRepository(DataRepository repository) {
        MemoryDataSource memoryDataSource = repository.getMemorySource();

        memoryDataSource.setCacheArtistsDirty(true);
        memoryDataSource.setCacheGenresDirty(true);
        memoryDataSource.setCacheFoldersDirty(true);
        memoryDataSource.setCacheVideoFilesDirty(true);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "onStartJob");
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.obj = params;
            msg.what = 0;
            handler.sendMessage(msg);
            Log.i(TAG, "Send job...");
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "onStopJob");
        return false;
    }


    private void checkDownloadFolder() {
        DataRepository repository = MediaApplication.getInstance().getRepository();
        File root = new File(CacheManager.CHECK_DOWNLOADS_FOLDER_PATH);
        File[] list = root.listFiles();
        for (File child : list) {
            if (!repository.containAudioTrack(child.getAbsolutePath())) {
                String folderId = repository.getDownloadFolderID();
                if (folderId == null) {
                    AudioFolder audioFolder = new AudioFolder();
                    audioFolder.folderImage = CacheManager.getDownloadFolderIconPath();
                    audioFolder.folderPath = new File(CacheManager.CHECK_DOWNLOADS_FOLDER_PATH);
                    audioFolder.folderName = "Downloads";
                    audioFolder.id = UUID.randomUUID().toString();

                    repository.insertAudioFolder(audioFolder);

                    repository.getMemorySource().setCacheArtistsDirty(true);
                    repository.getMemorySource().setCacheGenresDirty(true);
                    repository.getMemorySource().setCacheFoldersDirty(true);

                    folderId = audioFolder.id;

                }

                new AudioFile(getApplicationContext(), child, folderId,
                        audioFile -> MediaApplication.getInstance().getRepository().insertAudioFile(audioFile));
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
        shouldContinue = true;
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
        shouldContinue = true;
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
        shouldContinue = true;
        File root = new File(path);
        File[] list = root.listFiles();
        if (list == null) {
            return;
        }
        for (File child : list) {
            checkMediaFile(child);
            walkVideo(child.getAbsolutePath());
        }
    }

    public void walk(String path) {
        if (!shouldContinue) {
            return;
        }
        File root = new File(path);
        File[] list = root.listFiles();
        if (list == null) {
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
                File[] audioPaths = directoryFile.listFiles(audioFilter());
                if (audioPaths != null && audioPaths.length > 0 && shouldContinue) {
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
                    audioFolder.timestamp = System.currentTimeMillis();

                    sendAudioFolderNameBroadcast(audioFolder.folderName);

                    for (File path : audioPaths) {

                        new AudioFile(getApplicationContext(), path, audioFolder.id, audioFile -> {

                            Log.w(TAG, "audio file created");

                            MediaApplication.getInstance().getRepository().insertAudioFile(audioFile);

                            sendAudioTrackNameBroadcast(audioFile.artist + "-" + audioFile.title);
                        });
                    }

                    MediaApplication.getInstance().getRepository().insertAudioFolder(audioFolder);

                    sendAudioFolderCreatedBroadcast();
                }
            }
        }
    }

    public void sendStartFetchMediaBroadcast() {
        Intent intent = new Intent(ACTION_START_FETCH_MEDIA_CONTENT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void sendEndFetchMediaBroadcast() {
        if (shouldContinue) {
            Intent intent = new Intent(ACTION_END_FETCH_MEDIA_CONTENT);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
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

    public void sendAudioFolderCreatedBroadcast() {
        Intent intent = new Intent(ACTION_AUDIO_FOLDER_CREATED);
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
            return lowercaseName.endsWith(".mp3") || lowercaseName.endsWith(".flac") || lowercaseName.endsWith(".wav");
        };
    }

    private FilenameFilter folderImageFilter() {
        return (dir, name) -> {
            String lowercaseName = name.toLowerCase();
            return (lowercaseName.endsWith(".png") || lowercaseName.endsWith(".jpg"));
        };
    }
}
