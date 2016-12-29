package com.fesskiev.mediacenter.data.model.effects;


import java.util.Arrays;

public class EQState {

    private float lowLevel;
    private float midLevel;
    private float highLevel;

    private float lowBand;
    private float midBand;
    private float highBand;

    private float [] lowValues;
    private float [] midValues;
    private float [] highValues;

    public float getLowBand() {
        return lowBand;
    }

    public void setLowBand(float lowBand) {
        this.lowBand = lowBand;
    }

    public float getMidBand() {
        return midBand;
    }

    public void setMidBand(float midBand) {
        this.midBand = midBand;
    }

    public float getHighBand() {
        return highBand;
    }

    public void setHighBand(float highBand) {
        this.highBand = highBand;
    }

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
                ", lowBand=" + lowBand +
                ", midBand=" + midBand +
                ", highBand=" + highBand +
                ", lowValues=" + Arrays.toString(lowValues) +
                ", midValues=" + Arrays.toString(midValues) +
                ", highValues=" + Arrays.toString(highValues) +
                '}';
    }
}
