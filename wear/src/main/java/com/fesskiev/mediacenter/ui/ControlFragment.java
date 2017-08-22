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
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.service.DataLayerService;
import com.fesskiev.mediacenter.widgets.CoverBitmap;

import static com.fesskiev.common.Constants.REPEAT_OFF;
import static com.fesskiev.common.Constants.REPEAT_ON;
import static com.fesskiev.common.Constants.SHUTDOWN;
import static com.fesskiev.common.Constants.VOLUME_DOWN_PATH;
import static com.fesskiev.common.Constants.VOLUME_OFF;
import static com.fesskiev.common.Constants.VOLUME_UP_PATH;


public class ControlFragment extends Fragment implements View.OnClickListener {

    public static ControlFragment newInstance() {
        return new ControlFragment();
    }

    private View containerViews;

    private CoverBitmap coverView;
    private boolean repeat;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        containerViews = view.findViewById(R.id.containerViews);

        coverView = view.findViewById(R.id.cover);

        ImageView[] buttons = new ImageView[]{
                view.findViewById(R.id.volumeDown),
                view.findViewById(R.id.volumeOff),
                view.findViewById(R.id.volumeUp),
                view.findViewById(R.id.shutdown),
                view.findViewById(R.id.repeat),
        };
        for (ImageView button : buttons) {
            button.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        String path = null;
        switch (view.getId()) {
            case R.id.volumeUp:
                path = VOLUME_UP_PATH;
                break;
            case R.id.volumeDown:
                path = VOLUME_DOWN_PATH;
                break;
            case R.id.volumeOff:
                path = VOLUME_OFF;
                break;
            case R.id.repeat:
                if (repeat) {
                    path = REPEAT_OFF;
                } else {
                    path = REPEAT_ON;
                }
                break;
            case R.id.shutdown:
                path = SHUTDOWN;
                break;
        }
        DataLayerService.sendMessage(getActivity().getApplicationContext(), path);
    }

    public void updateCurrentTrack(MapAudioFile audioFile) {
        Bitmap cover = audioFile.cover;
        if (cover != null) {
            coverView.drawBitmap(cover);
        }
        showViews();
    }

    private void showViews() {
        containerViews.setVisibility(View.VISIBLE);
    }
}
