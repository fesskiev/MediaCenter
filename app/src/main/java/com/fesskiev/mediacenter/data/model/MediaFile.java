package com.fesskiev.mediacenter.data.model;


public interface MediaFile {

    MEDIA_TYPE getMediaType();

    String getTitle();

    String getFileName();

    String getFilePath();

    String getArtworkPath();

    long getLength();

    long getSize();

    long getTimestamp();

    boolean exists();

}
