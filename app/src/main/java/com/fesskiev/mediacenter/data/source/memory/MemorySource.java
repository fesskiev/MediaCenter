package com.fesskiev.mediacenter.data.source.memory;


import com.fesskiev.mediacenter.data.model.Artist;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.Genre;
import com.fesskiev.mediacenter.data.model.VideoFolder;

import java.util.List;

import rx.Observable;

public interface MemorySource {

    Observable<List<Genre>> getGenres();

    Observable<List<Artist>> getArtists();

    Observable<List<AudioFolder>> getAudioFolders();

    Observable<List<VideoFolder>> getVideoFolders();

}
