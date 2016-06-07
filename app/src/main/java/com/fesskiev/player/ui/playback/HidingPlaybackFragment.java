package com.fesskiev.player.ui.playback;


import android.support.v4.app.Fragment;

import com.fesskiev.player.ui.MainActivity;

public class HidingPlaybackFragment extends Fragment {

    protected void hidePlaybackControl() {
        ((MainActivity) getActivity()).hidePlayback();
    }

    protected void showPlaybackControl() {
        ((MainActivity) getActivity()).showPlayback();
    }
}
