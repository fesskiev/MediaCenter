package com.fesskiev.mediacenter.vk.data.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OAuth {

    @SerializedName("access_token")
    @Expose
    private String token;

    @SerializedName("user_id")
    @Expose
    private String userId;

    @SerializedName("expires_in")
    @Expose
    private int expires;


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getExpires() {
        return expires;
    }

    public void setExpires(int expires) {
        this.expires = expires;
    }

    @Override
    public String toString() {
        return "OAuth{" +
                "token='" + token + '\'' +
                ", userId='" + userId + '\'' +
                ", expires=" + expires +
                '}';
    }
}
