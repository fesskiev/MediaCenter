package com.fesskiev.player.model.vk;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupPost implements Parcelable {

    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_PHOTO = "photo";
    public static final String TYPE_DOC = "doc";

    public List<VKMusicFile> musicFiles;
    public String photo;
    public String text;
    public long date;
    public int id;
    public int likes;
    public int reposts;

    public GroupPost(Parcel in) {
        this.musicFiles = in.readArrayList(VKMusicFile.class.getClassLoader());
        this.photo = in.readString();
        this.text = in.readString();
        this.date = in.readLong();
        this.id = in.readInt();
        this.likes = in.readInt();
        this.reposts = in.readInt();
    }

    public GroupPost() {
        musicFiles = new ArrayList<>();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeList(musicFiles);
        out.writeString(photo);
        out.writeString(text);
        out.writeLong(date);
        out.writeInt(id);
        out.writeInt(likes);
        out.writeInt(reposts);
    }

    public static final Parcelable.Creator<GroupPost> CREATOR
            = new Parcelable.Creator<GroupPost>() {
        public GroupPost createFromParcel(Parcel in) {
            return new GroupPost(in);
        }

        public GroupPost[] newArray(int size) {
            return new GroupPost[size];
        }
    };

    @Override
    public String toString() {
        return "GroupPost{" +
                "musicFiles=" + Arrays.toString(musicFiles.toArray()) +
                ", photoURL='" + photo + '\'' +
                ", text='" + text + '\'' +
                ", date=" + date +
                ", id=" + id +
                ", likes=" + likes +
                ", reposts=" + reposts +
                '}';
    }
}
