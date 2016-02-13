package com.fesskiev.player.model;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudioFolder {

    public List<File> musicFiles;
    public List<File> folderImages;
    public List<AudioFile> audioFilesDescription;
    public String folderName;

    public AudioFolder() {
        musicFiles = new ArrayList<>();
        folderImages = new ArrayList<>();
        audioFilesDescription = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "AudioFolder{" +
                "musicFiles=" + musicFiles +
                ", folderImages=" + folderImages +
                ", audioFilesDescription=" + audioFilesDescription +
                ", folderName='" + folderName + '\'' +
                '}';
    }
}
