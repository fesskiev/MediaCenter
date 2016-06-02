package com.fesskiev.player.ui.playback;


import android.support.v4.app.Fragment;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.ui.MainActivity;

public class HidingPlaybackFragment extends Fragment {

    protected void hidePlaybackControl() {
        AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        if(audioPlayer.currentAudioFile != null) {
            ((MainActivity) getActivity()).hidePlayback();
        }
    }

    protected void showPlaybackControl() {
        AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        if(audioPlayer.currentAudioFile != null) {
            ((MainActivity) getActivity()).showPlayback();
        }
    }
}
