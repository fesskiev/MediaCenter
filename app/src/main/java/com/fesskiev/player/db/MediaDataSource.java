package com.fesskiev.player.db;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.model.Artist;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.model.Genre;
import com.fesskiev.player.model.MediaFile;
import com.fesskiev.player.model.VideoFile;
import com.fesskiev.player.utils.AppLog;
import com.fesskiev.player.utils.CacheManager;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.schedulers.Schedulers;

public class MediaDataSource {

    private static MediaDataSource INSTANCE;
    private final BriteDatabase briteDatabase;

    public static MediaDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MediaDataSource();
        }
        return INSTANCE;
    }

    private MediaDataSource() {
        MediaDatabaseHelper dbHelper
                = new MediaDatabaseHelper(MediaApplication.getInstance().getApplicationContext());
        SqlBrite sqlBrite = SqlBrite.create();
        briteDatabase = sqlBrite.wrapDatabaseHelper(dbHelper, Schedulers.io());
    }

    public void insertAudioFolder(AudioFolder audioFolder) {

        ContentValues values = new ContentValues();

        values.put(MediaDatabaseHelper.ID, audioFolder.id);
        values.put(MediaDatabaseHelper.FOLDER_PATH, audioFolder.folderPath.getAbsolutePath());
        values.put(MediaDatabaseHelper.FOLDER_NAME, audioFolder.folderName);
        values.put(MediaDatabaseHelper.FOLDER_COVER,
                audioFolder.folderImage != null ? audioFolder.folderImage.getAbsolutePath() : null);
        values.put(MediaDatabaseHelper.FOLDER_INDEX, audioFolder.index);
        values.put(MediaDatabaseHelper.FOLDER_SELECTED, audioFolder.isSelected ? 1 : 0);

        briteDatabase.insert(MediaDatabaseHelper.AUDIO_FOLDERS_TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);

    }

    public void insertAudioFile(AudioFile audioFile) {

        ContentValues values = new ContentValues();

        values.put(MediaDatabaseHelper.ID, audioFile.id);
        values.put(MediaDatabaseHelper.TRACK_ARTIST, audioFile.artist);
        values.put(MediaDatabaseHelper.TRACK_TITLE, audioFile.title);
        values.put(MediaDatabaseHelper.TRACK_ALBUM, audioFile.album);
        values.put(MediaDatabaseHelper.TRACK_GENRE, audioFile.genre);
        values.put(MediaDatabaseHelper.TRACK_PATH, audioFile.getFilePath());
        values.put(MediaDatabaseHelper.TRACK_BITRATE, audioFile.bitrate);
        values.put(MediaDatabaseHelper.TRACK_LENGTH, audioFile.length);
        values.put(MediaDatabaseHelper.TRACK_NUMBER, audioFile.trackNumber);
        values.put(MediaDatabaseHelper.TRACK_SAMPLE_RATE, audioFile.sampleRate);
        values.put(MediaDatabaseHelper.TRACK_IN_PLAY_LIST, audioFile.inPlayList ? 1 : 0);
        values.put(MediaDatabaseHelper.TRACK_SELECTED, audioFile.isSelected ? 1 : 0);
        values.put(MediaDatabaseHelper.TRACK_COVER, audioFile.artworkPath);

        briteDatabase.insert(MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void updateAudioFile(AudioFile audioFile) {

        ContentValues values = new ContentValues();

        values.put(MediaDatabaseHelper.ID, audioFile.id);
        values.put(MediaDatabaseHelper.TRACK_ARTIST, audioFile.artist);
        values.put(MediaDatabaseHelper.TRACK_TITLE, audioFile.title);
        values.put(MediaDatabaseHelper.TRACK_ALBUM, audioFile.album);
        values.put(MediaDatabaseHelper.TRACK_GENRE, audioFile.genre);
        values.put(MediaDatabaseHelper.TRACK_PATH, audioFile.getFilePath());
        values.put(MediaDatabaseHelper.TRACK_BITRATE, audioFile.bitrate);
        values.put(MediaDatabaseHelper.TRACK_LENGTH, audioFile.length);
        values.put(MediaDatabaseHelper.TRACK_NUMBER, audioFile.trackNumber);
        values.put(MediaDatabaseHelper.TRACK_SAMPLE_RATE, audioFile.sampleRate);
        values.put(MediaDatabaseHelper.TRACK_IN_PLAY_LIST, audioFile.inPlayList ? 1 : 0);
        values.put(MediaDatabaseHelper.TRACK_COVER, audioFile.artworkPath);

        briteDatabase.update(
                MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                values,
                MediaDatabaseHelper.TRACK_PATH + "=" + "'" + audioFile.filePath + "'");
    }

    public void insertVideoFile(VideoFile videoFile) {

        ContentValues values = new ContentValues();

        values.put(MediaDatabaseHelper.ID, videoFile.id);
        values.put(MediaDatabaseHelper.VIDEO_FILE_PATH, videoFile.getFilePath());
        values.put(MediaDatabaseHelper.VIDEO_FRAME_PATH, videoFile.framePath);
        values.put(MediaDatabaseHelper.VIDEO_DESCRIPTION, videoFile.description);
        values.put(MediaDatabaseHelper.VIDEO_IN_PLAY_LIST, videoFile.inPlayList ? 1 : 0);


        briteDatabase.insert(MediaDatabaseHelper.VIDEO_FILES_TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void updateAudioFolderIndex(AudioFolder audioFolder) {

        ContentValues values = new ContentValues();
        values.put(MediaDatabaseHelper.FOLDER_INDEX, audioFolder.index);

        String sql = MediaDatabaseHelper.FOLDER_PATH + "=" + "'" + audioFolder.folderPath + "'";

        briteDatabase.update(MediaDatabaseHelper.AUDIO_FOLDERS_TABLE_NAME, values, sql);
    }

    public void updateVideoFile(VideoFile videoFile) {

        ContentValues values = new ContentValues();

        values.put(MediaDatabaseHelper.ID, videoFile.id);
        values.put(MediaDatabaseHelper.VIDEO_FILE_PATH, videoFile.getFilePath());
        values.put(MediaDatabaseHelper.VIDEO_FRAME_PATH, videoFile.framePath);
        values.put(MediaDatabaseHelper.VIDEO_DESCRIPTION, videoFile.description);
        values.put(MediaDatabaseHelper.VIDEO_IN_PLAY_LIST, videoFile.inPlayList ? 1 : 0);

        briteDatabase.update(
                MediaDatabaseHelper.VIDEO_FILES_TABLE_NAME,
                values,
                MediaDatabaseHelper.VIDEO_FILE_PATH + "=" + "'" + videoFile.filePath + "'");
    }


    public Observable<AudioFolder> getSelectedAudioFolder() {

        String sql = String.format("SELECT * FROM %s WHERE %s", MediaDatabaseHelper.AUDIO_FOLDERS_TABLE_NAME,
                MediaDatabaseHelper.FOLDER_SELECTED + "=" + "'" + 1 + "'");

        return briteDatabase.createQuery(MediaDatabaseHelper.AUDIO_FOLDERS_TABLE_NAME, sql)
                .mapToOneOrDefault(AudioFolder::new, null);

    }

    public Observable<AudioFile> getSelectedAudioFile() {

        String sql = String.format("SELECT * FROM %s WHERE %s", MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                MediaDatabaseHelper.TRACK_SELECTED + "=" + "'" + 1 + "'");

        return briteDatabase.createQuery(MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql)
                .mapToOneOrDefault(AudioFile::new, null);

    }

    public Observable<List<AudioFile>> getSelectedFolderAudioFiles(AudioFolder audioFolder) {

        String sql = String.format("SELECT * FROM %s WHERE %s ORDER BY %s ASC", MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                MediaDatabaseHelper.ID + "=" + "'" + audioFolder.id + "'", MediaDatabaseHelper.TRACK_NUMBER);

        return briteDatabase.createQuery(MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql).mapToList(AudioFile::new);

    }

    public boolean containAudioTrack(String path) {

        String sql = String.format("SELECT * FROM %s WHERE %s",
                MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                MediaDatabaseHelper.TRACK_PATH + "=" + "'" + path + "'");

        Cursor cursor = briteDatabase.query(sql);

        boolean contain = cursor.getCount() > 0;
        cursor.close();

        return contain;
    }

    public String getDownloadFolderID() {

        String sql = String.format("SELECT %s FROM %s WHERE %s",
                MediaDatabaseHelper.ID,
                MediaDatabaseHelper.AUDIO_FOLDERS_TABLE_NAME,
                MediaDatabaseHelper.FOLDER_PATH + "=" + "'" + CacheManager.CHECK_DOWNLOADS_FOLDER_PATH + "'");

        Cursor cursor = briteDatabase.query(sql);

        cursor.moveToFirst();
        String id = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.ID));
        cursor.close();

        return id;
    }

    public void updateSelectedAudioFile(AudioFile audioFile) {
        ContentValues clearValues = new ContentValues();
        clearValues.put(MediaDatabaseHelper.TRACK_SELECTED, 0);

        briteDatabase.update(
                MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                clearValues,
                null);

        ContentValues dateValues = new ContentValues();
        dateValues.put(MediaDatabaseHelper.TRACK_SELECTED, audioFile.isSelected ? 1 : 0);

        briteDatabase.update(
                MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                dateValues,
                MediaDatabaseHelper.TRACK_PATH + "=" + "'" + audioFile.filePath + "'");

    }

    public void updateSelectedAudioFolder(AudioFolder audioFolder) {
        ContentValues clearValues = new ContentValues();
        clearValues.put(MediaDatabaseHelper.FOLDER_SELECTED, 0);

        briteDatabase.update(
                MediaDatabaseHelper.AUDIO_FOLDERS_TABLE_NAME,
                clearValues,
                null);

        ContentValues dateValues = new ContentValues();
        dateValues.put(MediaDatabaseHelper.FOLDER_SELECTED, audioFolder.isSelected ? 1 : 0);


        briteDatabase.update(
                MediaDatabaseHelper.AUDIO_FOLDERS_TABLE_NAME,
                dateValues,
                MediaDatabaseHelper.FOLDER_PATH + "=" + "'" + audioFolder.folderPath + "'");
    }

    public Callable<Integer> resetVideoContentDatabase() {
        return () -> briteDatabase.delete(MediaDatabaseHelper.VIDEO_FILES_TABLE_NAME, null);
    }

    public Callable<Integer> resetAudioContentDatabase() {
        return () -> {
            briteDatabase.delete(MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME, null);
            return briteDatabase.delete(MediaDatabaseHelper.AUDIO_FOLDERS_TABLE_NAME, null);
        };
    }

    public void deleteAudioFile(String path) {

        briteDatabase.delete(
                MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                MediaDatabaseHelper.TRACK_PATH + "=" + "'" + path + "'");
    }

    public void deleteVideoFile(String path) {

        briteDatabase.delete(
                MediaDatabaseHelper.VIDEO_FILES_TABLE_NAME,
                MediaDatabaseHelper.VIDEO_FILE_PATH + "=" + "'" + path + "'");
    }

    public Observable<List<String>> getFoldersPath() {

        String sql = String.format("SELECT %s FROM %s",
                MediaDatabaseHelper.FOLDER_PATH,
                MediaDatabaseHelper.AUDIO_FOLDERS_TABLE_NAME);

        return briteDatabase
                .createQuery(MediaDatabaseHelper.AUDIO_FOLDERS_TABLE_NAME, sql)
                .mapToList(cursor -> cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.FOLDER_PATH)));
    }

    public void clearPlaylist() {

        ContentValues contentValues;

        contentValues = new ContentValues();
        contentValues.put(MediaDatabaseHelper.TRACK_IN_PLAY_LIST, 0);

        briteDatabase.update(
                MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                contentValues,
                null);

        contentValues = new ContentValues();
        contentValues.put(MediaDatabaseHelper.VIDEO_IN_PLAY_LIST, 0);


        briteDatabase.update(
                MediaDatabaseHelper.VIDEO_FILES_TABLE_NAME,
                contentValues,
                null);
    }



    public Observable<List<VideoFile>> getVideoFilesFromDB() {

        String sql = String.format("SELECT * FROM %s",  MediaDatabaseHelper.VIDEO_FILES_TABLE_NAME);

        return briteDatabase
                .createQuery(MediaDatabaseHelper.VIDEO_FILES_TABLE_NAME, sql)
                .mapToList(VideoFile::new);
    }



    public Observable<List<AudioFolder>> getAudioFoldersFromDB() {

        String sql = String.format("SELECT * FROM %s",  MediaDatabaseHelper.AUDIO_FOLDERS_TABLE_NAME);

        return briteDatabase
                .createQuery(MediaDatabaseHelper.AUDIO_FOLDERS_TABLE_NAME, sql)
                .mapToList(AudioFolder::new);
    }


    public Observable<List<Artist>> getArtistsFromDB() {

        String[] projection = {
                MediaDatabaseHelper.TRACK_ARTIST,
                MediaDatabaseHelper.TRACK_COVER,
        };

        String sql = String.format("SELECT %s FROM %s GROUP BY %s",
                TextUtils.join(",", projection),
                MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                MediaDatabaseHelper.TRACK_ARTIST);

        return briteDatabase
                .createQuery(MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql)
                .mapToList(Artist::new);

    }


    public Observable<List<Genre>> getGenresFromDB() {

        String[] projection = {
                MediaDatabaseHelper.TRACK_GENRE,
                MediaDatabaseHelper.TRACK_COVER,
        };

        String sql = String.format("SELECT %s FROM %s GROUP BY %s",
                TextUtils.join(",", projection),
                MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                MediaDatabaseHelper.TRACK_GENRE);

        return briteDatabase
                .createQuery(MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql)
                .mapToList(Genre::new);
    }


    public Observable<List<AudioFile>> getGenreTracksFromDB(String contentValue) {

        String sql = String.format("SELECT * FROM %s WHERE %s ORDER BY %s",
                MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                MediaDatabaseHelper.TRACK_GENRE + "=" + "'" + contentValue + "'",
                MediaDatabaseHelper.TRACK_NUMBER + " ASC");

        return briteDatabase.createQuery(MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql)
                .mapToList(AudioFile::new);
    }

    public Observable<List<AudioFile>> getArtistTracksFromDB(String contentValue) {

        String sql = String.format("SELECT * FROM %s WHERE %s ORDER BY %s",
                MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                MediaDatabaseHelper.TRACK_ARTIST + "=" + "'" + contentValue + "'",
                MediaDatabaseHelper.TRACK_NUMBER + " ASC");

        return briteDatabase.createQuery(MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql)
                .mapToList(AudioFile::new);
    }

    public Observable<List<AudioFile>> getFolderTracksFromDB(String id) {

        String sql = String.format("SELECT * FROM %s WHERE %s ORDER BY %s",
                MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                MediaDatabaseHelper.ID + "=" + "'" + id + "'",
                MediaDatabaseHelper.TRACK_NUMBER + " ASC");

        return briteDatabase.createQuery(MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql)
                .mapToList(AudioFile::new);
    }

    public Observable<List<MediaFile>> getAudioFilePlaylistFromDB() {

        String sql = String.format("SELECT * FROM %s WHERE %s",
                MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                MediaDatabaseHelper.TRACK_IN_PLAY_LIST + "=1");

        return briteDatabase.createQuery(MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql)
                .mapToList(AudioFile::new);
    }

    public Observable<List<MediaFile>> getVideoFilePlaylistFromDB() {

        String sql = String.format("SELECT * FROM %s WHERE %s",
                MediaDatabaseHelper.VIDEO_FILES_TABLE_NAME,
                MediaDatabaseHelper.VIDEO_IN_PLAY_LIST + "=1");

        return briteDatabase.createQuery(MediaDatabaseHelper.VIDEO_FILES_TABLE_NAME, sql)
                .mapToList(VideoFile::new);
    }

    public Observable<AudioFile> getAudioFileByPathFromDB(String path) {

        String sql = String.format("SELECT * FROM %s WHERE %s",
                MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                MediaDatabaseHelper.TRACK_PATH + "=" + "'" + path + "'");

        return briteDatabase.createQuery(MediaDatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql)
                .mapToOne(AudioFile::new);
    }

}
