package com.fesskiev.mediacenter.ui.audio.player;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.utils.RxUtils;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AudioURIActivity extends AudioPlayerActivity {

    private DataRepository repository;
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

                repository = MediaApplication.getInstance().getRepository();

                subscription = Observable.just(repository.containAudioTrack(uri.getPath()))
                        .subscribeOn(Schedulers.io())
                        .flatMap(contain -> {
                            if (contain) {
                                return repository.getAudioFileByPath(uri.getPath());
                            }
                            return parseAudioFile(uri.getPath());
                        }).doOnNext(audioFile -> {
                            disablePreviousTrackButton();
                            disableNextTrackButton();
                        })
                        .first()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::openPlayURIAudioFile);

            }
        }
    }

    private Observable<AudioFile> parseAudioFile(String path) {
        return repository.getAudioFolders()
                .flatMap(audioFolders -> Observable.just(getAudioFolderByPath(audioFolders, new File(path).getParent())))
                .flatMap(audioFolder -> Observable.just(new AudioFile(getApplicationContext(), new File(path),
                        audioFolder == null ? "" : audioFolder.id, null)))
                .flatMap(audioFile -> {
                    if (!TextUtils.isEmpty(audioFile.id)) {

                        repository.insertAudioFile(audioFile);

                        repository.getMemorySource().setCacheArtistsDirty(true);
                        repository.getMemorySource().setCacheGenresDirty(true);
                        repository.getMemorySource().setCacheFoldersDirty(true);
                    }
                    return Observable.just(audioFile);
                });


    }

    private AudioFolder getAudioFolderByPath(List<AudioFolder> audioFolders, String path) {
        for (AudioFolder audioFolder : audioFolders) {
            if (audioFolder.folderPath.getAbsolutePath().equals(path)) {
                return audioFolder;
            }
        }
        return null;
    }

    private void openPlayURIAudioFile(AudioFile audioFile) {
        AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        if (audioPlayer != null && audioFile != null) {
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
