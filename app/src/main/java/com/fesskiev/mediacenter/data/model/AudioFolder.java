package com.fesskiev.mediacenter.data.model;


import android.database.Cursor;

import com.fesskiev.mediacenter.data.source.local.db.DatabaseHelper;

import java.io.File;

public class AudioFolder implements Comparable<AudioFolder> {

    public File folderPath;
    public File folderImage;
    public String id;
    public String folderName;
    public int index;
    public int trackCount;
    public long length;
    public long size;
    public long timestamp;
    public boolean isSelected;

    public AudioFolder(Cursor cursor) {

        id = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ID));
        folderName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.FOLDER_NAME));
        folderPath = new File(cursor.getString(cursor.getColumnIndex(DatabaseHelper.FOLDER_PATH)));

        String imagePath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.FOLDER_COVER));
        folderImage = imagePath != null ? new File(imagePath) : null;

        index = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.FOLDER_INDEX));
        isSelected = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.FOLDER_SELECTED)) == 1;
        length = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.FOLDER_LENGTH));
        size = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.FOLDER_SIZE));
        timestamp = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.FOLDER_TIMESTAMP));
        trackCount = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.FOLDER_TRACK_COUNT));
    }

    public AudioFolder() {

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
                ", trackCount=" + trackCount +
                ", length=" + length +
                ", size=" + size +
                ", timestamp=" + timestamp +
                ", isSelected=" + isSelected +
                '}';
    }
}
