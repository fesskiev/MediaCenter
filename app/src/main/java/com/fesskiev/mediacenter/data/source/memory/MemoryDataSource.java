package com.fesskiev.mediacenter.data.source.memory;


import android.util.LruCache;

import com.fesskiev.mediacenter.data.model.Artist;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.Genre;
import com.fesskiev.mediacenter.data.model.VideoFile;

import java.util.List;

import rx.Observable;

@SuppressWarnings("unchecked")
public class MemoryDataSource implements MemorySource {

    private static MemoryDataSource INSTANCE;

    private LruCache<Integer, Object> cache;

    private static final int GENRES = 0;
    private static final int ARTISTS = 1;
    private static final int FOLDERS = 2;
    private static final int VIDEO_FILES = 3;

    private boolean cacheGenresIsDirty = true;
    private boolean cacheArtistsIsDirty = true;
    private boolean cacheFoldersIsDirty = true;
    private boolean cacheVideoFilesIsDirty = true;

    public static MemoryDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MemoryDataSource();
        }
        return INSTANCE;
    }

    private MemoryDataSource() {
        cache = new LruCache<>(10);
    }

    public void addArtists(List<Artist> artists) {
        cache.put(ARTISTS, artists);
    }

    public void addGenres(List<Genre> genres) {
        cache.put(GENRES, genres);
    }

    public void addAudioFolders(List<AudioFolder> audioFolders) {
        cache.put(FOLDERS, audioFolders);
    }

    public void addVideoFiles(List<VideoFile> videoFiles) {
        cache.put(VIDEO_FILES, videoFiles);
    }

    @Override
    public Observable<List<Genre>> getGenres() {
        return Observable.just((List<Genre>) cache.get(GENRES));
    }

    @Override
    public Observable<List<Artist>> getArtists() {
        return Observable.just((List<Artist>) cache.get(ARTISTS));
    }

    @Override
    public Observable<List<AudioFolder>> getAudioFolders() {
        return Observable.just((List<AudioFolder>) cache.get(FOLDERS));
    }

    @Override
    public Observable<List<VideoFile>> getVideoFiles() {
        return Observable.just((List<VideoFile>) cache.get(VIDEO_FILES));
    }


    public boolean isCacheGenresDirty() {
        return cacheGenresIsDirty;
    }

    public void setCacheGenresDirty(boolean cacheGenresIsDirty) {
        this.cacheGenresIsDirty = cacheGenresIsDirty;
    }

    public boolean isCacheArtistsDirty() {
        return cacheArtistsIsDirty;
    }

    public void setCacheArtistsDirty(boolean cacheArtistsIsDirty) {
        this.cacheArtistsIsDirty = cacheArtistsIsDirty;
    }

    public boolean isCacheFoldersDirty() {
        return cacheFoldersIsDirty;
    }

    public boolean isCacheVideoFilesDirty() {
        return cacheVideoFilesIsDirty;
    }

    public void setCacheVideoFilesDirty(boolean cacheVideoIsDirty) {
        this.cacheVideoFilesIsDirty = cacheVideoIsDirty;
    }

    public void setCacheFoldersDirty(boolean cacheFoldersIsDirty) {
        this.cacheFoldersIsDirty = cacheFoldersIsDirty;
    }

    public boolean isArtistsEmpty() {
        List<Artist> artists = (List<Artist>) cache.get(ARTISTS);
        return artists != null && artists.isEmpty();
    }

    public boolean isFoldersEmpty() {
        List<AudioFolder> folders = (List<AudioFolder>) cache.get(FOLDERS);
        return folders != null && folders.isEmpty();
    }

    public boolean isGenresEmpty() {
        List<Genre> genres = (List<Genre>) cache.get(GENRES);
        return genres != null && genres.isEmpty();
    }

    public boolean isVideoFilesEmpty() {
        List<Genre> video = (List<Genre>) cache.get(VIDEO_FILES);
        return video != null && video.isEmpty();
    }
}
