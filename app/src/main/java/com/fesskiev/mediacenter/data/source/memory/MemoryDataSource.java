package com.fesskiev.mediacenter.data.source.memory;


import android.util.LruCache;

import com.fesskiev.mediacenter.data.model.Artist;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.Genre;
import com.fesskiev.mediacenter.data.model.VideoFolder;

import java.util.List;

import rx.Observable;

@SuppressWarnings("unchecked")
public class MemoryDataSource implements MemorySource {

    private static MemoryDataSource INSTANCE;

    private LruCache<Integer, Object> cache;

    private static final int GENRES = 0;
    private static final int ARTISTS = 1;
    private static final int FOLDERS = 2;
    private static final int VIDEO_FOLDERS = 3;

    private boolean cacheGenresIsDirty = true;
    private boolean cacheArtistsIsDirty = true;
    private boolean cacheFoldersIsDirty = true;
    private boolean cacheVideoFoldersIsDirty = true;

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

    public void addVideoFolders(List<VideoFolder> videoFolders) {
        cache.put(VIDEO_FOLDERS, videoFolders);
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
    public Observable<List<VideoFolder>> getVideoFolders() {
        return Observable.just((List<VideoFolder>) cache.get(VIDEO_FOLDERS));
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

    public boolean isCacheVideoFoldersDirty() {
        return cacheVideoFoldersIsDirty;
    }

    public void setCacheVideoFoldersDirty(boolean cacheVideoIsDirty) {
        this.cacheVideoFoldersIsDirty = cacheVideoIsDirty;
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

    public boolean isVideoFoldersEmpty() {
        List<Genre> video = (List<Genre>) cache.get(VIDEO_FOLDERS);
        return video != null && video.isEmpty();
    }
}
