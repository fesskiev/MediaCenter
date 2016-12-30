package com.fesskiev.mediacenter.data.model.effects;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class ReverbState implements Parcelable {


    private float weight;
    private float mix;
    private float damp;
    private float roomSize;

    private float [] weightValues;
    private float [] mixValues;
    private float [] dampValues;
    private float [] roomSizeValues;

    public ReverbState() {

    }

    protected ReverbState(Parcel in) {
        this.weight = in.readFloat();
        this.mix = in.readFloat();
        this.damp = in.readFloat();
        this.roomSize = in.readFloat();
    }


    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getMix() {
        return mix;
    }

    public void setMix(float mix) {
        this.mix = mix;
    }

    public float getDamp() {
        return damp;
    }

    public void setDamp(float damp) {
        this.damp = damp;
    }

    public float getRoomSize() {
        return roomSize;
    }

    public void setRoomSize(float roomSize) {
        this.roomSize = roomSize;
    }

    public float[] getWeightValues() {
        return weightValues;
    }

    public void setWeightValues(float[] weightValues) {
        this.weightValues = weightValues;
    }

    public float[] getMixValues() {
        return mixValues;
    }

    public void setMixValues(float[] mixValues) {
        this.mixValues = mixValues;
    }

    public float[] getDampValues() {
        return dampValues;
    }

    public void setDampValues(float[] dampValues) {
        this.dampValues = dampValues;
    }

    public float[] getRoomSizeValues() {
        return roomSizeValues;
    }

    public void setRoomSizeValues(float[] roomSizeValues) {
        this.roomSizeValues = roomSizeValues;
    }

    @Override
    public String toString() {
        return "ReverbState{" +
                "weight=" + weight +
                ", mix=" + mix +
                ", damp=" + damp +
                ", roomSize=" + roomSize +
                ", weightValues=" + Arrays.toString(weightValues) +
                ", mixValues=" + Arrays.toString(mixValues) +
                ", dampValues=" + Arrays.toString(dampValues) +
                ", roomSizeValues=" + Arrays.toString(roomSizeValues) +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.weight);
        dest.writeFloat(this.mix);
        dest.writeFloat(this.damp);
        dest.writeFloat(this.roomSize);
    }



    public static final Parcelable.Creator<ReverbState> CREATOR = new Parcelable.Creator<ReverbState>() {
        @Override
        public ReverbState createFromParcel(Parcel source) {
            return new ReverbState(source);
        }

        @Override
        public ReverbState[] newArray(int size) {
            return new ReverbState[size];
        }
    };
}
