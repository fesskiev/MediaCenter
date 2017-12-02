package com.fesskiev.mediacenter.data.source.local;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.MediaFile;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.VideoFolder;
import com.fesskiev.mediacenter.data.source.local.db.DatabaseHelper;
import com.squareup.sqlbrite2.BriteDatabase;


import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;

public class LocalDataSource implements LocalSource {

    private BriteDatabase briteDatabase;

    public LocalDataSource(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @Override
    public void insertAudioFolder(AudioFolder audioFolder) {

        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.AUDIO_FOLDER_ID, audioFolder.id);
        values.put(DatabaseHelper.FOLDER_PATH, audioFolder.folderPath.getAbsolutePath());
        values.put(DatabaseHelper.FOLDER_NAME, audioFolder.folderName);
        values.put(DatabaseHelper.FOLDER_COVER,
                audioFolder.folderImage != null ? audioFolder.folderImage.getAbsolutePath() : null);
        values.put(DatabaseHelper.FOLDER_TIMESTAMP, audioFolder.timestamp);
        values.put(DatabaseHelper.FOLDER_INDEX, audioFolder.index);
        values.put(DatabaseHelper.FOLDER_SELECTED, audioFolder.isSelected ? 1 : 0);
        values.put(DatabaseHelper.FOLDER_HIDDEN, audioFolder.isHidden ? 1 : 0);

        briteDatabase.insert(DatabaseHelper.AUDIO_FOLDERS_TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);

    }

    @Override
    public void updateAudioFolder(AudioFolder audioFolder) {
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.AUDIO_FOLDER_ID, audioFolder.id);
        values.put(DatabaseHelper.FOLDER_PATH, audioFolder.folderPath.getAbsolutePath());
        values.put(DatabaseHelper.FOLDER_NAME, audioFolder.folderName);
        values.put(DatabaseHelper.FOLDER_COVER,
                audioFolder.folderImage != null ? audioFolder.folderImage.getAbsolutePath() : null);
        values.put(DatabaseHelper.FOLDER_TIMESTAMP, audioFolder.timestamp);
        values.put(DatabaseHelper.FOLDER_INDEX, audioFolder.index);
        values.put(DatabaseHelper.FOLDER_SELECTED, audioFolder.isSelected ? 1 : 0);
        values.put(DatabaseHelper.FOLDER_HIDDEN, audioFolder.isHidden ? 1 : 0);

        briteDatabase.update(
                DatabaseHelper.AUDIO_FOLDERS_TABLE_NAME,
                values, DatabaseHelper.AUDIO_FOLDER_ID + "=" + "'" + audioFolder.id + "'");
    }


    @Override
    public void insertAudioFile(AudioFile audioFile) {

        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.AUDIO_FOLDER_ID, audioFile.folderId);
        values.put(DatabaseHelper.AUDIO_FILE_ID, audioFile.fileId);
        values.put(DatabaseHelper.TRACK_ARTIST, audioFile.artist);
        values.put(DatabaseHelper.TRACK_TITLE, audioFile.title);
        values.put(DatabaseHelper.TRACK_ALBUM, audioFile.album);
        values.put(DatabaseHelper.TRACK_GENRE, audioFile.genre);
        values.put(DatabaseHelper.TRACK_PATH, audioFile.getFilePath());
        values.put(DatabaseHelper.TRACK_BITRATE, audioFile.bitrate);
        values.put(DatabaseHelper.TRACK_LENGTH, audioFile.length);
        values.put(DatabaseHelper.TRACK_SIZE, audioFile.size);
        values.put(DatabaseHelper.TRACK_TIMESTAMP, audioFile.timestamp);
        values.put(DatabaseHelper.TRACK_NUMBER, audioFile.trackNumber);
        values.put(DatabaseHelper.TRACK_SAMPLE_RATE, audioFile.sampleRate);
        values.put(DatabaseHelper.TRACK_IN_PLAY_LIST, audioFile.inPlayList ? 1 : 0);
        values.put(DatabaseHelper.TRACK_SELECTED, audioFile.isSelected ? 1 : 0);
        values.put(DatabaseHelper.TRACK_HIDDEN, audioFile.isHidden ? 1 : 0);
        values.put(DatabaseHelper.TRACK_COVER, audioFile.artworkPath);
        values.put(DatabaseHelper.TRACK_FOLDER_COVER, audioFile.folderArtworkPath);

        briteDatabase.insert(DatabaseHelper.AUDIO_TRACKS_TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public Callable<Integer> updateAudioFile(AudioFile audioFile) {
        return () -> {

            ContentValues values = new ContentValues();

            values.put(DatabaseHelper.AUDIO_FOLDER_ID, audioFile.folderId);
            values.put(DatabaseHelper.AUDIO_FILE_ID, audioFile.fileId);
            values.put(DatabaseHelper.TRACK_ARTIST, audioFile.artist);
            values.put(DatabaseHelper.TRACK_TITLE, audioFile.title);
            values.put(DatabaseHelper.TRACK_ALBUM, audioFile.album);
            values.put(DatabaseHelper.TRACK_GENRE, audioFile.genre);
            values.put(DatabaseHelper.TRACK_PATH, audioFile.getFilePath());
            values.put(DatabaseHelper.TRACK_BITRATE, audioFile.bitrate);
            values.put(DatabaseHelper.TRACK_LENGTH, audioFile.length);
            values.put(DatabaseHelper.TRACK_SIZE, audioFile.size);
            values.put(DatabaseHelper.TRACK_TIMESTAMP, audioFile.timestamp);
            values.put(DatabaseHelper.TRACK_NUMBER, audioFile.trackNumber);
            values.put(DatabaseHelper.TRACK_SAMPLE_RATE, audioFile.sampleRate);
            values.put(DatabaseHelper.TRACK_IN_PLAY_LIST, audioFile.inPlayList ? 1 : 0);
            values.put(DatabaseHelper.TRACK_SELECTED, audioFile.isSelected ? 1 : 0);
            values.put(DatabaseHelper.TRACK_HIDDEN, audioFile.isHidden ? 1 : 0);
            values.put(DatabaseHelper.TRACK_COVER, audioFile.artworkPath);
            values.put(DatabaseHelper.TRACK_FOLDER_COVER, audioFile.folderArtworkPath);

            return briteDatabase.update(
                    DatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                    values, DatabaseHelper.AUDIO_FILE_ID + "=" + "'" + audioFile.fileId + "'");

        };
    }

    @Override
    public void insertVideoFolder(VideoFolder videoFolder) {

        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.VIDEO_FOLDER_ID, videoFolder.id);
        values.put(DatabaseHelper.FOLDER_PATH, videoFolder.folderPath.getAbsolutePath());
        values.put(DatabaseHelper.FOLDER_NAME, videoFolder.folderName);
        values.put(DatabaseHelper.FOLDER_TIMESTAMP, videoFolder.timestamp);
        values.put(DatabaseHelper.FOLDER_INDEX, videoFolder.index);
        values.put(DatabaseHelper.FOLDER_SELECTED, videoFolder.isSelected ? 1 : 0);
        values.put(DatabaseHelper.FOLDER_HIDDEN, videoFolder.isHidden ? 1 : 0);

        briteDatabase.insert(DatabaseHelper.VIDEO_FOLDERS_TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);

    }

    @Override
    public void updateVideoFolder(VideoFolder videoFolder) {

        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.VIDEO_FOLDER_ID, videoFolder.id);
        values.put(DatabaseHelper.FOLDER_PATH, videoFolder.folderPath.getAbsolutePath());
        values.put(DatabaseHelper.FOLDER_NAME, videoFolder.folderName);
        values.put(DatabaseHelper.FOLDER_TIMESTAMP, videoFolder.timestamp);
        values.put(DatabaseHelper.FOLDER_INDEX, videoFolder.index);
        values.put(DatabaseHelper.FOLDER_SELECTED, videoFolder.isSelected ? 1 : 0);
        values.put(DatabaseHelper.FOLDER_HIDDEN, videoFolder.isHidden ? 1 : 0);

        briteDatabase.update(
                DatabaseHelper.VIDEO_FOLDERS_TABLE_NAME,
                values,
                DatabaseHelper.VIDEO_FOLDER_ID + "=" + "'" + videoFolder.id + "'");

    }

    @Override
    public Callable<Integer> deleteVideoFolderWithFiles(VideoFolder videoFolder) {
        return () -> {
            briteDatabase.delete(
                    DatabaseHelper.VIDEO_FOLDERS_TABLE_NAME,
                    DatabaseHelper.VIDEO_FOLDER_ID + "=" + "'" + videoFolder.id + "'");

            return briteDatabase.delete(
                    DatabaseHelper.VIDEO_FILES_TABLE_NAME,
                    DatabaseHelper.VIDEO_FOLDER_ID + "=" + "'" + videoFolder.id + "'");

        };
    }

    @Override
    public void insertVideoFile(VideoFile videoFile) {

        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.VIDEO_FOLDER_ID, videoFile.folderId);
        values.put(DatabaseHelper.VIDEO_FILE_ID, videoFile.fileId);
        values.put(DatabaseHelper.VIDEO_FILE_PATH, videoFile.getFilePath());
        values.put(DatabaseHelper.VIDEO_FRAME_PATH, videoFile.framePath);
        values.put(DatabaseHelper.VIDEO_RESOLUTION, videoFile.resolution);
        values.put(DatabaseHelper.VIDEO_DESCRIPTION, videoFile.description);
        values.put(DatabaseHelper.VIDEO_LENGTH, videoFile.length);
        values.put(DatabaseHelper.VIDEO_SIZE, videoFile.size);
        values.put(DatabaseHelper.VIDEO_TIMESTAMP, videoFile.timestamp);
        values.put(DatabaseHelper.VIDEO_IN_PLAY_LIST, videoFile.inPlayList ? 1 : 0);
        values.put(DatabaseHelper.VIDEO_HIDDEN, videoFile.inPlayList ? 1 : 0);


        briteDatabase.insert(DatabaseHelper.VIDEO_FILES_TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
    }


    @Override
    public Callable<Integer> updateVideoFile(VideoFile videoFile) {
        return () -> {

            ContentValues values = new ContentValues();

            values.put(DatabaseHelper.VIDEO_FOLDER_ID, videoFile.folderId);
            values.put(DatabaseHelper.VIDEO_FILE_ID, videoFile.fileId);
            values.put(DatabaseHelper.VIDEO_FILE_PATH, videoFile.getFilePath());
            values.put(DatabaseHelper.VIDEO_FRAME_PATH, videoFile.framePath);
            values.put(DatabaseHelper.VIDEO_RESOLUTION, videoFile.resolution);
            values.put(DatabaseHelper.VIDEO_DESCRIPTION, videoFile.description);
            values.put(DatabaseHelper.VIDEO_LENGTH, videoFile.length);
            values.put(DatabaseHelper.VIDEO_SIZE, videoFile.size);
            values.put(DatabaseHelper.VIDEO_TIMESTAMP, videoFile.timestamp);
            values.put(DatabaseHelper.VIDEO_IN_PLAY_LIST, videoFile.inPlayList ? 1 : 0);
            values.put(DatabaseHelper.VIDEO_HIDDEN, videoFile.isHidden ? 1 : 0);

            return briteDatabase.update(
                    DatabaseHelper.VIDEO_FILES_TABLE_NAME,
                    values,
                    DatabaseHelper.VIDEO_FILE_ID + "=" + "'" + videoFile.fileId + "'");

        };
    }

    @Override
    public Observable<AudioFolder> getSelectedAudioFolder() {

        String sql = String.format("SELECT * FROM %s WHERE %s", DatabaseHelper.AUDIO_FOLDERS_TABLE_NAME,
                DatabaseHelper.FOLDER_SELECTED + "=" + "'" + 1 + "'");

        return briteDatabase.createQuery(DatabaseHelper.AUDIO_FOLDERS_TABLE_NAME, sql)
                .mapToOne(AudioFolder::new);

    }

    @Override
    public Observable<AudioFile> getSelectedAudioFile() {

        String sql = String.format("SELECT * FROM %s WHERE %s", DatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                DatabaseHelper.TRACK_SELECTED + "=" + "'" + 1 + "'");

        return briteDatabase.createQuery(DatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql)
                .mapToOne(AudioFile::new);

    }

    @Override
    public Observable<List<AudioFile>> getSelectedFolderAudioFiles(AudioFolder audioFolder) {

        String sql = String.format("SELECT * FROM %s WHERE %s ORDER BY %s ASC", DatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                DatabaseHelper.AUDIO_FOLDER_ID + "=" + "'" + audioFolder.id + "'", DatabaseHelper.TRACK_NUMBER);

        return briteDatabase.createQuery(DatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql).mapToList(AudioFile::new);

    }

    @Override
    public boolean containAudioTrack(String path) {

        String sql = String.format("SELECT * FROM %s WHERE %s",
                DatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                DatabaseHelper.TRACK_PATH + "=" + "'" + path.replaceAll("'", "''") + "'");

        Cursor cursor = briteDatabase.query(sql);

        boolean contain = cursor.getCount() > 0;
        cursor.close();

        return contain;
    }

    @Override
    public boolean containAudioFolder(String path) {

        String sql = String.format("SELECT * FROM %s WHERE %s",
                DatabaseHelper.AUDIO_FOLDERS_TABLE_NAME,
                DatabaseHelper.FOLDER_PATH + "=" + "'" + path.replaceAll("'", "''") + "'");
        Cursor cursor = briteDatabase.query(sql);

        boolean contain = cursor.getCount() > 0;
        cursor.close();

        return contain;
    }

    @Override
    public boolean containVideoFolder(String path) {
        String sql = String.format("SELECT * FROM %s WHERE %s",
                DatabaseHelper.VIDEO_FOLDERS_TABLE_NAME,
                DatabaseHelper.FOLDER_PATH + "=" + "'" + path.replaceAll("'", "''") + "'");
        Cursor cursor = briteDatabase.query(sql);

        boolean contain = cursor.getCount() > 0;
        cursor.close();

        return contain;
    }

    @Override
    public void updateSelectedAudioFile(AudioFile audioFile) {
        ContentValues clearValues = new ContentValues();
        clearValues.put(DatabaseHelper.TRACK_SELECTED, 0);

        briteDatabase.update(
                DatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                clearValues,
                null);

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TRACK_SELECTED, audioFile.isSelected ? 1 : 0);

        briteDatabase.update(
                DatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                values,
                DatabaseHelper.TRACK_PATH + "=" + "'" + audioFile.filePath.getAbsolutePath().replaceAll("'", "''") + "'");

    }

    @Override
    public void updateSelectedAudioFolder(AudioFolder audioFolder) {
        ContentValues clearValues = new ContentValues();
        clearValues.put(DatabaseHelper.FOLDER_SELECTED, 0);

        briteDatabase.update(
                DatabaseHelper.AUDIO_FOLDERS_TABLE_NAME,
                clearValues,
                null);

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.FOLDER_SELECTED, audioFolder.isSelected ? 1 : 0);

        briteDatabase.update(
                DatabaseHelper.AUDIO_FOLDERS_TABLE_NAME,
                values,
                DatabaseHelper.FOLDER_PATH + "=" + "'" + audioFolder.folderPath.getAbsolutePath().replaceAll("'", "''") + "'");
    }

    @Override
    public Callable<Integer> resetVideoContentDatabase() {
        return () -> {
            briteDatabase.delete(DatabaseHelper.VIDEO_FILES_TABLE_NAME, null);
            return briteDatabase.delete(DatabaseHelper.VIDEO_FOLDERS_TABLE_NAME, null);
        };
    }

    @Override
    public Callable<Integer> resetAudioContentDatabase() {
        return () -> {
            briteDatabase.delete(DatabaseHelper.AUDIO_TRACKS_TABLE_NAME, null);
            return briteDatabase.delete(DatabaseHelper.AUDIO_FOLDERS_TABLE_NAME, null);
        };
    }

    @Override
    public Callable<Integer> deleteAudioFile(String path) {
        return () -> briteDatabase.delete(
                DatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                DatabaseHelper.TRACK_PATH + "=" + "'" + path + "'");
    }


    @Override
    public Callable<Integer> deleteAudioFolderWithFiles(AudioFolder audioFolder) {
        return () -> {
            briteDatabase.delete(
                    DatabaseHelper.AUDIO_FOLDERS_TABLE_NAME,
                    DatabaseHelper.FOLDER_PATH + "=" + "'" + audioFolder.folderPath.getAbsolutePath().replaceAll("'", "''") + "'");

            return briteDatabase.delete(
                    DatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                    DatabaseHelper.AUDIO_FOLDER_ID + "=" + "'" + audioFolder.id + "'");

        };
    }

    @Override
    public Callable<Integer> updateAudioFoldersIndex(List<AudioFolder> audioFolders) {
        return () -> {
            for (int index = 0; index < audioFolders.size(); index++) {
                AudioFolder audioFolder = audioFolders.get(index);
                if (audioFolder != null) {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.FOLDER_INDEX, index);

                    String sql = DatabaseHelper.FOLDER_PATH + "=" + "'"
                            + audioFolder.folderPath.getAbsolutePath().replaceAll("'", "''") + "'";

                    briteDatabase.update(DatabaseHelper.AUDIO_FOLDERS_TABLE_NAME, values, sql);
                }
            }
            return 1;
        };
    }

    @Override
    public Callable<Integer> updateVideoFoldersIndex(List<VideoFolder> videoFolders) {
        return () -> {
            for (int index = 0; index < videoFolders.size(); index++) {
                VideoFolder videoFolder = videoFolders.get(index);
                if (videoFolder != null) {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.FOLDER_INDEX, index);

                    String sql = DatabaseHelper.FOLDER_PATH + "=" + "'"
                            + videoFolder.folderPath.getAbsolutePath().replaceAll("'", "''") + "'";

                    briteDatabase.update(DatabaseHelper.VIDEO_FOLDERS_TABLE_NAME, values, sql);
                }
            }
            return 1;
        };
    }


    @Override
    public Callable<Integer> deleteVideoFile(String path) {
        return () -> briteDatabase.delete(
                DatabaseHelper.VIDEO_FILES_TABLE_NAME,
                DatabaseHelper.VIDEO_FILE_PATH + "=" + "'" + path + "'");
    }

    @Override
    public Callable<Integer> clearPlaylist() {
        return () -> {
            ContentValues contentValues;

            contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.TRACK_IN_PLAY_LIST, 0);

            briteDatabase.update(
                    DatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                    contentValues,
                    null);

            contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.VIDEO_IN_PLAY_LIST, 0);


            return briteDatabase.update(
                    DatabaseHelper.VIDEO_FILES_TABLE_NAME,
                    contentValues,
                    null);
        };
    }

    @Override
    public Observable<List<VideoFile>> getVideoFiles(String id) {

        String sql = String.format("SELECT * FROM %s WHERE %s",
                DatabaseHelper.VIDEO_FILES_TABLE_NAME,
                DatabaseHelper.VIDEO_FOLDER_ID + "=" + "'" + id + "'");

        return briteDatabase.createQuery(DatabaseHelper.VIDEO_FILES_TABLE_NAME, sql)
                .mapToList(VideoFile::new);
    }

    @Override
    public Observable<List<String>> getVideoFilesFrame(String id) {

        String sql = String.format("SELECT * FROM %s WHERE %s",
                DatabaseHelper.VIDEO_FILES_TABLE_NAME,
                DatabaseHelper.VIDEO_FOLDER_ID + "=" + "'" + id + "'");

        return briteDatabase
                .createQuery(DatabaseHelper.VIDEO_FILES_TABLE_NAME, sql)
                .mapToList(cursor -> cursor.getString(cursor.getColumnIndex(DatabaseHelper.VIDEO_FRAME_PATH)));
    }

    @Override
    public Observable<List<AudioFolder>> getAudioFolders() {

        String sql = String.format("SELECT * FROM %s", DatabaseHelper.AUDIO_FOLDERS_TABLE_NAME);

        return briteDatabase
                .createQuery(DatabaseHelper.AUDIO_FOLDERS_TABLE_NAME, sql)
                .mapToList(AudioFolder::new);
    }

    @Override
    public Observable<List<VideoFolder>> getVideoFolders() {

        String sql = String.format("SELECT * FROM %s", DatabaseHelper.VIDEO_FOLDERS_TABLE_NAME);

        return briteDatabase
                .createQuery(DatabaseHelper.VIDEO_FOLDERS_TABLE_NAME, sql)
                .mapToList(VideoFolder::new);
    }


    @Override
    public Observable<List<String>> getArtistsList() {
        String sql = String.format("SELECT DISTINCT %s FROM %s",
                DatabaseHelper.TRACK_ARTIST,
                DatabaseHelper.AUDIO_TRACKS_TABLE_NAME);

        return briteDatabase
                .createQuery(DatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql)
                .mapToList(cursor -> cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRACK_ARTIST)));
    }

    @Override
    public Observable<List<String>> getGenresList() {
        String sql = String.format("SELECT DISTINCT %s FROM %s",
                DatabaseHelper.TRACK_GENRE,
                DatabaseHelper.AUDIO_TRACKS_TABLE_NAME);

        return briteDatabase
                .createQuery(DatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql)
                .mapToList(cursor -> cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRACK_GENRE)));
    }

    @Override
    public Observable<List<AudioFile>> getGenreTracks(String contentValue) {

        String sql = String.format("SELECT * FROM %s WHERE %s ORDER BY %s",
                DatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                DatabaseHelper.TRACK_GENRE + "=" + "'" + contentValue.replaceAll("'", "''") + "'",
                DatabaseHelper.TRACK_NUMBER + " ASC");

        return briteDatabase.createQuery(DatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql)
                .mapToList(AudioFile::new);
    }

    @Override
    public Observable<List<AudioFile>> getArtistTracks(String contentValue) {

        String sql = String.format("SELECT * FROM %s WHERE %s ORDER BY %s",
                DatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                DatabaseHelper.TRACK_ARTIST + "=" + "'" + contentValue.replaceAll("'", "''") + "'",
                DatabaseHelper.TRACK_NUMBER + " ASC");

        return briteDatabase.createQuery(DatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql)
                .mapToList(AudioFile::new);
    }

    @Override
    public Observable<List<AudioFile>> getAudioTracks(String id) {

        String sql = String.format("SELECT * FROM %s WHERE %s ORDER BY %s",
                DatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                DatabaseHelper.AUDIO_FOLDER_ID + "=" + "'" + id + "'",
                DatabaseHelper.TRACK_NUMBER + " ASC");

        return briteDatabase.createQuery(DatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql)
                .mapToList(AudioFile::new);
    }

    @Override
    public Observable<List<MediaFile>> getAudioFilePlaylist() {

        String sql = String.format("SELECT * FROM %s WHERE %s",
                DatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                DatabaseHelper.TRACK_IN_PLAY_LIST + "=1");

        return briteDatabase.createQuery(DatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql)
                .mapToList(AudioFile::new);
    }

    @Override
    public Observable<List<MediaFile>> getVideoFilePlaylist() {

        String sql = String.format("SELECT * FROM %s WHERE %s",
                DatabaseHelper.VIDEO_FILES_TABLE_NAME,
                DatabaseHelper.VIDEO_IN_PLAY_LIST + "=1");

        return briteDatabase.createQuery(DatabaseHelper.VIDEO_FILES_TABLE_NAME, sql)
                .mapToList(VideoFile::new);
    }

    @Override
    public Observable<AudioFile> getAudioFileByPath(String path) {

        String sql = String.format("SELECT * FROM %s WHERE %s",
                DatabaseHelper.AUDIO_TRACKS_TABLE_NAME,
                DatabaseHelper.TRACK_PATH + "=" + "'" + path + "'");

        return briteDatabase.createQuery(DatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql)
                .mapToOne(AudioFile::new);
    }

    @Override
    public Observable<AudioFolder> getAudioFolderByPath(String path) {

        String sql = String.format("SELECT * FROM %s WHERE %s",
                DatabaseHelper.AUDIO_FOLDERS_TABLE_NAME,
                DatabaseHelper.FOLDER_PATH + "=" + "'" + path + "'");

        return briteDatabase.createQuery(DatabaseHelper.AUDIO_FOLDERS_TABLE_NAME, sql)
                .mapToOne(AudioFolder::new);
    }

    @Override
    public Observable<VideoFolder> getVideoFolderByPath(String path) {

        String sql = String.format("SELECT * FROM %s WHERE %s",
                DatabaseHelper.VIDEO_FOLDERS_TABLE_NAME,
                DatabaseHelper.FOLDER_PATH + "=" + "'" + path + "'");

        return briteDatabase.createQuery(DatabaseHelper.VIDEO_FOLDERS_TABLE_NAME, sql)
                .mapToOne(VideoFolder::new);
    }

    @Override
    public Observable<List<String>> getFolderFilePaths(String name) {

        String sql = String.format("SELECT * FROM %s WHERE %s",
                DatabaseHelper.AUDIO_FOLDERS_TABLE_NAME,
                DatabaseHelper.FOLDER_NAME + "=" + "'" + name + "'");

        return briteDatabase
                .createQuery(DatabaseHelper.AUDIO_FOLDERS_TABLE_NAME, sql)
                .mapToList(cursor -> cursor.getString(cursor.getColumnIndex(DatabaseHelper.FOLDER_PATH)));
    }

    @Override
    public Observable<List<AudioFile>> getSearchAudioFiles(String query) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ");
        sql.append(DatabaseHelper.AUDIO_TRACKS_TABLE_NAME);
        sql.append(" WHERE ");
        sql.append(DatabaseHelper.TRACK_TITLE);
        sql.append(" LIKE ");
        sql.append("'");
        sql.append(query);
        sql.append("%'");
        sql.append(" OR ");
        sql.append(DatabaseHelper.TRACK_TITLE);
        sql.append(" LIKE ");
        sql.append("'%");
        sql.append(query);
        sql.append("%'");
        sql.append(" OR ");
        sql.append(DatabaseHelper.TRACK_TITLE);
        sql.append(" LIKE ");
        sql.append("'%");
        sql.append(query);
        sql.append("'");

        return briteDatabase.createQuery(DatabaseHelper.AUDIO_TRACKS_TABLE_NAME, sql.toString())
                .mapToList(AudioFile::new);
    }

}
