package com.fesskiev.mediacenter.data.model.vk.response;


import com.fesskiev.mediacenter.data.model.vk.Groups;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GroupsResponse {

    @SerializedName("response")
    @Expose
    private Groups groups;

    public Groups getGroups() {
        return groups;
    }
}
