package com.fesskiev.mediacenter.data.source.remote.retrofit;


import com.fesskiev.mediacenter.data.model.search.AlbumResponse;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;

public interface APIService {

    @GET(".")
    Observable<AlbumResponse> getAlbum(@QueryMap Map<String, String> options);

}
