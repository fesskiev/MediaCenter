package com.fesskiev.player.model;


import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import java.io.File;


public class VideoFile {

    public String filePath;
    public Bitmap frame;
    public String description;

    public VideoFile(String path){
        this.filePath = path;

        fetchVideoData();
    }

    private void fetchVideoData(){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        frame = retriever.getFrameAtTime();

        StringBuilder sb = new StringBuilder();
        String name = new File(filePath).getName();
        if(name.length() >= 14) {
            String cutName = name.substring(name.length() - 14);
            sb.append(cutName);
        }
        sb.append(":");
        sb.append(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        sb.append("x");
        sb.append(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

        description = sb.toString();
        retriever.release();
    }
}
