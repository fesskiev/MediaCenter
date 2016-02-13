package com.fesskiev.player.model;


import java.util.ArrayList;
import java.util.List;

public class AudioPlayer {

    public List<AudioFolder> audioFolders;
    public AudioFolder currentAudioFolder;
    public AudioFile currentAudioFile;
    public int position;
    public int volume;
    public boolean isPlaying;

    public AudioPlayer() {
        this.audioFolders = new ArrayList<>();
        this.volume = 100;
    }

    public void next() {
        if (position < currentAudioFolder.audioFilesDescription.size() - 1) {
            position++;
        }
        currentAudioFile = currentAudioFolder.audioFilesDescription.get(position);
    }

    public void previous() {
        if (position > 0) {
            position--;
        }
        currentAudioFile = currentAudioFolder.audioFilesDescription.get(position);
    }
}
