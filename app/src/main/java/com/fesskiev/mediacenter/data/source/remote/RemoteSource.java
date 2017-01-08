package com.fesskiev.mediacenter.data.source.remote;


import com.fesskiev.mediacenter.data.model.vk.OAuth;
import com.fesskiev.mediacenter.data.model.vk.response.AudioFilesResponse;
import com.fesskiev.mediacenter.data.model.vk.response.GroupPostsResponse;
import com.fesskiev.mediacenter.data.model.vk.response.GroupsResponse;
import com.fesskiev.mediacenter.data.model.vk.response.UserResponse;

import rx.Observable;

public interface RemoteSource {

    Observable<OAuth> auth(String login, String password);

    Observable<UserResponse> getUser();

    Observable<AudioFilesResponse> getUserMusicFiles(int offset);

    Observable<GroupsResponse> getGroups();

    Observable<AudioFilesResponse> getSearchMusicFiles(String request, int offset);

    Observable<GroupPostsResponse> getGroupPots(int id, int offset);
}
