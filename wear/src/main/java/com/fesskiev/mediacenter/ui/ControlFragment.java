package com.fesskiev.mediacenter.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.service.DataLayerService;

import static com.fesskiev.common.Constants.VOLUME_DOWN;
import static com.fesskiev.common.Constants.VOLUME_OFF;
import static com.fesskiev.common.Constants.VOLUME_UP;


public class ControlFragment extends Fragment implements View.OnClickListener  {

    public static ControlFragment newInstance() {
        return new ControlFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView[] buttons = new ImageView[]{
                view.findViewById(R.id.volumeDown),
                view.findViewById(R.id.volumeOff),
                view.findViewById(R.id.volumeUp),
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
