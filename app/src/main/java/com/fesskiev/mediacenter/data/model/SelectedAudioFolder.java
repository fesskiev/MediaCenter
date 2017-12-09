package com.fesskiev.mediacenter.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "SelectedAudioFolder")
public class SelectedAudioFolder {

    public String audioFolderId;

    @NonNull
    @PrimaryKey()
    public boolean isSelected;

    public SelectedAudioFolder(String audioFolderId) {
        this.audioFolderId = audioFolderId;
        this.isSelected = true;
    }

    @Override
    public String toString() {
        return "SelectedAudioFolder{" +
                "audioFolderId='" + audioFolderId + '\'' +
                ", isSelected=" + isSelected +
                '}';
    }
}
