package com.fesskiev.player.model;


import java.util.ArrayList;
import java.util.List;

public class MusicPlayer {

    public List<MusicFolder> musicFolders;
    public MusicFolder currentMusicFolder;
    public MusicFile currentMusicFile;
    public int position;
    public int volume;
    public boolean isPlaying;

    public MusicPlayer() {
        this.musicFolders = new ArrayList<>();
        this.volume = 100;
    }

    public void next() {
        if (position < currentMusicFolder.musicFilesDescription.size() - 1) {
            position++;
        }
        currentMusicFile = currentMusicFolder.musicFilesDescription.get(position);
    }

    public void previous() {
        if (position > 0) {
            position--;
        }
        currentMusicFile = currentMusicFolder.musicFilesDescription.get(position);
    }
}
