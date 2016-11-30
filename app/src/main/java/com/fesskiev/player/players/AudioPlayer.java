package com.fesskiev.player.players;



import android.content.Context;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.data.model.AudioFile;
import com.fesskiev.player.data.model.AudioFolder;
import com.fesskiev.player.data.model.MediaFile;
import com.fesskiev.player.data.source.DataRepository;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.ui.playback.Playable;

import org.greenrobot.eventbus.EventBus;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AudioPlayer implements Playable {


    private Context context;
    private DataRepository repository;

    private LinkedList<AudioFile> currentTrackList;
    private ListIterator<AudioFile> listIterator;
    private AudioFile currentTrack;


    public AudioPlayer(DataRepository repository) {
        this.repository = repository;

        context = MediaApplication.getInstance().getApplicationContext();
    }

    public void getCurrentTrackAndTrackList() {

        Observable.zip(repository.getSelectedFolderAudioFiles(),
                repository.getSelectedAudioFile(), (audioFiles, audioFile) -> {
                    currentTrack = audioFile;
                    currentTrackList = new LinkedList<>(audioFiles);
                    listIterator = currentTrackList.listIterator();

                    EventBus.getDefault().post(this);

                    return Observable.empty();
                }).first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public void open(MediaFile audioFile) {
        if (audioFile == null) {
            getCurrentAudioFile()
                    .first()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(currentAudioFile -> {
                        if (currentAudioFile != null) {
                            PlaybackService.openFile(context, currentAudioFile.getFilePath());

                            currentTrack = currentAudioFile;
                            EventBus.getDefault().post(AudioPlayer.this);

                        }
                    });
        } else {
            PlaybackService.openFile(context, audioFile.getFilePath());

            currentTrack = (AudioFile) audioFile;
            EventBus.getDefault().post(AudioPlayer.this);
        }
    }


    @Override
    public void play() {
        PlaybackService.startPlayback(context);
    }

    @Override
    public void pause() {
        PlaybackService.stopPlayback(context);
    }

    @Override
    public void next() {
        if (listIterator.hasNext()) {
            AudioFile audioFile = listIterator.next();
            if (audioFile != null) {
                repository.updateSelectedAudioFile(audioFile);

                currentTrack = audioFile;
                EventBus.getDefault().post(AudioPlayer.this);

                PlaybackService.openFile(context, audioFile.getFilePath());
                PlaybackService.startPlayback(context);
            }
        }
    }

    @Override
    public void previous() {
        if (listIterator.hasPrevious()) {
            AudioFile audioFile = listIterator.previous();
            if (audioFile != null) {
                repository.updateSelectedAudioFile(audioFile);

                currentTrack = audioFile;
                EventBus.getDefault().post(AudioPlayer.this);

                PlaybackService.openFile(context, audioFile.getFilePath());
                PlaybackService.startPlayback(context);
            }
        }
    }


    public void setCurrentAudioFile(AudioFile audioFile) {
        currentTrack = audioFile;
        repository.updateSelectedAudioFile(audioFile);
    }


    public void setCurrentTrackList(AudioFolder audioFolder) {
        repository.updateSelectedAudioFolder(audioFolder);
        repository.getSelectedFolderAudioFiles()
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioFiles -> {
                    if (audioFiles != null) {
                        currentTrackList = new LinkedList<>(audioFiles);
                        listIterator = currentTrackList.listIterator();

                        EventBus.getDefault().post(AudioPlayer.this);
                    }
                });
    }

    public Observable<AudioFile> getCurrentAudioFile() {
        return repository.getSelectedAudioFile();
    }

    public Observable<AudioFolder> getCurrentAudioFolder() {
        return repository.getSelectedAudioFolder();
    }

    public List<AudioFile> getCurrentTrackList() {
        return currentTrackList;
    }

    public AudioFile getCurrentTrack() {
        return currentTrack;
    }


}
