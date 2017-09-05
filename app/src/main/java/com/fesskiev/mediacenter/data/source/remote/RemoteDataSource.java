package com.fesskiev.mediacenter.data.source.remote;


import android.content.Context;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.search.AlbumResponse;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.data.source.remote.retrofit.APIService;
import com.fesskiev.mediacenter.data.source.remote.retrofit.RxErrorHandlingCallAdapterFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import io.reactivex.Observable;;

public class RemoteDataSource implements RemoteSource {

    private static RemoteDataSource INSTANCE;
    private OkHttpClient client;
    private APIService service;
    private Context context;

    public static RemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RemoteDataSource();
        }
        return INSTANCE;
    }

    private RemoteDataSource() {
        context = MediaApplication.getInstance().getApplicationContext();

        client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(new LoggingInterceptor())
                .build();

        service = buildRetrofit().create(APIService.class);


    }

    private Retrofit buildRetrofit() {
        return new Retrofit.Builder()
                .client(client)
                .baseUrl("http://ws.audioscrobbler.com/2.0/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
                .build();
    }

    private static class LoggingInterceptor implements Interceptor {

        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            AppLog.INFO(String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            AppLog.INFO(String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));

            return response;
        }
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
