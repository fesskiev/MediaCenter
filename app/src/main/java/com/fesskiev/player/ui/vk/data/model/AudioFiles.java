package com.fesskiev.player.ui.vk.data.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class AudioFiles {

    @SerializedName("count")
    @Expose
    private int count;

    @SerializedName("items")
    @Expose
    private List<Audio> audios = new ArrayList<>();

    public List<Audio> getMusicFilesList() {
        return audios;
    }

    public int getCount() {
        return count;
    }
}
