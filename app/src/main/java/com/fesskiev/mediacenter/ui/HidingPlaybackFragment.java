package com.fesskiev.mediacenter.ui;


import android.support.v4.app.Fragment;

import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.ui.MainActivity;

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
