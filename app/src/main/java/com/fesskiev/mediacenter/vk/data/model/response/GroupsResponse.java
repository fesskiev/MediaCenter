package com.fesskiev.mediacenter.vk.data.model.response;


import com.fesskiev.mediacenter.vk.data.model.Groups;
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
