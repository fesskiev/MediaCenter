package com.fesskiev.player.ui.player;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.services.PlaybackService;

import java.lang.reflect.Field;


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

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioPlayerActivity.startPlayerActivity(getActivity(), false);
            }
        });

        AudioFile audioFile = MediaApplication.getInstance().getAudioPlayer().currentAudioFile;
        if(audioFile != null) {
            setMusicFileInfo(audioFile);
        }
    }

    public void setPlyingStateButton(boolean isPlaying) {
        this.isPlaying = isPlaying;
        int resource = isPlaying ? R.drawable.pause_icon : R.drawable.play_icon;
        playPause.setImageDrawable(ContextCompat.getDrawable(getActivity(), resource));
    }

    public void setMusicFileInfo(AudioFile audioFile) {
        track.setText(audioFile.title);
        artist.setText(audioFile.artist);
        Bitmap bitmap = audioFile.getArtwork();
        if (cover != null) {
            cover.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
