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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.util.Log;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.VideoFolder;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.data.source.local.db.LocalDataSource;
import com.fesskiev.mediacenter.data.source.memory.MemoryDataSource;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.NotificationHelper;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.StorageUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import rx.Observable;


public class FileSystemService extends JobService {

    public enum SCAN_TYPE {
        AUDIO, VIDEO, BOTH
    }

    public enum SCAN_STATE {
        PREPARE, SCANNING, FINISHED
    }


    private static final String TAG = FileSystemService.class.getSimpleName();

    private static Uri MEDIA_URI = Uri.parse("content://" + MediaStore.AUTHORITY + "/");

    private static final int MEDIA_CONTENT_JOB = 31;


    private static final String ACTION_START_FETCH_MEDIA = "com.fesskiev.player.action.FETCH_MEDIA";
    private static final String ACTION_START_FETCH_FOUND_MEDIA = "com.fesskiev.player.action.FETCH_FOUND_MEDIA";
    private static final String ACTION_START_FETCH_AUDIO = "com.fesskiev.player.action.START_FETCH_AUDIO";
    private static final String ACTION_START_FETCH_VIDEO = "com.fesskiev.player.action.START_FETCH_VIDEO";

    private static final String ACTION_CHECK_DOWNLOAD_FOLDER = "com.fesskiev.player.action.CHECK_DOWNLOAD_FOLDER";

    public static final String EXTRA_MEDIA_PATH = "com.fesskiev.player.extra.EXTRA_MEDIA_PATH";


    private Handler handler;
    private FetchContentThread fetchContentThread;
    private MediaObserver observer;

    public static volatile boolean shouldContinue;

    private SCAN_TYPE scanType;
    private SCAN_STATE scanState;

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
        intent.setAction(ACTION_START_FETCH_MEDIA);
        context.startService(intent);
    }

    public static void startFetchFoundMedia(Context context, String path) {
        Intent intent = new Intent(context, FileSystemService.class);
        intent.setAction(ACTION_START_FETCH_FOUND_MEDIA);
        intent.putExtra(EXTRA_MEDIA_PATH, path);
        context.startService(intent);
    }

    public static void startFetchAudio(Context context) {
        Intent intent = new Intent(context, FileSystemService.class);
        intent.setAction(ACTION_START_FETCH_AUDIO);
        context.startService(intent);
    }

    public static void startFetchVideo(Context context) {
        Intent intent = new Intent(context, FileSystemService.class);
        intent.setAction(ACTION_START_FETCH_VIDEO);
        context.startService(intent);
    }

    public static void startCheckDownloadFolderService(Context context) {
        Intent intent = new Intent(context, FileSystemService.class);
        intent.setAction(ACTION_CHECK_DOWNLOAD_FOLDER);
        context.startService(intent);
    }


    public static void scheduleJob(Context context, int periodic) {

        JobScheduler js = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        JobInfo.Builder builder = new JobInfo.Builder(MEDIA_CONTENT_JOB,
                new ComponentName(context, FileSystemService.class));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setMinimumLatency(periodic);
        } else {
            builder.setPeriodic(periodic);
        }
        builder.setOverrideDeadline(periodic);
        builder.setRequiresDeviceIdle(false);
        builder.setRequiresCharging(false);

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
                    case ACTION_START_FETCH_MEDIA:
                        handler.sendEmptyMessage(0);
                        break;
                    case ACTION_START_FETCH_VIDEO:
                        handler.sendEmptyMessage(1);
                        break;
                    case ACTION_START_FETCH_AUDIO:
                        handler.sendEmptyMessage(2);
                        break;
                    case ACTION_CHECK_DOWNLOAD_FOLDER:
                        handler.sendEmptyMessage(3);
                        break;
                    case ACTION_START_FETCH_FOUND_MEDIA:
                        Message msg = handler.obtainMessage();
                        msg.obj = intent.getStringExtra(EXTRA_MEDIA_PATH);
                        msg.what = 4;
                        handler.sendMessage(msg);
                        break;
                }
            }
        }
        return START_NOT_STICKY;
    }

    private void prepareScan() {
        scanState = SCAN_STATE.PREPARE;
        EventBus.getDefault().post(FileSystemService.this);
    }

    private void startScan() {
        shouldContinue = true;
        scanState = SCAN_STATE.SCANNING;
        EventBus.getDefault().post(FileSystemService.this);
    }

    private void finishScan() {
        scanState = SCAN_STATE.FINISHED;
        shouldContinue = false;
        EventBus.getDefault().post(FileSystemService.this);

    }

    private void startScan(SCAN_TYPE scanType, String path) {
        prepareScan();
        if (path == null) {
            List<StorageUtils.StorageInfo> storageInfos = StorageUtils.getStorageList();
            if (storageInfos != null && !storageInfos.isEmpty()) {
                startScan();
                for (StorageUtils.StorageInfo storageInfo : storageInfos) {
                    Log.e(TAG, "storage: " + storageInfo.getDisplayName() + " path: " + storageInfo.path);
                    fileWalk(storageInfo.path, scanType);
                }
            }
        } else {
            startScan();

            fileWalk(path, scanType);
        }

        finishScan();
    }

    private void fileWalk(String startPath, SCAN_TYPE scanType) {
        Iterator<File> iterator = FileUtils.iterateFilesAndDirs(new File(startPath),
                TrueFileFilter.INSTANCE,
                new IOFileFilter() {
                    @Override
                    public boolean accept(File file) {
                        try {
                            return isPlainDir(file);
                        } catch (IOException ex) {
                            return false;
                        }
                    }

                    @Override
                    public boolean accept(File dir, String name) {
                        try {
                            return isPlainDir(dir);
                        } catch (IOException ex) {
                            return false;
                        }
                    }
                });

        File n;
        try {
            while (iterator.hasNext() && shouldContinue) {
                n = iterator.next();

                if (n.getAbsolutePath().equals(CacheManager.EXTERNAL_STORAGE)) {
                    continue;
                }
                if (isPlainDir(n)) {
                    checkDir(n, scanType);
                } else {
                    checkFile(n, scanType);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private void checkFile(File file, SCAN_TYPE scanType) {
        switch (scanType) {
            case BOTH:
                filterAudioFile(file);
                filterVideoFile(file);
                break;
            case AUDIO:
                filterAudioFile(file);
                break;
            case VIDEO:
                filterVideoFile(file);
                break;
        }
    }

    private void filterVideoFile(File file) {

    }

    private void filterAudioFile(File file) {

    }

    private void checkDir(File dir, SCAN_TYPE scanType) {
        switch (scanType) {
            case BOTH:
                filterAudioFolders(dir);
                filterVideoFolders(dir);
                break;
            case AUDIO:
                filterAudioFolders(dir);
                break;
            case VIDEO:
                filterVideoFolders(dir);
                break;
        }

    }

    private void sendFileDescription(String name) {
        EventBus.getDefault().post(new FetchDescription(null, name));
    }

    private void sendFolderDescription(String name) {
        EventBus.getDefault().post(new FetchDescription(name, null));
    }

    private void sendFolderCreated(int type) {
        EventBus.getDefault().post(new FetchFolderCreate(type));
    }

    private void filterVideoFolders(File directoryFile) {
        File[] videoPaths = directoryFile.listFiles(videoFilter());
        if (videoPaths != null && videoPaths.length > 0) {

            VideoFolder videoFolder = new VideoFolder();

            videoFolder.folderPath = directoryFile;
            videoFolder.folderName = directoryFile.getName();
            videoFolder.id = UUID.randomUUID().toString();
            videoFolder.timestamp = System.currentTimeMillis();


            sendFolderDescription(videoFolder.folderName);

            for (File path : videoPaths) {

                VideoFile videoFile = new VideoFile(path);
                videoFile.id = videoFolder.id;

                LocalDataSource.getInstance().insertVideoFile(videoFile);

                sendFileDescription(videoFile.description);


            }

            MediaApplication.getInstance().getRepository().insertVideoFolder(videoFolder);

            sendFolderCreated(FetchFolderCreate.VIDEO);

        }
    }

    private void filterAudioFolders(File directoryFile) {

        File[] audioPaths = directoryFile.listFiles(audioFilter());
        if (audioPaths != null && audioPaths.length > 0) {

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

            sendFolderDescription(audioFolder.folderName);
            AppLog.INFO("FOLDER CREATED: " + audioFolder.toString());

            for (File path : audioPaths) {

                new AudioFile(getApplicationContext(), path, audioFolder.id, audioFile -> {

                    MediaApplication.getInstance().getRepository().insertAudioFile(audioFile);

                    sendFileDescription(audioFile.artist + "-" + audioFile.title);
                });
            }

            MediaApplication.getInstance().getRepository().insertAudioFolder(audioFolder);

            sendFolderCreated(FetchFolderCreate.AUDIO);
        }
    }

    private class MediaObserver extends ContentObserver {

        private Set<String> foldersPath;

        private long lastTimeCall = 0L;
        private long lastTimeUpdate = 0L;
        private long threshold = 100;

        public MediaObserver(Handler handler) {
            super(handler);
            foldersPath = new TreeSet<>();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange);
            Log.e(TAG, "onChange");

            lastTimeCall = System.currentTimeMillis();

            if (lastTimeCall - lastTimeUpdate > threshold) {

                Cursor cursor = null;
                try {
                    String[] projection = {MediaStore.Audio.Media.DATA};
                    cursor = getContentResolver().query(uri, projection, null, null, null);
                    if (cursor != null) {
                        if (cursor.moveToLast()) {
                            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

                            File file = new File(path);
                            File parent = file.getParentFile();
                            if (parent.isDirectory()) {
                                String parentPath = parent.getAbsolutePath();
                                if (!foldersPath.contains(parentPath)) {
                                    File[] audioPaths = parent.listFiles(audioFilter());
                                    if (audioPaths != null && audioPaths.length > 0) {
                                        foldersPath.add(parentPath);
                                        NotificationHelper.getInstance(getApplicationContext())
                                                .createMediaFoundNotification(parent);
                                    }

                                    File[] videoPaths = parent.listFiles(videoFilter());
                                    if (videoPaths != null && videoPaths.length > 0) {
                                        foldersPath.add(parentPath);
                                        NotificationHelper.getInstance(getApplicationContext())
                                                .createMediaFoundNotification(parent);
                                    }

                                }
                            }
                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

                lastTimeUpdate = System.currentTimeMillis();
            }
        }

        public void removeFoundPath(String path) {
            if (foldersPath.contains(path)) {
                foldersPath.remove(path);
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
                        case 4:
                            getFoundMediaContent(msg);
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
                        NotificationHelper.getInstance(getApplicationContext()).createFetchNotification();
                    }

                    getMediaContent();

                    if (jobParameters != null) {
                        int interval = (int) AppSettingsManager.getInstance().getMediaContentUpdateTime();
                        if (interval > 0) {
                            scheduleJob(getApplicationContext(), interval);
                            jobFinished(jobParameters, false);
                        } else {
                            jobFinished(jobParameters, true);
                        }
                    }
                });
    }

    private void clearImagesCache() {
        CacheManager.clearVideoImagesCache();
        CacheManager.clearAudioImagesCache();
        BitmapHelper.getInstance().saveDownloadFolderIcon();
    }

    private void refreshRepository(DataRepository repository) {
        MemoryDataSource memoryDataSource = repository.getMemorySource();

        memoryDataSource.setCacheArtistsDirty(true);
        memoryDataSource.setCacheGenresDirty(true);
        memoryDataSource.setCacheFoldersDirty(true);
        memoryDataSource.setCacheVideoFoldersDirty(true);
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
        scanType = SCAN_TYPE.AUDIO;
        startScan(scanType, null);
    }

    private void getVideoContent() {
        scanType = SCAN_TYPE.VIDEO;
        startScan(scanType, null);
    }

    private void getMediaContent() {
        scanType = SCAN_TYPE.BOTH;
        startScan(scanType, null);
    }

    private void getFoundMediaContent(Message msg) {
        String path = (String) msg.obj;
        AppLog.INFO("fetch found media: " + path);
        observer.removeFoundPath(path);

        scanType = SCAN_TYPE.BOTH;
        startScan(scanType, path);

    }

    public boolean checkDownloadFolder(File file) {
        return file.getAbsolutePath().equals(CacheManager.CHECK_DOWNLOADS_FOLDER_PATH);
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

    private FilenameFilter videoFilter() {
        return (dir, name) -> {
            String lowercaseName = name.toLowerCase();
            return (lowercaseName.endsWith(".mp4") || lowercaseName.endsWith(".ts") || lowercaseName.endsWith(".mkv"));
        };
    }


    /**
     * Determine if {@code file} is a directory and is not a symbolic link.
     *
     * @param file File to test.
     * @return True if {@code file} is a directory and is not a symbolic link.
     * @throws IOException If a symbolic link could not be determined. This is ultimately
     *                     caused by a call to {@link File#getCanonicalFile()}.
     */
    private static boolean isPlainDir(File file) throws IOException {
        return file.isDirectory() && !isSymbolicLink(file);
    }

    /**
     * Given a {@link File} object, test if it is likely to be a symbolic link.
     *
     * @param file File to test for symbolic link.
     * @return {@code true} if {@code file} is a symbolic link.
     * @throws NullPointerException If {@code file} is null.
     * @throws IOException          If a symbolic link could not be determined. This is ultimately
     *                              caused by a call to {@link File#getCanonicalFile()}.
     */
    private static boolean isSymbolicLink(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }
        File canon;
        if (file.getParent() == null) {
            canon = file;
        } else {
            File canonDir = file.getParentFile().getCanonicalFile();
            canon = new File(canonDir, file.getName());
        }
        return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
    }

    public SCAN_TYPE getScanType() {
        return scanType;
    }

    public SCAN_STATE getScanState() {
        return scanState;
    }

    public static class FetchDescription {

        private String folderName;
        private String fileName;

        public FetchDescription(String folderName, String fileName) {
            this.folderName = folderName;
            this.fileName = fileName;
        }

        public String getFolderName() {
            return folderName;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public static class FetchFolderCreate {

        public static final int AUDIO = 0;
        public static final int VIDEO = 1;

        private int type;

        public FetchFolderCreate(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }

}
