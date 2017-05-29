package com.fesskiev.mediacenter.data.model.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AlbumResponse {

    @SerializedName("album")
    @Expose
    private Album album;

    public Album getAlbum() {
        return album;
    }

    @Override
    public String toString() {
        return "AlbumResponse{" +
                "album=" + album +
                '}';
    }
}
