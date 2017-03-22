package com.fesskiev.mediacenter.players;


import android.content.Context;
import android.util.Log;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.MediaFile;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.ui.playback.Playable;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import rx.Observable;
import rx.schedulers.Schedulers;

public class AudioPlayer implements Playable {

    private static final String TAG = AudioPlayer.class.getSimpleName();


    private Context context;
    private DataRepository repository;

    private TrackListIterator trackListIterator;
    private List<AudioFile> currentTrackList;
    private AudioFile currentTrack;
    private int position;


    public AudioPlayer(DataRepository repository) {
        this.repository = repository;

        context = MediaApplication.getInstance().getApplicationContext();
        trackListIterator = new TrackListIterator();
    }

    public void getCurrentTrackAndTrackList() {

        Observable.zip(repository.getSelectedFolderAudioFiles(),
                repository.getSelectedAudioFile(), (audioFiles, audioFile) -> {

                    if (audioFiles != null) {
                        currentTrackList = audioFiles;
                        EventBus.getDefault().post(currentTrackList);
                    }

                    if (audioFile != null) {
                        currentTrack = audioFile;
                        PlaybackService.openFile(context, currentTrack.getFilePath());
                        EventBus.getDefault().post(currentTrack);
                    }

                    if (audioFiles != null && audioFile != null) {
                        trackListIterator.findPosition();
                    }

                    return Observable.empty();
                })
                .first()
                .subscribeOn(Schedulers.io())
                .subscribe(object -> Log.e(TAG, AudioPlayer.this.toString()));
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

        Log.e(TAG, AudioPlayer.this.toString());
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
        if (trackListIterator.hasNext()) {
            AudioFile audioFile = trackListIterator.next();
            if (audioFile != null) {
                currentTrack = audioFile;

                audioFile.isSelected = true;
                repository.updateSelectedAudioFile(audioFile);

                EventBus.getDefault().post(currentTrack);

                PlaybackService.openFile(context, audioFile.getFilePath());
                PlaybackService.startPlayback(context);

                Log.e(TAG, AudioPlayer.this.toString());
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
                repository.updateSelectedAudioFile(audioFile);

                EventBus.getDefault().post(currentTrack);

                PlaybackService.openFile(context, audioFile.getFilePath());
                PlaybackService.startPlayback(context);

                Log.e(TAG, AudioPlayer.this.toString());
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

    public void setCurrentAudioFileAndPlay(AudioFile audioFile) {
        currentTrack = audioFile;

        trackListIterator.findPosition();

        audioFile.isSelected = true;
        repository.updateSelectedAudioFile(audioFile);

        open(audioFile);
        play();

        EventBus.getDefault().post(currentTrack);

        Log.e(TAG, AudioPlayer.this.toString());
    }


    public void setCurrentTrackList(AudioFolder audioFolder, List<AudioFile> audioFiles) {
        currentTrackList = audioFiles;

        audioFolder.isSelected = true;
        repository.updateSelectedAudioFolder(audioFolder);

        EventBus.getDefault().post(currentTrackList);

        Log.e(TAG, AudioPlayer.this.toString());
    }

    public void setCurrentTrackList(List<AudioFile> audioFiles) {
        currentTrackList = audioFiles;

        EventBus.getDefault().post(currentTrackList);

        Log.e(TAG, AudioPlayer.this.toString());
    }

    public void setSortingTrackList(List<AudioFile> audioFiles) {
        currentTrackList = audioFiles;
        trackListIterator.findPosition();
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


    public List<AudioFile> getCurrentTrackList() {
        return currentTrackList;
    }

    public boolean isDeletedFolderSelect(AudioFolder audioFolder) {
        if (currentTrack != null && currentTrack.id.equals(audioFolder.id)) {
            return true;
        }
        if (currentTrackList != null) {
            for (AudioFile audioFile : currentTrackList) {
                if (audioFile.id.equals(audioFolder.id)) {
                    return true;
                }
            }
        }
        return false;
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
            return position == (currentTrackList.size() - 1);
        }

        public boolean firstTrack() {
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
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentTrackList.size(); i++) {
            AudioFile audioFile = currentTrackList.get(i);
            sb.append(String.format(Locale.getDefault(), "%d. %s", i, audioFile.getFileName()));
            sb.append("\n");
        }
        return sb.toString();
    }
}
