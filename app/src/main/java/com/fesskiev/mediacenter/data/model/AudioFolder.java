package com.fesskiev.mediacenter.data.model;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.fesskiev.mediacenter.utils.BitmapHelper;

import java.io.File;

@Entity(tableName = "AudioFolders")
public class AudioFolder implements Comparable<AudioFolder>, Parcelable, MediaFolder {

    @NonNull
    @PrimaryKey()
    public String id;

    public File folderPath;
    public File folderImage;
    public String folderName;
    public int folderIndex;
    public long timestamp;
    public boolean isSelected;
    public boolean isHidden;

    @Ignore
    public BitmapHelper.PaletteColor color;


    public AudioFolder() {

    }

    protected AudioFolder(Parcel in) {
        this.folderPath = (File) in.readSerializable();
        this.folderImage = (File) in.readSerializable();
        this.id = in.readString();
        this.folderName = in.readString();
        this.folderIndex = in.readInt();
        this.timestamp = in.readLong();
        this.isSelected = in.readByte() != 0;
        this.isHidden = in.readByte() != 0;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPath() {
        return folderPath.getAbsolutePath();
    }

    @Override
    public String getFolderName() {
        return folderName;
    }

    @Override
    public boolean isHidden() {
        return isHidden;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean exists() {
        return folderPath.exists();
    }

    @Override
    public int compareTo(AudioFolder another) {
        if (this.folderIndex < another.folderIndex) {
            return -1;
        }
        if (this.folderIndex == another.folderIndex) {
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
                ", folderIndex=" + folderIndex +
                ", timestamp=" + timestamp +
                ", isSelected=" + isSelected +
                ", isHidden=" + isHidden +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.folderPath);
        dest.writeSerializable(this.folderImage);
        dest.writeString(this.id);
        dest.writeString(this.folderName);
        dest.writeInt(this.folderIndex);
        dest.writeLong(this.timestamp);
        dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isHidden ? (byte) 1 : (byte) 0);
    }

    public static final Parcelable.Creator<AudioFolder> CREATOR = new Parcelable.Creator<AudioFolder>() {
        @Override
        public AudioFolder createFromParcel(Parcel source) {
            return new AudioFolder(source);
        }

        @Override
        public AudioFolder[] newArray(int size) {
            return new AudioFolder[size];
        }
    };
}
