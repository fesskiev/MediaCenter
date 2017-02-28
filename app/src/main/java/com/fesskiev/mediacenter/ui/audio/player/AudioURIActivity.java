package com.fesskiev.mediacenter.ui.audio.player;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.utils.RxUtils;

import java.io.File;

import rx.Observable;
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

                DataRepository repository = MediaApplication.getInstance().getRepository();

                subscription = Observable.just(repository.containAudioTrack(uri.getPath()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(contain -> {
                            if (contain) {
                                return repository.getAudioFileByPath(uri.getPath());
                            }
                            return Observable.just(parseAudioFile(uri.getPath()));
                        })
                        .first()
                        .subscribe(this::openPlayURIAudioFile);

            }
        }
    }

    private AudioFile parseAudioFile(String path) {
        return new AudioFile(getApplicationContext(), new File(path), "",
                audioFile -> FileSystemService.startFetchAudioByPath(getApplicationContext(), path));
    }

    private void openPlayURIAudioFile(AudioFile audioFile) {
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
