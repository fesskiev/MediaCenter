package com.fesskiev.player.ui.vk.data.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Audio implements Parcelable {

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("artist")
    @Expose
    private String artist;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("duration")
    @Expose
    private int duration;

    @SerializedName("url")
    @Expose
    private String url;

    @SerializedName("genre_id")
    @Expose
    private int genreId;

    public Audio() {

    }

    public Audio(Parcel in) {
        this.id = in.readInt();
        this.artist = in.readString();
        this.title = in.readString();
        this.duration = in.readInt();
        this.url = in.readString();
        this.genreId = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Audio> CREATOR
            = new Parcelable.Creator<Audio>() {
        public Audio createFromParcel(Parcel in) {
            return new Audio(in);
        }

        public Audio[] newArray(int size) {
            return new Audio[size];
        }
    };

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(artist);
        out.writeString(title);
        out.writeInt(duration);
        out.writeString(url);
        out.writeInt(genreId);
    }

    public int getId() {
        return id;
    }


    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public int getDuration() {
        return duration;
    }

    public int getGenre() {
        return genreId;
    }

    public String getUrl() {
        return url;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }

    @Override
    public String toString() {
        return "Audio{" +
                "id=" + id +
                ", artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                ", duration=" + duration +
                ", url='" + url + '\'' +
                ", genreId=" + genreId +
                '}';
    }
}
