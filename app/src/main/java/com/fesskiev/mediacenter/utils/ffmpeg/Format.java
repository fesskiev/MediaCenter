package com.fesskiev.mediacenter.utils.ffmpeg;

public enum Format {

    AVI,
    MP4,
    MOV,
    AAC,
    MP3,
    M4A,
    WAV,
    FLAC;

    public String getFormat() {
        return name().toLowerCase();
    }
}