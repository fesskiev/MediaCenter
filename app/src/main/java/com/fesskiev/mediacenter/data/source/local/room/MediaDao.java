package com.fesskiev.mediacenter.data.source.local.room;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.SelectedAudioFile;
import com.fesskiev.mediacenter.data.model.SelectedAudioFolder;
import com.fesskiev.mediacenter.data.model.SelectedVideoFile;
import com.fesskiev.mediacenter.data.model.SelectedVideoFolder;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.VideoFolder;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public abstract class MediaDao {

    @Query("SELECT * FROM AudioFolders")
    public abstract List<AudioFolder> getAudioFolders();

    @Insert(onConflict = REPLACE)
    public abstract void insertAudioFolder(AudioFolder audioFolder);

    @Update(onConflict = REPLACE)
    public abstract void updateAudioFolder(AudioFolder audioFolder);

    @Delete
    public abstract int deleteAudioFolderWithFiles(AudioFolder audioFolder);

    @Query("SELECT * FROM AudioFolders WHERE folderPath LIKE :path")
    public abstract AudioFolder getAudioFolderByPath(String path);

    @Query("SELECT AudioFolders.*, SelectedAudioFolder.isSelected FROM AudioFolders INNER JOIN SelectedAudioFolder ON AudioFolders.id = SelectedAudioFolder.audioFolderId")
    public abstract AudioFolder getSelectedAudioFolder();

    @Query("SELECT * FROM AudioFiles WHERE folderId LIKE :id")
    public abstract List<AudioFile> getSelectedAudioFiles(String id);

    @Insert(onConflict = REPLACE)
    public abstract void updateSelectedAudioFolder(SelectedAudioFolder audioFolder);

    @Update(onConflict = REPLACE)
    public abstract void updateAudioFoldersIndex(List<AudioFolder> audioFolders);


    @Query("SELECT * FROM VideoFolders")
    public abstract List<VideoFolder> getVideoFolders();

    @Insert(onConflict = REPLACE)
    public abstract void insertVideoFolder(VideoFolder videoFolder);

    @Update(onConflict = REPLACE)
    public abstract void updateVideoFolder(VideoFolder videoFolder);

    @Delete
    public abstract int deleteVideoFolderWithFiles(VideoFolder videoFolder);

    @Query("SELECT * FROM VideoFolders WHERE folderPath LIKE :path")
    public abstract VideoFolder getVideoFolderByPath(String path);

    @Update(onConflict = REPLACE)
    public abstract void updateVideoFoldersIndex(List<VideoFolder> videoFolders);

    @Query("SELECT VideoFolders.*, SelectedVideoFolder.isSelected FROM VideoFolders INNER JOIN SelectedVideoFolder ON VideoFolders.id = SelectedVideoFolder.videoFolderId")
    public abstract VideoFolder getSelectedVideoFolder();

    @Insert(onConflict = REPLACE)
    public abstract void updateSelectedVideoFolder(SelectedVideoFolder videFolder);


    @Insert(onConflict = REPLACE)
    public abstract void insertAudioFile(AudioFile audioFile);

    @Insert(onConflict = REPLACE)
    public abstract void updateSelectedAudioFile(SelectedAudioFile selectedAudioFile);

    @Query("SELECT AudioFiles.*, SelectedAudioFile.isSelected FROM AudioFiles INNER JOIN SelectedAudioFile ON AudioFiles.fileId = SelectedAudioFile.audioFileId")
    public abstract AudioFile getSelectedAudioFile();

    @Update(onConflict = REPLACE)
    public abstract void updateAudioFile(AudioFile audioFile);

    @Query("SELECT * FROM AudioFiles WHERE filePath LIKE :path")
    public abstract AudioFile getAudioFileByPath(String path);

    @Query("SELECT * FROM AudioFiles WHERE title LIKE '%' || :query || '%'")
    public abstract List<AudioFile> getSearchAudioFiles(String query);

    @Query("SELECT * FROM AudioFiles WHERE genre LIKE :contentValue ORDER BY trackNumber ASC")
    public abstract List<AudioFile> getGenreTracks(String contentValue);

    @Query("SELECT * FROM AudioFiles WHERE artist LIKE :contentValue ORDER BY trackNumber ASC")
    public abstract List<AudioFile> getArtistTracks(String contentValue);

    @Query("SELECT * FROM AudioFiles WHERE folderId LIKE :id ORDER BY trackNumber ASC")
    public abstract List<AudioFile> getAudioTracks(String id);

    @Delete
    public abstract int deleteAudioFile(AudioFile audioFile);


    @Insert(onConflict = REPLACE)
    public abstract void insertVideoFile(VideoFile videoFile);

    @Update(onConflict = REPLACE)
    public abstract void updateVideoFile(VideoFile videoFile);

    @Query("SELECT VideoFiles.*, SelectedVideoFile.isSelected FROM VideoFiles INNER JOIN SelectedVideoFile ON VideoFiles.fileId = SelectedVideoFile.videoFileId")
    public abstract VideoFile getSelectedVideoFile();

    @Insert(onConflict = REPLACE)
    public abstract void updateSelectedVideoFile(SelectedVideoFile selectedVideoFile);

    @Query("SELECT * FROM VideoFiles WHERE folderId LIKE :id")
    public abstract List<VideoFile> getSelectedVideoFiles(String id);

    @Query("SELECT * FROM VideoFiles WHERE folderId LIKE :id")
    public abstract List<VideoFile> getVideoFiles(String id);

    @Query("SELECT framePath FROM VideoFiles WHERE folderId LIKE :id")
    public abstract List<String> getVideoFilesFrame(String id);

    @Delete
    public abstract int deleteVideoFile(VideoFile videoFile);



    @Query("SELECT * FROM AudioFiles WHERE inPlayList LIKE 1")
    public abstract List<AudioFile> getAudioFilePlaylist();

    @Query("SELECT * FROM VideoFiles WHERE inPlayList LIKE 1")
    public abstract List<VideoFile> getVideoFilePlaylist();

    @Query("UPDATE VideoFiles SET inPlayList = 0")
    public abstract int clearVideoFilesPlaylist();

    @Query("UPDATE AudioFiles SET inPlayList = 0")
    public abstract int clearAudioFilesPlaylist();



    @Query("SELECT DISTINCT artist FROM AudioFiles")
    public abstract List<String> getArtistsList();

    @Query("SELECT DISTINCT genre FROM AudioFiles")
    public abstract List<String> getGenresList();



    @Query("SELECT folderPath FROM AudioFolders WHERE folderName LIKE :name")
    public abstract List<String> getFolderFilePaths(String name);



    @Query("SELECT * FROM AudioFiles WHERE filePath LIKE :path")
    public abstract AudioFile getAudioFile(String path);

    @Query("SELECT * FROM AudioFolders WHERE folderPath LIKE :path")
    public abstract AudioFolder getAudioFolder(String path);

    @Query("SELECT * FROM VideoFolders WHERE folderPath LIKE :path")
    public abstract VideoFolder getVideoFolder(String path);



    @Query("DELETE FROM VideoFolders")
    public abstract int dropVideoFolders();

    @Query("DELETE FROM VideoFiles")
    public abstract int dropVideoFiles();

    @Query("DELETE FROM AudioFolders")
    public abstract int dropAudioFolders();

    @Query("DELETE FROM AudioFiles")
    public abstract int dropAudioFiles();

}
