package com.fesskiev.player.model;


import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;

public class MusicFile {

    public interface OnMp3TagListener {
        void onFetchCompleted();
    }

    public String filePath;
    public String artist;
    public String title;
    public String album;
    public String genre;
    public String bitrate;
    public String sampleRate;
    private OnMp3TagListener listener;

    public MusicFile(String filePath, OnMp3TagListener listener) {
        this.filePath = filePath;
        this.listener = listener;
        getTrackInfo(filePath);
    }


    private void getTrackInfo(String path) {
        try {

            Mp3File mp3file = new Mp3File(path);

            bitrate = mp3file.getBitrate() + " kbps " + (mp3file.isVbr() ? "(VBR)" : "(CBR)");
            sampleRate = mp3file.getSampleRate() + " Hz";

            if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                artist = id3v2Tag.getArtist();
                title = id3v2Tag.getTitle();
                album = id3v2Tag.getAlbumArtist();
                genre = id3v2Tag.getGenreDescription();
                if(listener != null){
                    listener.onFetchCompleted();
                }
            }else if(mp3file.hasId3v1Tag()){
                ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                artist = id3v1Tag.getArtist();
                title = id3v1Tag.getTitle();
                album = id3v1Tag.getAlbum();
                genre = id3v1Tag.getGenreDescription();
                if(listener != null){
                    listener.onFetchCompleted();
                }
            }


        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "MusicFile{" +
                "filePath='" + filePath + '\'' +
                ", artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                ", album='" + album + '\'' +
                ", genre='" + genre + '\'' +
                ", bitrate='" + bitrate + '\'' +
                ", sampleRate='" + sampleRate + '\'' +
                '}';
    }
}
