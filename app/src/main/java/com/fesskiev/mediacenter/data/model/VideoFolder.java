package com.fesskiev.mediacenter.data.model;


import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.fesskiev.mediacenter.data.source.local.db.DatabaseHelper;

import java.io.File;

public class VideoFolder implements Comparable<VideoFolder>, Parcelable, MediaFolder {

    public File folderPath;
    public String id;
    public String folderName;
    public int index;
    public long timestamp;
    public boolean isSelected;
    public boolean isHidden;


    public VideoFolder() {

    }

    public VideoFolder(Cursor cursor) {

        id = cursor.getString(cursor.getColumnIndex(DatabaseHelper.VIDEO_FOLDER_ID));
        folderName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.FOLDER_NAME));
        folderPath = new File(cursor.getString(cursor.getColumnIndex(DatabaseHelper.FOLDER_PATH)));

        index = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.FOLDER_INDEX));
        isSelected = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.FOLDER_SELECTED)) == 1;
        isHidden = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.FOLDER_HIDDEN)) == 1;
        timestamp = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.FOLDER_TIMESTAMP));

    }

    protected VideoFolder(Parcel in) {
        this.folderPath = (File) in.readSerializable();
        this.id = in.readString();
        this.folderName = in.readString();
        this.index = in.readInt();
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
    public int compareTo(VideoFolder another) {
        if (this.index < another.index) {
            return -1;
        }
        if (this.index == another.index) {
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
        dest.writeInt(this.index);
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
                ", index=" + index +
                ", timestamp=" + timestamp +
                ", isSelected=" + isSelected +
                ", isHidden=" + isHidden +
                '}';
    }
}
