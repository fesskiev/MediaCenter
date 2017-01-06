package com.fesskiev.mediacenter.vk.data.source;


import android.content.Context;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.vk.data.model.OAuth;
import com.fesskiev.mediacenter.vk.data.model.response.GroupPostsResponse;
import com.fesskiev.mediacenter.vk.data.model.response.GroupsResponse;
import com.fesskiev.mediacenter.vk.data.model.response.AudioFilesResponse;
import com.fesskiev.mediacenter.vk.data.model.response.UserResponse;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private OkHttpClient client;
    private APIService service;
    private AppSettingsManager settingsManager;
    private Context context;
    private String BASE_URL;

    public static DataRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DataRepository();
        }
        return INSTANCE;
    }

    private DataRepository() {
        context = MediaApplication.getInstance().getApplicationContext();
        settingsManager =
                AppSettingsManager.getInstance();

        client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(new LoggingInterceptor())
                .build();

        service = buildRetrofit(APIService.BASE_URL).create(APIService.class);

    }

    private Retrofit buildRetrofit(String url) {
        return new Retrofit.Builder()
                .client(client)
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    private Retrofit buildRetrofit(String url, Gson gson) {
        return new Retrofit.Builder()
                .client(client)
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }


    @Override
    public Observable<OAuth> auth(String login, String password) {
        return buildRetrofit(APIService.OAUTH_URL)
                .create(APIService.class)
                .auth(getAuthQueryParams(login, password));
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

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(GroupPostsResponse.class, new GroupPostDeserialize())
                .create();

        return buildRetrofit(APIService.BASE_URL, gson)
                .create(APIService.class)
                .getGroupPosts(getGroupPostsQueryMap(id, offset));

    }

    private Map<String, String> getAuthQueryParams(String login, String password) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("grant_type", "password");
        data.put("client_id", context.getString(R.string.vk_iphone_official_id));
        data.put("client_secret", context.getString(R.string.vk_iphone_official_secret));
        data.put("username", login);
        data.put("password", password);
        data.put("scope", "audio,video,groups");

        return data;
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
        data.put("access_token", settingsManager.getAuthToken());
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
