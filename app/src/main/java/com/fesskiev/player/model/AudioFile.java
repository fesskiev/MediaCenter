package com.fesskiev.player.model;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.util.Log;

import com.fesskiev.player.R;
import com.fesskiev.player.db.MediaCenterProvider;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.CacheManager;
import com.fesskiev.player.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class AudioFile implements MediaFile, Comparable<AudioFile> {

    public interface OnMp3TagListener {
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
    private OnMp3TagListener listener;

    public AudioFile(Context context, File filePath, OnMp3TagListener listener) {
        this.context = context;
        this.filePath = filePath;
        this.listener = listener;
        renameFileCorrect();
        getTrackInfo();
    }

    public AudioFile(Cursor cursor) {

        id = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.ID));
        filePath = new File(cursor.getString(cursor.getColumnIndex(MediaCenterProvider.TRACK_PATH)));
        artist = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.TRACK_ARTIST));
        title = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.TRACK_TITLE));
        album = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.TRACK_ALBUM));
        genre = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.TRACK_GENRE));
        bitrate = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.TRACK_BITRATE));
        sampleRate = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.TRACK_SAMPLE_RATE));
        artworkPath = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.TRACK_COVER));
        trackNumber = cursor.getInt(cursor.getColumnIndex(MediaCenterProvider.TRACK_NUMBER));
        inPlayList = cursor.getInt(cursor.getColumnIndex(MediaCenterProvider.TRACK_IN_PLAY_LIST)) == 1;
        isSelected = cursor.getInt(cursor.getColumnIndex(MediaCenterProvider.TRACK_SELECTED)) == 1;
        length = cursor.getInt(cursor.getColumnIndex(MediaCenterProvider.TRACK_LENGTH));

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
        parseMetadata();
        if (listener != null) {
            listener.onFetchCompleted(this);
        }
    }

    private void parseMetadata() {

        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(filePath.getAbsolutePath());

        artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        album = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        genre = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
        bitrate = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE) + " kbps";

        String tracksValue = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS);
        if (tracksValue != null) {
            trackNumber = Integer.valueOf(tracksValue);
        }

        String lengthValue = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if (lengthValue != null) {
            length = Integer.valueOf(lengthValue);
        }

        long seconds = length / 1000;
        long fileSize = filePath.length();
        if (fileSize != 0) {
            sampleRate = (fileSize / seconds) + " Hz";
        }

        byte[] artwork = metadataRetriever.getEmbeddedPicture();
        if (artwork != null) {
            saveArtwork(artwork);
        }

        fillEmptyFields();
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
