package com.fesskiev.player.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContentResolverCompat;
import android.util.Log;

import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    public static boolean containsFolder(Context context, File file) {
        Cursor cursor = ContentResolverCompat.query(context.getContentResolver(),
                MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                null,
                MediaCenterProvider.FOLDER_PATH + "=" + "'" + file.getAbsolutePath() + "'",
                null,
                null,
                null);
        boolean contain = cursor.getCount() > 0;
        cursor.close();
        return contain;
    }

    public static boolean containsTrack(Context context, File file) {
        Cursor cursor = ContentResolverCompat.query(context.getContentResolver(),
                MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                null,
                MediaCenterProvider.TRACK_PATH + "=" + "'" + file.getAbsolutePath() + "'",
                null,
                null,
                null);
        boolean contain = cursor.getCount() > 0;
        cursor.close();
        return contain;
    }

    public static String getFolderID(Context context, File file) {
        Cursor cursor = ContentResolverCompat.query(context.getContentResolver(),
                MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                new String[]{MediaCenterProvider.ID},
                MediaCenterProvider.TRACK_PATH + "=" + "'" + file.getAbsolutePath() + "'",
                null,
                null,
                null);

        String id = null;
        if (cursor.getCount() > 0) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                id = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.ID));
            }
        }
        cursor.close();
        return id;
    }

    public static void resetDatabase(Context context) {
        context.getContentResolver().delete( MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI, null, null);
        context.getContentResolver().delete( MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI, null, null);
    }


    public static List<AudioFolder> getAudioFolders(Context context) {
        List<AudioFolder> audioFolders = null;
        Cursor cursor = ContentResolverCompat.query(context.getContentResolver(),
                MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                null,
                null,
                null,
                null,
                null);

        if (cursor.getCount() > 0) {
            audioFolders = new ArrayList<>();

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                AudioFolder audioFolder = new AudioFolder(cursor);
                Log.d(MediaCenterProvider.TAG, "folder: " + audioFolder.toString() + "\n");
                audioFolders.add(audioFolder);
            }
        }
        cursor.close();

        return audioFolders;
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

    public static void deleteAudioFolder(Context context, AudioFolder audioFolder) {

    }

    public static void updateAudioFolder(Context context, AudioFolder audioFolder) {

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
        dateValues.put(MediaCenterProvider.TRACK_COVER, audioFile.artworkBinaryData);


        context.getContentResolver().insert(
                MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                dateValues);
    }

    public static List<AudioFile> getAudioFiles(Context context) {
        List<AudioFile> audioFiles = null;

        Cursor cursor = ContentResolverCompat.query(context.getContentResolver(),
                MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                null,
                null,
                null,
                null,
                null);

        if (cursor.getCount() > 0) {
            audioFiles = new ArrayList<>();
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                AudioFile audioFile = new AudioFile(cursor);
                Log.d(MediaCenterProvider.TAG, "file: " + audioFile.toString() + "\n");
                audioFiles.add(audioFile);
            }
        }
        cursor.close();

        return audioFiles;
    }

    public static List<AudioFile> getAudioFilesByID(Context context, String id) {
        Log.d(MediaCenterProvider.TAG, "getAudioFilesByID: " + id);
        List<AudioFile> audioFiles = null;

        Cursor cursor = ContentResolverCompat.query(context.getContentResolver(),
                MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                null,
                MediaCenterProvider.ID + "=" + "'" + id + "'",
                null,
                null,
                null);

        if (cursor.getCount() > 0) {
            audioFiles = new ArrayList<>();
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                AudioFile audioFile = new AudioFile(cursor);
                Log.d(MediaCenterProvider.TAG, "file: " + audioFile.toString() + "\n");
                audioFiles.add(audioFile);
            }
        } else {
            Log.d(MediaCenterProvider.TAG, "cursor empty");
        }
        cursor.close();

        return audioFiles;
    }
}
