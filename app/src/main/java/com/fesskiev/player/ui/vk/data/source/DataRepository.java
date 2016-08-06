package com.fesskiev.player.ui.vk.data.source;


import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.ui.vk.data.model.response.GroupPostsResponse;
import com.fesskiev.player.ui.vk.data.model.response.GroupsResponse;
import com.fesskiev.player.ui.vk.data.model.response.AudioFilesResponse;
import com.fesskiev.player.ui.vk.data.model.response.UserResponse;
import com.fesskiev.player.utils.AppLog;
import com.fesskiev.player.utils.AppSettingsManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

public class DataRepository implements DataSource {

    private static DataRepository INSTANCE;
    private APIService service;
    private AppSettingsManager settingsManager;

    public static DataRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DataRepository();
        }
        return INSTANCE;
    }

    private DataRepository() {
        settingsManager =
                AppSettingsManager.getInstance(MediaApplication.getInstance().getApplicationContext());

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new LoggingInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(APIService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build();

        service = retrofit.create(APIService.class);

    }


    @Override
    public Observable<UserResponse> getUser() {
        return service.getUser(getUserQueryMap());
    }

    @Override
    public Observable<AudioFilesResponse> getUserMusicFiles(int offset) {
        return service.getUserMusicFiles(getUserMusicFilesQueryMap(offset));
    }

    @Override
    public Observable<GroupsResponse> getGroups() {
        return service.getGroups(getGroupsQueryMap());
    }

    @Override
    public Observable<AudioFilesResponse> getSearchMusicFiles(String request, int offset) {
        return service.getSearchMusicFiles(getSearchMusicFilesQueryMap(request, offset));
    }

    @Override
    public Observable<GroupPostsResponse> getGroupPots(int id, int offset) {

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new LoggingInterceptor())
                .build();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(GroupPostsResponse.class, new GroupPostDeserialize())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(APIService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build();

        APIService service = retrofit.create(APIService.class);

        return service.getGroupPosts(getGroupPostsQueryMap(id, offset));
    }

    private Map<String, String> getUserQueryMap() {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("user_ids", settingsManager.getUserId());
        data.put("fields", "photo_200");
        return data;
    }

    private Map<String, String> getSearchMusicFilesQueryMap(String request, int offset) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("user_id", settingsManager.getUserId());
        data.put("access_token", settingsManager.getAuthToken());
        data.put("q", request);
        data.put("auto_complete", String.valueOf(1));
        data.put("sort", String.valueOf(2));
        data.put("count", String.valueOf(20));
        data.put("offset", String.valueOf(offset));
        data.put("v", APIService.VERSION);
        return data;
    }

    private Map<String, String> getGroupsQueryMap() {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("user_id", settingsManager.getUserId());
        data.put("access_token", settingsManager.getAuthToken());
        data.put("extended", String.valueOf(1));
        data.put("v", APIService.VERSION);
        return data;
    }

    private Map<String, String> getUserMusicFilesQueryMap(int offset) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("user_id", settingsManager.getUserId());
        data.put("access_token", settingsManager.getAuthToken());
        data.put("count", String.valueOf(20));
        data.put("offset", String.valueOf(offset));
        data.put("v", APIService.VERSION);
        return data;
    }

    private Map<String, String> getGroupPostsQueryMap(int id, int offset) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("owner_id", "-" + id);
        data.put("filter", "all");
        data.put("count", String.valueOf(20));
        data.put("offset", String.valueOf(offset));
        data.put("v", APIService.VERSION);
        return data;
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
}
