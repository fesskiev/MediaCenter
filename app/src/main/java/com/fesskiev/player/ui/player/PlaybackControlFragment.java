package com.fesskiev.player.ui.player;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.R;
import com.fesskiev.player.model.MusicFile;
import com.fesskiev.player.services.PlaybackService;


public class PlaybackControlFragment extends Fragment {

    private ImageView playPause;
    private TextView track;
    private TextView artist;
    private ImageView cover;
    private boolean isPlaying;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playback_control, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        track = (TextView) view.findViewById(R.id.track);
        artist = (TextView) view.findViewById(R.id.artist);
        cover = (ImageView) view.findViewById(R.id.cover);
        playPause = (ImageView) view.findViewById(R.id.playPauseButton);
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    PlaybackService.stopPlayback(getActivity());
                } else {
                    PlaybackService.startPlayback(getActivity());
                }
            }
        });
    }

    public void setPlyingStateButton(boolean isPlaying) {
        this.isPlaying = isPlaying;
        int resource = isPlaying ? R.drawable.pause_icon : R.drawable.play_icon;
        playPause.setImageDrawable(ContextCompat.getDrawable(getActivity(), resource));
    }

    public void setMusicFileInfo(MusicFile musicFile) {
        track.setText(musicFile.title);
        artist.setText(musicFile.artist);
    }
}
