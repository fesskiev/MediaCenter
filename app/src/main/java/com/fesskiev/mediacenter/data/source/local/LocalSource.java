package com.fesskiev.mediacenter.data.source.local;


import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.MediaFile;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.VideoFolder;

import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;

public interface LocalSource {

    Observable<List<String>> getArtistsList();

    Observable<List<String>> getGenresList();

    Observable<List<AudioFolder>> getAudioFolders();

    Observable<List<VideoFolder>> getVideoFolders();

    Observable<List<VideoFile>> getVideoFiles(String id);

    Observable<List<MediaFile>> getAudioFilePlaylist();

    Observable<List<MediaFile>> getVideoFilePlaylist();

    Observable<AudioFile> getAudioFileByPath(String path);

    Observable<AudioFolder> getAudioFolderByPath(String path);

    Observable<List<AudioFile>> getSearchAudioFiles(String query);

    Observable<List<AudioFile>> getGenreTracks(String contentValue);

    Observable<List<AudioFile>> getArtistTracks(String contentValue);

    Observable<List<AudioFile>> getAudioTracks(String id);

    Observable<AudioFolder> getSelectedAudioFolder();

    Observable<AudioFile> getSelectedAudioFile();

    Observable<List<String>> getFoldersPath();

    Observable<List<String>> getVideoFilesFrame(String id);

    Observable<List<String>> getFolderFilePaths(String name);

    Observable<List<AudioFile>> getSelectedFolderAudioFiles(AudioFolder audioFolder);

    void updateAudioFile(AudioFile audioFile);

    void insertAudioFolder(AudioFolder audioFolder);

    void updateAudioFolder(AudioFolder audioFolder);

    Callable<Integer> deleteAudioFolderWithFiles(AudioFolder audioFolder);

    void insertVideoFolder(VideoFolder videoFolder);

    void updateVideoFolder(VideoFolder videoFolder);

    Callable<Integer> deleteVideoFolderWithFiles(VideoFolder videoFolder);

    void insertAudioFile(AudioFile audioFile);

    void insertVideoFile(VideoFile videoFile);

    void updateVideoFile(VideoFile videoFile);

    void updateSelectedAudioFolder(AudioFolder audioFolder);

    Callable<Integer> updateAudioFoldersIndex(List<AudioFolder> audioFolders);

    Callable<Integer> updateVideoFoldersIndex(List<VideoFolder> videoFolders);

    void updateSelectedAudioFile(AudioFile audioFile);

    void deleteAudioFile(String path);

    Callable<Integer> deleteVideoFile(String path);

    void clearPlaylist();

    Callable<Integer> resetVideoContentDatabase();

    Callable<Integer> resetAudioContentDatabase();

    boolean containAudioTrack(String path);

}
