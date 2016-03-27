package com.fesskiev.player.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContentResolverCompat;
import android.util.Log;

import com.fesskiev.player.model.AudioFolder;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

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
}
