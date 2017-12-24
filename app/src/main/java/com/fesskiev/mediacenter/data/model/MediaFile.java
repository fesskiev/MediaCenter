package com.fesskiev.mediacenter.data.model;


public interface MediaFile {

    String getId();

    MEDIA_TYPE getMediaType();

    String getTitle();

    String getFileName();

    String getFilePath();

    String getArtworkPath();

    long getDuration();

    long getSize();

    long getTimestamp();

    boolean exists();

    boolean inPlayList();

    boolean isSelected();

    boolean isHidden();

    void setToPlayList(boolean inPlaylist);
}
