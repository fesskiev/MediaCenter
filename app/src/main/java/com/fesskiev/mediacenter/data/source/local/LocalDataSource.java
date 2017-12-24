package com.fesskiev.mediacenter.data.source.local;


import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.SelectedAudioFile;
import com.fesskiev.mediacenter.data.model.SelectedAudioFolder;
import com.fesskiev.mediacenter.data.model.SelectedVideoFile;
import com.fesskiev.mediacenter.data.model.SelectedVideoFolder;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.VideoFolder;
import com.fesskiev.mediacenter.data.source.local.room.MediaCenterDb;
import com.fesskiev.mediacenter.data.source.local.room.MediaDao;


import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observable;

public class LocalDataSource implements LocalSource {

    enum Irrelevant {INSTANCE}

    private MediaDao mediaDao;

    public LocalDataSource(MediaCenterDb db) {
        this.mediaDao = db.mediaDao();
    }

    @Override
    public Observable<List<AudioFolder>> getAudioFolders() {
        return Observable.fromCallable(() -> mediaDao.getAudioFolders());
    }

    @Override
    public void insertAudioFolder(AudioFolder audioFolder) {
        mediaDao.insertAudioFolder(audioFolder);
    }

    @Override
    public Observable<Object> updateAudioFolder(AudioFolder audioFolder) {
        return Observable.fromCallable(() -> {
            mediaDao.updateAudioFolder(audioFolder);
            return Irrelevant.INSTANCE;
        });
    }

    @Override
    public Observable<Integer> deleteAudioFolderWithFiles(AudioFolder audioFolder) {
        return Observable.fromCallable(() -> mediaDao.deleteAudioFolderWithFiles(audioFolder));
    }

    @Override
    public Observable<AudioFolder> getAudioFolderByPath(String path) {
        return Observable.fromCallable(() -> mediaDao.getAudioFolderByPath(path));
    }

    @Override
    public Observable<AudioFolder> getSelectedAudioFolder() {
        return Observable.fromCallable(() -> mediaDao.getSelectedAudioFolder());
    }

    @Override
    public Observable<List<AudioFile>> getSelectedAudioFiles(AudioFolder audioFolder) {
        return Observable.fromCallable(() -> mediaDao.getSelectedAudioFiles(audioFolder.id));
    }

    @Override
    public Observable<Object> updateSelectedAudioFolder(AudioFolder audioFolder) {
        return Observable.fromCallable(() -> {
            mediaDao.updateSelectedAudioFolder(new SelectedAudioFolder(audioFolder.id));
            return Irrelevant.INSTANCE;
        });
    }

    @Override
    public Observable<Integer> updateAudioFoldersIndex(List<AudioFolder> audioFolders) {
        return Observable.fromCallable(() -> {
            for (int i = 0; i < audioFolders.size(); i++) {
                AudioFolder audioFolder = audioFolders.get(i);
                audioFolder.folderIndex = i;
            }
            mediaDao.updateAudioFoldersIndex(audioFolders);
            return 1;
        });
    }

    @Override
    public Observable<List<VideoFolder>> getVideoFolders() {
        return Observable.fromCallable(() -> mediaDao.getVideoFolders());
    }

    @Override
    public void insertVideoFolder(VideoFolder videoFolder) {
        mediaDao.insertVideoFolder(videoFolder);
    }

    @Override
    public Observable<Object> updateVideoFolder(VideoFolder videoFolder) {
        return Observable.fromCallable(() -> {
            mediaDao.updateVideoFolder(videoFolder);
            return Irrelevant.INSTANCE;
        });
    }

    @Override
    public Observable<Integer> deleteVideoFolderWithFiles(VideoFolder videoFolder) {
        return Observable.fromCallable(() -> mediaDao.deleteVideoFolderWithFiles(videoFolder));
    }

    @Override
    public Observable<VideoFolder> getVideoFolderByPath(String path) {
        return Observable.fromCallable(() -> mediaDao.getVideoFolderByPath(path));
    }

    @Override
    public Observable<Integer> updateVideoFoldersIndex(List<VideoFolder> videoFolders) {
        return Observable.fromCallable(() -> {
            for (int i = 0; i < videoFolders.size(); i++) {
                VideoFolder videoFolder = videoFolders.get(i);
                videoFolder.folderIndex = i;
            }
            mediaDao.updateVideoFoldersIndex(videoFolders);
            return 1;
        });
    }

    @Override
    public void insertAudioFile(AudioFile audioFile) {
        mediaDao.insertAudioFile(audioFile);
    }

    @Override
    public Observable<Object> updateAudioFile(AudioFile audioFile) {
        return Observable.fromCallable(() -> {
            mediaDao.updateAudioFile(audioFile);
            return Irrelevant.INSTANCE;
        });
    }

    @Override
    public Observable<Object> updateSelectedAudioFile(AudioFile audioFile) {
        return Observable.fromCallable(() -> {
            mediaDao.updateSelectedAudioFile(new SelectedAudioFile(audioFile.fileId));
            return Irrelevant.INSTANCE;
        });
    }

    @Override
    public Observable<AudioFile> getSelectedAudioFile() {
        return Observable.fromCallable(() -> mediaDao.getSelectedAudioFile());
    }

    @Override
    public Observable<AudioFile> getAudioFileByPath(String path) {
        return Observable.fromCallable(() -> mediaDao.getAudioFileByPath(path));
    }

    @Override
    public Observable<List<AudioFile>> getSearchAudioFiles(String query) {
        return Observable.fromCallable(() -> mediaDao.getSearchAudioFiles(query));
    }

    @Override
    public Observable<List<AudioFile>> getGenreTracks(String contentValue) {
        return Observable.fromCallable(() -> mediaDao.getGenreTracks(contentValue));
    }

    @Override
    public Observable<List<AudioFile>> getArtistTracks(String contentValue) {
        return Observable.fromCallable(() -> mediaDao.getArtistTracks(contentValue));
    }

    @Override
    public Observable<List<AudioFile>> getAudioTracks(String id) {
        return Observable.fromCallable(() -> mediaDao.getAudioTracks(id));
    }

    @Override
    public Observable<Integer> deleteAudioFile(AudioFile audioFile) {
        return Observable.fromCallable(() -> mediaDao.deleteAudioFile(audioFile));
    }

    @Override
    public void insertVideoFile(VideoFile videoFile) {
        mediaDao.insertVideoFile(videoFile);
    }

    @Override
    public Observable<Object> updateVideoFile(VideoFile videoFile) {
        return Observable.fromCallable(() -> {
            mediaDao.updateVideoFile(videoFile);
            return Irrelevant.INSTANCE;
        });
    }

    @Override
    public Observable<List<VideoFile>> getVideoFiles(String id) {
        return Observable.fromCallable(() -> mediaDao.getVideoFiles(id));
    }

    @Override
    public Observable<List<String>> getVideoFilesFrame(String id) {
        return Observable.fromCallable(() -> mediaDao.getVideoFilesFrame(id));
    }

    @Override
    public Observable<Integer> deleteVideoFile(VideoFile videoFile) {
        return Observable.fromCallable(() -> mediaDao.deleteVideoFile(videoFile));
    }

    @Override
    public Observable<List<AudioFile>> getAudioFilePlaylist() {
        return Observable.fromCallable(() -> mediaDao.getAudioFilePlaylist());
    }

    @Override
    public Observable<List<VideoFile>> getVideoFilePlaylist() {
        return Observable.fromCallable(() -> mediaDao.getVideoFilePlaylist());
    }

    @Override
    public Observable<Integer> clearPlaylist() {
        return Observable.fromCallable(() -> {
            mediaDao.clearAudioFilesPlaylist();
            return mediaDao.clearVideoFilesPlaylist();
        });
    }

    @Override
    public Observable<Integer> resetVideoContentDatabase() {
        return Observable.fromCallable(() -> {
            mediaDao.dropVideoFolders();
            return mediaDao.dropVideoFiles();
        });
    }

    @Override
    public Observable<Integer> resetAudioContentDatabase() {
        return Observable.fromCallable(() -> {
            mediaDao.dropAudioFolders();
            return mediaDao.dropAudioFiles();
        });
    }

    @Override
    public Observable<List<String>> getArtistsList() {
        return Observable.fromCallable(() -> mediaDao.getArtistsList());
    }

    @Override
    public Observable<List<String>> getGenresList() {
        return Observable.fromCallable(() -> mediaDao.getGenresList());
    }

    @Override
    public Observable<List<String>> getFolderFilePaths(String name) {
        return Observable.fromCallable(() -> mediaDao.getFolderFilePaths(name));
    }

    @Override
    public Observable<VideoFolder> getSelectedVideoFolder() {
        return Observable.fromCallable(() -> mediaDao.getSelectedVideoFolder());
    }

    @Override
    public Observable<List<VideoFile>> getSelectedVideoFiles(VideoFolder videoFolder) {
        return Observable.fromCallable(() -> mediaDao.getSelectedVideoFiles(videoFolder.id));
    }

    @Override
    public Observable<Object> updateSelectedVideoFolder(VideoFolder videoFolder) {
        return Observable.fromCallable(() -> {
            mediaDao.updateSelectedVideoFolder(new SelectedVideoFolder(videoFolder.id));
            return Irrelevant.INSTANCE;
        });
    }

    @Override
    public Observable<Object> updateSelectedVideoFile(VideoFile videoFile) {
        return Observable.fromCallable(() -> {
            mediaDao.updateSelectedVideoFile(new SelectedVideoFile(videoFile.fileId));
            return Irrelevant.INSTANCE;
        });
    }

    @Override
    public Observable<VideoFile> getSelectedVideoFile() {
        return Observable.fromCallable(() -> mediaDao.getSelectedVideoFile());
    }

    @Override
    public boolean containAudioTrack(String path) {
        return mediaDao.getAudioFile(path) != null;
    }

    @Override
    public boolean containAudioFolder(String path) {
        return mediaDao.getAudioFolder(path) != null;
    }

    @Override
    public boolean containVideoFolder(String path) {
        return mediaDao.getVideoFolder(path) != null;
    }
}
