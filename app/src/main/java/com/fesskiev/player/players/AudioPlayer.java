package com.fesskiev.player.players;


import android.content.Context;
import android.util.Log;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.data.model.AudioFile;
import com.fesskiev.player.data.model.AudioFolder;
import com.fesskiev.player.data.model.MediaFile;
import com.fesskiev.player.data.source.DataRepository;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.ui.playback.Playable;

import org.greenrobot.eventbus.EventBus;

import java.util.LinkedList;
import java.util.ListIterator;

import rx.Observable;
import rx.schedulers.Schedulers;

public class AudioPlayer implements Playable {


    private Context context;
    private DataRepository repository;

    private TrackListIterator<AudioFile> listIterator;

    private LinkedList<AudioFile> currentTrackList;
    private AudioFile currentTrack;


    public AudioPlayer(DataRepository repository) {
        this.repository = repository;

        context = MediaApplication.getInstance().getApplicationContext();
    }

    public void getCurrentTrackAndTrackList() {

        Observable.zip(repository.getSelectedFolderAudioFiles(),
                repository.getSelectedAudioFile(), (audioFiles, audioFile) -> {

                    if (audioFiles != null) {
                        currentTrackList = new LinkedList<>(audioFiles);
                        listIterator = new TrackListIterator<>(currentTrackList.listIterator());
                        EventBus.getDefault().post(currentTrackList);
                    }

                    if (audioFile != null) {
                        currentTrack = audioFile;
                        PlaybackService.openFile(context, currentTrack.getFilePath());
                        EventBus.getDefault().post(currentTrack);
                    }

                    return Observable.empty();
                })
                .first()
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    @Override
    public void open(MediaFile audioFile) {
        if (audioFile == null) {
            getCurrentAudioFile()
                    .first()
                    .subscribeOn(Schedulers.io())
                    .subscribe(currentAudioFile -> {
                        if (currentAudioFile != null) {
                            PlaybackService.openFile(context, currentAudioFile.getFilePath());

                            currentTrack = currentAudioFile;
                            EventBus.getDefault().post(currentTrack);
                        }
                    });
        } else {
            PlaybackService.openFile(context, audioFile.getFilePath());
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
                Log.d("state", "NEXT: " + audioFile.toString());
                currentTrack = audioFile;

                audioFile.isSelected = true;
                repository.updateSelectedAudioFile(audioFile);

                EventBus.getDefault().post(currentTrack);

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
                Log.d("state", "PREV: " + audioFile.toString());
                currentTrack = audioFile;

                audioFile.isSelected = true;
                repository.updateSelectedAudioFile(audioFile);

                EventBus.getDefault().post(currentTrack);

                PlaybackService.openFile(context, audioFile.getFilePath());
                PlaybackService.startPlayback(context);
            }
        }
    }


    public void setCurrentAudioFileAndPlay(AudioFile audioFile) {
        currentTrack = audioFile;

        audioFile.isSelected = true;
        repository.updateSelectedAudioFile(audioFile);

        open(audioFile);
        play();

        EventBus.getDefault().post(currentTrack);
    }


    public void setCurrentTrackList(AudioFolder audioFolder) {
        audioFolder.isSelected = true;
        repository.updateSelectedAudioFolder(audioFolder);
        repository.getSelectedFolderAudioFiles()
                .first()
                .subscribeOn(Schedulers.io())
                .subscribe(audioFiles -> {
                    if (audioFiles != null) {
                        currentTrackList = new LinkedList<>(audioFiles);
                        listIterator = new TrackListIterator<>(currentTrackList.listIterator());

                        EventBus.getDefault().post(currentTrackList);
                    }
                });
    }

    public Observable<AudioFile> getCurrentAudioFile() {
        return repository.getSelectedAudioFile();
    }

    public Observable<AudioFolder> getCurrentAudioFolder() {
        return repository.getSelectedAudioFolder();
    }

    public AudioFile getCurrentTrack() {
        return currentTrack;
    }

    private static class TrackListIterator<T> {

        private final ListIterator<T> listIterator;

        private boolean nextWasCalled = false;
        private boolean previousWasCalled = false;

        public TrackListIterator(ListIterator<T> listIterator) {
            this.listIterator = listIterator;
        }

        public T next() {
            nextWasCalled = true;
            if (previousWasCalled) {
                previousWasCalled = false;
                listIterator.next();
            }
            return listIterator.next();
        }

        public T previous() {
            if (nextWasCalled) {
                listIterator.previous();
                nextWasCalled = false;
            }
            previousWasCalled = true;
            return listIterator.previous();
        }

        public boolean hasPrevious() {
            return true;
        }

        public boolean hasNext() {
            return true;
        }
    }

}
