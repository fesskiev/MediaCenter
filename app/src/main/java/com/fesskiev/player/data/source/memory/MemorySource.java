package com.fesskiev.player.data.source.memory;


import com.fesskiev.player.data.model.Artist;
import com.fesskiev.player.data.model.AudioFolder;
import com.fesskiev.player.data.model.Genre;
import com.fesskiev.player.data.model.VideoFile;

import java.util.List;

import rx.Observable;

public interface MemorySource {

    Observable<List<Genre>> getGenres();

    Observable<List<Artist>> getArtists();

    Observable<List<AudioFolder>> getAudioFolders();

    Observable<List<VideoFile>> getVideoFiles();

}
