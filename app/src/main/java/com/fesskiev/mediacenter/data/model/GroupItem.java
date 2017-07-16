package com.fesskiev.mediacenter.data.model;


import android.os.Parcel;
import android.os.Parcelable;

public class GroupItem implements Parcelable {

    public enum TYPE {
        GENRE, ARTIST
    }

    private TYPE type;
    private String name;

    public GroupItem(String name, TYPE type) {
        this.name = name;
        this.type = type;
    }

    public GroupItem() {

    }

    public TYPE getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    protected GroupItem(Parcel in) {
        this.name = in.readString();
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : TYPE.values()[tmpType];
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
    }


    public static final Parcelable.Creator<GroupItem> CREATOR = new Parcelable.Creator<GroupItem>() {
        @Override
        public GroupItem createFromParcel(Parcel source) {
            return new GroupItem(source);
        }

        @Override
        public GroupItem[] newArray(int size) {
            return new GroupItem[size];
        }
    };

    @Override
    public String toString() {
        return "GroupItem{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
