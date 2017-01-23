package com.fesskiev.mediacenter.data.model.vk.response;

import com.fesskiev.mediacenter.data.model.vk.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class UserResponse {

    @SerializedName("response")
    @Expose
    private List<User> user = new ArrayList<>();

    public User getUser() {
        return user.get(0);
    }
}