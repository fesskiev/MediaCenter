package com.fesskiev.mediacenter.data.source;


import android.util.Log;

import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.VideoFolder;
import com.fesskiev.mediacenter.data.model.search.AlbumResponse;
import com.fesskiev.mediacenter.data.source.local.LocalDataSource;
import com.fesskiev.mediacenter.data.source.remote.RemoteDataSource;

import java.util.List;

import io.reactivex.Observable;

public class DataRepository {

    private static final String TAG = DataRepository.class.getSimpleName();

    private LocalDataSource localSource;
    private RemoteDataSource remoteSource;

    public DataRepository(RemoteDataSource remoteSource, LocalDataSource localSource) {
        this.localSource = localSource;
        this.remoteSource = remoteSource;
    }

    public Observable<List<String>> getArtistsList() {
        Log.w(TAG, "get local cached artists");
        return localSource.getArtistsList();
    }

    public Observable<List<String>> getGenresList() {
        Log.w(TAG, "get local cached genres");
        return localSource.getGenresList();
    }

    public Observable<List<AudioFolder>> getAudioFolders() {
        Log.w(TAG, "get local cached audio folders");
        return localSource.getAudioFolders();
    }

    public Observable<List<VideoFolder>> getVideoFolders() {
        Log.w(TAG, "get local cached video");
        return localSource.getVideoFolders();
    }

    public Observable<List<String>> getFolderFilePaths(String name) {
        return localSource.getFolderFilePaths(name);
    }


    public Observable<List<AudioFile>> getGenreTracks(String genreName) {
        return localSource.getGenreTracks(genreName);
    }

    public Observable<List<AudioFile>> getAudioTracks(String id) {
        return localSource.getAudioTracks(id);
    }

    public Observable<List<AudioFile>> getArtistTracks(String artistName) {
        return localSource.getArtistTracks(artistName);
    }

    public Observable<Object> updateSelectedAudioFolder(AudioFolder audioFolder) {
        return localSource.updateSelectedAudioFolder(audioFolder);
    }

    public Observable<Object> updateAudioFolder(AudioFolder audioFolder) {
        return localSource.updateAudioFolder(audioFolder);
    }

    public Observable<Object> updateVideoFolder(VideoFolder videoFolder) {
        return localSource.updateVideoFolder(videoFolder);
    }

    public Observable<Integer> updateAudioFoldersIndex(List<AudioFolder> audioFolders) {
        return localSource.updateAudioFoldersIndex(audioFolders);
    }

    public Observable<Integer> updateVideoFoldersIndex(List<VideoFolder> videoFolders) {
        return localSource.updateVideoFoldersIndex(videoFolders);
    }

    public Observable<Object> updateAudioFile(AudioFile audioFile) {
        return localSource.updateAudioFile(audioFile);
    }

    public Observable<Object> updateSelectedAudioFile(AudioFile audioFile) {
        return localSource.updateSelectedAudioFile(audioFile);
    }

    public Observable<Integer> deleteAudioFile(AudioFile audioFile) {
        return localSource.deleteAudioFile(audioFile);
    }

    public Observable<AudioFile> getAudioFileByPath(String path) {
        return localSource.getAudioFileByPath(path);
    }

    public Observable<AudioFolder> getAudioFolderByPath(String path) {
        return localSource.getAudioFolderByPath(path);
    }

    public Observable<VideoFolder> getVideoFolderByPath(String path) {
        return localSource.getVideoFolderByPath(path);
    }

    private Observable<AudioFolder> getSelectedAudioFolder() {
        return localSource.getSelectedAudioFolder();
    }

    public Observable<AudioFile> getSelectedAudioFile() {
        return localSource.getSelectedAudioFile();
    }

    public Observable<List<AudioFile>> getSelectedAudioFiles() {
        return getSelectedAudioFolder()
                .flatMap(audioFolder -> {
                    if (audioFolder != null) {
                        return localSource.getSelectedAudioFiles(audioFolder);
                    }
                    return Observable.never();
                });
    }

    public Observable<VideoFile> getSelectedVideoFile() {
        return localSource.getSelectedVideoFile();
    }

    public Observable<List<VideoFile>> getSelectedVideoFiles() {
        return getSelectedVideoFolder()
                .flatMap(videoFolder-> {
                    if (videoFolder != null) {
                        return localSource.getSelectedVideoFiles(videoFolder);
                    }
                    return Observable.never();
                });
    }

    public Observable<Object> updateSelectedVideoFile(VideoFile videoFile) {
        return localSource.updateSelectedVideoFile(videoFile);
    }

    public Observable<Object> updateSelectedVideoFolder(VideoFolder videoFolder) {
        return localSource.updateSelectedVideoFolder(videoFolder);
    }

    private Observable<VideoFolder> getSelectedVideoFolder() {
        return localSource.getSelectedVideoFolder();
    }

    public Observable<Integer> deleteAudioFolder(AudioFolder audioFolder) {
        return localSource.deleteAudioFolderWithFiles(audioFolder);
    }

    public Observable<Integer> deleteVideoFolder(VideoFolder videoFolder) {
        return localSource.deleteVideoFolderWithFiles(videoFolder);
    }

    public Observable<Integer> clearPlaylist() {
        return localSource.clearPlaylist();
    }

    public Observable<List<AudioFile>> getAudioFilePlaylist() {
        return localSource.getAudioFilePlaylist();
    }

    public Observable<List<VideoFile>> getVideoFilePlaylist() {
        return localSource.getVideoFilePlaylist();
    }

    public Observable<Integer> resetVideoContentDatabase() {
        return localSource.resetVideoContentDatabase();
    }

    public Observable<Integer> resetAudioContentDatabase() {
        return localSource.resetAudioContentDatabase();
    }

    public Observable<Integer> deleteVideoFile(VideoFile videoFile) {
        return localSource.deleteVideoFile(videoFile);
    }

    public Observable<Object> updateVideoFile(VideoFile videoFile) {
        return localSource.updateVideoFile(videoFile);
    }

    public Observable<List<AudioFile>> getSearchAudioFiles(String query) {
        return localSource.getSearchAudioFiles(query);
    }

    public Observable<List<VideoFile>> getVideoFiles(String id) {
        return localSource.getVideoFiles(id);
    }

    public Observable<AlbumResponse> getAlbum(String artist, String album) {
        return remoteSource.getAlbum(artist, album);
    }

    public Observable<List<String>> getVideoFilesFrame(String id) {
        return localSource.getVideoFilesFrame(id);
    }

    public void insertAudioFolder(AudioFolder audioFolder) {
        localSource.insertAudioFolder(audioFolder);
    }

    public void insertVideoFolder(VideoFolder videoFolder) {
        localSource.insertVideoFolder(videoFolder);
    }

    public void insertVideoFile(VideoFile videoFile) {
        localSource.insertVideoFile(videoFile);
    }

    public void insertAudioFile(AudioFile audioFile) {
        localSource.insertAudioFile(audioFile);
    }

    public boolean containAudioTrack(String path) {
        return localSource.containAudioTrack(path);
    }

    public boolean containAudioFolder(String path) {
        return localSource.containAudioFolder(path);
    }

    public boolean containVideoFolder(String path) {
        return localSource.containVideoFolder(path);
    }
}
