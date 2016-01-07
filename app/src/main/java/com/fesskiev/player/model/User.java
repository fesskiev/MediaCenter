package com.fesskiev.player.model;


import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    public int uid;
    public String firstName;
    public String lastName;
    public String photoUrl;

    public User() {

    }

    public User(Parcel in) {
        this.uid = in.readInt();
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.photoUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<User> CREATOR
            = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(uid);
        out.writeString(firstName);
        out.writeString(lastName);
        out.writeString(photoUrl);
    }

    @Override
    public String toString() {
        return "User{" +
                "uid=" + uid +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                '}';
    }
}
