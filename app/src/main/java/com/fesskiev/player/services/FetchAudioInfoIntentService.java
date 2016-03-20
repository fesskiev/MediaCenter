package com.fesskiev.player.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioFolder;

import java.io.File;


public class FetchAudioInfoIntentService extends IntentService {

    private static final String TAG = FetchAudioInfoIntentService.class.getSimpleName();

    private static final String ACTION_START_FETCH_AUDIO_INFO_SERVICE =
            "com.fesskiev.player.action.ACTION_START_FETCH_AUDIO_INFO_SERVICE";
    public static final String ACTION_MUSIC_FILES_RESULT = "com.fesskiev.player.action.MUSIC_FILES_RESULT";



    public FetchAudioInfoIntentService() {
        super(FetchAudioInfoIntentService.class.getSimpleName());
    }


    public static void startFetchAudioInfo(Context context) {
        Intent intent = new Intent(context, FetchAudioInfoIntentService.class);
        intent.setAction(ACTION_START_FETCH_AUDIO_INFO_SERVICE);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_FETCH_AUDIO_INFO_SERVICE.equals(action)) {
                    fetchAudioInfo();
            }
        }
    }

    private void fetchAudioInfo() {
        AudioFolder audioFolder = MediaApplication.getInstance().getAudioPlayer().currentAudioFolder;
        if (audioFolder != null) {
            for (File file : audioFolder.musicFiles) {
                AudioFile audioFile = new AudioFile(getApplicationContext(), file,
                        new AudioFile.OnMp3TagListener() {
                    @Override
                    public void onFetchCompleted() {
                        sendMusicFilesBroadcast();
                    }
                });
                audioFolder.audioFilesDescription.add(audioFile);
            }
        }
    }

    private void sendMusicFilesBroadcast() {
        Intent intent = new Intent(ACTION_MUSIC_FILES_RESULT);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
