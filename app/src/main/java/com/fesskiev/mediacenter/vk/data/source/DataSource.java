package com.fesskiev.mediacenter.vk.data.source;

import com.fesskiev.mediacenter.vk.data.model.OAuth;
import com.fesskiev.mediacenter.vk.data.model.response.GroupPostsResponse;
import com.fesskiev.mediacenter.vk.data.model.response.GroupsResponse;
import com.fesskiev.mediacenter.vk.data.model.response.AudioFilesResponse;
import com.fesskiev.mediacenter.vk.data.model.response.UserResponse;

import rx.Observable;

public interface DataSource {

    Observable<OAuth> auth(String login, String password);

    Observable<UserResponse> getUser();

    Observable<AudioFilesResponse> getUserMusicFiles(int offset);

    Observable<GroupsResponse> getGroups();

    Observable<AudioFilesResponse> getSearchMusicFiles(String request, int offset);

    Observable<GroupPostsResponse> getGroupPots(int id, int offset);

}
