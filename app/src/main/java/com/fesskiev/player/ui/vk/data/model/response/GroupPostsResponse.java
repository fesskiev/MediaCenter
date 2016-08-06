package com.fesskiev.player.ui.vk.data.model.response;


import com.fesskiev.player.ui.vk.data.model.GroupPost;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupPostsResponse {

    @SerializedName("count")
    @Expose
    private int count;

    @SerializedName("items")
    @Expose
    private List<GroupPost> groupPosts = new ArrayList<>();

    public int getCount() {
        return count;
    }

    public List<GroupPost> getGroupPostList() {
        return groupPosts;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setGroupPosts(List<GroupPost> groupPosts) {
        this.groupPosts = groupPosts;
    }

    @Override
    public String toString() {
        return "GroupPostsResponse{" +
                "count=" + count +
                ", groupPost=" + groupPosts +
                '}';
    }
}
