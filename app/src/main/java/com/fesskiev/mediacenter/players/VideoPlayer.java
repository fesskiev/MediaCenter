package com.fesskiev.mediacenter.players;


import com.fesskiev.mediacenter.data.model.MediaFile;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.ui.playback.Playable;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.ListIterator;

public class VideoPlayer implements Playable {


    private VideoFilesIterator videoFilesIterator;
    private List<VideoFile> videoFiles;
    private VideoFile currentVideoFile;
    private int position;

    public VideoPlayer() {
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
                EventBus.getDefault().post(currentVideoFile);
            }
        }
    }

    @Override
    public void previous() {
        if (videoFilesIterator.hasPrevious()) {
            VideoFile videoFile = videoFilesIterator.previous();
            if (videoFile != null) {
                currentVideoFile = videoFile;
                EventBus.getDefault().post(currentVideoFile);
            }
        }
    }

    @Override
    public boolean first() {
        return videoFilesIterator.firstVideo();
    }

    @Override
    public boolean last() {
        return videoFilesIterator.lastVideo();
    }

    public void setVideoFiles(List<VideoFile> videoFiles) {
        this.videoFiles = videoFiles;
    }

    public void setCurrentVideoFile(VideoFile videoFile) {
        this.currentVideoFile = videoFile;
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
            return videoFiles.get(position);
        }

        @Override
        public VideoFile previous() {
            previousIndex();
            return videoFiles.get(position);
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
            return position == 0;
        }

        private boolean lastVideo() {
            return position == (videoFiles.size() - 1);
        }
    }
}
