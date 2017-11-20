package com.fesskiev.mediacenter.ui;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.fesskiev.mediacenter.data.model.AudioFile;

import java.util.List;


public class MainViewModel extends ViewModel {

    private final MutableLiveData<AudioFile> currentTrackLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<AudioFile> > currentTrackListLiveData = new MutableLiveData<>();

    public MainViewModel() {

    }
}
