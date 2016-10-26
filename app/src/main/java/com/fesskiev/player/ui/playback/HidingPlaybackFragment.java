package com.fesskiev.player.ui.playback;


import android.support.v4.app.Fragment;

import com.fesskiev.player.data.model.AudioFolder;
import com.fesskiev.player.ui.MainActivity;

import java.util.List;

public class HidingPlaybackFragment extends Fragment {

    private final static int MIN_FOLDER_SIZE = 10;

    protected void checkNeedShowPlayback(List<AudioFolder> audioFolders) {
        if (audioFolders.size() < MIN_FOLDER_SIZE) {
            showPlaybackControl();
        }
    }

    protected void hidePlaybackControl() {
        ((MainActivity) getActivity()).hidePlayback();
    }

    protected void showPlaybackControl() {
        ((MainActivity) getActivity()).showPlayback();
    }
}
