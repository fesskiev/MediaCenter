package com.fesskiev.player.data.model;


public class EQState {

    private float lowBand;
    private float midBand;
    private float highBand;

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

    @Override
    public String toString() {
        return "EQState{" +
                "lowBand=" + lowBand +
                ", midBand=" + midBand +
                ", highBand=" + highBand +
                '}';
    }
}
