package com.fesskiev.mediacenter.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "SelectedVideoFile")
public class SelectedVideoFile {

    public String videoFileId;

    @NonNull
    @PrimaryKey()
    public boolean isSelected;

    public SelectedVideoFile(String videoFileId) {
        this.videoFileId = videoFileId;
        this.isSelected = true;
    }

    @Override
    public String toString() {
        return "SelectedVideoFile{" +
                "videoFileId='" + videoFileId + '\'' +
                ", isSelected=" + isSelected +
                '}';
    }
}
