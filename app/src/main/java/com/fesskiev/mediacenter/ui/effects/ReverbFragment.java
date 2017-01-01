package com.fesskiev.mediacenter.ui.effects;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.effects.ReverbState;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.widgets.reverb.ReverbControlView;

import org.greenrobot.eventbus.EventBus;


public class ReverbFragment extends Fragment implements ReverbControlView.OnAttachStateListener,
        ReverbControlView.OnReverbControlListener {

    private static final String REVERB_WIDTH = "Width";
    private static final String REVERB_MIX = "Mix";
    private static final String REVERB_DAMP = "Damp";
    private static final String REVERB_ROOM_SIZE = "Room size";

    public static ReverbFragment newInstance() {
        return new ReverbFragment();
    }

    private Context context;
    private AppSettingsManager settingsManager;
    private ReverbState state;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext().getApplicationContext();
        settingsManager = AppSettingsManager.getInstance(context);

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

        SwitchCompat switchEQState = (SwitchCompat) view.findViewById(R.id.stateReverb);
        switchEQState.setOnClickListener(v -> {
            boolean checked = ((SwitchCompat) v).isChecked();

            PlaybackService.changeReverbEnable(context, checked);
            EventBus.getDefault().post(state);

        });
        switchEQState.setChecked(settingsManager.isReverbEnable());

        ReverbControlView[] reverbControlViews = new ReverbControlView[]{
                (ReverbControlView) view.findViewById(R.id.reverbMix),
                (ReverbControlView) view.findViewById(R.id.reverbWidth),
                (ReverbControlView) view.findViewById(R.id.reverbDamp),
                (ReverbControlView) view.findViewById(R.id.reverRoomSize)
        };

        for (ReverbControlView reverbControlView : reverbControlViews) {
            reverbControlView.setAttachStateListener(this);
            reverbControlView.setControlListener(this);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        settingsManager.setReverbState(state);
    }

    @Override
    public void onAttachReverbControlView(ReverbControlView view) {
        String name = view.getName();
//        switch (name) {
//            case REVERB_WIDTH:
//                view.setLevel(state.getWeightValues());
//                break;
//            case REVERB_MIX:
//                view.setLevel(state.getMixValues());
//                break;
//            case REVERB_DAMP:
//                view.setLevel(state.getDampValues());
//                break;
//            case REVERB_ROOM_SIZE:
//                view.setLevel(state.getRoomSizeValues());
//                break;
//        }
    }

    @Override
    public void onReverbControlChanged(String name, float level, float[] values) {
        Log.d("reverb", "reverb name: " + name + " level: " + level);
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
        PlaybackService.changeReverbLevel(context, state);

    }
}
