package com.fesskiev.player.utils.http;


import android.util.Log;

import com.fesskiev.player.model.User;
import com.fesskiev.player.model.vk.Group;
import com.fesskiev.player.model.vk.VKMusicFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JSONHelper {

    private final static String TAG = JSONHelper.class.getSimpleName();


    public static ArrayList<VKMusicFile> getVKMusicFiles(JSONObject response) {

        ArrayList<VKMusicFile> vkMusicFiles = new ArrayList<>();
        try {

            JSONArray jsonArray = response.getJSONArray("response");
            for (int i = 0; i < jsonArray.length(); i++) {
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

    public static ArrayList<Group> getGroups(JSONObject response){
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
                group.photoBigURL= jsonObject.getString("photo_big");


                Log.d(TAG, "group: " + group.toString());

                groups.add(group);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return groups;
    }
}
