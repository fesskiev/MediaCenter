package com.fesskiev.player.model;


import android.database.Cursor;
import android.util.Log;

import com.fesskiev.player.db.MediaCenterProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudioFolder implements Comparable<AudioFolder> {

    public List<AudioFile> audioFiles;
    public File folderPath;
    public File folderImage;
    public String id;
    public String folderName;
    public int index;

    public AudioFolder(Cursor cursor) {
        audioFiles = new ArrayList<>();

        id = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.ID));
        folderName = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.FOLDER_NAME));
        folderPath = new File(cursor.getString(cursor.getColumnIndex(MediaCenterProvider.FOLDER_PATH)));

        String imagePath = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.FOLDER_COVER));
        folderImage = imagePath != null ? new File(imagePath) : null;

        index = cursor.getInt(cursor.getColumnIndex(MediaCenterProvider.FOLDER_INDEX));
        Log.d("test", "index: " + index);
    }

    public AudioFolder() {
        audioFiles = new ArrayList<>();
        index = Integer.MAX_VALUE;
    }

    @Override
    public int compareTo(AudioFolder another) {
        if (this.index < another.index) {
            return -1;
        }
        if (this.index == another.index) {
            return 0;
        }
        return 1;
    }

    @Override
    public String toString() {
        return "AudioFolder{" +
                "audioFiles=" + audioFiles +
                ", folderPath=" + folderPath +
                ", folderImage=" + folderImage +
                ", id='" + id + '\'' +
                ", folderName='" + folderName + '\'' +
                ", index=" + index +
                '}';
    }
}
