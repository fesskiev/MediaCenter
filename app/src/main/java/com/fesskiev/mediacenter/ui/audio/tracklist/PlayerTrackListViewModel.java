package com.fesskiev.mediacenter.ui.audio.tracklist;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.players.AudioPlayer;

import java.util.List;

import javax.inject.Inject;


public class PlayerTrackListViewModel extends ViewModel {

    private final MutableLiveData<List<AudioFile>> currentTrackListLiveData = new MutableLiveData<>();

    @Inject
    AudioPlayer audioPlayer;

    public PlayerTrackListViewModel() {
        MediaApplication.getInstance().getAppComponent().inject(this);
        currentTrackListLiveData.setValue(audioPlayer.getCurrentTrackList());
    }

    public MutableLiveData<List<AudioFile>> getCurrentTrackListLiveData() {
        return currentTrackListLiveData;
    }

    public void setCurrentAudioFileAndPlay(AudioFile audioFile) {
        audioPlayer.setCurrentAudioFileAndPlay(audioFile);
    }
}
