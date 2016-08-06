package com.fesskiev.player.ui.vk.data.source;


import android.net.ParseException;

import com.fesskiev.player.ui.vk.data.model.Attachment;
import com.fesskiev.player.ui.vk.data.model.Audio;
import com.fesskiev.player.ui.vk.data.model.GroupPost;
import com.fesskiev.player.ui.vk.data.model.Likes;
import com.fesskiev.player.ui.vk.data.model.Photo;
import com.fesskiev.player.ui.vk.data.model.Reposts;
import com.fesskiev.player.ui.vk.data.model.response.GroupPostsResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GroupPostDeserialize implements JsonDeserializer<GroupPostsResponse> {

    @Override
    public GroupPostsResponse deserialize(JsonElement json, Type typeOfT,
                                          JsonDeserializationContext context) throws JsonParseException {
        final GroupPostsResponse groupPostsResponse = new GroupPostsResponse();
        final JsonObject jsonObject = json.getAsJsonObject();

        try {

            final JsonObject responseObject = jsonObject.getAsJsonObject("response");

            groupPostsResponse.setCount(responseObject.get("count").getAsInt());

            List<GroupPost> groupPosts = new ArrayList<>();
            final JsonArray jsonArray = responseObject.get("items").getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                GroupPost groupPost = new GroupPost();

                JsonObject object = jsonArray.get(i).getAsJsonObject();
                groupPost.setDate(object.get("date").getAsLong());
                groupPost.setText(object.get("text").getAsString());

                List<Attachment> attachments = new ArrayList<>();
                if (object.has("attachments")) {
                    JsonArray attachmentsArray = object.getAsJsonArray("attachments");
                    for (int j = 0; j < attachmentsArray.size(); j++) {
                        JsonObject attachmentsObject = attachmentsArray.get(j).getAsJsonObject();

                        Attachment attachment = new Attachment();

                        String type = attachmentsObject.get("type").getAsString();
                        attachment.setType(type);

                        if (type.equals("photo")) {

                            JsonObject photoObject = attachmentsObject.getAsJsonObject("photo");

                            Photo photo = new Photo();
                            if (photoObject.has("photo_75")) {
                                photo.setPhoto75(photoObject.get("photo_75").getAsString());
                            }
                            if (photoObject.has("photo_130")) {
                                photo.setPhoto130(photoObject.get("photo_130").getAsString());
                            }
                            if (photoObject.has("photo_604")) {
                                photo.setPhoto604(photoObject.get("photo_604").getAsString());
                            }
                            if (photoObject.has("photo_807")) {
                                photo.setPhoto807(photoObject.get("photo_807").getAsString());
                            }
                            if (photoObject.has("text")) {
                                photo.setText(photoObject.get("text").getAsString());
                            }
                            if (photoObject.has("date")) {
                                photo.setDate(photoObject.get("date").getAsInt());
                            }
                            attachment.setPhoto(photo);

                        } else if (type.equals("audio")) {

                            JsonObject audioObject = attachmentsObject.getAsJsonObject("audio");

                            Audio audio = new Audio();
                            audio.setId(audioObject.get("id").getAsInt());
                            audio.setArtist(audioObject.get("artist").getAsString());
                            audio.setTitle(audioObject.get("title").getAsString());
                            audio.setDuration(audioObject.get("duration").getAsInt());
                            audio.setUrl(audioObject.get("url").getAsString());
                            if (audioObject.has("genre_id")) {
                                audio.setGenreId(audioObject.get("genre_id").getAsInt());
                            }

                            attachment.setAudio(audio);
                        }

                        attachments.add(attachment);
                    }
                    groupPost.setAttachments(attachments);
                    groupPosts.add(groupPost);
                }

                JsonObject likesObject = object.getAsJsonObject("likes");
                Likes likes = new Likes();
                likes.setCount(likesObject.get("count").getAsInt());
                groupPost.setLikes(likes);


                JsonObject repostsObject = object.getAsJsonObject("reposts");
                Reposts reposts = new Reposts();
                reposts.setCount(repostsObject.get("count").getAsInt());
                groupPost.setReposts(reposts);
            }
            groupPostsResponse.setGroupPosts(groupPosts);

            return groupPostsResponse;
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return groupPostsResponse;
    }
}
