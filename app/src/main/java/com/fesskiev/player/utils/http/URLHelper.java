package com.fesskiev.player.utils.http;


public class URLHelper {

    private static final String BASE_URL = "https://api.vk.com/method/";
    private static final String GET_AUDIO = BASE_URL + "audio.get";
    private static final String GET_USER_PROFILE = BASE_URL + "users.get";
    private static final String GET_USER_GROUP = BASE_URL + "groups.get";
    private static final String SEARCH_AUDIO = BASE_URL + "audio.search";

    public static String getUserGroupsURL(String token, String userId) {
        StringBuilder sb = new StringBuilder();
        sb.append(GET_USER_GROUP);
        sb.append("?user_id=");
        sb.append(userId);
        sb.append("&access_token=");
        sb.append(token);
        sb.append("&extended=1");
        return sb.toString();
    }

    public static String getUserProfileURL(String userId) {
        StringBuilder sb = new StringBuilder();
        sb.append(GET_USER_PROFILE);
        sb.append("?user_ids=");
        sb.append(userId);
        sb.append("&fields=photo_200");
        return sb.toString();
    }

    public static String getUserAudioURL(String token, String userId, int count, int offset) {
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

    public static String getSearchAudioURL(String token, String request, int count, int offset) {
        StringBuilder sb = new StringBuilder();
        sb.append(SEARCH_AUDIO);
        sb.append("?access_token=");
        sb.append(token);
        sb.append("&q=");
        sb.append(request);
        sb.append("&auto_complete=");
        sb.append("1");
        sb.append("&sort=");
        sb.append("2");
        sb.append("&count=");
        sb.append(count);
        sb.append("&offset=");
        sb.append(offset);
        return sb.toString();
    }

    public static String getGroupAudioURL(String token, int groupId, int count, int offset) {
        StringBuilder sb = new StringBuilder();
        sb.append(GET_AUDIO);
        sb.append("?group_id=");
        sb.append(groupId);
        sb.append("&access_token=");
        sb.append(token);
        sb.append("&count=");
        sb.append(count);
        sb.append("&offset=");
        sb.append(offset);
        return sb.toString();
    }
}
