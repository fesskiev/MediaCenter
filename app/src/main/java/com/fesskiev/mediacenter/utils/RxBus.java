package com.fesskiev.mediacenter.utils;


import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.services.PlaybackService;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public final class RxBus {

    private final PublishSubject<PlaybackService> busPlayback = PublishSubject.create();
    private final PublishSubject<FileSystemService> busFileSystem = PublishSubject.create();

    private final PublishSubject<List<AudioFile>> busCurrentTrackList = PublishSubject.create();
    private final PublishSubject<AudioFile> busCurrentTrack = PublishSubject.create();

    private final PublishSubject<VideoFile> busCurrentVideoFile = PublishSubject.create();

    public void sendFileSystemEvent(final FileSystemService event) {
        if (busFileSystem.hasObservers()) {
            busFileSystem.onNext(event);
        }
    }

    public void sendPlaybackEvent(final PlaybackService event) {
        if (busPlayback.hasObservers()) {
            busPlayback.onNext(event);
        }
    }

    public void sendCurrentVideoFileEvent(final VideoFile event) {
        if (busCurrentVideoFile.hasObservers()) {
            busCurrentVideoFile.onNext(event);
        }
    }

    public void sendCurrentTrackEvent(final AudioFile event) {
        if (busCurrentTrack.hasObservers()) {
            busCurrentTrack.onNext(event);
        }
    }

    public void sendCurrentTrackListEvent(final List<AudioFile> event) {
        if (busCurrentTrackList.hasObservers()) {
            busCurrentTrackList.onNext(event);
        }
    }

    public Observable<PlaybackService> toPlaybackObservable() {
        return busPlayback;
    }

    public Observable<FileSystemService> toFileSystemObservable() {
        return busFileSystem;
    }

    public Observable<AudioFile> toCurrentTrackObservable() {
        return busCurrentTrack;
    }

    public Observable<VideoFile> toCurrentVideoFileObservable() {
        return busCurrentVideoFile;
    }

    public Observable<List<AudioFile>> toCurrentTrackListObservable() {
        return busCurrentTrackList;
    }
}