package com.fesskiev.player.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContentResolverCompat;
import android.util.Log;

import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioFolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DatabaseHelper {

    private static DatabaseHelper instance;
    private static ExecutorService service;

    private DatabaseHelper() {

    }

    public static DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        service = Executors.newSingleThreadExecutor();
        return instance;
    }

    public static void insertAudioFolder(Context context, AudioFolder audioFolder) {

        ContentValues dateValues = new ContentValues();

        dateValues.put(MediaCenterProvider.ID, audioFolder.id);
        dateValues.put(MediaCenterProvider.FOLDER_PATH, audioFolder.folderPath.getAbsolutePath());
        dateValues.put(MediaCenterProvider.FOLDER_NAME, audioFolder.folderName);
        dateValues.put(MediaCenterProvider.FOLDER_COVER,
                audioFolder.folderImage != null ? audioFolder.folderImage.getAbsolutePath() : null);

        context.getContentResolver().insert(
                MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                dateValues);

    }

    public static void insertAudioFile(Context context, AudioFile audioFile) {

        ContentValues dateValues = new ContentValues();

        dateValues.put(MediaCenterProvider.ID, audioFile.id);
        dateValues.put(MediaCenterProvider.TRACK_ARTIST, audioFile.artist);
        dateValues.put(MediaCenterProvider.TRACK_TITLE, audioFile.title);
        dateValues.put(MediaCenterProvider.TRACK_ALBUM, audioFile.album);
        dateValues.put(MediaCenterProvider.TRACK_GENRE, audioFile.genre);
        dateValues.put(MediaCenterProvider.TRACK_PATH, audioFile.filePath.getAbsolutePath());
        dateValues.put(MediaCenterProvider.TRACK_BITRATE, audioFile.bitrate);
        dateValues.put(MediaCenterProvider.TRACK_LENGTH, audioFile.length);
        dateValues.put(MediaCenterProvider.TRACK_NUMBER, audioFile.trackNumber);
        dateValues.put(MediaCenterProvider.TRACK_SAMPLE_RATE, audioFile.sampleRate);
        dateValues.put(MediaCenterProvider.TRACK_COVER, audioFile.artworkBinaryData);


        context.getContentResolver().insert(
                MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                dateValues);
    }

    public void resetDatabase(Context context) {
        service.submit(new ResetDatabaseTask(context));
        service.shutdown();
    }


    public void deleteAudioFile(Context context, String path) {
        service.submit(new RemoveFileTask(context, path));
        service.shutdown();
    }

    public List<String> getFoldersPath(Context context) {
        Future<List<String>> future = service.submit(new FolderPathTask(context));
        List<String> result = null;
        try {
            result = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        service.shutdown();
        return result;
    }

    private class FolderPathTask implements Callable<List<String>> {

        private Context context;

        public FolderPathTask(Context context) {
            this.context = context;
        }

        @Override
        public List<String> call() throws Exception {

            Cursor cursor = ContentResolverCompat.query(context.getContentResolver(),
                    MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                    new String[]{MediaCenterProvider.FOLDER_PATH},
                    null,
                    null,
                    null,
                    null);
            List<String> paths = null;
            if (cursor.getCount() > 0) {
                paths = new ArrayList<>();
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    String path =
                            cursor.getString(cursor.getColumnIndex(MediaCenterProvider.FOLDER_PATH));
                    paths.add(path);
                }
            }
            cursor.close();

            return paths;
        }
    }

    private class RemoveFileTask implements Runnable {

        private Context context;
        private String path;

        public RemoveFileTask(Context context, String path) {
            this.context = context;
            this.path = path;
        }

        @Override
        public void run() {
            context.getContentResolver().
                    delete(MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                            MediaCenterProvider.TRACK_PATH + "=" + "'" + path + "'",
                            null);
        }
    }

    private class ResetDatabaseTask implements Runnable {

        private Context context;

        public ResetDatabaseTask(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            context.getContentResolver().delete( MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI, null, null);
            context.getContentResolver().delete(MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI, null, null);
        }
    }

}
