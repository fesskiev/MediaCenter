package com.fesskiev.mediacenter.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.Utils;

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
import java.util.List;
import java.util.UUID;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "AudioFiles",
        foreignKeys = @ForeignKey(
                entity = AudioFolder.class, parentColumns = "id",
                childColumns = "folderId",
                onDelete = CASCADE),
        indices = @Index("folderId"))
public class AudioFile implements Comparable<AudioFile>, Parcelable, MediaFile {

    @NonNull
    @PrimaryKey()
    public String fileId;
    public String folderId;

    public File filePath;
    @Ignore
    public File convertedPath;
    public String artist;
    public String title;
    public String album;
    public String genre;
    public String bitrate;
    public String sampleRate;
    public String artworkPath;
    public String folderArtworkPath;
    public int trackNumber;
    public long length;
    public long size;
    public long timestamp;
    public boolean inPlayList;
    public boolean isSelected;
    public boolean isHidden;

    public AudioFile() {
        fillEmptyFields();
    }

    public AudioFile(File filePath, String folderId) {
        this.folderId = folderId;
        this.fileId = UUID.randomUUID().toString();
        this.filePath = filePath;
        renameFileCorrect();
        parseMetadataTagger();
    }

    private void renameFileCorrect() {
        //TODO fix replace symbols folder
        File newPath = new File(filePath.getParent(), Utils.replaceSymbols(filePath.getName()));
        boolean rename = filePath.renameTo(newPath);
        if (rename) {
            filePath = newPath;
        }
    }

    private void saveArtwork(Tag tag) {
        List<Artwork> artworks = tag.getArtworkList();
        for (Artwork artwork : artworks) {
            byte[] imageRawData = artwork != null ? artwork.getBinaryData() : null;
            if (imageRawData != null) {
                try {
                    File path = File.createTempFile(UUID.randomUUID().toString(),
                            ".jpg", new File(CacheManager.IMAGES_AUDIO_CACHE_PATH));

                    BitmapHelper.saveBitmap(artwork.getBinaryData(), path);

                    artworkPath = path.getAbsolutePath();
                } catch (IOException e) {
//                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void fillEmptyFields() {
        Context context = MediaApplication.getInstance().getApplicationContext();
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

    private void parseMetadataTagger() {

        size = filePath.length();
        timestamp = System.currentTimeMillis();

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
    public String getId() {
        return fileId;
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
        if (artworkPath != null) {
            return artworkPath;
        }
        return folderArtworkPath;
    }

    @Override
    public String getFileName() {
        return filePath.getName();
    }

    @Override
    public String getFilePath() {
        if (convertedPath != null) {
            return convertedPath.getAbsolutePath();
        }
        return filePath.getAbsolutePath();
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean exists() {
        return filePath.exists();
    }

    @Override
    public boolean inPlayList() {
        return inPlayList;
    }

    @Override
    public void setToPlayList(boolean inPlaylist) {
        this.inPlayList = inPlaylist;
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

        if (!fileId.equals(audioFile.fileId)) return false;
        if (!artist.equals(audioFile.artist)) return false;
        return title.equals(audioFile.title);
    }

    @Override
    public int hashCode() {
        int result = fileId.hashCode();
        result = 31 * result + artist.hashCode();
        result = 31 * result + title.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AudioFile{" +
                "folderId='" + folderId + '\'' +
                ", fileId=" + fileId +
                ", filePath=" + filePath + "\n" +
                ", convertedPath=" + convertedPath + "\n" +
                ", artist='" + artist + "\n" +
                ", title='" + title + "\n" +
                ", album='" + album + "\n" +
                ", genre='" + genre + "\n" +
                ", bitrate='" + bitrate + "\n" +
                ", sampleRate='" + sampleRate + "\n" +
                ", artworkPath='" + artworkPath + "\n" +
                ", folderArtworkPath='" + folderArtworkPath + "\n" +
                ", trackNumber=" + trackNumber + "\n" +
                ", length=" + length + "\n" +
                ", size=" + size + "\n" +
                ", timestamp=" + timestamp + "\n" +
                ", inPlayList=" + inPlayList + "\n" +
                ", isSelected=" + isSelected + "\n" +
                ", isHidden=" + isHidden +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.folderId);
        dest.writeString(this.fileId);
        dest.writeSerializable(this.filePath);
        dest.writeSerializable(this.convertedPath);
        dest.writeString(this.artist);
        dest.writeString(this.title);
        dest.writeString(this.album);
        dest.writeString(this.genre);
        dest.writeString(this.bitrate);
        dest.writeString(this.sampleRate);
        dest.writeString(this.artworkPath);
        dest.writeString(this.folderArtworkPath);
        dest.writeInt(this.trackNumber);
        dest.writeLong(this.length);
        dest.writeLong(this.size);
        dest.writeLong(this.timestamp);
        dest.writeByte(this.inPlayList ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isHidden ? (byte) 1 : (byte) 0);
    }

    protected AudioFile(Parcel in) {
        this.folderId = in.readString();
        this.fileId = in.readString();
        this.filePath = (File) in.readSerializable();
        this.convertedPath = (File) in.readSerializable();
        this.artist = in.readString();
        this.title = in.readString();
        this.album = in.readString();
        this.genre = in.readString();
        this.bitrate = in.readString();
        this.sampleRate = in.readString();
        this.artworkPath = in.readString();
        this.folderArtworkPath = in.readString();
        this.trackNumber = in.readInt();
        this.length = in.readLong();
        this.size = in.readLong();
        this.timestamp = in.readLong();
        this.inPlayList = in.readByte() != 0;
        this.isSelected = in.readByte() != 0;
        this.isHidden = in.readByte() != 0;
    }

    public static final Parcelable.Creator<AudioFile> CREATOR = new Parcelable.Creator<AudioFile>() {
        @Override
        public AudioFile createFromParcel(Parcel source) {
            return new AudioFile(source);
        }

        @Override
        public AudioFile[] newArray(int size) {
            return new AudioFile[size];
        }
    };
}
