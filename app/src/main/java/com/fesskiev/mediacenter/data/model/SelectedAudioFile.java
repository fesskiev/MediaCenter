package com.fesskiev.mediacenter.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "SelectedAudioFile")
public class SelectedAudioFile {

    public String audioFileId;

    @NonNull
    @PrimaryKey()
    public boolean isSelected;

    public SelectedAudioFile(String audioFileId) {
        this.audioFileId = audioFileId;
        this.isSelected = true;
    }

    @Override
    public String toString() {
        return "SelectedAudioFile{" +
                "audioFileId='" + audioFileId + '\'' +
                ", isSelected=" + isSelected +
                '}';
    }
}
