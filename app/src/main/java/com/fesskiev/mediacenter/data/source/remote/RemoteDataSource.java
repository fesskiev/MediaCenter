package com.fesskiev.mediacenter.data.source.remote;


import android.content.Context;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.search.AlbumResponse;
import com.fesskiev.mediacenter.data.source.remote.retrofit.APIService;

import java.util.LinkedHashMap;
import java.util.Map;

import retrofit2.Retrofit;
import io.reactivex.Observable;

public class RemoteDataSource implements RemoteSource {

    private APIService service;
    private Context context;

    public RemoteDataSource(Context context, Retrofit retrofit) {
        this.context = context;
        service = retrofit.create(APIService.class);
    }

    @Override
    public Observable<AlbumResponse> getAlbum(String artist, String album) {
        return service.getAlbum(albumQueryParams(artist, album));
    }

    private Map<String, String> albumQueryParams(String artist, String album) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("method", "album.getinfo");
        data.put("api_key", context.getString(R.string.last_fm_api_key));
        data.put("artist", artist);
        data.put("album", album);
        data.put("format", "json");
        return data;
    }
}
