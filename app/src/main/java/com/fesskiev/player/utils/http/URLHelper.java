package com.fesskiev.player.utils.http;




public class URLHelper {

    private static final String BASE_URL = "https://api.vk.com/method/";
    private static final String GET_AUDIO = BASE_URL + "audio.get";
    private static final String GET_USER_PROFILE = BASE_URL + "users.get";

    public static String getUserProfileURL(String userId){
        StringBuilder sb = new StringBuilder();
        sb.append(GET_USER_PROFILE);
        sb.append("?user_ids=");
        sb.append(userId);
        sb.append("&fields=photo_200");
        return sb.toString();
    }

    public static String getAudioURL(String token, String userId, int count, int offset){
        StringBuilder sb = new StringBuilder();
        sb.append(GET_AUDIO);
        sb.append("?user_id=");
        sb.append(userId);
        sb.append("&access_token=");
        sb.append(token);
        sb.append("&count=");
        sb.append(count);
        sb.append("&offset=");
        sb.append(offset);
        return sb.toString();
    }
}
