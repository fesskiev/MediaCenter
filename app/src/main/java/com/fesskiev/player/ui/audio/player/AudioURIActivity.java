package com.fesskiev.player.ui.audio.player;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.players.AudioPlayer;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.utils.AppLog;
import com.fesskiev.player.utils.RxUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AudioURIActivity extends AudioPlayerActivity {

    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_VIEW.equals(action) && type != null) {
            if (type.startsWith("audio/")) {
                Uri uri = intent.getData();
                subscription = MediaApplication.
                        getInstance().
                        getRepository().
                        getAudioFileByPath(uri.getPath())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(audioFile -> {
                            AppLog.VERBOSE("Audio file: " + audioFile);
                            AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();
                            if (audioPlayer != null) {
                                PlaybackService.startPlaybackService(getApplicationContext());
                                PlaybackService.openFile(getApplicationContext(), audioFile.getFilePath());
                                PlaybackService.startPlayback(getApplicationContext());

                                RxUtils.unsubscribe(subscription);
                            }
                        });

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
        PlaybackService.stopPlayback(getApplicationContext());
        resetAudioPlayer();
    }

    private void resetAudioPlayer() {
        AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        audioPlayer.resetAudioPlayer();
    }
}
