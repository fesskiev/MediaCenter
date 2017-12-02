package com.fesskiev.mediacenter.ui.effects;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.effects.ReverbState;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.widgets.effects.DialerView;
import com.fesskiev.mediacenter.widgets.effects.ReverbControlView;

import javax.inject.Inject;


public class ReverbFragment extends Fragment implements DialerView.OnDialerViewListener,
        ReverbControlView.OnReverbControlListener {

    private static final String REVERB_WIDTH = "Width";
    private static final String REVERB_MIX = "Mix";
    private static final String REVERB_DAMP = "Damp";
    private static final String REVERB_ROOM_SIZE = "Room size";

    public static ReverbFragment newInstance() {
        return new ReverbFragment();
    }

    private ReverbState state;

    @Inject
    AppSettingsManager settingsManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MediaApplication.getInstance().getAppComponent().inject(this);

        state = settingsManager.getReverbState();
        if (state == null) {
            state = new ReverbState();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reverb, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SwitchCompat switchEQState = view.findViewById(R.id.stateReverb);
        switchEQState.setOnClickListener(v -> {
            boolean checked = ((SwitchCompat) v).isChecked();

            PlaybackService.changeReverbEnable(getContext(), checked);

        });
        switchEQState.setChecked(settingsManager.isReverbEnable());

        ReverbControlView[] reverbControlViews = new ReverbControlView[]{
                view.findViewById(R.id.reverbMix),
                view.findViewById(R.id.reverbWidth),
                view.findViewById(R.id.reverbDamp),
                view.findViewById(R.id.reverRoomSize)
        };

        for (ReverbControlView reverbControlView : reverbControlViews) {
            reverbControlView.setOnDealerViewListener(this);
            reverbControlView.setControlListener(this);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        settingsManager.setReverbState(state);
    }

    @Override
    public void onAttachDealerView(DialerView view) {
        String name = ((ReverbControlView) view).getName();
        switch (name) {
            case REVERB_WIDTH:
                view.setLevel(state.getWeightValues(), false);
                break;
            case REVERB_MIX:
                view.setLevel(state.getMixValues(), false);
                break;
            case REVERB_DAMP:
                view.setLevel(state.getDampValues(), false);
                break;
            case REVERB_ROOM_SIZE:
                view.setLevel(state.getRoomSizeValues(), false);
                break;
        }
    }

    @Override
    public void onReverbControlChanged(String name, float level, float[] values) {
        switch (name) {
            case REVERB_WIDTH:
                state.setWeightValues(values);
                state.setWeight(level);
                break;
            case REVERB_MIX:
                state.setMixValues(values);
                state.setMix(level);
                break;
            case REVERB_DAMP:
                state.setDampValues(values);
                state.setDamp(level);
                break;
            case REVERB_ROOM_SIZE:
                state.setRoomSizeValues(values);
                state.setRoomSize(level);
                break;
        }
        PlaybackService.changeReverbLevel(getContext(), state);

    }
}
