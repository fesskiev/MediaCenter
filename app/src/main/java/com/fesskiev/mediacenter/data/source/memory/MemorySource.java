package com.fesskiev.mediacenter.data.source.memory;


import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.VideoFolder;

import java.util.List;

import rx.Observable;

public interface MemorySource {

    Observable<List<String>> getGenresList();

    Observable<List<String>> getArtistsLis();

    Observable<List<AudioFolder>> getAudioFolders();

    Observable<List<VideoFolder>> getVideoFolders();

}
