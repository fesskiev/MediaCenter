package com.fesskiev.mediacenter.utils.converter;

public enum AudioFormat {
    AAC,
    MP3,
    M4A,
    WAV,
    FLAC;

    public String getFormat() {
        return name().toLowerCase();
    }
}