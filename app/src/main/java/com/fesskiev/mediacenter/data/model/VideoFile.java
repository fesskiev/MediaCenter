package com.fesskiev.mediacenter.data.model;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Parcel;
import android.os.Parcelable;

import com.fesskiev.mediacenter.data.source.local.db.DatabaseHelper;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


public class VideoFile implements MediaFile, Parcelable {

    public String folderId;
    public String fileId;
    public File filePath;
    public String framePath;
    public String description;
    public String resolution;
    public boolean inPlayList;
    public boolean isHidden;
    public long size;
    public long timestamp;
    public long length;

    public VideoFile(Cursor cursor) {

        folderId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.VIDEO_FOLDER_ID));
        fileId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.VIDEO_FILE_ID));
        filePath = new File(cursor.getString(cursor.getColumnIndex(DatabaseHelper.VIDEO_FILE_PATH)));
        framePath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.VIDEO_FRAME_PATH));
        resolution = cursor.getString(cursor.getColumnIndex(DatabaseHelper.VIDEO_RESOLUTION));
        description = cursor.getString(cursor.getColumnIndex(DatabaseHelper.VIDEO_DESCRIPTION));
        inPlayList = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VIDEO_IN_PLAY_LIST)) == 1;
        isHidden = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VIDEO_HIDDEN)) == 1;
        length = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.VIDEO_LENGTH));
        size = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.VIDEO_SIZE));
        timestamp = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.VIDEO_TIMESTAMP));
    }

    public VideoFile(File path, String folderId) {
        this.folderId = folderId;
        this.fileId = UUID.randomUUID().toString();
        File newPath = new File(path.getParent(), Utils.replaceSymbols(path.getName()));
        boolean rename = path.renameTo(newPath);
        if (rename) {
            filePath = newPath;
        }

        fetchVideoData();
    }

    protected VideoFile(Parcel in) {
        this.folderId = in.readString();
        this.fileId = in.readString();
        this.filePath = (File) in.readSerializable();
        this.framePath = in.readString();
        this.description = in.readString();
        this.resolution = in.readString();
        this.inPlayList = in.readByte() != 0;
        this.isHidden = in.readByte() != 0;
        this.size = in.readLong();
        this.timestamp = in.readLong();
        this.length = in.readLong();
    }

    public VideoFile fetchVideoData() {

        size = filePath.length();

        timestamp = System.currentTimeMillis();

        try {

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath.getAbsolutePath());

            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (duration != null) {
                length = Integer.valueOf(duration);
            }

            Bitmap frame = retriever.getFrameAtTime(ThreadLocalRandom.current().nextInt(0, (int) length) * 1000000);
            if (frame != null) {
                saveFrame(frame);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            sb.append("x");
            sb.append(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            resolution = sb.toString();

            description = filePath.getName();
            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }

    private void saveFrame(Bitmap bitmap) {
        try {

            File dir = new File(CacheManager.IMAGES_VIDEO_CACHE_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File path = File.createTempFile(UUID.randomUUID().toString(), ".jpg", dir);

            BitmapHelper.getInstance().saveBitmap(bitmap, path);

            framePath = path.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getId() {
        return fileId;
    }

    @Override
    public MEDIA_TYPE getMediaType() {
        return MEDIA_TYPE.VIDEO;
    }

    @Override
    public String getTitle() {
        return description;
    }

    @Override
    public String getArtworkPath() {
        return framePath;
    }

    @Override
    public String getFileName() {
        return filePath.getName();
    }

    @Override
    public String getFilePath() {
        return filePath.getPath();
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean exists() {
        return filePath.exists();
    }

    @Override
    public boolean inPlayList() {
        return inPlayList;
    }

    @Override
    public void setToPlayList(boolean inPlaylist) {
        this.inPlayList = inPlaylist;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VideoFile videoFile = (VideoFile) o;

        if (size != videoFile.size) return false;
        if (timestamp != videoFile.timestamp) return false;
        if (length != videoFile.length) return false;
        return fileId != null ? fileId.equals(videoFile.fileId) : videoFile.fileId == null;
    }

    @Override
    public int hashCode() {
        int result = fileId != null ? fileId.hashCode() : 0;
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) (length ^ (length >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "VideoFile{" +
                "folderId='" + folderId + '\'' +
                ", fileId=" + fileId +
                ", filePath=" + filePath +
                ", framePath='" + framePath + '\'' +
                ", description='" + description + '\'' +
                ", resolution='" + resolution + '\'' +
                ", inPlayList=" + inPlayList +
                ", isHidden=" + isHidden +
                ", size=" + size +
                ", timestamp=" + timestamp +
                ", length=" + length +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.folderId);
        dest.writeString(this.fileId);
        dest.writeSerializable(this.filePath);
        dest.writeString(this.framePath);
        dest.writeString(this.description);
        dest.writeString(this.resolution);
        dest.writeByte(this.inPlayList ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isHidden ? (byte) 1 : (byte) 0);
        dest.writeLong(this.size);
        dest.writeLong(this.timestamp);
        dest.writeLong(this.length);
    }

    public static final Creator<VideoFile> CREATOR = new Creator<VideoFile>() {
        @Override
        public VideoFile createFromParcel(Parcel source) {
            return new VideoFile(source);
        }

        @Override
        public VideoFile[] newArray(int size) {
            return new VideoFile[size];
        }
    };
}
