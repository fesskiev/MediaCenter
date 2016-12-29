package com.fesskiev.mediacenter.data.model.effects;


import java.util.Arrays;

public class ReverbState {

    private float weight;
    private float mix;
    private float dump;
    private float roomSize;

    private float [] weightValues;
    private float [] mixValues;
    private float [] dumpValues;
    private float [] roomSizeValues;

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getDump() {
        return dump;
    }

    public void setDump(float dump) {
        this.dump = dump;
    }

    public float getMix() {
        return mix;
    }

    public void setMix(float mix) {
        this.mix = mix;
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

    public float[] getDumpValues() {
        return dumpValues;
    }

    public void setDumpValues(float[] dumpValues) {
        this.dumpValues = dumpValues;
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
                ", dump=" + dump +
                ", roomSize=" + roomSize +
                ", weightValues=" + Arrays.toString(weightValues) +
                ", mixValues=" + Arrays.toString(mixValues) +
                ", dumpValues=" + Arrays.toString(dumpValues) +
                ", roomSizeValues=" + Arrays.toString(roomSizeValues) +
                '}';
    }
}
