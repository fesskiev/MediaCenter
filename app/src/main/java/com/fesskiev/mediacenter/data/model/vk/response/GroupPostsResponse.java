package com.fesskiev.mediacenter.data.model.vk.response;


import com.fesskiev.mediacenter.data.model.vk.GroupPost;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
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
