package com.fesskiev.mediacenter.data.model.effects;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class WhooshState implements Parcelable {

    private float mix;
    private float frequency;

    private float [] frequencyValues;
    private float [] mixValues;

    public WhooshState() {

    }

    protected WhooshState(Parcel in) {
        this.mix = in.readFloat();
        this.frequency = in.readFloat();
        this.frequencyValues = in.createFloatArray();
        this.mixValues = in.createFloatArray();
    }

    public float getMix() {
        return mix;
    }

    public void setMix(float mix) {
        this.mix = mix;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public float[] getFrequencyValues() {
        return frequencyValues;
    }

    public void setFrequencyValues(float[] frequencyValues) {
        this.frequencyValues = frequencyValues;
    }

    public float[] getMixValues() {
        return mixValues;
    }

    public void setMixValues(float[] mixValues) {
        this.mixValues = mixValues;
    }

    @Override
    public String toString() {
        return "WhooshState{" +
                "mix=" + mix +
                ", frequency=" + frequency +
                ", frequencyValues=" + Arrays.toString(frequencyValues) +
                ", mixValues=" + Arrays.toString(mixValues) +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.mix);
        dest.writeFloat(this.frequency);
        dest.writeFloatArray(this.frequencyValues);
        dest.writeFloatArray(this.mixValues);
    }


    public static final Creator<WhooshState> CREATOR = new Creator<WhooshState>() {
        @Override
        public WhooshState createFromParcel(Parcel source) {
            return new WhooshState(source);
        }

        @Override
        public WhooshState[] newArray(int size) {
            return new WhooshState[size];
        }
    };
}
