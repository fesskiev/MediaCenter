package com.fesskiev.player.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.fesskiev.player.MusicApplication;
import com.fesskiev.player.model.MusicFile;
import com.fesskiev.player.model.MusicFolder;

import java.io.File;


public class FetchAudioInfoIntentService extends IntentService {

    private static final String TAG = FetchAudioInfoIntentService.class.getSimpleName();

    private static final String ACTION_START_FETCH_AUDIO_INFO_SERVICE =
            "com.fesskiev.player.action.ACTION_START_FETCH_AUDIO_INFO_SERVICE";
    public static final String ACTION_MUSIC_FILES_RESULT = "com.fesskiev.player.action.MUSIC_FILES_RESULT";



    public FetchAudioInfoIntentService() {
        super(FetchAudioInfoIntentService.class.getSimpleName());
    }


    public static void startFetchMp3Info(Context context) {
        Intent intent = new Intent(context, FetchAudioInfoIntentService.class);
        intent.setAction(ACTION_START_FETCH_AUDIO_INFO_SERVICE);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_FETCH_AUDIO_INFO_SERVICE.equals(action)) {
                    fetchMp3Info();
            }
        }
    }

    private void fetchMp3Info() {
        MusicFolder musicFolder = MusicApplication.getInstance().getMusicPlayer().currentMusicFolder;
        if (musicFolder != null) {
            for (File file : musicFolder.musicFiles) {
                MusicFile musicFile = new MusicFile(getApplicationContext(), file.getAbsolutePath(),
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

    private void sendMusicFilesBroadcast() {
        Intent intent = new Intent(ACTION_MUSIC_FILES_RESULT);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
