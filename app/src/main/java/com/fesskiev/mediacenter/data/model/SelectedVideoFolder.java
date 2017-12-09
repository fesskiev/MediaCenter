package com.fesskiev.mediacenter.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "SelectedVideoFolder")
public class SelectedVideoFolder {


    public String videoFolderId;

    @NonNull
    @PrimaryKey()
    public boolean isSelected;

    public SelectedVideoFolder(String videoFolderId) {
        this.videoFolderId = videoFolderId;
        this.isSelected = true;
    }

    @Override
    public String toString() {
        return "SelectedVideoFolder{" +
                "videoFolderId='" + videoFolderId + '\'' +
                ", isSelected=" + isSelected +
                '}';
    }
}
