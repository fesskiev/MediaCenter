package com.fesskiev.mediacenter.vk.data.source;




import com.fesskiev.mediacenter.vk.data.model.OAuth;
import com.fesskiev.mediacenter.vk.data.model.response.GroupPostsResponse;
import com.fesskiev.mediacenter.vk.data.model.response.GroupsResponse;
import com.fesskiev.mediacenter.vk.data.model.response.AudioFilesResponse;
import com.fesskiev.mediacenter.vk.data.model.response.UserResponse;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;

public interface APIService {

    String OAUTH_URL = "https://oauth.vk.com/token/";
    String BASE_URL = "https://api.vk.com/method/";
    String VERSION = "5.53";

    @GET(".")
    Observable<OAuth> auth(@QueryMap Map<String, String> options);

    @GET("users.get")
    Observable<UserResponse> getToken(@QueryMap Map<String, String> options);

    @GET("users.get")
    Observable<UserResponse> getUser(@QueryMap Map<String, String> options);

    @GET("audio.get")
    Observable<AudioFilesResponse> getUserMusicFiles(@QueryMap Map<String, String> options);

    @GET("groups.get")
    Observable<GroupsResponse> getGroups(@QueryMap Map<String, String> options);

    @GET("audio.search")
    Observable<AudioFilesResponse> getSearchMusicFiles(@QueryMap Map<String, String> options);

    @GET("wall.get")
    Observable<GroupPostsResponse> getGroupPosts(@QueryMap Map<String, String> options);

}