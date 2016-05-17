package com.fesskiev.player.ui.audio;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.player.R;


public class AudioPlaylistFragment extends Fragment implements AudioContent {

    public static AudioPlaylistFragment newInstance() {
        return new AudioPlaylistFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_playlist, container, false);
    }

    @Override
    public void fetchAudioContent() {

    }
}
