package com.fesskiev.player.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.model.User;
import com.fesskiev.player.model.vk.Group;
import com.fesskiev.player.model.vk.GroupPost;
import com.fesskiev.player.model.vk.VKMusicFile;
import com.fesskiev.player.utils.NetworkHelper;
import com.fesskiev.player.utils.http.JSONHelper;
import com.fesskiev.player.utils.http.JSONUTF8Request;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class RESTService extends Service {

    private static final String TAG = RESTService.class.getSimpleName();

    public static final String ACTION_GET_GROUP_POST =
            "com.fesskiev.player.ACTION_GET_GROUP_POST";
    public static final String ACTION_GET_GROUP_AUDIO =
            "com.fesskiev.player.ACTION_GET_GROUP_AUDIO";
    public static final String ACTION_GET_GROUPS =
            "com.fesskiev.player.ACTION_GET_GROUPS";
    public static final String ACTION_GET_USER_AUDIO =
            "com.fesskiev.player.ACTION_GET_USER_AUDIO";
    public static final String ACTION_GET_SEARCH_AUDIO =
            "com.fesskiev.player.ACTION_GET_SEARCH_AUDIO";
    public static final String ACTION_USER_PROFILE =
            "com.fesskiev.player.ACTION_USER_PROFILE";
    public static final String ACTION_USER_PROFILE_RESULT =
            "com.fesskiev.player.ACTION_USER_PROFILE_RESULT";
    public static final String ACTION_USER_AUDIO_RESULT =
            "com.fesskiev.player.ACTION_USER_AUDIO_RESULT";
    public static final String ACTION_SEARCH_AUDIO_RESULT =
            "com.fesskiev.player.ACTION_SEARCH_AUDIO_RESULT";
    public static final String ACTION_GROUPS_RESULT =
            "com.fesskiev.player.ACTION_GROUPS_RESULT";
    public static final String ACTION_GROUP_AUDIO_RESULT =
            "com.fesskiev.player.ACTION_GROUP_AUDIO_RESULT";
    public static final String ACTION_GROUP_POSTS_RESULT =
            "com.fesskiev.player.ACTION_GROUP_POSTS_RESULT";
    public static final String ACTION_SERVER_ERROR_RESULT =
            "com.fesskiev.player.ACTION_SERVER_ERROR_RESULT";
    public static final String ACTION_INTERNET_CONNECTION_ERROR =
            "com.fesskiev.player.ACTION_INTERNET_CONNECTION_ERROR";
    public static final String ACTION_INTERNET_CONNECTION_SLOW =
            "com.fesskiev.player.ACTION_INTERNET_CONNECTION_SLOW";


    public static final String EXTRA_REQUEST_URL
            = "com.fesskiev.player.EXTRA_REQUEST_URL";
    public static final String EXTRA_USER_PROFILE_RESULT
            = "com.fesskiev.player.EXTRA_USER_PROFILE_RESULT";
    public static final String EXTRA_AUDIO_RESULT
            = "com.fesskiev.player.EXTRA_AUDIO_RESULT";
    public static final String EXTRA_GROUPS_RESULT
            = "com.fesskiev.player.EXTRA_GROUPS_RESULT";
    public static final String EXTRA_GROUP_POSTS
            = "com.fesskiev.player.EXTRA_GROUP_POSTS";
    public static final String EXTRA_ERROR_MESSAGE
            = "com.fesskiev.player.EXTRA_ERROR_MESSAGE";


    public static void fetchGroupPost(Context context, String url) {
        Intent intent = new Intent(context, RESTService.class);
        intent.setAction(ACTION_GET_GROUP_POST);
        intent.putExtra(EXTRA_REQUEST_URL, url);
        context.startService(intent);
    }


    public static void fetchGroupAudio(Context context, String url) {
        Intent intent = new Intent(context, RESTService.class);
        intent.setAction(ACTION_GET_GROUP_AUDIO);
        intent.putExtra(EXTRA_REQUEST_URL, url);
        context.startService(intent);
    }

    public static void fetchGroups(Context context, String url) {
        Intent intent = new Intent(context, RESTService.class);
        intent.setAction(ACTION_GET_GROUPS);
        intent.putExtra(EXTRA_REQUEST_URL, url);
        context.startService(intent);
    }

    public static void fetchUserProfile(Context context, String url) {
        Intent intent = new Intent(context, RESTService.class);
        intent.setAction(ACTION_USER_PROFILE);
        intent.putExtra(EXTRA_REQUEST_URL, url);
        context.startService(intent);
    }

    public static void fetchUserAudio(Context context, String url) {
        Intent intent = new Intent(context, RESTService.class);
        intent.setAction(ACTION_GET_USER_AUDIO);
        intent.putExtra(EXTRA_REQUEST_URL, url);
        context.startService(intent);
    }

    public static void fetchSearchAudio(Context context, String url) {
        Intent intent = new Intent(context, RESTService.class);
        intent.setAction(ACTION_GET_SEARCH_AUDIO);
        intent.putExtra(EXTRA_REQUEST_URL, url);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        String url = intent.getStringExtra(EXTRA_REQUEST_URL);
        Log.wtf(TAG, "HANDLE INTENT, action: " + action + " url: " + url);
        if (!NetworkHelper.isConnected(getApplicationContext())) {
            Log.wtf(TAG, "INTERNET CONNECTION ERROR");
            sendInternetConnectionError();
            return START_NOT_STICKY;
        }
        if (!NetworkHelper.isConnectedFast(getApplicationContext())) {
            sendInternetConnectionSlow();
        }

        switch (action) {
            case ACTION_GET_USER_AUDIO:
                doGET(url, action);
                break;
            case ACTION_USER_PROFILE:
                doGET(url, action);
                break;
            case ACTION_GET_GROUPS:
                doGET(url, action);
                break;
            case ACTION_GET_GROUP_AUDIO:
                doGET(url, action);
                break;
            case ACTION_GET_SEARCH_AUDIO:
                doGET(url, action);
                break;
            case ACTION_GET_GROUP_POST:
                doGET(url, action);
                break;
        }

        return START_NOT_STICKY;
    }

    private void doGET(String url, final String action) {
        JSONUTF8Request jsonRequest = new JSONUTF8Request
                (Request.Method.GET,
                        url, null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                parseSendBroadcastByAction(response, action);
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleErrors(error);
                    }
                });
        MediaApplication.getInstance().addToRequestQueue(jsonRequest);
    }


    private void doPOST(String url, final String action, Map<String, String> params) {
        JSONUTF8Request jsonRequest =
                new JSONUTF8Request(Request.Method.POST, url,
                        new JSONObject(params), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        parseSendBroadcastByAction(response, action);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleErrors(error);
                    }

                });

        MediaApplication.getInstance().addToRequestQueue(jsonRequest);
    }

    private void parseSendBroadcastByAction(final JSONObject response, String action) {
        switch (action) {
            case ACTION_GET_USER_AUDIO:
                sendBroadcastUserAudio(JSONHelper.getVKMusicFiles(response, 0));
                break;
            case ACTION_USER_PROFILE:
                sendBroadcastUserProfile(JSONHelper.getUserProfile(response));
                break;
            case ACTION_GET_GROUPS:
                sendBroadcastGroups(JSONHelper.getGroups(response));
                break;
            case ACTION_GET_GROUP_AUDIO:
                sendBroadcastGroupAudio(JSONHelper.getVKMusicFiles(response, 0));
                break;
            case ACTION_GET_SEARCH_AUDIO:
                sendBroadcastSearchAudio(JSONHelper.getVKMusicFiles(response, 1));
                break;
            case ACTION_GET_GROUP_POST:
                sendBroadcastGroupPosts(JSONHelper.getGroupPosts(response));
                break;
        }
    }

    private void handleErrors(VolleyError error) {
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            sendServerError("TimeoutError: " + error.getMessage());
        } else if (error instanceof AuthFailureError) {
            sendServerError("AuthFailureError: " + error.getMessage());
        } else if (error instanceof ServerError) {
            sendServerError("ServerError: " + error.getMessage());
        } else if (error instanceof NetworkError) {
            sendServerError("NetworkError: " + error.getMessage());
        } else if (error instanceof ParseError) {
            sendServerError("ParseError: " + error.getMessage());
        }
    }

    private void sendServerError(String message) {
        Intent intent = new Intent();
        intent.setAction(ACTION_SERVER_ERROR_RESULT);
        intent.putExtra(EXTRA_ERROR_MESSAGE, message);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendBroadcastGroupPosts(ArrayList<GroupPost> groupPosts) {
        Intent intent = new Intent();
        intent.setAction(ACTION_GROUP_POSTS_RESULT);
        intent.putExtra(EXTRA_GROUP_POSTS, groupPosts);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendBroadcastGroupAudio(ArrayList<VKMusicFile> vkMusicFiles) {
        Intent intent = new Intent();
        intent.setAction(ACTION_GROUP_AUDIO_RESULT);
        intent.putExtra(EXTRA_AUDIO_RESULT, vkMusicFiles);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendBroadcastGroups(ArrayList<Group> groups) {
        Intent intent = new Intent();
        intent.setAction(ACTION_GROUPS_RESULT);
        intent.putExtra(EXTRA_GROUPS_RESULT, groups);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendBroadcastSearchAudio(ArrayList<VKMusicFile> vkMusicFiles) {
        Intent intent = new Intent();
        intent.setAction(ACTION_SEARCH_AUDIO_RESULT);
        intent.putExtra(EXTRA_AUDIO_RESULT, vkMusicFiles);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendBroadcastUserAudio(ArrayList<VKMusicFile> vkMusicFiles) {
        Intent intent = new Intent();
        intent.setAction(ACTION_USER_AUDIO_RESULT);
        intent.putExtra(EXTRA_AUDIO_RESULT, vkMusicFiles);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendBroadcastUserProfile(User user) {
        Intent intent = new Intent();
        intent.setAction(ACTION_USER_PROFILE_RESULT);
        intent.putExtra(EXTRA_USER_PROFILE_RESULT, user);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendInternetConnectionSlow() {
        Intent intent = new Intent();
        intent.setAction(ACTION_INTERNET_CONNECTION_SLOW);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendInternetConnectionError() {
        Intent intent = new Intent();
        intent.setAction(ACTION_INTERNET_CONNECTION_ERROR);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MediaApplication.getInstance().cancelPendingRequests();
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
