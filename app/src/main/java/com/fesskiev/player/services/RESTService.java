package com.fesskiev.player.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fesskiev.player.MusicApplication;
import com.fesskiev.player.model.User;
import com.fesskiev.player.model.VKMusicFile;
import com.fesskiev.player.utils.http.JSONHelper;
import com.fesskiev.player.utils.http.JSONUTF8Request;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RESTService extends Service {

    private static final String TAG = RESTService.class.getSimpleName();

    public static final String ACTION_GET_AUDIO =
            "com.fesskiev.player.ACTION_GET_AUDIO";
    public static final String ACTION_USER_PROFILE =
            "com.fesskiev.player.ACTION_USER_PROFILE";
    public static final String ACTION_USER_PROFILE_RESULT =
            "com.fesskiev.player.ACTION_USER_PROFILE_RESULT";
    public static final String ACTION_AUDIO_RESULT =
            "com.fesskiev.player.ACTION_AUDIO_RESULT";

    public static final String EXTRA_REQUEST_URL
            = "com.fesskiev.player.EXTRA_REQUEST_URL";
    public static final String EXTRA_USER_PROFILE_RESULT
            = "ua.com.minfin.action.EXTRA_USER_PROFILE_RESULT";
    public static final String EXTRA_AUDIO_RESULT
            = "ua.com.minfin.action.EXTRA_AUDIO_RESULT";

    public static void fetchUserProfile(Context context, String url) {
        Intent intent = new Intent(context, RESTService.class);
        intent.setAction(ACTION_USER_PROFILE);
        intent.putExtra(EXTRA_REQUEST_URL, url);
        context.startService(intent);
    }

    public static void fetchAudio(Context context, String url) {
        Intent intent = new Intent(context, RESTService.class);
        intent.setAction(ACTION_GET_AUDIO);
        intent.putExtra(EXTRA_REQUEST_URL, url);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        String url = intent.getStringExtra(EXTRA_REQUEST_URL);
        Log.wtf(TAG, "HANDLE INTENT, action: " + action + " url: " + url);

        switch (action) {
            case ACTION_GET_AUDIO:
                doGET(url, action);
                break;
            case ACTION_USER_PROFILE:
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
                        Log.wtf(TAG, "volley error: " + error.getMessage());
                    }
                });
        MusicApplication.getInstance().addToRequestQueue(jsonRequest);

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
                        Log.wtf(TAG, "volley error: " + error.getMessage());
                    }

                });

        MusicApplication.getInstance().addToRequestQueue(jsonRequest);
    }

    private void parseSendBroadcastByAction(final JSONObject response, String action) {
        switch (action) {
            case ACTION_GET_AUDIO:
                sendBroadcastAudio(JSONHelper.getVKMusicFiles(response));
                break;
            case ACTION_USER_PROFILE:
                sendBroadcastUserProfile(JSONHelper.getUserProfile(response));
                break;
        }
    }

    private void sendBroadcastAudio(ArrayList<VKMusicFile> vkMusicFiles) {
        Intent intent = new Intent();
        intent.setAction(ACTION_AUDIO_RESULT);
        intent.putExtra(EXTRA_AUDIO_RESULT, vkMusicFiles);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendBroadcastUserProfile(User user) {
        Intent intent = new Intent();
        intent.setAction(ACTION_USER_PROFILE_RESULT);
        intent.putExtra(EXTRA_USER_PROFILE_RESULT, user);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MusicApplication.getInstance().cancelPendingRequests();
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
