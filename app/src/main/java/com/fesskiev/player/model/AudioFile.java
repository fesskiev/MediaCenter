package com.fesskiev.player.model;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.fesskiev.player.R;
import com.fesskiev.player.db.MediaDatabaseHelper;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.CacheManager;
import com.fesskiev.player.utils.Utils;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.jaudiotagger.tag.images.Artwork;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class AudioFile implements MediaFile, Comparable<AudioFile> {

    public interface OnAudioTagListener {
        void onFetchCompleted(AudioFile audioFile);
    }

    private Context context;
    public String id;
    public File filePath;
    public String artist;
    public String title;
    public String album;
    public String genre;
    public String bitrate;
    public String sampleRate;
    public String artworkPath;
    public int trackNumber;
    public int length;
    public boolean inPlayList;
    public boolean isSelected;
    private OnAudioTagListener listener;

    public AudioFile() {
        fillEmptyFields();
    }

    public AudioFile(Context context, File filePath, OnAudioTagListener listener) {
        this.context = context;
        this.filePath = filePath;
        this.listener = listener;
        renameFileCorrect();
        getTrackInfo();
    }

    public AudioFile(Cursor cursor) {

        id = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.ID));
        filePath = new File(cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.TRACK_PATH)));
        artist = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.TRACK_ARTIST));
        title = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.TRACK_TITLE));
        album = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.TRACK_ALBUM));
        genre = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.TRACK_GENRE));
        bitrate = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.TRACK_BITRATE));
        sampleRate = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.TRACK_SAMPLE_RATE));
        artworkPath = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.TRACK_COVER));
        trackNumber = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.TRACK_NUMBER));
        inPlayList = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.TRACK_IN_PLAY_LIST)) == 1;
        isSelected = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.TRACK_SELECTED)) == 1;
        length = cursor.getInt(cursor.getColumnIndex(MediaDatabaseHelper.TRACK_LENGTH));

    }

    private void renameFileCorrect() {
        File newPath = new File(filePath.getParent(), Utils.replaceSymbols(filePath.getName()));
        boolean rename = filePath.renameTo(newPath);
        if (rename) {
            filePath = newPath;
        }
    }

    private void saveArtwork(byte[] data) {
        try {
            File path = File.createTempFile(UUID.randomUUID().toString(),
                    ".png", new File(CacheManager.IMAGES_CACHE_PATH));

            BitmapHelper.saveBitmap(data, path);

            artworkPath = path.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveArtwork(Tag tag) {
        Artwork artwork = tag.getFirstArtwork();
        if (artwork != null) {
            try {
                File path = File.createTempFile(UUID.randomUUID().toString(),
                        ".png", new File(CacheManager.IMAGES_CACHE_PATH));

                BitmapHelper.saveBitmap(artwork.getBinaryData(), path);

                artworkPath = path.getAbsolutePath();

            } catch (IOException e) {
                e.printStackTrace();
            }
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

    private void getTrackInfo() {
        parseMetadataTagger();
        if (listener != null) {
            listener.onFetchCompleted(this);
        }
    }

    private void parseMetadataMP3Agic() {
        try {

            Mp3File mp3file = new Mp3File(filePath);

            length = (int) mp3file.getLengthInSeconds();
            bitrate = mp3file.getBitrate() + " kbps " + (mp3file.isVbr() ? "(VBR)" : "(CBR)");
            sampleRate = mp3file.getSampleRate() + " Hz";

            if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();

                String track = id3v2Tag.getTrack();
                if (track != null && !TextUtils.isEmpty(track)) {
                    trackNumber = Integer.valueOf(Utils.replaceSymbols(track));
                }

                artist = id3v2Tag.getArtist();
                title = id3v2Tag.getTitle();
                album = id3v2Tag.getAlbum();
                genre = id3v2Tag.getGenreDescription();

                byte[] albumImageData = id3v2Tag.getAlbumImage();
                if (albumImageData != null) {
                    saveArtwork(albumImageData);
                }
            } else if (mp3file.hasId3v1Tag()) {
                ID3v1 id3v1Tag = mp3file.getId3v1Tag();

                String track = id3v1Tag.getTrack();
                if (track != null && !TextUtils.isEmpty(track)) {
                    trackNumber = Integer.valueOf(Utils.replaceSymbols(track));
                }

                artist = id3v1Tag.getArtist();
                title = id3v1Tag.getTitle();
                album = id3v1Tag.getAlbum();
                genre = id3v1Tag.getGenreDescription();
            }

        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            e.printStackTrace();
        } finally {
            fillEmptyFields();
        }
    }

    private void parseMetadataTagger() {
        try {
            TagOptionSingleton.getInstance().setAndroid(true);
            org.jaudiotagger.audio.AudioFile file = AudioFileIO.read(filePath);
            AudioHeader audioHeader = file.getAudioHeader();

            bitrate = audioHeader.getBitRate() + " kbps " + (audioHeader.isVariableBitRate() ? "(VBR)" : "(CBR)");
            sampleRate = audioHeader.getSampleRateAsNumber() + " Hz";
            length = audioHeader.getTrackLength();

            if (audioHeader.isLossless()) {
                parseLossless(file);
            } else {
                parseMP3(file);
            }

        } catch (CannotReadException | IOException | TagException
                | ReadOnlyFileException | InvalidAudioFrameException e) {
            e.printStackTrace();
        } finally {
            fillEmptyFields();
        }
    }


    private void parseMP3(org.jaudiotagger.audio.AudioFile file) {
        Tag tag = file.getTag();
        if (tag != null && tag.hasCommonFields()) {
            if (tag.hasField(ID3v24Frames.FRAME_ID_ARTIST)) {
                artist = tag.getFirst(ID3v24Frames.FRAME_ID_ARTIST);
            }
            if (tag.hasField(ID3v24Frames.FRAME_ID_TITLE)) {
                title = tag.getFirst(ID3v24Frames.FRAME_ID_TITLE);
            }
            if (tag.hasField(ID3v24Frames.FRAME_ID_ALBUM)) {
                album = tag.getFirst(ID3v24Frames.FRAME_ID_ALBUM);
            }
            if (tag.hasField(ID3v24Frames.FRAME_ID_GENRE)) {
                genre = tag.getFirst(ID3v24Frames.FRAME_ID_GENRE);
            }
            if (tag.hasField(ID3v24Frames.FRAME_ID_TRACK)) {
                String number = tag.getFirst(ID3v24Frames.FRAME_ID_TRACK);
                if (!number.equals("null") && !TextUtils.isEmpty(number)) {
                    trackNumber = Integer.valueOf(number);
                }
            }

            saveArtwork(tag);
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
                try {
                    trackNumber = Integer.valueOf(number);
                } catch (NumberFormatException e) {
                    trackNumber = 0;
                    e.printStackTrace();
                }
            }

            saveArtwork(flacTag);

            fillEmptyFields();
        }
    }


    @Override
    public MEDIA_TYPE getMediaType() {
        return MEDIA_TYPE.AUDIO;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getArtworkPath() {
        return artworkPath;
    }

    @Override
    public String getFileName() {
        return filePath.getName();
    }

    @Override
    public String getFilePath() {
        return filePath.getPath();
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public boolean exists() {
        return filePath.exists();
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

        if (!filePath.equals(audioFile.filePath)) return false;
        if (!artist.equals(audioFile.artist)) return false;
        return title.equals(audioFile.title);

    }

    @Override
    public int hashCode() {
        int result = filePath.hashCode();
        result = 31 * result + artist.hashCode();
        result = 31 * result + title.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AudioFile{" +
                "filePath=" + filePath +
                ", artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                ", album='" + album + '\'' +
                ", genre='" + genre + '\'' +
                ", bitrate='" + bitrate + '\'' +
                ", sampleRate='" + sampleRate + '\'' +
                ", artworkPath='" + artworkPath + '\'' +
                ", trackNumber=" + trackNumber +
                ", length=" + length +
                ", inPlayList=" + inPlayList +
                ", isSelected=" + isSelected +
                ", id='" + id + '\'' +
                '}';
    }
}
