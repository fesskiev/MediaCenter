package com.fesskiev.player.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.fesskiev.player.R;
import com.fesskiev.player.utils.Utils;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.jaudiotagger.tag.images.Artwork;

import java.io.File;
import java.io.IOException;

public class AudioFile implements Comparable<AudioFile> {

    public interface OnMp3TagListener {
        void onFetchCompleted();
    }

    private Context context;
    public Artwork artwork;
    public String filePath;
    public String artist;
    public String title;
    public String album;
    public String genre;
    public String bitrate;
    public String sampleRate;
    public int trackNumber;
    public int length;
    private Bitmap bitmapArtwork;
    private OnMp3TagListener listener;

    public AudioFile(Context context, String filePath, OnMp3TagListener listener) {
        this.context = context;
        this.filePath = filePath;
        this.listener = listener;
        getTrackInfo();
    }


    private void parseMP3(org.jaudiotagger.audio.AudioFile file) {
        Tag tag = file.getTag();
        if (tag != null && tag.hasCommonFields()) {
            if(tag.hasField(ID3v24Frames.FRAME_ID_ARTIST)) {
                artist = tag.getFirst(ID3v24Frames.FRAME_ID_ARTIST);
            }
            if(tag.hasField(ID3v24Frames.FRAME_ID_TITLE)) {
                title = tag.getFirst(ID3v24Frames.FRAME_ID_TITLE);
            }
            if(tag.hasField(ID3v24Frames.FRAME_ID_ALBUM)) {
                album = tag.getFirst(ID3v24Frames.FRAME_ID_ALBUM);
            }
            if(tag.hasField(ID3v24Frames.FRAME_ID_GENRE)) {
                genre = tag.getFirst(ID3v24Frames.FRAME_ID_GENRE);
            }
            if(tag.hasField(ID3v24Frames.FRAME_ID_TRACK)) {
                String number = tag.getFirst(ID3v24Frames.FRAME_ID_TRACK);
                if (!TextUtils.isEmpty(number)) {
                    trackNumber = Integer.valueOf(number);
                }
            }
            artwork = tag.getFirstArtwork();
        }

        fillEmptyFields();
    }

    private void parseLossless(org.jaudiotagger.audio.AudioFile file) {
        FlacTag flacTag = (FlacTag) file.getTag();
        if (flacTag != null && flacTag.hasCommonFields()) {

            title = flacTag.getFirst(FieldKey.TITLE);
            artist = flacTag.getFirst(FieldKey.ARTIST);
            album = flacTag.getFirst(FieldKey.ALBUM);
            genre = flacTag.getFirst(FieldKey.GENRE);
            String number = flacTag.getFirst(FieldKey.TRACK);
            if (!TextUtils.isEmpty(number)) {
                trackNumber = Integer.valueOf(number);
            }
            artwork = flacTag.getFirstArtwork();
            fillEmptyFields();
        }
    }

    private void fillEmptyFields() {
        if (artist == null || TextUtils.isEmpty(artist)) {
            artist = context.getString(R.string.empty_music_file_artist);
        }
        if (title == null || TextUtils.isEmpty(title)) {
            title = context.getString(R.string.empty_music_file_title);
        }
        if (album == null || TextUtils.isEmpty(album)) {
            album = context.getString(R.string.empty_music_file_album);
        }
        if (genre == null || TextUtils.isEmpty(genre)) {
            genre = context.getString(R.string.empty_music_file_genre);
        }
    }

    public Bitmap getArtwork() {
        if (artwork != null && bitmapArtwork == null) {
            bitmapArtwork =  Utils.getBitmap(artwork.getBinaryData());
        }
        return bitmapArtwork;
    }

    private void getTrackInfo() {
        try {

            org.jaudiotagger.audio.AudioFile file = AudioFileIO.read(new File(filePath));
            AudioHeader audioHeader = file.getAudioHeader();

            bitrate = audioHeader.getBitRate() + " kbps "
                    + (audioHeader.isVariableBitRate() ? "(VBR)" : "(CBR)");
            sampleRate = audioHeader.getSampleRateAsNumber() + " Hz";
            length = audioHeader.getTrackLength();

            if (audioHeader.isLossless()) {
                parseLossless(file);
            } else {
                parseMP3(file);
            }

        } catch (CannotReadException |
                IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            e.printStackTrace();
        }
        if (listener != null) {
            listener.onFetchCompleted();
        }
    }

    @Override
    public int compareTo(AudioFile another) {
        if (this.trackNumber > another.trackNumber) {
            return 1;
        } else if (this.trackNumber < another.trackNumber) {
            return -1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AudioFile audioFile = (AudioFile) o;

        if (filePath != null ? !filePath.equals(audioFile.filePath) : audioFile.filePath != null)
            return false;
        if (artist != null ? !artist.equals(audioFile.artist) : audioFile.artist != null)
            return false;
        return !(title != null ? !title.equals(audioFile.title) : audioFile.title != null);

    }

    @Override
    public int hashCode() {
        int result = filePath != null ? filePath.hashCode() : 0;
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AudioFile{" +
                "context=" + context +
                ", artwork=" + artwork +
                ", filePath='" + filePath + '\'' +
                ", artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                ", album='" + album + '\'' +
                ", genre='" + genre + '\'' +
                ", bitrate='" + bitrate + '\'' +
                ", sampleRate='" + sampleRate + '\'' +
                ", trackNumber=" + trackNumber +
                ", length=" + length +
                '}';
    }
}
