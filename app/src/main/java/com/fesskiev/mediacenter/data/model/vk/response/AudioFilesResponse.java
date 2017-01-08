package com.fesskiev.mediacenter.data.model.vk.response;


import com.fesskiev.mediacenter.data.model.vk.AudioFiles;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AudioFilesResponse {

    @SerializedName("response")
    @Expose
    private AudioFiles audioFiles;

    public AudioFiles getAudioFiles() {
        return audioFiles;
    }
}
