package com.fesskiev.mediacenter.ui;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fesskiev.common.data.MapAudioFile;
import com.fesskiev.common.data.MapPlayback;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.service.DataLayerService;
import com.fesskiev.mediacenter.widgets.CoverBitmap;

import static com.fesskiev.common.Constants.NEXT_PATH;
import static com.fesskiev.common.Constants.PAUSE_PATH;
import static com.fesskiev.common.Constants.PLAY_PATH;
import static com.fesskiev.common.Constants.PREVIOUS_PATH;
import static com.fesskiev.common.Constants.VOLUME_DOWN;
import static com.fesskiev.common.Constants.VOLUME_OFF;
import static com.fesskiev.common.Constants.VOLUME_UP;


public class ControlFragment extends Fragment implements View.OnClickListener {

    public static ControlFragment newInstance() {
        return new ControlFragment();
    }

    private ImageView[] buttons;
    private CoverBitmap coverView;
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
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        coverView = view.findViewById(R.id.cover);
        buttons = new ImageView[]{
                view.findViewById(R.id.previous),
                view.findViewById(R.id.next),
                view.findViewById(R.id.playPause),
                view.findViewById(R.id.volumeDown),
                view.findViewById(R.id.volumeOff),
                view.findViewById(R.id.volumeUp),
        };
        for (ImageView button : buttons) {
            button.setOnClickListener(this);
        }
    }

    public void updateCurrentTrack(MapAudioFile audioFile) {
        Bitmap cover = audioFile.cover;
        if (cover != null) {
            coverView.drawBitmap(cover);
        }
    }

    public void updatePlayback(MapPlayback playback) {
        this.playback = playback;
        ImageView playPauseButton = findButtonById(R.id.playPause);
        if (playPauseButton != null) {
            if (playback.isPlaying()) {
                playPauseButton.setImageResource(R.drawable.icon_pause);
            } else {
                playPauseButton.setImageResource(R.drawable.icon_play);
            }
        }
    }

    @Override
    public void onClick(View view) {
        String path = null;
        switch (view.getId()) {
            case R.id.previous:
                path = PREVIOUS_PATH;
                break;
            case R.id.next:
                path = NEXT_PATH;
                break;
            case R.id.playPause:
                if (playback.isPlaying()) {
                    path = PAUSE_PATH;
                } else {
                    path = PLAY_PATH;
                }
                break;
            case R.id.volumeUp:
                path = VOLUME_UP;
                break;
            case R.id.volumeDown:
                path = VOLUME_DOWN;
                break;
            case R.id.volumeOff:
                path = VOLUME_OFF;
                break;
        }
        DataLayerService.sendMessage(getActivity().getApplicationContext(), path);
    }

    private ImageView findButtonById(int id) {
        for (ImageView button : buttons) {
            if (button.getId() == id) {
                return button;
            }
        }
        return null;
    }
}
