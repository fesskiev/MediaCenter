package com.fesskiev.mediacenter.data.model.vk;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Attachment {

    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_PHOTO = "photo";

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("photo")
    @Expose
    private Photo photo;

    @SerializedName("audio")
    @Expose
    private Audio audio;

    public String getType() {
        return type;
    }

    public Photo getPhoto() {
        return photo;
    }

    public Audio getAudio() {
        return audio;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public void setAudio(Audio audio) {
        this.audio = audio;
    }
}
