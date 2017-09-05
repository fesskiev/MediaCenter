package com.fesskiev.mediacenter.data.source.remote;


import com.fesskiev.mediacenter.data.model.search.AlbumResponse;

import io.reactivex.Observable;;

public interface RemoteSource {

    Observable<AlbumResponse> getAlbum(String artist, String album);
}
