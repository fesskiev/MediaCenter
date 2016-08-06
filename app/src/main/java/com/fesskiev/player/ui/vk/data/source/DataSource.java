package com.fesskiev.player.ui.vk.data.source;

import com.fesskiev.player.ui.vk.data.model.response.GroupPostsResponse;
import com.fesskiev.player.ui.vk.data.model.response.GroupsResponse;
import com.fesskiev.player.ui.vk.data.model.response.AudioFilesResponse;
import com.fesskiev.player.ui.vk.data.model.response.UserResponse;

import rx.Observable;

public interface DataSource {

    Observable<UserResponse> getUser();

    Observable<AudioFilesResponse> getUserMusicFiles(int offset);

    Observable<GroupsResponse> getGroups();

    Observable<AudioFilesResponse> getSearchMusicFiles(String request, int offset);

    Observable<GroupPostsResponse> getGroupPots(int id, int offset);

}
