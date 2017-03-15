package com.fesskiev.mediacenter.data.source.local;


import com.fesskiev.mediacenter.data.model.Artist;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.Genre;
import com.fesskiev.mediacenter.data.model.MediaFile;
import com.fesskiev.mediacenter.data.model.VideoFile;

import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;

public interface LocalSource {

    Observable<List<Genre>> getGenres();

    Observable<List<Artist>> getArtists();

    Observable<List<AudioFolder>> getAudioFolders();

    Observable<List<VideoFile>> getVideoFiles();

    Observable<List<MediaFile>> getAudioFilePlaylist();

    Observable<List<MediaFile>> getVideoFilePlaylist();

    Observable<AudioFile> getAudioFileByPath(String path);

    Observable<List<AudioFile>> getSearchAudioFiles(String query);

    Observable<List<AudioFile>> getGenreTracks(String contentValue);

    Observable<List<AudioFile>> getArtistTracks(String contentValue);

    Observable<List<AudioFile>> getFolderTracks(String id);

    Observable<AudioFolder> getSelectedAudioFolder();

    Observable<AudioFile> getSelectedAudioFile();

    Observable<List<String>> getFoldersPath();

    Observable<List<String>> getFolderFilePaths(String name);

    Observable<List<AudioFile>> getSelectedFolderAudioFiles(AudioFolder audioFolder);

    void updateAudioFile(AudioFile audioFile);

    void insertAudioFolder(AudioFolder audioFolder);

    void updateAudioFolder(AudioFolder audioFolder);

    void insertAudioFile(AudioFile audioFile);

    void insertVideoFile(VideoFile videoFile);

    void updateVideoFile(VideoFile videoFile);

    void updateSelectedAudioFolder(AudioFolder audioFolder);

    Callable<Integer> updateAudioFoldersIndex(List<AudioFolder> audioFolders);

    void updateSelectedAudioFile(AudioFile audioFile);

    void deleteAudioFile(String path);

    Callable<Integer> deleteVideoFile(String path);

    void clearPlaylist();

    Callable<Integer> resetVideoContentDatabase();

    Callable<Integer> resetAudioContentDatabase();

    boolean containAudioTrack(String path);

    String getDownloadFolderID();


}
