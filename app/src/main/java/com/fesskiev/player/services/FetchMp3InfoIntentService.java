package com.fesskiev.player.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fesskiev.player.MusicApplication;
import com.fesskiev.player.model.MusicFile;
import com.fesskiev.player.model.MusicFolder;

import java.io.File;
import java.util.List;


public class FetchMp3InfoIntentService extends IntentService {

    private static final String TAG = FetchMp3InfoIntentService.class.getSimpleName();

    private static final String ACTION_START_FETCH_MP3_INFO_SERVICE =
            "com.fesskiev.player.action.ACTION_START_FETCH_MP3_INFO_SERVICE";
    public static final String ACTION_MUSIC_FILES = "com.fesskiev.player.action.MUSIC_FILES";

    public static final String EXTRA_MUSIC_FOLDER_POSITION
            = "com.fesskiev.player.extra.MUSIC_FOLDER_POSITION ";


    public FetchMp3InfoIntentService() {
        super(FetchMp3InfoIntentService.class.getSimpleName());
    }


    public static void startFetchMp3Info(Context context, int position) {
        Intent intent = new Intent(context, FetchMp3InfoIntentService.class);
        intent.setAction(ACTION_START_FETCH_MP3_INFO_SERVICE);
        intent.putExtra(EXTRA_MUSIC_FOLDER_POSITION, position);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_FETCH_MP3_INFO_SERVICE.equals(action)) {
                int position = intent.getIntExtra(EXTRA_MUSIC_FOLDER_POSITION, -1);
                if (position != -1) {
                    Log.d(TAG, "fetch mp3 info");
                    fetchMp3Info(position);
                }
            }
        }
    }

    private void fetchMp3Info(int position) {
        MusicFolder musicFolder =
                ((MusicApplication) getApplication()).getMusicFolders().get(position);
        if (musicFolder != null) {
            for (File file : musicFolder.musicFiles) {
                MusicFile musicFile = new MusicFile(file.getAbsolutePath(),
                        new MusicFile.OnMp3TagListener() {
                    @Override
                    public void onFetchCompleted() {
                        sendMusicFilesBroadcast();
                    }
                });
                musicFolder.musicFilesDescription.add(musicFile);
            }
        }
    }

    public void sendMusicFilesBroadcast() {
        Intent intent = new Intent(ACTION_MUSIC_FILES);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
