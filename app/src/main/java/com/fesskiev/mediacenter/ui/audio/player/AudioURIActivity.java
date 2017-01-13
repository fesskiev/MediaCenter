package com.fesskiev.mediacenter.ui.audio.player;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.RxUtils;

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
                subscription = MediaApplication.getInstance().getRepository()
                        .getAudioFileByPath(uri.getPath())
                        .first()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::openPlayURIAudioFile);

            }
        }
    }

    private void openPlayURIAudioFile(AudioFile audioFile) {
        AppLog.VERBOSE("Audio file: " + audioFile);
        AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        if (audioPlayer != null) {
            audioPlayer.setCurrentAudioFileAndPlay(audioFile);
            PlaybackService.startPlaybackService(getApplicationContext());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
        PlaybackService.stopPlayback(getApplicationContext());
    }

}
