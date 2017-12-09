package com.fesskiev.mediacenter.data.source.local;


import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.VideoFolder;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;;

public interface LocalSource {

    /**
     * Audio folders methods
     */
    Observable<List<AudioFolder>> getAudioFolders();

    void insertAudioFolder(AudioFolder audioFolder);

    void updateAudioFolder(AudioFolder audioFolder);

    Callable<Integer> deleteAudioFolderWithFiles(AudioFolder audioFolder);

    Observable<AudioFolder> getAudioFolderByPath(String path);

    Observable<AudioFolder> getSelectedAudioFolder();

    Observable<List<AudioFile>> getSelectedFolderAudioFiles(AudioFolder audioFolder);

    Callable<Object> updateSelectedAudioFolder(AudioFolder audioFolder);

    Callable<Integer> updateAudioFoldersIndex(List<AudioFolder> audioFolders);

    /**
     * Video folders methods
     */
    Observable<List<VideoFolder>> getVideoFolders();

    void insertVideoFolder(VideoFolder videoFolder);

    void updateVideoFolder(VideoFolder videoFolder);

    Callable<Integer> deleteVideoFolderWithFiles(VideoFolder videoFolder);

    Observable<VideoFolder> getVideoFolderByPath(String path);

    Callable<Integer> updateVideoFoldersIndex(List<VideoFolder> videoFolders);

    /**
     * Audio files methods
     */
    void insertAudioFile(AudioFile audioFile);

    Callable<Object> updateSelectedAudioFile(AudioFile audioFile);

    Observable<AudioFile> getAudioFileByPath(String path);

    Observable<List<AudioFile>> getSearchAudioFiles(String query);

    Observable<List<AudioFile>> getGenreTracks(String contentValue);

    Observable<List<AudioFile>> getArtistTracks(String contentValue);

    Observable<List<AudioFile>> getAudioTracks(String id);

    Observable<AudioFile> getSelectedAudioFile();

    Callable<Integer> updateAudioFile(AudioFile audioFile);

    Callable<Integer> deleteAudioFile(AudioFile audioFile);

    /**
     * Video files methods
     */
    void insertVideoFile(VideoFile videoFile);

    Callable<Integer> updateVideoFile(VideoFile videoFile);

    Observable<List<VideoFile>> getVideoFiles(String id);

    Observable<List<String>> getVideoFilesFrame(String id);

    Callable<Integer> deleteVideoFile(VideoFile videoFile);

    Observable<List<AudioFile>> getAudioFilePlaylist();

    Observable<List<VideoFile>> getVideoFilePlaylist();

    Callable<Integer> clearPlaylist();

    /**
     * drop database
     */
    Callable<Integer> resetVideoContentDatabase();

    Callable<Integer> resetAudioContentDatabase();


    Observable<List<String>> getArtistsList();

    Observable<List<String>> getGenresList();


    boolean containAudioTrack(String path);

    boolean containAudioFolder(String path);

    boolean containVideoFolder(String path);

    Observable<List<String>> getFolderFilePaths(String name);

}
