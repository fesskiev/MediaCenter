package com.fesskiev.mediacenter.data.model.vk;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CopyHistory {

    @SerializedName("text")
    @Expose
    public String text;

    @SerializedName("attachments")
    @Expose
    public List<Attachment> attachments = new ArrayList<>();


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    @Override
    public String toString() {
        return "CopyHistory{" +
                ", text='" + text + '\'' +
                ", attachments=" + attachments +
                '}';
    }
}