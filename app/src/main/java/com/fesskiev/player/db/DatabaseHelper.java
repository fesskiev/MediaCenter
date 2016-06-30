package com.fesskiev.player.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContentResolverCompat;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.model.Artist;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.model.Genre;
import com.fesskiev.player.model.VideoFile;
import com.fesskiev.player.utils.CacheManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

public class DatabaseHelper {


    public static void insertAudioFolder(AudioFolder audioFolder) {

        ContentValues dateValues = new ContentValues();

        dateValues.put(MediaCenterProvider.ID, audioFolder.id);
        dateValues.put(MediaCenterProvider.FOLDER_PATH, audioFolder.folderPath.getAbsolutePath());
        dateValues.put(MediaCenterProvider.FOLDER_NAME, audioFolder.folderName);
        dateValues.put(MediaCenterProvider.FOLDER_COVER,
                audioFolder.folderImage != null ? audioFolder.folderImage.getAbsolutePath() : null);
        dateValues.put(MediaCenterProvider.FOLDER_INDEX, audioFolder.index);
        dateValues.put(MediaCenterProvider.FOLDER_SELECTED, audioFolder.isSelected ? 1 : 0);

        MediaApplication.getInstance().
                getContentResolver().insert(
                MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                dateValues);

    }

    public static void updateAudioFolderIndex(AudioFolder audioFolder) {

        ContentValues dateValues = new ContentValues();
        dateValues.put(MediaCenterProvider.FOLDER_INDEX, audioFolder.index);

        MediaApplication.getInstance().
                getContentResolver().update(MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                dateValues,
                MediaCenterProvider.FOLDER_PATH + "=" + "'" + audioFolder.folderPath + "'",
                null);
    }

    public static AudioFolder getSelectedAudioFolder() {
        Cursor cursor = ContentResolverCompat.query(MediaApplication.getInstance().
                        getContentResolver(),
                MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                null,
                MediaCenterProvider.FOLDER_SELECTED + "=" + "'" + 1 + "'",
                null,
                null,
                null);
        if (cursor.getCount() > 0) {
            cursor.moveToPosition(0);
            return new AudioFolder(cursor);

        }
        return null;
    }

    public static List<AudioFile> getSelectedFolderAudioFiles(AudioFolder audioFolder) {

        Cursor cursor = ContentResolverCompat.query(MediaApplication.getInstance().
                        getContentResolver(),
                MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                null,
                MediaCenterProvider.ID + "=" + "'" + audioFolder.id + "'",
                null,
                MediaCenterProvider.TRACK_NUMBER + " ASC",
                null);
        List<AudioFile> audioFiles = null;
        if (cursor.getCount() > 0) {
            audioFiles = new ArrayList<>();
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                audioFiles.add(new AudioFile(cursor));
            }
        }
        cursor.close();
        return audioFiles;
    }

    public static void updateSelectedAudioFolder(AudioFolder audioFolder) {
        ContentValues clearValues = new ContentValues();
        clearValues.put(MediaCenterProvider.FOLDER_SELECTED, 0);

        MediaApplication.getInstance().
                getContentResolver().update(MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                clearValues,
                null,
                null);

        ContentValues dateValues = new ContentValues();
        dateValues.put(MediaCenterProvider.FOLDER_SELECTED, audioFolder.isSelected ? 1 : 0);

        MediaApplication.getInstance().
                getContentResolver().update(MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                dateValues,
                MediaCenterProvider.FOLDER_PATH + "=" + "'" + audioFolder.folderPath + "'",
                null);

    }

    public static void updateAudioFile(AudioFile audioFile) {

        ContentValues dateValues = new ContentValues();

        dateValues.put(MediaCenterProvider.ID, audioFile.id);
        dateValues.put(MediaCenterProvider.TRACK_ARTIST, audioFile.artist);
        dateValues.put(MediaCenterProvider.TRACK_TITLE, audioFile.title);
        dateValues.put(MediaCenterProvider.TRACK_ALBUM, audioFile.album);
        dateValues.put(MediaCenterProvider.TRACK_GENRE, audioFile.genre);
        dateValues.put(MediaCenterProvider.TRACK_PATH, audioFile.getFilePath());
        dateValues.put(MediaCenterProvider.TRACK_BITRATE, audioFile.bitrate);
        dateValues.put(MediaCenterProvider.TRACK_LENGTH, audioFile.length);
        dateValues.put(MediaCenterProvider.TRACK_NUMBER, audioFile.trackNumber);
        dateValues.put(MediaCenterProvider.TRACK_SAMPLE_RATE, audioFile.sampleRate);
        dateValues.put(MediaCenterProvider.TRACK_IN_PLAY_LIST, audioFile.inPlayList ? 1 : 0);
        dateValues.put(MediaCenterProvider.TRACK_COVER, audioFile.artworkPath);

        MediaApplication.getInstance().
                getContentResolver().update(MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                dateValues,
                MediaCenterProvider.TRACK_PATH + "=" + "'" + audioFile.filePath + "'",
                null);
    }

    public static void insertAudioFile(AudioFile audioFile) {

        ContentValues dateValues = new ContentValues();

        dateValues.put(MediaCenterProvider.ID, audioFile.id);
        dateValues.put(MediaCenterProvider.TRACK_ARTIST, audioFile.artist);
        dateValues.put(MediaCenterProvider.TRACK_TITLE, audioFile.title);
        dateValues.put(MediaCenterProvider.TRACK_ALBUM, audioFile.album);
        dateValues.put(MediaCenterProvider.TRACK_GENRE, audioFile.genre);
        dateValues.put(MediaCenterProvider.TRACK_PATH, audioFile.getFilePath());
        dateValues.put(MediaCenterProvider.TRACK_BITRATE, audioFile.bitrate);
        dateValues.put(MediaCenterProvider.TRACK_LENGTH, audioFile.length);
        dateValues.put(MediaCenterProvider.TRACK_NUMBER, audioFile.trackNumber);
        dateValues.put(MediaCenterProvider.TRACK_SAMPLE_RATE, audioFile.sampleRate);
        dateValues.put(MediaCenterProvider.TRACK_IN_PLAY_LIST, audioFile.inPlayList ? 1 : 0);
        dateValues.put(MediaCenterProvider.TRACK_SELECTED, audioFile.isSelected ? 1 : 0);
        dateValues.put(MediaCenterProvider.TRACK_COVER, audioFile.artworkPath);

        MediaApplication.getInstance().
                getContentResolver().insert(MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                dateValues);
    }

    public static boolean containAudioTrack(String path) {
        Cursor cursor = ContentResolverCompat.query(MediaApplication.getInstance().
                        getContentResolver(),
                MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                null,
                MediaCenterProvider.TRACK_PATH + "=" + "'" + path + "'",
                null,
                null,
                null);

        boolean contain = cursor.getCount() > 0;
        cursor.close();

        return contain;
    }

    public static void updateSelectedAudioFile(AudioFile audioFile) {
        ContentValues clearValues = new ContentValues();
        clearValues.put(MediaCenterProvider.TRACK_SELECTED, 0);

        MediaApplication.getInstance().
                getContentResolver().update(MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                clearValues,
                null,
                null);

        ContentValues dateValues = new ContentValues();
        dateValues.put(MediaCenterProvider.TRACK_SELECTED, audioFile.isSelected ? 1 : 0);

        MediaApplication.getInstance().
                getContentResolver().update(MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                dateValues,
                MediaCenterProvider.TRACK_PATH + "=" + "'" + audioFile.filePath + "'",
                null);

    }

    public static AudioFile getSelectedAudioFile() {
        Cursor cursor = ContentResolverCompat.query(MediaApplication.getInstance().
                        getContentResolver(),
                MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                null,
                MediaCenterProvider.TRACK_SELECTED + "=" + "'" + 1 + "'",
                null,
                null,
                null);
        if (cursor.getCount() > 0) {
            cursor.moveToPosition(0);
            return new AudioFile(cursor);

        }
        cursor.close();
        return null;
    }

    public static String getDownloadFolderID() {
        Cursor cursor = ContentResolverCompat.query(MediaApplication.getInstance().
                        getContentResolver(),
                MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                new String[]{MediaCenterProvider.ID},
                MediaCenterProvider.FOLDER_PATH + "=" + "'" + CacheManager.CHECK_DOWNLOADS_FOLDER_PATH + "'",
                null,
                null,
                null);

        cursor.moveToFirst();
        String id = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.ID));
        cursor.close();

        return id;
    }

    public static Callable<Void> resetVideoContentDatabase() {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MediaApplication.getInstance().
                        getContentResolver().delete(MediaCenterProvider.VIDEO_FILES_TABLE_CONTENT_URI, null, null);
                return null;
            }
        };
    }

    public static Callable<Void>resetAudioContentDatabase() {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MediaApplication.getInstance().
                        getContentResolver().delete(MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI, null, null);
                MediaApplication.getInstance().
                        getContentResolver().delete(MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI, null, null);
                return null;
            }
        };
    }


    public static void deleteAudioFile(String path) {
        MediaApplication.getInstance().
                getContentResolver().
                delete(MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                        MediaCenterProvider.TRACK_PATH + "=" + "'" + path + "'",
                        null);
    }

    public static List<String> getFoldersPath() {
        Cursor cursor = ContentResolverCompat.query(MediaApplication.getInstance().
                        getContentResolver(),
                MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                new String[]{MediaCenterProvider.FOLDER_PATH},
                null,
                null,
                null,
                null);
        List<String> paths = null;
        if (cursor.getCount() > 0) {
            paths = new ArrayList<>();
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                String path =
                        cursor.getString(cursor.getColumnIndex(MediaCenterProvider.FOLDER_PATH));
                paths.add(path);
            }
        }
        cursor.close();

        return paths;
    }

    public static void clearPlaylist() {

        ContentValues contentValues;

        contentValues = new ContentValues();
        contentValues.put(MediaCenterProvider.TRACK_IN_PLAY_LIST, 0);

        MediaApplication.getInstance().
                getContentResolver().update(MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                contentValues,
                null,
                null);

        contentValues = new ContentValues();
        contentValues.put(MediaCenterProvider.VIDEO_IN_PLAY_LIST, 0);

        MediaApplication.getInstance().
                getContentResolver().update(MediaCenterProvider.VIDEO_FILES_TABLE_CONTENT_URI,
                contentValues,
                null,
                null);
    }


    public static void insertVideoFile(VideoFile videoFile) {

        ContentValues dateValues = new ContentValues();

        dateValues.put(MediaCenterProvider.ID, videoFile.id);
        dateValues.put(MediaCenterProvider.VIDEO_FILE_PATH, videoFile.getFilePath());
        dateValues.put(MediaCenterProvider.VIDEO_FRAME_PATH, videoFile.framePath);
        dateValues.put(MediaCenterProvider.VIDEO_DESCRIPTION, videoFile.description);
        dateValues.put(MediaCenterProvider.VIDEO_IN_PLAY_LIST, videoFile.inPlayList ? 1 : 0);

        MediaApplication.getInstance().
                getContentResolver().insert(MediaCenterProvider.VIDEO_FILES_TABLE_CONTENT_URI,
                dateValues);
    }

    public static void updateVideoFile(VideoFile videoFile) {

        ContentValues dateValues = new ContentValues();

        dateValues.put(MediaCenterProvider.ID, videoFile.id);
        dateValues.put(MediaCenterProvider.VIDEO_FILE_PATH, videoFile.getFilePath());
        dateValues.put(MediaCenterProvider.VIDEO_FRAME_PATH, videoFile.framePath);
        dateValues.put(MediaCenterProvider.VIDEO_DESCRIPTION, videoFile.description);
        dateValues.put(MediaCenterProvider.VIDEO_IN_PLAY_LIST, videoFile.inPlayList ? 1 : 0);

        MediaApplication.getInstance().
                getContentResolver().update(MediaCenterProvider.VIDEO_FILES_TABLE_CONTENT_URI,
                dateValues,
                MediaCenterProvider.VIDEO_FILE_PATH + "=" + "'" + videoFile.filePath + "'",
                null);
    }


    public static Callable<List<VideoFile>> getVideoFiles() {
        return new Callable<List<VideoFile>>() {
            @Override
            public List<VideoFile> call() throws Exception {
                return getVideoFilesFromDatabase();
            }
        };
    }

    private static List<VideoFile> getVideoFilesFromDatabase() {
        Cursor cursor = MediaApplication.getInstance().getContentResolver().
                query(MediaCenterProvider.VIDEO_FILES_TABLE_CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );

        if (cursor != null && cursor.getCount() > 0) {

            List<VideoFile> videoFiles = new ArrayList<>();

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                videoFiles.add(new VideoFile(cursor));
            }
            cursor.close();
            return videoFiles;
        }
        return null;
    }


    public static Callable<List<AudioFolder>> getAudioFolders() {
        return new Callable<List<AudioFolder>>() {
            @Override
            public List<AudioFolder> call() throws Exception {
                return getAudioFoldersFromDatabase();
            }
        };
    }

    private static List<AudioFolder> getAudioFoldersFromDatabase() {
        Cursor cursor = MediaApplication.getInstance().getContentResolver().
                query(MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );

        if (cursor != null && cursor.getCount() > 0) {

            List<AudioFolder> audioFolders = new ArrayList<>();

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                audioFolders.add(new AudioFolder(cursor));
            }
            cursor.close();
            return audioFolders;
        }
        return null;
    }


    public static Callable<Set<Artist>> getArtists() {
        return new Callable<Set<Artist>>() {
            @Override
            public Set<Artist> call() throws Exception {
                return getArtistsFromDatabase();
            }
        };
    }

    private static Set<Artist> getArtistsFromDatabase() {
        Cursor cursor = MediaApplication.getInstance().getContentResolver().
                query(MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                        new String[]{MediaCenterProvider.TRACK_ARTIST, MediaCenterProvider.TRACK_COVER},
                        null,
                        null,
                        null
                );

        if (cursor != null && cursor.getCount() > 0) {

            Set<Artist> artists = new TreeSet<>();

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                artists.add(new Artist(cursor));
            }
            cursor.close();
            return artists;
        }
        return null;
    }


    public static Callable<Set<Genre>> getGenres() {
        return new Callable<Set<Genre>>() {
            @Override
            public Set<Genre> call() throws Exception {
                return getGenresFromDatabase();
            }
        };
    }

    private static Set<Genre> getGenresFromDatabase() {
        Cursor cursor = MediaApplication.getInstance().getContentResolver().
                query(MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                        new String[]{MediaCenterProvider.TRACK_GENRE, MediaCenterProvider.TRACK_COVER},
                        null,
                        null,
                        null
                );

        if (cursor != null && cursor.getCount() > 0) {

            Set<Genre> genres = new TreeSet<>();

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                genres.add(new Genre(cursor));
            }
            cursor.close();
            return genres;
        }
        return null;
    }
}
