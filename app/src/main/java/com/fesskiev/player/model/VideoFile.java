package com.fesskiev.player.model;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import com.fesskiev.player.db.MediaCenterProvider;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.CacheManager;

import java.io.File;
import java.io.IOException;
import java.util.UUID;


public class VideoFile implements MediaFile {

    public String id;
    public String filePath;
    public String framePath;
    public String description;

    public VideoFile(Cursor cursor) {

        id = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.ID));
        filePath = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.VIDEO_FILE_PATH));
        framePath = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.VIDEO_FRAME_PATH));
        description = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.VIDEO_DESCRIPTION));
    }

    public VideoFile(String path) {
        this.filePath = path;

        fetchVideoData();
    }

    private void fetchVideoData() {
        id = UUID.randomUUID().toString();
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
            saveFrame(retriever.getFrameAtTime());

            StringBuilder sb = new StringBuilder();
            String name = new File(filePath).getName();
            if (name.length() >= 14) {
                String cutName = name.substring(name.length() - 14);
                sb.append(cutName);
            }
            sb.append(":");
            sb.append(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            sb.append("x");
            sb.append(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

            description = sb.toString();
            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveFrame(Bitmap bitmap) {
        try {

            File path = File.createTempFile(UUID.randomUUID().toString(),
                    ".png", new File(CacheManager.IMAGES_CACHE_PATH));

            BitmapHelper.saveBitmap(bitmap, path);

            framePath = path.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VideoFile videoFile = (VideoFile) o;

        if (id != null ? !id.equals(videoFile.id) : videoFile.id != null) return false;
        return filePath != null ? filePath.equals(videoFile.filePath) : videoFile.filePath == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (filePath != null ? filePath.hashCode() : 0);
        return result;
    }
}
