package com.fesskiev.player.data.model;


public class EQState {

    private float firstBand;
    private float secondBand;
    private float thirdBand;

    public float getFirstBand() {
        return firstBand;
    }

    public void setFirstBand(float firstBand) {
        this.firstBand = firstBand;
    }

    public float getSecondBand() {
        return secondBand;
    }

    public void setSecondBand(float secondBand) {
        this.secondBand = secondBand;
    }

    public float getThirdBand() {
        return thirdBand;
    }

    public void setThirdBand(float thirdBand) {
        this.thirdBand = thirdBand;
    }

    @Override
    public String toString() {
        return "EQState{" +
                "firstBand=" + firstBand +
                ", secondBand=" + secondBand +
                ", thirdBand=" + thirdBand +
                '}';
    }
}
