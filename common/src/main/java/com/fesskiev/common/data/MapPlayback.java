package com.fesskiev.common.data;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.wearable.DataMap;

public class MapPlayback implements Parcelable {

    private int duration;
    private int position;
    private float positionPercent;
    private float volume;
    private float focusedVolume;
    private int durationScale;
    private boolean playing;
    private boolean looping;


    public MapPlayback() {

    }

    public DataMap toDataMap(DataMap map) {
        map.putInt("duration", duration);
        map.putInt("position", position);
        map.putFloat("positionPercent", positionPercent);
        map.putFloat("volume", volume);
        map.putFloat("focusedVolume", focusedVolume);
        map.putInt("durationScale", durationScale);
        map.putBoolean("playing", playing);
        map.putBoolean("looping", looping);
        return map;
    }

    public static MapPlayback toMapPlayback(DataMap map) {
        return new MapPlayback.MapPlaybackBuilder()
                .withDuration(map.getInt("duration"))
                .withPosition(map.getInt("position"))
                .withPositionPercent(map.getFloat("positionPercent"))
                .withVolume(map.getFloat("volume"))
                .withFocusedVolume(map.getFloat("focusedVolume"))
                .withDurationScale(map.getInt("durationScale"))
                .withPlaying(map.getBoolean("playing"))
                .withLooping(map.getBoolean("looping"))
                .build();
    }


    @Override
    public String toString() {
        return "MapPlayback{" +
                "duration=" + duration +
                ", position=" + position +
                ", positionPercent=" + positionPercent +
                ", volume=" + volume +
                ", focusedVolume=" + focusedVolume +
                ", durationScale=" + durationScale +
                ", playing=" + playing +
                ", looping=" + looping +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.duration);
        dest.writeInt(this.position);
        dest.writeFloat(this.positionPercent);
        dest.writeFloat(this.volume);
        dest.writeFloat(this.focusedVolume);
        dest.writeInt(this.durationScale);
        dest.writeByte(this.playing ? (byte) 1 : (byte) 0);
        dest.writeByte(this.looping ? (byte) 1 : (byte) 0);
    }


    protected MapPlayback(Parcel in) {
        this.duration = in.readInt();
        this.position = in.readInt();
        this.positionPercent = in.readFloat();
        this.volume = in.readFloat();
        this.focusedVolume = in.readFloat();
        this.durationScale = in.readInt();
        this.playing = in.readByte() != 0;
        this.looping = in.readByte() != 0;
    }

    public static final Parcelable.Creator<MapPlayback> CREATOR = new Parcelable.Creator<MapPlayback>() {
        @Override
        public MapPlayback createFromParcel(Parcel source) {
            return new MapPlayback(source);
        }

        @Override
        public MapPlayback[] newArray(int size) {
            return new MapPlayback[size];
        }
    };

    public boolean isPlaying() {
        return playing;
    }

    public static final class MapPlaybackBuilder {
        private int duration;
        private int position;
        private float positionPercent;
        private float volume;
        private float focusedVolume;
        private int durationScale;
        private boolean playing;
        private boolean looping;

        public MapPlaybackBuilder() {

        }


        public static MapPlaybackBuilder buildMapPlayback() {
            return new MapPlaybackBuilder();
        }

        public MapPlaybackBuilder withDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public MapPlaybackBuilder withPosition(int position) {
            this.position = position;
            return this;
        }

        public MapPlaybackBuilder withPositionPercent(float positionPercent) {
            this.positionPercent = positionPercent;
            return this;
        }

        public MapPlaybackBuilder withVolume(float volume) {
            this.volume = volume;
            return this;
        }

        public MapPlaybackBuilder withFocusedVolume(float focusedVolume) {
            this.focusedVolume = focusedVolume;
            return this;
        }

        public MapPlaybackBuilder withDurationScale(int durationScale) {
            this.durationScale = durationScale;
            return this;
        }

        public MapPlaybackBuilder withPlaying(boolean playing) {
            this.playing = playing;
            return this;
        }

        public MapPlaybackBuilder withLooping(boolean looping) {
            this.looping = looping;
            return this;
        }

        public MapPlayback build() {
            MapPlayback mapPlayback = new MapPlayback();
            mapPlayback.duration = this.duration;
            mapPlayback.positionPercent = this.positionPercent;
            mapPlayback.playing = this.playing;
            mapPlayback.looping = this.looping;
            mapPlayback.focusedVolume = this.focusedVolume;
            mapPlayback.position = this.position;
            mapPlayback.volume = this.volume;
            mapPlayback.durationScale = this.durationScale;
            return mapPlayback;
        }
    }
}
