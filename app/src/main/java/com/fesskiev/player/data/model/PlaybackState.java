package com.fesskiev.player.data.model;


public class PlaybackState {

    private int volume;
    private int duration;
    private int progress;
    private int progressScale;
    private int durationScale;
    private boolean isPlaying;
    private boolean mute;
    private boolean repeat;

    public PlaybackState() {
        this.volume = 100;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getProgressScale() {
        return progressScale;
    }

    public void setProgressScale(int progressScale) {
        this.progressScale = progressScale;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public int getDurationScale() {
        return durationScale;
    }

    public void setDurationScale(int durationScale) {
        this.durationScale = durationScale;
    }

    @Override
    public String toString() {
        return "PlaybackState{" +
                "volume=" + volume +
                ", duration=" + duration +
                ", progress=" + progress +
                ", progressScale=" + progressScale +
                ", durationScale=" + durationScale +
                ", isPlaying=" + isPlaying +
                ", mute=" + mute +
                ", repeat=" + repeat +
                '}';
    }
}
