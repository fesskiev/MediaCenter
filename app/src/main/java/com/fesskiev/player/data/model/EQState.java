package com.fesskiev.player.data.model;


import java.util.Arrays;

public class EQState {

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

    @Override
    public String toString() {
        return "EQState{" +
                "lowBand=" + lowBand +
                ", midBand=" + midBand +
                ", highBand=" + highBand +
                ", lowValues=" + Arrays.toString(lowValues) +
                ", midValues=" + Arrays.toString(midValues) +
                ", highValues=" + Arrays.toString(highValues) +
                '}';
    }
}
