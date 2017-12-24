package com.fesskiev.mediacenter.players;


import android.content.Context;
import android.util.Log;

import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.MediaFile;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.services.AudioPlaybackService;
import com.fesskiev.mediacenter.ui.Playable;
import com.fesskiev.mediacenter.utils.RxBus;
import com.fesskiev.mediacenter.utils.comparators.SortByDuration;
import com.fesskiev.mediacenter.utils.comparators.SortByFileSize;
import com.fesskiev.mediacenter.utils.comparators.SortByTimestamp;
import com.fesskiev.mediacenter.utils.ffmpeg.FFmpegHelper;
import com.fesskiev.mediacenter.utils.schedulers.SchedulerProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

public class AudioPlayer implements Playable {

    private static final String TAG = AudioPlayer.class.getSimpleName();

    public static final int SORT_DURATION = 0;
    public static final int SORT_FILE_SIZE = 1;
    public static final int SORT_TRACK_NUMBER = 2;
    public static final int SORT_TIMESTAMP = 3;

    private Context context;
    private DataRepository repository;
    private RxBus rxBus;
    private SchedulerProvider provider;
    private FFmpegHelper fFmpegHelper;


    private TrackListIterator trackListIterator;
    private List<AudioFile> currentTrackList;
    private AudioFile currentTrack;
    private int position;


    public AudioPlayer(Context context, RxBus rxBus, DataRepository repository, SchedulerProvider provider,
                       FFmpegHelper fFmpegHelper) {
        this.context = context;
        this.rxBus = rxBus;
        this.repository = repository;
        this.provider = provider;
        this.fFmpegHelper = fFmpegHelper;
        trackListIterator = new TrackListIterator();
    }

    @Override
    public void open(MediaFile audioFile) {
        if (audioFile != null) {
            AudioPlaybackService.openFile(context, audioFile.getFilePath());
        }
    }

    @Override
    public void play() {
        AudioPlaybackService.startPlayback(context);
    }

    @Override
    public void pause() {
        AudioPlaybackService.stopPlayback(context);
    }

    @Override
    public void next() {
        if (trackListIterator.hasNext()) {
            AudioFile audioFile = trackListIterator.next();
            if (audioFile != null) {
                currentTrack = audioFile;

                audioFile.isSelected = true;
                repository.updateSelectedAudioFile(audioFile)
                        .subscribeOn(provider.computation())
                        .observeOn(provider.ui())
                        .subscribe(Void -> {
                            openAudioFile();
                            notifyCurrentTrack();
                        });
            }
        }
    }

    @Override
    public void previous() {
        if (trackListIterator.hasPrevious()) {
            AudioFile audioFile = trackListIterator.previous();
            if (audioFile != null) {
                currentTrack = audioFile;

                audioFile.isSelected = true;
                repository.updateSelectedAudioFile(audioFile)
                        .subscribeOn(provider.computation())
                        .observeOn(provider.ui())
                        .subscribe(Void -> {
                            openAudioFile();
                            notifyCurrentTrack();
                        });
            }
        }
    }

    @Override
    public boolean first() {
        return trackListIterator.firstTrack();
    }

    @Override
    public boolean last() {
        return trackListIterator.lastTrack();
    }


    public void openAudioFile() {
        Log.e(TAG, AudioPlayer.this.toString());
        if (fFmpegHelper.isCommandRunning()) {
            fFmpegHelper.killRunningProcesses();
        }
        if (FFmpegHelper.isAudioFileFLAC(currentTrack)) {
            pause();
            fFmpegHelper.convertAudioIfNeed(currentTrack, new FFmpegHelper.OnConvertProcessListener() {

                @Override
                public void onStart() {
                    Log.e(TAG, "onStart() convert");
                    AudioPlaybackService.startConvert(context);
                }

                @Override
                public void onSuccess(AudioFile audioFile) {
                    Log.e(TAG, "onSuccess convert");
                    open(currentTrack);
                    play();
                }

                @Override
                public void onFailure(Exception error) {
                    Log.e(TAG, "onFailure: " + error.getMessage());
                }
            });
        } else {
            open(currentTrack);
            play();
        }
    }

    public void setCurrentAudioFile(AudioFile audioFile) {
        currentTrack = audioFile;
        trackListIterator.findPosition();
    }

    public void setCurrentTrackList(List<AudioFile> audioFiles) {
        currentTrackList = audioFiles;
        trackListIterator.findPosition();
    }

    public void setCurrentAudioFileAndPlay(AudioFile audioFile) {
        currentTrack = audioFile;
        trackListIterator.findPosition();

        audioFile.isSelected = true;
        repository.updateSelectedAudioFile(audioFile)
                .subscribeOn(provider.computation())
                .observeOn(provider.ui())
                .subscribe(Void -> {
                    openAudioFile();
                    notifyCurrentTrack();
                });
    }

    public void setCurrentTrackList(AudioFolder audioFolder, List<AudioFile> audioFiles) {
        if (audioFiles != null) {
            currentTrackList = audioFiles;
        }
        if (audioFolder != null) {
            audioFolder.isSelected = true;
            repository.updateSelectedAudioFolder(audioFolder)
                    .subscribeOn(provider.computation())
                    .observeOn(provider.ui())
                    .subscribe(Void -> notifyCurrentTrackList());
        }
    }

    public void setSortingTrackList(List<AudioFile> audioFiles) {
        if (audioFiles == null || audioFiles.isEmpty()) {
            return;
        }
        if (isCurrentTrackList(audioFiles)) {
            currentTrackList = audioFiles;
            trackListIterator.findPosition();
        }
    }

    public boolean isCurrentTrackList(List<AudioFile> audioFiles) {
        return currentTrackList != null && currentTrackList.containsAll(audioFiles);
    }

    public AudioFile getCurrentTrack() {
        return currentTrack;
    }


    public List<AudioFile> getCurrentTrackList() {
        return currentTrackList;
    }

    public boolean isDeletedFolderSelect(AudioFolder audioFolder) {
        if (currentTrack != null && currentTrack.folderId.equals(audioFolder.id)) {
            return true;
        }
        if (currentTrackList != null) {
            for (AudioFile audioFile : currentTrackList) {
                if (audioFile.folderId.equals(audioFolder.id)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void nextAfterEnd() {
        if (!last()) {
            next();
        }
    }

    private void notifyCurrentTrack() {
        rxBus.sendCurrentTrackEvent(currentTrack);
    }

    private void notifyCurrentTrackList() {
        rxBus.sendCurrentTrackListEvent(currentTrackList);
    }

    public static List<AudioFile> sortAudioFiles(int type, List<AudioFile> unsortedList) {
        List<AudioFile> sortedList = new ArrayList<>(unsortedList);
        switch (type) {
            case SORT_DURATION:
                Collections.sort(sortedList, new SortByDuration());
                break;
            case SORT_FILE_SIZE:
                Collections.sort(sortedList, new SortByFileSize());
                break;
            case SORT_TIMESTAMP:
                Collections.sort(sortedList, new SortByTimestamp());
                break;
            case SORT_TRACK_NUMBER:
                Collections.sort(sortedList);
                break;
        }
        return sortedList;
    }

    public void playTrackByTitle(String title) {
        if (currentTrackList != null) {
            for (AudioFile audioFile : currentTrackList) {
                if (audioFile.title.equals(title)) {
                    setCurrentAudioFileAndPlay(audioFile);
                }
            }
        }
    }


    private class TrackListIterator implements ListIterator<AudioFile> {

        public TrackListIterator() {
            position = -1;
        }

        @Override
        public boolean hasNext() {
            return !lastTrack();
        }

        @Override
        public boolean hasPrevious() {
            return !firstTrack();
        }

        @Override
        public AudioFile next() {
            nextIndex();
            try {
                return currentTrackList.get(position);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public AudioFile previous() {
            previousIndex();
            try {
                return currentTrackList.get(position);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public int nextIndex() {
            return position++;
        }

        @Override
        public int previousIndex() {
            return position--;
        }

        public boolean lastTrack() {
            if (currentTrackList == null) {
                return true;
            }
            return position == (currentTrackList.size() - 1);
        }

        public boolean firstTrack() {
            if (currentTrackList == null) {
                return true;
            }
            return position == 0;
        }

        public void findPosition() {
            if (currentTrackList != null && currentTrackList.contains(currentTrack)) {
                position = currentTrackList.indexOf(currentTrack);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(AudioFile audioFile) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(AudioFile audioFile) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String toString() {
        return "AudioPlayer{" +
                "currentTrackList=" + "\n" + printCurrentTrackList() +
                "currentTrack=" + currentTrack + "\n" +
                "position=" + position +
                '}';
    }

    private String printCurrentTrackList() {
        if (currentTrackList != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < currentTrackList.size(); i++) {
                AudioFile audioFile = currentTrackList.get(i);
                sb.append(String.format(Locale.getDefault(), "%d. %s", i, audioFile.getFileName()));
                sb.append("\n");
            }
            return sb.toString();
        }
        return null;
    }

}
