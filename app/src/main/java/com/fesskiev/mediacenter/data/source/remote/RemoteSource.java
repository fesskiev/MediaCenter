package com.fesskiev.mediacenter.data.source.remote;


import com.fesskiev.mediacenter.data.model.search.AlbumResponse;

import rx.Observable;

public interface RemoteSource {

    Observable<AlbumResponse> getAlbum(String artist, String album);
}
