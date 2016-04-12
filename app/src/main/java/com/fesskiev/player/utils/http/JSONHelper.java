package com.fesskiev.player.utils.http;


import android.util.Log;

import com.fesskiev.player.model.User;
import com.fesskiev.player.model.vk.Group;
import com.fesskiev.player.model.vk.GroupPost;
import com.fesskiev.player.model.vk.VKMusicFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JSONHelper {

    private final static String TAG = JSONHelper.class.getSimpleName();


    public static ArrayList<VKMusicFile> getVKMusicFiles(JSONObject response, int offset) {

        ArrayList<VKMusicFile> vkMusicFiles = new ArrayList<>();
        try {

            JSONArray jsonArray = response.getJSONArray("response");
            for (int i = offset; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                VKMusicFile musicFile = new VKMusicFile();

                musicFile.aid = jsonObject.getInt("aid");
                musicFile.ownerId = jsonObject.getInt("owner_id");
                musicFile.artist = jsonObject.getString("artist");
                musicFile.title = jsonObject.getString("title");
                musicFile.duration = jsonObject.getInt("duration");
                musicFile.url = jsonObject.getString("url");
                if (jsonObject.has("genre")) {
                    musicFile.genre = jsonObject.getInt("genre");
                }

                Log.d(TAG, "vk file: " + musicFile.toString());

                vkMusicFiles.add(musicFile);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return vkMusicFiles;
    }

    public static User getUserProfile(JSONObject response) {
        User user = new User();
        try {

            JSONArray jsonArray = response.getJSONArray("response");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                user.uid = jsonObject.getInt("uid");
                user.firstName = jsonObject.getString("first_name");
                user.lastName = jsonObject.getString("last_name");
                user.photoUrl = jsonObject.getString("photo_200");

                Log.d(TAG, "user: " + user.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return user;
    }

    public static ArrayList<Group> getGroups(JSONObject response) {
        ArrayList<Group> groups = new ArrayList<>();

        try {

            JSONArray jsonArray = response.getJSONArray("response");
            for (int i = 1; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Group group = new Group();

                group.gid = jsonObject.getInt("gid");
                group.name = jsonObject.getString("name");
                group.screenName = jsonObject.getString("screen_name");
                group.isClosed = jsonObject.getInt("is_closed");
                group.type = jsonObject.getString("type");
                group.photoURL = jsonObject.getString("photo");
                group.photoMediumURL = jsonObject.getString("photo_medium");
                group.photoBigURL = jsonObject.getString("photo_big");


                Log.d(TAG, "group: " + group.toString());

                groups.add(group);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return groups;
    }

    public static ArrayList<GroupPost> getGroupPosts(JSONObject response) {
        ArrayList<GroupPost> groupPosts = new ArrayList<>();

        try {

            JSONArray responseArray = response.getJSONArray("response");
            for (int i = 1; i < responseArray.length(); i++) {
                JSONObject jsonObject = responseArray.getJSONObject(i);

                GroupPost groupPost = new GroupPost();
                groupPost.id = jsonObject.getInt("id");
                groupPost.date = jsonObject.getLong("date");
                groupPost.text = jsonObject.getString("text");

                JSONObject likesObject = jsonObject.getJSONObject("likes");
                groupPost.likes = likesObject.optInt("count");

                JSONObject repostsObject = jsonObject.getJSONObject("reposts");
                groupPost.reposts = repostsObject.getInt("count");

                if(jsonObject.has("attachment")){
                    JSONObject attachmentObject = jsonObject.getJSONObject("attachment");
                    String attachmentType = attachmentObject.getString("type");
                    if(attachmentType.equals(GroupPost.TYPE_PHOTO)){
                        JSONObject photoObject = attachmentObject.getJSONObject(GroupPost.TYPE_PHOTO);
                        groupPost.photo = photoObject.getString("src_big");
                    }
                }

                if(jsonObject.has("attachments")) {
                    JSONArray attachmentsArray = jsonObject.getJSONArray("attachments");
                    for (int j = 0; j < attachmentsArray.length(); j++) {
                        JSONObject attachmentsObject = attachmentsArray.getJSONObject(j);

                        String attachmentsType = attachmentsObject.getString("type");
                        if (attachmentsType.equals(GroupPost.TYPE_AUDIO)) {
                            JSONObject musicObject = attachmentsObject.getJSONObject(GroupPost.TYPE_AUDIO);

                            VKMusicFile musicFile = new VKMusicFile();

                            musicFile.aid = musicObject.getInt("aid");
                            musicFile.ownerId = musicObject.getInt("owner_id");
                            musicFile.artist = musicObject.getString("artist");
                            musicFile.title = musicObject.getString("title");
                            musicFile.duration = musicObject.getInt("duration");
                            musicFile.url = musicObject.getString("url");
                            if (jsonObject.has("genre")) {
                                musicFile.genre = musicObject.getInt("genre");
                            }

                            groupPost.musicFiles.add(musicFile);
                        }
                    }
                }

                groupPosts.add(groupPost);

                Log.e(TAG, "group post: " + groupPost.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return groupPosts;
    }
}
