package com.fesskiev.common.data;


import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.wearable.DataMap;

public class MapAudioFile implements Parcelable {

    public String id;
    public String artist;
    public String title;
    public String album;
    public String genre;
    public String bitrate;
    public String sampleRate;
    public int trackNumber;
    public long length;
    public long size;
    public long timestamp;

    public Bitmap cover;


    public MapAudioFile() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MapAudioFile that = (MapAudioFile) o;

        if (length != that.length) return false;
        if (size != that.size) return false;
        if (!id.equals(that.id)) return false;
        if (!artist.equals(that.artist)) return false;
        return title.equals(that.title);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + artist.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + (int) (length ^ (length >>> 32));
        result = 31 * result + (int) (size ^ (size >>> 32));
        return result;
    }

    public DataMap toDataMap(DataMap map) {
        map.putString("album", album);
        map.putString("bitrate", bitrate);
        map.putString("sampleRate", sampleRate);
        map.putString("title", title);
        map.putLong("size", size);
        map.putLong("timestamp", timestamp);
        map.putString("id", id);
        map.putString("genre", genre);
        map.putString("artist", artist);
        map.putLong("length", length);
        map.putInt("trackNumber", trackNumber);
        return map;
    }

    public static MapAudioFile toMapAudioFile(DataMap map) {
        return new MapAudioFileBuilder()
                .withAlbum(map.getString("album"))
                .withBitrate(map.getString("bitrate"))
                .withSampleRate(map.getString("sampleRate"))
                .withTitle(map.getString("title"))
                .withSize(map.getLong("size"))
                .withTimestamp(map.getLong("timestamp"))
                .withId(map.getString("id"))
                .withGenre(map.getString("genre"))
                .withArtist(map.getString("artist"))
                .withLength(map.getLong("length"))
                .withTrackNumber(map.getInt("trackNumber"))
                .build();
    }

    @Override
    public String toString() {
        return "MapAudioFile{" +
                "id='" + id + '\'' +
                ", artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                ", album='" + album + '\'' +
                ", genre='" + genre + '\'' +
                ", bitrate='" + bitrate + '\'' +
                ", sampleRate='" + sampleRate + '\'' +
                ", trackNumber=" + trackNumber +
                ", length=" + length +
                ", size=" + size +
                ", timestamp=" + timestamp +
                '}';
    }


    public static final class MapAudioFileBuilder {

        public String id;
        public String artist;
        public String title;
        public String album;
        public String genre;
        public String bitrate;
        public String sampleRate;
        public int trackNumber;
        public long length;
        public long size;
        public long timestamp;

        private MapAudioFileBuilder() {

        }

        public static MapAudioFileBuilder buildMapAudioFile() {
            return new MapAudioFileBuilder();
        }

        public MapAudioFileBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public MapAudioFileBuilder withArtist(String artist) {
            this.artist = artist;
            return this;
        }

        public MapAudioFileBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public MapAudioFileBuilder withAlbum(String album) {
            this.album = album;
            return this;
        }

        public MapAudioFileBuilder withGenre(String genre) {
            this.genre = genre;
            return this;
        }

        public MapAudioFileBuilder withBitrate(String bitrate) {
            this.bitrate = bitrate;
            return this;
        }

        public MapAudioFileBuilder withSampleRate(String sampleRate) {
            this.sampleRate = sampleRate;
            return this;
        }

        public MapAudioFileBuilder withTrackNumber(int trackNumber) {
            this.trackNumber = trackNumber;
            return this;
        }

        public MapAudioFileBuilder withLength(long length) {
            this.length = length;
            return this;
        }

        public MapAudioFileBuilder withSize(long size) {
            this.size = size;
            return this;
        }

        public MapAudioFileBuilder withTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public MapAudioFile build() {
            MapAudioFile mapAudioFile = new MapAudioFile();
            mapAudioFile.album = this.album;
            mapAudioFile.bitrate = this.bitrate;
            mapAudioFile.sampleRate = this.sampleRate;
            mapAudioFile.title = this.title;
            mapAudioFile.size = this.size;
            mapAudioFile.timestamp = this.timestamp;
            mapAudioFile.id = this.id;
            mapAudioFile.genre = this.genre;
            mapAudioFile.artist = this.artist;
            mapAudioFile.length = this.length;
            mapAudioFile.trackNumber = this.trackNumber;
            return mapAudioFile;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.artist);
        dest.writeString(this.title);
        dest.writeString(this.album);
        dest.writeString(this.genre);
        dest.writeString(this.bitrate);
        dest.writeString(this.sampleRate);
        dest.writeInt(this.trackNumber);
        dest.writeLong(this.length);
        dest.writeLong(this.size);
        dest.writeLong(this.timestamp);
        dest.writeParcelable(this.cover, flags);
    }

    protected MapAudioFile(Parcel in) {
        this.id = in.readString();
        this.artist = in.readString();
        this.title = in.readString();
        this.album = in.readString();
        this.genre = in.readString();
        this.bitrate = in.readString();
        this.sampleRate = in.readString();
        this.trackNumber = in.readInt();
        this.length = in.readLong();
        this.size = in.readLong();
        this.timestamp = in.readLong();
        this.cover = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<MapAudioFile> CREATOR = new Creator<MapAudioFile>() {
        @Override
        public MapAudioFile createFromParcel(Parcel source) {
            return new MapAudioFile(source);
        }

        @Override
        public MapAudioFile[] newArray(int size) {
            return new MapAudioFile[size];
        }
    };
}
