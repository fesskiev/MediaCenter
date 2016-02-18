package com.fesskiev.player.model;


import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;


public class VideoFile {

    public String filePath;
    public Bitmap frame;
    public String description;

    public VideoFile(String path){
        this.filePath = path;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        frame = retriever.getFrameAtTime();
        description = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        retriever.release();
    }
}
