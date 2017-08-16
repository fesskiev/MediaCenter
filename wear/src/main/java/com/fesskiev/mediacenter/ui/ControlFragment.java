package com.fesskiev.mediacenter.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fesskiev.common.data.MapAudioFile;
import com.fesskiev.common.data.MapPlayback;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.service.DataLayerService;

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

    private static final int ITEM_DELAY = 300;


    private ImageView[] buttons;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        buttons = new ImageView[]{
                view.findViewById(R.id.previous),
                view.findViewById(R.id.next),
                view.findViewById(R.id.play),
                view.findViewById(R.id.pause),
                view.findViewById(R.id.volumeDown),
                view.findViewById(R.id.volumeOff),
                view.findViewById(R.id.volumeUp),
        };
        for (ImageView button : buttons) {
            button.setOnClickListener(this);
        }
        animateButtons(view);
    }

    public void updateCurrentTrack(MapAudioFile audioFile) {

    }

    public void updatePlayback(MapPlayback playback) {

    }

    private void animateButtons(View view) {
        ViewGroup container = view.findViewById(R.id.rootContainer);
        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            ViewCompat.animate(v)
                    .alpha(1)
                    .setStartDelay((ITEM_DELAY * i) + 500)
                    .setDuration(1000)
                    .start();
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
            case R.id.play:
                path = PLAY_PATH;
                break;
            case R.id.pause:
                path = PAUSE_PATH;
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
}
