package com.fesskiev.mediacenter.data.model.effects;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class EchoState implements Parcelable {

    private float level;

    private float [] levelValues;

    public EchoState() {

    }

    protected EchoState(Parcel in) {
        this.level = in.readFloat();
        this.levelValues = in.createFloatArray();
    }

    public float getLevel() {
        return level;
    }

    public void setLevel(float level) {
        this.level = level;
    }

    public float[] getLevelValues() {
        return levelValues;
    }

    public void setLevelValues(float[] levelValues) {
        this.levelValues = levelValues;
    }

    @Override
    public String toString() {
        return "EchoState{" +
                "level=" + level +
                ", levelValues=" + Arrays.toString(levelValues) +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.level);
        dest.writeFloatArray(this.levelValues);
    }


    public static final Parcelable.Creator<EchoState> CREATOR = new Parcelable.Creator<EchoState>() {
        @Override
        public EchoState createFromParcel(Parcel source) {
            return new EchoState(source);
        }

        @Override
        public EchoState[] newArray(int size) {
            return new EchoState[size];
        }
    };
}
