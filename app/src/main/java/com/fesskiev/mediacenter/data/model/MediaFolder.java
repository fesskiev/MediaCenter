package com.fesskiev.mediacenter.data.model;


public interface MediaFolder {

    String getId();

    String getPath();

    String getFolderName();

    boolean isHidden();

    long getTimestamp();
}
