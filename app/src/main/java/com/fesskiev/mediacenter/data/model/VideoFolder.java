package com.fesskiev.mediacenter.data.model;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.List;

@Entity(tableName = "VideoFolders")
public class VideoFolder implements Comparable<VideoFolder>, Parcelable, MediaFolder {

    @NonNull
    @PrimaryKey()
    public String id;

    public File folderPath;
    public String folderName;
    public int folderIndex;
    public long timestamp;
    public boolean isSelected;
    public boolean isHidden;

    @Ignore
    public List<Bitmap> frames;


    public VideoFolder() {

    }

    protected VideoFolder(Parcel in) {
        this.folderPath = (File) in.readSerializable();
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
    public boolean isSelected() {
        return isSelected;
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
    public int compareTo(VideoFolder another) {
        if (this.folderIndex < another.folderIndex) {
            return -1;
        }
        if (this.folderIndex == another.folderIndex) {
            return 0;
        }
        return 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.folderPath);
        dest.writeString(this.id);
        dest.writeString(this.folderName);
        dest.writeInt(this.folderIndex);
        dest.writeLong(this.timestamp);
        dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isHidden ? (byte) 1 : (byte) 0);
    }

    public static final Creator<VideoFolder> CREATOR = new Creator<VideoFolder>() {
        @Override
        public VideoFolder createFromParcel(Parcel source) {
            return new VideoFolder(source);
        }

        @Override
        public VideoFolder[] newArray(int size) {
            return new VideoFolder[size];
        }
    };

    @Override
    public String toString() {
        return "VideoFolder{" +
                "folderPath=" + folderPath +
                ", id='" + id + '\'' +
                ", folderName='" + folderName + '\'' +
                ", folderIndex=" + folderIndex +
                ", timestamp=" + timestamp +
                ", isSelected=" + isSelected +
                ", isHidden=" + isHidden +
                '}';
    }
}
