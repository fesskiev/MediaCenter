package com.fesskiev.mediacenter.data.model.video;


public class RendererState implements Comparable<RendererState> {

    private int index;
    private int groupIndex;
    private int trackIndex;
    private boolean isDisabled;


    public RendererState(int index, int groupIndex, int trackIndex, boolean isDisabled) {
        this.index = index;
        this.groupIndex = groupIndex;
        this.trackIndex = trackIndex;
        this.isDisabled = isDisabled;
    }

    public int getIndex() {
        return index;
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public int getTrackIndex() {
        return trackIndex;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    @Override
    public int compareTo(RendererState o) {
        return Integer.compare(this.index, o.getIndex());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RendererState that = (RendererState) o;

        if (index != that.index) return false;
        if (groupIndex != that.groupIndex) return false;
        return trackIndex == that.trackIndex;

    }

    @Override
    public int hashCode() {
        int result = index;
        result = 31 * result + groupIndex;
        result = 31 * result + trackIndex;
        return result;
    }

    @Override
    public String toString() {
        return "RendererState{" +
                "index=" + index +
                ", groupIndex=" + groupIndex +
                ", trackIndex=" + trackIndex +
                ", isDisabled=" + isDisabled +
                '}';
    }
}
