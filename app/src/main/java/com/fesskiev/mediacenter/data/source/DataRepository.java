package com.fesskiev.mediacenter.data.source;


import android.util.Log;

import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.MediaFile;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.VideoFolder;
import com.fesskiev.mediacenter.data.model.search.AlbumResponse;
import com.fesskiev.mediacenter.data.source.local.LocalDataSource;
import com.fesskiev.mediacenter.data.source.remote.RemoteDataSource;

import java.util.List;
import java.util.concurrent.Callable;

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


    public void updateSelectedAudioFolder(AudioFolder audioFolder) {
        localSource.updateSelectedAudioFolder(audioFolder);
    }

    public void updateAudioFolder(AudioFolder audioFolder) {
        localSource.updateAudioFolder(audioFolder);
    }

    public void updateVideoFolder(VideoFolder videoFolder) {
        localSource.updateVideoFolder(videoFolder);
    }

    public Callable<Integer> updateAudioFoldersIndex(List<AudioFolder> audioFolders) {
        return localSource.updateAudioFoldersIndex(audioFolders);
    }

    public Callable<Integer> updateVideoFoldersIndex(List<VideoFolder> videoFolders) {
        return localSource.updateVideoFoldersIndex(videoFolders);
    }

    public Callable<Integer> updateAudioFile(AudioFile audioFile) {
        return localSource.updateAudioFile(audioFile);
    }

    public void updateSelectedAudioFile(AudioFile audioFile) {
        localSource.updateSelectedAudioFile(audioFile);
    }

    public Callable<Integer> deleteAudioFile(String path) {
        return localSource.deleteAudioFile(path);
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


    public Observable<List<AudioFile>> getSelectedFolderAudioFiles() {
        return getSelectedAudioFolder()
                .firstOrError()
                .toObservable()
                .flatMap(audioFolder -> {
                    if (audioFolder != null) {
                        return localSource.getSelectedFolderAudioFiles(audioFolder)
                                .firstOrError()
                                .toObservable();
                    }
                    return Observable.empty();
                });
    }

    public Callable<Integer> deleteAudioFolder(AudioFolder audioFolder) {
        return localSource.deleteAudioFolderWithFiles(audioFolder);
    }

    public Callable<Integer> deleteVideoFolder(VideoFolder videoFolder) {
        return localSource.deleteVideoFolderWithFiles(videoFolder);
    }

    public Callable<Integer> clearPlaylist() {
        return localSource.clearPlaylist();
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

    public Callable<Integer> updateVideoFile(VideoFile videoFile) {
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
