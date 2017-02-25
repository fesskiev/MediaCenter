package com.fesskiev.mediacenter.data.model.vk;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Photo {

    @SerializedName("photo_75")
    @Expose
    private String photo75;

    @SerializedName("photo_130")
    @Expose
    private String photo130;

    @SerializedName("photo_604")
    @Expose
    private String photo604;

    @SerializedName("photo_807")
    @Expose
    private String photo807;

    @SerializedName("text")
    @Expose
    private String text;

    @SerializedName("date")
    @Expose
    private int date;

    public String getPhoto75() {
        return photo75;
    }

    public String getPhoto130() {
        return photo130;
    }

    public String getPhoto604() {
        return photo604;
    }

    public String getPhoto807() {
        return photo807;
    }

    public String getText() {
        return text;
    }

    public int getDate() {
        return date;
    }

    public void setPhoto75(String photo75) {
        this.photo75 = photo75;
    }

    public void setPhoto130(String photo130) {
        this.photo130 = photo130;
    }

    public void setPhoto604(String photo604) {
        this.photo604 = photo604;
    }

    public void setPhoto807(String photo807) {
        this.photo807 = photo807;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDate(int date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "photo75='" + photo75 + '\'' +
                ", photo130='" + photo130 + '\'' +
                ", photo604='" + photo604 + '\'' +
                ", photo807='" + photo807 + '\'' +
                ", text='" + text + '\'' +
                ", date=" + date +
                '}';
    }
}
