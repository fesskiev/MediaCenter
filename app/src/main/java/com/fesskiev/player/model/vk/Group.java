package com.fesskiev.player.model.vk;


import android.os.Parcel;
import android.os.Parcelable;

public class Group implements Parcelable {

    public int gid;
    public String name;
    public String screenName;
    public int isClosed;
    public String type;
    public String photoURL;
    public String photoMediumURL;
    public String photoBigURL;

    public Group() {

    }

    public Group(Parcel in) {
        this.gid = in.readInt();
        this.name = in.readString();
        this.screenName = in.readString();
        this.isClosed = in.readInt();
        this.photoURL = in.readString();
        this.photoMediumURL = in.readString();
        this.photoBigURL = in.readString();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Group> CREATOR
            = new Parcelable.Creator<Group>() {
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(gid);
        out.writeString(name);
        out.writeString(screenName);
        out.writeInt(isClosed);
        out.writeString(photoURL);
        out.writeString(photoMediumURL);
        out.writeString(photoBigURL);
    }


    @Override
    public String toString() {
        return "Group{" +
                "gid=" + gid +
                ", name='" + name + '\'' +
                ", screenName='" + screenName + '\'' +
                ", isClosed=" + isClosed +
                ", type='" + type + '\'' +
                ", photoURL='" + photoURL + '\'' +
                ", photoMediumURL='" + photoMediumURL + '\'' +
                ", photoBigURL='" + photoBigURL + '\'' +
                '}';
    }


}
