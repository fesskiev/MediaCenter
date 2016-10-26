package com.fesskiev.player.data.model;


import android.database.Cursor;

import com.fesskiev.player.data.source.local.db.DatabaseHelper;

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
    public boolean isSelected;

    public AudioFolder(Cursor cursor) {
        audioFiles = new ArrayList<>();

        id = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ID));
        folderName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.FOLDER_NAME));
        folderPath = new File(cursor.getString(cursor.getColumnIndex(DatabaseHelper.FOLDER_PATH)));

        String imagePath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.FOLDER_COVER));
        folderImage = imagePath != null ? new File(imagePath) : null;

        index = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.FOLDER_INDEX));
        isSelected = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.FOLDER_SELECTED)) == 1;
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
                "folderPath=" + folderPath +
                ", folderImage=" + folderImage +
                ", id='" + id + '\'' +
                ", folderName='" + folderName + '\'' +
                ", index=" + index +
                ", isSelected=" + isSelected +
                '}';
    }
}
