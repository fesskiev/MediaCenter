package com.fesskiev.mediacenter.data.source.local;


import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.SelectedAudioFile;
import com.fesskiev.mediacenter.data.model.SelectedAudioFolder;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.VideoFolder;
import com.fesskiev.mediacenter.data.source.local.room.MediaCenterDb;
import com.fesskiev.mediacenter.data.source.local.room.MediaDao;
import com.fesskiev.mediacenter.utils.RxUtils;


import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;

public class LocalDataSource implements LocalSource {

    enum Irrelevant { INSTANCE; }

    private MediaDao mediaDao;

    public LocalDataSource(MediaCenterDb db) {
        this.mediaDao = db.mediaDao();
    }

    @Override
    public Observable<List<AudioFolder>> getAudioFolders() {
        return RxUtils.fromCallable(() -> mediaDao.getAudioFolders());
    }

    @Override
    public void insertAudioFolder(AudioFolder audioFolder) {
        mediaDao.insertAudioFolder(audioFolder);
    }

    @Override
    public void updateAudioFolder(AudioFolder audioFolder) {
        mediaDao.updateAudioFolder(audioFolder);
    }

    @Override
    public Callable<Integer> deleteAudioFolderWithFiles(AudioFolder audioFolder) {
        return () -> mediaDao.deleteAudioFolderWithFiles(audioFolder);
    }

    @Override
    public Observable<AudioFolder> getAudioFolderByPath(String path) {
        return RxUtils.fromCallable(() -> mediaDao.getAudioFolderByPath(path.replaceAll("'", "''") + "'"));
    }

    @Override
    public Observable<AudioFolder> getSelectedAudioFolder() {
        return RxUtils.fromCallable(() -> mediaDao.getSelectedAudioFolder());
    }

    @Override
    public Observable<List<AudioFile>> getSelectedFolderAudioFiles(AudioFolder audioFolder) {
        return RxUtils.fromCallable(() -> mediaDao.getSelectedFolderAudioFiles(audioFolder.id));
    }

    @Override
    public Callable<Object> updateSelectedAudioFolder(AudioFolder audioFolder) {
        return () -> {
            mediaDao.updateSelectedAudioFolder(new SelectedAudioFolder(audioFolder.id));
            return Irrelevant.INSTANCE;
        };
    }

    @Override
    public Callable<Integer> updateAudioFoldersIndex(List<AudioFolder> audioFolders) {
        return () -> {
            mediaDao.updateAudioFoldersIndex(audioFolders);
            return 1;
        };
    }

    @Override
    public Observable<List<VideoFolder>> getVideoFolders() {
        return RxUtils.fromCallable(() -> mediaDao.getVideoFolders());
    }

    @Override
    public void insertVideoFolder(VideoFolder videoFolder) {
        mediaDao.insertVideoFolder(videoFolder);
    }

    @Override
    public void updateVideoFolder(VideoFolder videoFolder) {
        mediaDao.updateVideoFolder(videoFolder);
    }

    @Override
    public Callable<Integer> deleteVideoFolderWithFiles(VideoFolder videoFolder) {
        return () -> mediaDao.deleteVideoFolderWithFiles(videoFolder);
    }

    @Override
    public Observable<VideoFolder> getVideoFolderByPath(String path) {
        return RxUtils.fromCallable(() -> mediaDao.getVideoFolderByPath(path.replaceAll("'", "''") + "'"));
    }

    @Override
    public Callable<Integer> updateVideoFoldersIndex(List<VideoFolder> videoFolders) {
        return () -> {
            mediaDao.updateVideoFoldersIndex(videoFolders);
            return 1;
        };
    }

    @Override
    public void insertAudioFile(AudioFile audioFile) {
        mediaDao.insertAudioFile(audioFile);
    }

    @Override
    public Callable<Integer> updateAudioFile(AudioFile audioFile) {
        return () -> mediaDao.updateAudioFile(audioFile);
    }

    @Override
    public Callable<Object> updateSelectedAudioFile(AudioFile audioFile) {
        return () -> {
            mediaDao.updateSelectedAudioFile(new SelectedAudioFile(audioFile.fileId));
            return Irrelevant.INSTANCE;
        };
    }

    @Override
    public Observable<AudioFile> getSelectedAudioFile() {
        return RxUtils.fromCallable(() -> mediaDao.getSelectedAudioFile());
    }

    @Override
    public Observable<AudioFile> getAudioFileByPath(String path) {
        return RxUtils.fromCallable(() -> mediaDao.getAudioFileByPath(path.replaceAll("'", "''") + "'"));
    }

    @Override
    public Observable<List<AudioFile>> getSearchAudioFiles(String query) {
        return RxUtils.fromCallable(() -> mediaDao.getSearchAudioFiles(query.replaceAll("'", "''") + "'"));
    }

    @Override
    public Observable<List<AudioFile>> getGenreTracks(String contentValue) {
        return RxUtils.fromCallable(() -> mediaDao.getGenreTracks(contentValue.replaceAll("'", "''") + "'"));
    }

    @Override
    public Observable<List<AudioFile>> getArtistTracks(String contentValue) {
        return RxUtils.fromCallable(() -> mediaDao.getArtistTracks(contentValue.replaceAll("'", "''") + "'"));
    }

    @Override
    public Observable<List<AudioFile>> getAudioTracks(String id) {
        return RxUtils.fromCallable(() -> mediaDao.getAudioTracks(id));
    }

    @Override
    public Callable<Integer> deleteAudioFile(AudioFile audioFile) {
        return () -> mediaDao.deleteAudioFile(audioFile);
    }

    @Override
    public void insertVideoFile(VideoFile videoFile) {
        mediaDao.insertVideoFile(videoFile);
    }

    @Override
    public Callable<Integer> updateVideoFile(VideoFile videoFile) {
        return () -> mediaDao.updateVideoFile(videoFile);
    }

    @Override
    public Observable<List<VideoFile>> getVideoFiles(String id) {
        return RxUtils.fromCallable(() -> mediaDao.getVideoFiles(id));
    }

    @Override
    public Observable<List<String>> getVideoFilesFrame(String id) {
        return RxUtils.fromCallable(() -> mediaDao.getVideoFilesFrame(id));
    }

    @Override
    public Callable<Integer> deleteVideoFile(VideoFile videoFile) {
        return () -> mediaDao.deleteVideoFile(videoFile);
    }

    @Override
    public Observable<List<AudioFile>> getAudioFilePlaylist() {
        return RxUtils.fromCallable(() -> mediaDao.getAudioFilePlaylist());
    }

    @Override
    public Observable<List<VideoFile>> getVideoFilePlaylist() {
        return RxUtils.fromCallable(() -> mediaDao.getVideoFilePlaylist());
    }

    @Override
    public Callable<Integer> clearPlaylist() {
        return () -> {
            mediaDao.clearAudioFilesPlaylist();
            return mediaDao.clearVideoFilesPlaylist();
        };
    }

    @Override
    public Callable<Integer> resetVideoContentDatabase() {
        return () -> {
            mediaDao.dropVideoFolders();
            return mediaDao.dropVideoFiles();
        };
    }

    @Override
    public Callable<Integer> resetAudioContentDatabase() {
        return () -> {
            mediaDao.dropAudioFolders();
            return mediaDao.dropAudioFiles();
        };
    }

    @Override
    public Observable<List<String>> getArtistsList() {
        return RxUtils.fromCallable(() -> mediaDao.getArtistsList());
    }

    @Override
    public Observable<List<String>> getGenresList() {
        return RxUtils.fromCallable(() -> mediaDao.getGenresList());
    }

    @Override
    public Observable<List<String>> getFolderFilePaths(String name) {
        return RxUtils.fromCallable(() -> mediaDao.getFolderFilePaths(name));
    }


    @Override
    public boolean containAudioTrack(String path) {
        return mediaDao.getAudioFile(path.replaceAll("'", "''") + "'") == null;
    }

    @Override
    public boolean containAudioFolder(String path) {
        return mediaDao.getAudioFolder(path.replaceAll("'", "''") + "'") == null;
    }

    @Override
    public boolean containVideoFolder(String path) {
        return mediaDao.getVideoFolder(path.replaceAll("'", "''") + "'") == null;
    }
}
