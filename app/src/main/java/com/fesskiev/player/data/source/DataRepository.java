package com.fesskiev.player.data.source;


import android.util.Log;

import com.fesskiev.player.data.model.Artist;
import com.fesskiev.player.data.model.AudioFile;
import com.fesskiev.player.data.model.AudioFolder;
import com.fesskiev.player.data.model.Genre;
import com.fesskiev.player.data.model.MediaFile;
import com.fesskiev.player.data.model.VideoFile;
import com.fesskiev.player.data.source.local.db.LocalDataSource;
import com.fesskiev.player.data.source.memory.MemoryDataSource;

import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;


public class DataRepository {

    private static final String TAG = DataRepository.class.getSimpleName();

    private static DataRepository INSTANCE;

    private LocalDataSource localSource;
    private MemoryDataSource memorySource;

    private DataRepository(LocalDataSource localSource, MemoryDataSource memorySource) {
        this.memorySource = memorySource;
        this.localSource = localSource;

    }

    public static DataRepository getInstance(LocalDataSource localSource, MemoryDataSource memorySource) {
        if (INSTANCE == null) {
            INSTANCE = new DataRepository(localSource, memorySource);
        }
        return INSTANCE;
    }


    public Observable<List<Artist>> getArtists() {
        if (!memorySource.isCacheArtistsDirty() && !memorySource.isArtistsEmpty()) {
            Log.w(TAG, "get memory cached artists");
            return memorySource.getArtists();
        }

        Log.w(TAG, "get local cached artists");
        return localSource.getArtists().flatMap(artists -> {
            memorySource.addArtists(artists);
            memorySource.setCacheArtistsDirty(false);
            return Observable.just(artists);
        });
    }

    public Observable<List<AudioFolder>> getAudioFolders() {
        if (!memorySource.isCacheFoldersDirty() && !memorySource.isFoldersEmpty()) {
            Log.w(TAG, "get memory cached audio folders");
            return memorySource.getAudioFolders();
        }

        Log.w(TAG, "get local cached audio folders");
        return localSource.getAudioFolders().flatMap(audioFolders -> {
            memorySource.addAudioFolders(audioFolders);
            memorySource.setCacheFoldersDirty(false);
            return Observable.just(audioFolders);
        });
    }

    public Observable<List<Genre>> getGenres() {
        if (!memorySource.isCacheGenresDirty() && !memorySource.isGenresEmpty()) {
            Log.w(TAG, "get memory cached genres");
            return memorySource.getGenres();
        }

        Log.w(TAG, "get local cached genres");
        return localSource.getGenres().flatMap(genres -> {
            memorySource.addGenres(genres);
            memorySource.setCacheGenresDirty(false);
            return Observable.just(genres);
        });
    }

    public Observable<List<VideoFile>> getVideoFiles() {
        if (!memorySource.isCacheVideoFilesDirty() && !memorySource.isVideoFilesEmpty()) {
            Log.w(TAG, "get memory cached video");
            return memorySource.getVideoFiles();
        }

        Log.w(TAG, "get local cached video");
        return localSource.getVideoFiles().flatMap(videoFiles -> {
            memorySource.addVideoFiles(videoFiles);
            memorySource.setCacheVideoFilesDirty(false);
            return Observable.just(videoFiles);
        });
    }


    public Observable<List<AudioFile>> getGenreTracks(String genreName) {
        return localSource.getGenreTracks(genreName);
    }

    public Observable<List<AudioFile>> getFolderTracks(String id) {
        return localSource.getFolderTracks(id);
    }

    public Observable<List<AudioFile>> getArtistTracks(String artistName) {
        return localSource.getArtistTracks(artistName);
    }


    public void updateSelectedAudioFolder(AudioFolder audioFolder) {
        localSource.updateSelectedAudioFolder(audioFolder);
    }

    public void updateAudioFolderIndex(AudioFolder audioFolder) {
        localSource.updateAudioFolderIndex(audioFolder);
    }


    public void updateAudioFile(AudioFile audioFile) {
        localSource.updateAudioFile(audioFile);
    }

    public void updateSelectedAudioFile(AudioFile audioFile) {
        localSource.updateSelectedAudioFile(audioFile);
    }

    public void deleteAudioFile(String path) {
        localSource.deleteAudioFile(path);
    }

    public Observable<AudioFile> getAudioFileByPath(String path) {
        return localSource.getAudioFileByPath(path);
    }

    public Observable<AudioFolder> getSelectedAudioFolder() {
        return localSource.getSelectedAudioFolder();
    }

    public Observable<AudioFile> getSelectedAudioFile() {
        return localSource.getSelectedAudioFile();
    }


    public Observable<List<AudioFile>> getSelectedFolderAudioFiles() {
        return getSelectedAudioFolder().flatMap(audioFolder -> {
            if (audioFolder != null) {
                return localSource.getSelectedFolderAudioFiles(audioFolder);
            }
            return Observable.empty();
        });
    }

    public void clearPlaylist() {
        localSource.clearPlaylist();
    }

    public Observable<List<MediaFile>> getAudioFilePlaylist() {
        return localSource.getAudioFilePlaylist();
    }

    public Observable<List<MediaFile>> getVideoFilePlaylist() {
        return localSource.getVideoFilePlaylist();
    }


    public Callable<Integer> resetVideoContentDatabase() {
        return localSource.resetVideoContentDatabase();
    }

    public Callable<Integer> resetAudioContentDatabase() {
        return localSource.resetAudioContentDatabase();
    }

    public void deleteVideoFile(String path) {
        localSource.deleteVideoFile(path);
    }

    public void updateVideoFile(VideoFile videoFile) {
        localSource.updateVideoFile(videoFile);
    }

    public Observable<List<AudioFile>> getSearchAudioFiles(String query) {
        return localSource.getSearchAudioFiles(query);
    }

    public void insertAudioFolder(AudioFolder audioFolder) {
        localSource.insertAudioFolder(audioFolder);
    }

    public void insertAudioFile(AudioFile audioFile) {
        localSource.insertAudioFile(audioFile);
    }

    public boolean containAudioTrack(String path) {
        return localSource.containAudioTrack(path);
    }

    public String getDownloadFolderID() {
        return localSource.getDownloadFolderID();
    }

    public Observable<List<String>> getFoldersPath() {
        return localSource.getFoldersPath();
    }


    public MemoryDataSource getMemorySource() {
        return memorySource;
    }
}
