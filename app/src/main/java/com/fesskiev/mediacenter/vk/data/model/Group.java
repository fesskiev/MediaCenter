package com.fesskiev.mediacenter.vk.data.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Group implements Parcelable {

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("screen_name")
    @Expose
    private String screenName;

    @SerializedName("is_closed")
    @Expose
    private int isClosed;

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("photo_50")
    @Expose
    private String photoSmallURL;

    @SerializedName("photo_100")
    @Expose
    private String photoMediumURL;

    @SerializedName("photo_200")
    @Expose
    private String photoBigURL;

    public Group() {

    }

    public Group(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.screenName = in.readString();
        this.isClosed = in.readInt();
        this.photoSmallURL= in.readString();
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
        out.writeInt(id);
        out.writeString(name);
        out.writeString(screenName);
        out.writeInt(isClosed);
        out.writeString(photoSmallURL);
        out.writeString(photoMediumURL);
        out.writeString(photoBigURL);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getIsClosed() {
        return isClosed;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getType() {
        return type;
    }

    public String getPhotoSamllURL() {
        return photoMediumURL;
    }

    public String getPhotoMediumURL() {
        return photoMediumURL;
    }

    public String getPhotoBigURL() {
        return photoBigURL;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", screenName='" + screenName + '\'' +
                ", isClosed=" + isClosed +
                ", type='" + type + '\'' +
                ", photoSmallURL='" + photoSmallURL + '\'' +
                ", photoMediumURL='" + photoMediumURL + '\'' +
                ", photoBigURL='" + photoBigURL + '\'' +
                '}';
    }


}
