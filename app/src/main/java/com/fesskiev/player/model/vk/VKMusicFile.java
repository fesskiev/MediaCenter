package com.fesskiev.player.model.vk;


import android.os.Parcel;
import android.os.Parcelable;

public class VKMusicFile implements Parcelable {

    public int aid;
    public int ownerId;
    public String artist;
    public String title;
    public int duration;
    public String url;
    public int genre;

    public VKMusicFile() {

    }

    public VKMusicFile(Parcel in) {
        this.aid = in.readInt();
        this.ownerId = in.readInt();
        this.artist = in.readString();
        this.title = in.readString();
        this.duration = in.readInt();
        this.url = in.readString();
        this.genre = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<VKMusicFile> CREATOR
            = new Parcelable.Creator<VKMusicFile>() {
        public VKMusicFile createFromParcel(Parcel in) {
            return new VKMusicFile(in);
        }

        public VKMusicFile[] newArray(int size) {
            return new VKMusicFile[size];
        }
    };

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(aid);
        out.writeInt(ownerId);
        out.writeString(artist);
        out.writeString(title);
        out.writeInt(duration);
        out.writeString(url);
        out.writeInt(genre);
    }

    @Override
    public String toString() {
        return "VKMusicFile{" +
                "aid=" + aid +
                ", ownerId=" + ownerId +
                ", artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                ", duration=" + duration +
                ", url='" + url + '\'' +
                ", genre=" + genre +
                '}';
    }
}
