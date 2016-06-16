package com.fesskiev.player.model;


public interface MediaFile {

    MEDIA_TYPE getMediaType();

    String getTitle();

    String getFileName();

    String getFilePath();

    String getArtworkPath();

    int getLength();

    boolean exists();
}
