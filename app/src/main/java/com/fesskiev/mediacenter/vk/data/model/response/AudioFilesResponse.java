package com.fesskiev.mediacenter.vk.data.model.response;


import com.fesskiev.mediacenter.vk.data.model.AudioFiles;
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
