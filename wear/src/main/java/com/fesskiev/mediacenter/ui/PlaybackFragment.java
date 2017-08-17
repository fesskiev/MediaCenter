package com.fesskiev.mediacenter.ui;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.common.data.MapAudioFile;
import com.fesskiev.common.data.MapPlayback;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.service.DataLayerService;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.CoverBitmap;
import com.fesskiev.mediacenter.widgets.PlayPauseButton;

import static com.fesskiev.common.Constants.NEXT_PATH;
import static com.fesskiev.common.Constants.PAUSE_PATH;
import static com.fesskiev.common.Constants.PLAY_PATH;
import static com.fesskiev.common.Constants.PREVIOUS_PATH;


public class PlaybackFragment extends Fragment {

    public static PlaybackFragment newInstance() {
        return new PlaybackFragment();
    }

    private CoverBitmap coverView;
    private ImageView prevTrack;
    private ImageView nextTrack;
    private PlayPauseButton playPauseButton;
    private TextView albumText;
    private TextView titleText;
    private TextView durationText;

    private MapPlayback playback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        playback = new MapPlayback.MapPlaybackBuilder()
                .withDuration(0)
                .withPosition(0)
                .withPositionPercent(0)
                .withVolume(0)
                .withFocusedVolume(0)
                .withDurationScale(0)
                .withPlaying(false)
                .withLooping(false)
                .build();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playback, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumText = view.findViewById(R.id.album);
        titleText = view.findViewById(R.id.title);
        durationText = view.findViewById(R.id.duration);

        coverView = view.findViewById(R.id.cover);
        playPauseButton = view.findViewById(R.id.playPause);
        playPauseButton.setOnClickListener(v -> togglePlayback());
        playPauseButton.setPlay(false);

        prevTrack = view.findViewById(R.id.previous);
        prevTrack.setOnClickListener(v -> {
            ((Animatable) prevTrack.getDrawable()).start();
            previous();
        });

        nextTrack = view.findViewById(R.id.next);
        nextTrack.setOnClickListener(v -> {
            ((Animatable) nextTrack.getDrawable()).start();
            next();
        });
    }

    private void next() {
        DataLayerService.sendMessage(getActivity().getApplicationContext(), NEXT_PATH);
    }

    private void previous() {
        DataLayerService.sendMessage(getActivity().getApplicationContext(), PREVIOUS_PATH);
    }

    private void togglePlayback() {
        String path;
        if (playback.isPlaying()) {
            path = PAUSE_PATH;
        } else {
            path = PLAY_PATH;
        }
        DataLayerService.sendMessage(getActivity().getApplicationContext(), path);
    }

    public void updateCurrentTrack(MapAudioFile audioFile) {
        Bitmap cover = audioFile.cover;
        if (cover != null) {
            coverView.drawBitmap(cover);
        }
        albumText.setText(audioFile.album);
        titleText.setText(audioFile.title);
        durationText.setText(Utils.getDurationString(audioFile.length));
    }

    public void updatePlayback(MapPlayback playback) {
        this.playback = playback;
        playPauseButton.setPlay(playback.isPlaying());
    }

}
