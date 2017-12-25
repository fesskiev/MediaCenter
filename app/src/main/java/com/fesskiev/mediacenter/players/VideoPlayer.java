package com.fesskiev.mediacenter.players;


import com.fesskiev.mediacenter.data.model.MediaFile;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.VideoFolder;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.ui.Playable;
import com.fesskiev.mediacenter.utils.RxBus;
import com.fesskiev.mediacenter.utils.schedulers.SchedulerProvider;

import java.util.List;
import java.util.ListIterator;

public class VideoPlayer implements Playable {

    private RxBus rxBus;
    private DataRepository repository;
    private SchedulerProvider provider;

    private VideoFilesIterator videoFilesIterator;
    private List<VideoFile> currentVideoFiles;
    private VideoFile currentVideoFile;
    private int position;


    public VideoPlayer(RxBus rxBus, DataRepository repository, SchedulerProvider schedulerProvider) {
        this.rxBus = rxBus;
        this.repository = repository;
        this.provider = schedulerProvider;
        videoFilesIterator = new VideoFilesIterator();
    }


    @Override
    public void open(MediaFile mediaFile) {

    }

    @Override
    public void play() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void next() {
        if (videoFilesIterator.hasNext()) {
            VideoFile videoFile = videoFilesIterator.next();
            if (videoFile != null) {
                currentVideoFile = videoFile;
                videoFile.isSelected = true;
                repository.updateSelectedVideoFile(videoFile)
                        .subscribeOn(provider.computation())
                        .observeOn(provider.ui())
                        .subscribe(Void -> notifyCurrentVideoFile());
            }
        }
    }

    @Override
    public void previous() {
        if (videoFilesIterator.hasPrevious()) {
            VideoFile videoFile = videoFilesIterator.previous();
            if (videoFile != null) {
                currentVideoFile = videoFile;
                videoFile.isSelected = true;
                repository.updateSelectedVideoFile(videoFile)
                        .subscribeOn(provider.computation())
                        .observeOn(provider.ui())
                        .subscribe(Void -> notifyCurrentVideoFile());
            }
        }
    }

    private void notifyCurrentVideoFile() {
        rxBus.sendCurrentVideoFileEvent(currentVideoFile);
    }

    private void notifyCurrentVideoFiles() {
        rxBus.sendCurrentVideoFilesEvent(currentVideoFiles);
    }

    @Override
    public boolean first() {
        return videoFilesIterator.firstVideo();
    }

    @Override
    public boolean last() {
        return videoFilesIterator.lastVideo();
    }

    public void setCurrentVideoFiles(List<VideoFile> currentVideoFiles) {
        this.currentVideoFiles = currentVideoFiles;
    }

    public void setCurrentVideoFile(VideoFile videoFile) {
        this.currentVideoFile = videoFile;
        videoFilesIterator.findPosition();
    }

    public void updateCurrentVideoFile(VideoFile videoFile) {
        currentVideoFile = videoFile;
        videoFilesIterator.findPosition();

        videoFile.isSelected = true;
        repository.updateSelectedVideoFile(videoFile)
                .subscribeOn(provider.computation())
                .observeOn(provider.ui())
                .subscribe(Void -> notifyCurrentVideoFile());
    }

    public void updateCurrentVideoFolders(VideoFolder videoFolder, List<VideoFile> videoFiles) {
        if (videoFiles != null) {
            currentVideoFiles = videoFiles;
        }
        if (videoFolder != null) {
            videoFolder.isSelected = true;
            repository.updateSelectedVideoFolder(videoFolder)
                    .subscribeOn(provider.computation())
                    .observeOn(provider.ui())
                    .subscribe(Void -> notifyCurrentVideoFiles());
        }
    }

    private class VideoFilesIterator implements ListIterator<VideoFile> {

        public VideoFilesIterator() {
            position = -1;
        }

        @Override
        public boolean hasNext() {
            return !lastVideo();
        }

        @Override
        public boolean hasPrevious() {
            return !firstVideo();
        }

        @Override
        public VideoFile next() {
            nextIndex();
            return currentVideoFiles.get(position);
        }

        @Override
        public VideoFile previous() {
            previousIndex();
            return currentVideoFiles.get(position);
        }

        @Override
        public int nextIndex() {
            return position++;
        }

        @Override
        public int previousIndex() {
            return position--;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(VideoFile videoFile) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(VideoFile videoFile) {
            throw new UnsupportedOperationException();
        }

        private boolean firstVideo() {
            if (currentVideoFiles == null) {
                return true;
            }
            return position == 0;
        }

        private boolean lastVideo() {
            if (currentVideoFiles == null) {
                return true;
            }
            return position == (currentVideoFiles.size() - 1);
        }

        public void findPosition() {
            if (currentVideoFiles != null && currentVideoFiles.contains(currentVideoFile)) {
                position = currentVideoFiles.indexOf(currentVideoFile);
            }
        }
    }

    public VideoFile getCurrentVideoFile() {
        return currentVideoFile;
    }
}
