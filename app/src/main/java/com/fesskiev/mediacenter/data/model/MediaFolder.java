package com.fesskiev.mediacenter.data.model;


public interface MediaFolder {

    String getId();

    String getPath();

    String getFolderName();

    long getTimestamp();

    boolean exists();

    boolean isHidden();

    boolean isSelected();
}
