package com.fesskiev.mediacenter.data.source;


import android.util.Log;

import com.fesskiev.mediacenter.data.model.Artist;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.Genre;
import com.fesskiev.mediacenter.data.model.MediaFile;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.VideoFolder;
import com.fesskiev.mediacenter.data.model.vk.OAuth;
import com.fesskiev.mediacenter.data.model.vk.response.AudioFilesResponse;
import com.fesskiev.mediacenter.data.model.vk.response.GroupPostsResponse;
import com.fesskiev.mediacenter.data.model.vk.response.GroupsResponse;
import com.fesskiev.mediacenter.data.model.vk.response.UserResponse;
import com.fesskiev.mediacenter.data.source.local.db.LocalDataSource;
import com.fesskiev.mediacenter.data.source.memory.MemoryDataSource;
import com.fesskiev.mediacenter.data.source.remote.RemoteDataSource;

import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;


public class DataRepository {

    private static final String TAG = DataRepository.class.getSimpleName();

    private static DataRepository INSTANCE;

    private LocalDataSource localSource;
    private MemoryDataSource memorySource;
    private RemoteDataSource remoteSource;

    private DataRepository(RemoteDataSource remoteSource, LocalDataSource localSource, MemoryDataSource memorySource) {
        this.memorySource = memorySource;
        this.localSource = localSource;
        this.remoteSource = remoteSource;

    }

    public static DataRepository getInstance(RemoteDataSource remoteSource, LocalDataSource localSource,
                                             MemoryDataSource memorySource) {
        if (INSTANCE == null) {
            INSTANCE = new DataRepository(remoteSource, localSource, memorySource);
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

    public Observable<List<VideoFolder>> getVideoFolders() {
        if (!memorySource.isCacheVideoFoldersDirty() && !memorySource.isVideoFoldersEmpty()) {
            Log.w(TAG, "get memory cached video");
            return memorySource.getVideoFolders();
        }

        Log.w(TAG, "get local cached video");
        return localSource.getVideoFolders().flatMap(videoFolders -> {
            memorySource.addVideoFolders(videoFolders);
            memorySource.setCacheVideoFoldersDirty(false);
            return Observable.just(videoFolders);
        });
    }

    public Observable<OAuth> auth(String login, String password) {
        return remoteSource.auth(login, password);
    }

    public Observable<UserResponse> getUser() {
        return remoteSource.getUser();
    }

    public Observable<AudioFilesResponse> getUserMusicFiles(int offset) {
        return remoteSource.getUserMusicFiles(offset);
    }

    public Observable<GroupsResponse> getGroups() {
        return remoteSource.getGroups();
    }

    public Observable<AudioFilesResponse> getSearchMusicFiles(String request, int offset) {
        return remoteSource.getSearchMusicFiles(request, offset);
    }

    public Observable<GroupPostsResponse> getGroupPots(int id, int offset) {
        return remoteSource.getGroupPots(id, offset);
    }

    public Observable<List<String>> getFolderFilePaths(String name) {
        return localSource.getFolderFilePaths(name);
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

    public void updateAudioFolder(AudioFolder audioFolder) {
        localSource.updateAudioFolder(audioFolder);
    }

    public Callable<Integer> updateAudioFoldersIndex(List<AudioFolder> audioFolders) {
        return localSource.updateAudioFoldersIndex(audioFolders);
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

    public Callable<Integer> deleteAudioFolder(AudioFolder audioFolder) {
        return localSource.deleteAudioFolderWithFiles(audioFolder);
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

    public Callable<Integer> deleteVideoFile(String path) {
        return localSource.deleteVideoFile(path);
    }

    public void updateVideoFile(VideoFile videoFile) {
        localSource.updateVideoFile(videoFile);
    }

    public Observable<List<AudioFile>> getSearchAudioFiles(String query) {
        return localSource.getSearchAudioFiles(query);
    }

    public Observable<List<VideoFile>> getVideoFiles(String id) {
        return localSource.getVideoFiles(id);
    }

    public void insertAudioFolder(AudioFolder audioFolder) {
        localSource.insertAudioFolder(audioFolder);
    }

    public void insertVideoFolder(VideoFolder videoFolder) {
        localSource.insertVideoFolder(videoFolder);
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

    public MemoryDataSource getMemorySource() {
        return memorySource;
    }


}
