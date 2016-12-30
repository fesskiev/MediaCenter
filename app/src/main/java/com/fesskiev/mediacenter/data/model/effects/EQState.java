package com.fesskiev.mediacenter.data.model.effects;


import java.util.Arrays;

public class EQState {

    private float lowLevel;
    private float midLevel;
    private float highLevel;


    private float [] lowValues;
    private float [] midValues;
    private float [] highValues;


    public float[] getMidValues() {
        return midValues;
    }

    public void setMidValues(float[] midValues) {
        this.midValues = midValues;
    }

    public float[] getHighValues() {
        return highValues;
    }

    public void setHighValues(float[] highValues) {
        this.highValues = highValues;
    }

    public float[] getLowValues() {
        return lowValues;
    }

    public void setLowValues(float[] lowValues) {
        this.lowValues = lowValues;
    }

    public float getLowLevel() {
        return lowLevel;
    }

    public void setLowLevel(float lowLevel) {
        this.lowLevel = lowLevel;
    }

    public float getMidLevel() {
        return midLevel;
    }

    public void setMidLevel(float midLevel) {
        this.midLevel = midLevel;
    }

    public float getHighLevel() {
        return highLevel;
    }

    public void setHighLevel(float highLevel) {
        this.highLevel = highLevel;
    }

    @Override
    public String toString() {
        return "EQState{" +
                "lowLevel=" + lowLevel +
                ", midLevel=" + midLevel +
                ", highLevel=" + highLevel +
                ", lowValues=" + Arrays.toString(lowValues) +
                ", midValues=" + Arrays.toString(midValues) +
                ", highValues=" + Arrays.toString(highValues) +
                '}';
    }
}
