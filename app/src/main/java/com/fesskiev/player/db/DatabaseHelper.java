package com.fesskiev.player.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContentResolverCompat;

import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.utils.CacheManager;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {


    public static void insertAudioFolder(Context context, AudioFolder audioFolder) {

        ContentValues dateValues = new ContentValues();

        dateValues.put(MediaCenterProvider.ID, audioFolder.id);
        dateValues.put(MediaCenterProvider.FOLDER_PATH, audioFolder.folderPath.getAbsolutePath());
        dateValues.put(MediaCenterProvider.FOLDER_NAME, audioFolder.folderName);
        dateValues.put(MediaCenterProvider.FOLDER_COVER,
                audioFolder.folderImage != null ? audioFolder.folderImage.getAbsolutePath() : null);
        dateValues.put(MediaCenterProvider.FOLDER_INDEX, audioFolder.index);

        context.getContentResolver().insert(
                MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                dateValues);

    }

    public static void updateAudioFolderIndex(Context context, AudioFolder audioFolder) {

        ContentValues dateValues = new ContentValues();
        dateValues.put(MediaCenterProvider.FOLDER_INDEX, audioFolder.index);

        context.getContentResolver().update(MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                dateValues,
                MediaCenterProvider.FOLDER_PATH + "=" + "'" + audioFolder.folderPath + "'",
                null);
    }

    public static void updateAudioFile(Context context, AudioFile audioFile) {

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
        dateValues.put(MediaCenterProvider.TRACK_COVER, audioFile.artworkPath);

        context.getContentResolver().update(MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                dateValues,
                MediaCenterProvider.TRACK_PATH + "=" + "'" + audioFile.filePath + "'",
                null);
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
        dateValues.put(MediaCenterProvider.TRACK_COVER, audioFile.artworkPath);


        context.getContentResolver().insert(MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                dateValues);
    }

    public static boolean containAudioTrack(Context context, String path) {
        Cursor cursor = ContentResolverCompat.query(context.getContentResolver(),
                MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                null,
                MediaCenterProvider.TRACK_PATH + "=" + "'" + path + "'",
                null,
                null,
                null);

        boolean contain = cursor.getCount() > 0;

        cursor.close();

        return contain;
    }

    public static String getDownloadFolderID(Context context) {
        Cursor cursor = ContentResolverCompat.query(context.getContentResolver(),
                MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                new String[]{MediaCenterProvider.ID},
                MediaCenterProvider.FOLDER_PATH + "=" + "'" + CacheManager.CHECK_DOWNLOADS_FOLDER_PATH + "'",
                null,
                null,
                null);

        cursor.moveToFirst();
        String id = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.ID));
        cursor.close();

        return id;
    }

    public static void resetDatabase(Context context) {
        context.getContentResolver().delete(MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI, null, null);
        context.getContentResolver().delete(MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI, null, null);
    }


    public static void deleteAudioFile(Context context, String path) {
        context.getContentResolver().
                delete(MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                        MediaCenterProvider.TRACK_PATH + "=" + "'" + path + "'",
                        null);
    }

    public static List<String> getFoldersPath(Context context) {
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
