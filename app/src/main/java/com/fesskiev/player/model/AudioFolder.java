package com.fesskiev.player.model;


import android.database.Cursor;

import com.fesskiev.player.db.MediaCenterProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudioFolder {

    public String id;
    public File folderPath;
    public File folderImage;
    public List<AudioFile> audioFiles;
    public String folderName;

    public AudioFolder(Cursor cursor) {
        audioFiles = new ArrayList<>();
        id = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.ID));
        folderName = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.FOLDER_NAME));
        folderPath = new File(cursor.getString(cursor.getColumnIndex(MediaCenterProvider.FOLDER_PATH)));

        String imagePath = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.FOLDER_COVER));
        folderImage = imagePath != null ? new File(imagePath) : null;
    }

    public AudioFolder() {
        audioFiles = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "AudioFolder{" +
                "id=" + id +
                ", folderPath=" + folderPath +
                ", folderImage=" + folderImage +
                ", audioFiles=" + audioFiles +
                ", folderName='" + folderName + '\'' +
                '}';
    }
}
