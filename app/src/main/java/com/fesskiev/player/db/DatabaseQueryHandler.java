package com.fesskiev.player.db;


import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.utils.CacheManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class DatabaseQueryHandler extends AsyncQueryHandler {

    private static final String TAG = DatabaseQueryHandler.class.getSimpleName();

    private static final int TOKEN_INSERT_AUDIO_FOLDER = 1001;
    private static final int TOKEN_UPDATE_AUDIO_FOLDER_INDEX = 1002;
    private static final int TOKEN_UPDATE_AUDIO_FILE = 1003;
    private static final int TOKEN_INSERT_AUDIO_FILE = 1004;
    private static final int TOKEN_CONTAIN_AUDIO_FILE = 1005;
    private static final int TOKEN_DOWNLOAD_FOLDER_ID = 1006;
    private static final int TOKEN_RESET_DATABASE = 1007;
    private static final int TOKEN_DELETE_AUDIO_FILE = 1008;
    private static final int TOKEN_FOLDERS_PATH = 1009;

    public interface DatabaseQueryHandlerListener<T> {
        void onQueryComplete(T result);
    }

    private WeakReference<DatabaseQueryHandlerListener> listener;

    public DatabaseQueryHandler(Context context) {
        super(context.getContentResolver());
    }

    public DatabaseQueryHandler(Context context, DatabaseQueryHandlerListener listener) {
        super(context.getContentResolver());
        this.listener = new WeakReference<>(listener);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);
        Log.d(TAG, "onQueryComplete: " + token);

        switch (token) {
            case TOKEN_CONTAIN_AUDIO_FILE:
                checkContainAudioFile(cursor);
                break;
            case TOKEN_DOWNLOAD_FOLDER_ID:
                queryAudioFolderId(cursor);
                break;
            case TOKEN_FOLDERS_PATH:
                queryFoldersPaths(cursor);
                break;
            case TOKEN_RESET_DATABASE:
                Log.d(TAG, "TOKEN_RESET_DATABASE");
                queryResetDatabase();
                break;

        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void queryResetDatabase() {
        if (listener != null && listener.get() != null) {
            listener.get().onQueryComplete(null);
        }
    }

    private void checkContainAudioFile(Cursor cursor) {
        boolean contain = cursor.getCount() > 0;
        if (listener != null && listener.get() != null) {
            listener.get().onQueryComplete(contain);
        }
    }

    private void queryAudioFolderId(Cursor cursor) {
        cursor.moveToPosition(-1);
        String id = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.ID));
        if (listener != null && listener.get() != null) {
            listener.get().onQueryComplete(id);
        }
    }

    private void queryFoldersPaths(Cursor cursor) {
        List<String> paths;
        if (cursor.getCount() > 0) {
            paths = new ArrayList<>();
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                String path =
                        cursor.getString(cursor.getColumnIndex(MediaCenterProvider.FOLDER_PATH));
                paths.add(path);
            }
            if (listener != null && listener.get() != null) {
                listener.get().onQueryComplete(paths);
            }
        }
    }

    public void insertAudioFolder(AudioFolder audioFolder) {

        ContentValues dateValues = new ContentValues();
        dateValues.put(MediaCenterProvider.ID, audioFolder.id);
        dateValues.put(MediaCenterProvider.FOLDER_PATH, audioFolder.folderPath.getAbsolutePath());
        dateValues.put(MediaCenterProvider.FOLDER_NAME, audioFolder.folderName);
        dateValues.put(MediaCenterProvider.FOLDER_COVER,
                audioFolder.folderImage != null ? audioFolder.folderImage.getAbsolutePath() : null);
        dateValues.put(MediaCenterProvider.FOLDER_INDEX, audioFolder.index);

        startInsert(TOKEN_INSERT_AUDIO_FOLDER,
                null,
                MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                dateValues);

    }

    public void updateAudioFolderIndex(AudioFolder audioFolder) {

        ContentValues dateValues = new ContentValues();
        dateValues.put(MediaCenterProvider.FOLDER_INDEX, audioFolder.index);

        startUpdate(TOKEN_UPDATE_AUDIO_FOLDER_INDEX,
                null,
                MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                dateValues,
                MediaCenterProvider.FOLDER_PATH + "=" + "'" + audioFolder.folderPath + "'",
                null);
    }

    public void updateAudioFile(AudioFile audioFile) {

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
        dateValues.put(MediaCenterProvider.TRACK_IN_PLAY_LIST, audioFile.inPlayList ? 1 : 0);
        dateValues.put(MediaCenterProvider.TRACK_COVER, audioFile.artworkPath);

        startUpdate(0,
                TOKEN_UPDATE_AUDIO_FILE,
                MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                dateValues,
                MediaCenterProvider.TRACK_PATH + "=" + "'" + audioFile.filePath + "'",
                null);
    }

    public void insertAudioFile(AudioFile audioFile) {

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
        dateValues.put(MediaCenterProvider.TRACK_IN_PLAY_LIST, audioFile.inPlayList ? 1 : 0);
        dateValues.put(MediaCenterProvider.TRACK_COVER, audioFile.artworkPath);

        startInsert(0,
                TOKEN_INSERT_AUDIO_FILE,
                MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                dateValues);

    }

    public void containAudioFile(String path) {
        startQuery(0,
                TOKEN_CONTAIN_AUDIO_FILE,
                MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                null,
                MediaCenterProvider.TRACK_PATH + "=" + "'" + path + "'",
                null,
                null);

    }

    public void getDownloadFolderID() {
        startQuery(0,
                TOKEN_DOWNLOAD_FOLDER_ID,
                MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                new String[]{MediaCenterProvider.ID},
                MediaCenterProvider.FOLDER_PATH + "=" + "'" + CacheManager.CHECK_DOWNLOADS_FOLDER_PATH + "'",
                null,
                null);
    }

    public void resetDatabase() {
        startDelete(0,
                -1,
                MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                null,
                null);

        startDelete(0,
                TOKEN_RESET_DATABASE,
                MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                null,
                null);
    }

    public void deleteAudioFile(String path) {
        startDelete(0,
                TOKEN_DELETE_AUDIO_FILE,
                MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                MediaCenterProvider.TRACK_PATH + "=" + "'" + path + "'",
                null);
    }

    public void getFoldersPath() {
        startQuery(0,
                TOKEN_FOLDERS_PATH,
                MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                new String[]{MediaCenterProvider.FOLDER_PATH},
                null,
                null,
                null);
    }
}
