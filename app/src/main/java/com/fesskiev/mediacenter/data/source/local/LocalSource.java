package com.fesskiev.mediacenter.data.source.local;


import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.VideoFolder;

import java.util.List;

import io.reactivex.Observable;

public interface LocalSource {

    /**
     * Audio folders methods
     */
    Observable<List<AudioFolder>> getAudioFolders();

    void insertAudioFolder(AudioFolder audioFolder);

    Observable<Object> updateAudioFolder(AudioFolder audioFolder);

    Observable<Integer> deleteAudioFolderWithFiles(AudioFolder audioFolder);

    Observable<AudioFolder> getAudioFolderByPath(String path);

    Observable<AudioFolder> getSelectedAudioFolder();

    Observable<List<AudioFile>> getSelectedAudioFiles(AudioFolder audioFolder);

    Observable<Object> updateSelectedAudioFolder(AudioFolder audioFolder);

    Observable<Integer> updateAudioFoldersIndex(List<AudioFolder> audioFolders);

    /**
     * Video folders methods
     */
    Observable<List<VideoFolder>> getVideoFolders();

    void insertVideoFolder(VideoFolder videoFolder);

    Observable<Object> updateVideoFolder(VideoFolder videoFolder);

    Observable<Integer> deleteVideoFolderWithFiles(VideoFolder videoFolder);

    Observable<VideoFolder> getVideoFolderByPath(String path);

    Observable<VideoFolder> getSelectedVideoFolder();

    Observable<List<VideoFile>> getSelectedVideoFiles(VideoFolder videoFolder);

    Observable<Object> updateSelectedVideoFolder(VideoFolder videoFolder);

    Observable<Integer> updateVideoFoldersIndex(List<VideoFolder> videoFolders);

    /**
     * Audio files methods
     */
    void insertAudioFile(AudioFile audioFile);

    Observable<Object> updateSelectedAudioFile(AudioFile audioFile);

    Observable<Object> updateAudioFile(AudioFile audioFile);

    Observable<AudioFile> getAudioFileByPath(String path);

    Observable<List<AudioFile>> getSearchAudioFiles(String query);

    Observable<List<AudioFile>> getGenreTracks(String contentValue);

    Observable<List<AudioFile>> getArtistTracks(String contentValue);

    Observable<List<AudioFile>> getAudioTracks(String id);

    Observable<AudioFile> getSelectedAudioFile();

    Observable<Integer> deleteAudioFile(AudioFile audioFile);

    /**
     * Video files methods
     */
    void insertVideoFile(VideoFile videoFile);

    Observable<Object> updateSelectedVideoFile(VideoFile videoFile);

    Observable<Object> updateVideoFile(VideoFile videoFile);

    Observable<VideoFile> getSelectedVideoFile();

    Observable<List<VideoFile>> getVideoFiles(String id);

    Observable<List<String>> getVideoFilesFrame(String id);

    Observable<Integer> deleteVideoFile(VideoFile videoFile);

    Observable<List<AudioFile>> getAudioFilePlaylist();

    Observable<List<VideoFile>> getVideoFilePlaylist();

    Observable<Integer> clearPlaylist();

    /**
     * drop database
     */
    Observable<Integer> resetVideoContentDatabase();

    Observable<Integer> resetAudioContentDatabase();


    Observable<List<String>> getArtistsList();

    Observable<List<String>> getGenresList();


    boolean containAudioTrack(String path);

    boolean containAudioFolder(String path);

    boolean containVideoFolder(String path);

    Observable<List<String>> getFolderFilePaths(String name);

}
