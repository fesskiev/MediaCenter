package com.fesskiev.mediacenter.ui.effects;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.effects.EQState;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.widgets.effects.DialerView;
import com.fesskiev.mediacenter.widgets.effects.EQBandControlView;

import javax.inject.Inject;


public class EqualizerFragment extends Fragment implements EQBandControlView.OnBandLevelListener,
        DialerView.OnDialerViewListener {

    private EQState state;

    @Inject
    AppSettingsManager settingsManager;

    public static EqualizerFragment newInstance() {
        return new EqualizerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MediaApplication.getInstance().getAppComponent().inject(this);

        state = settingsManager.getEQState();
        if (state == null) {
            state = new EQState();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_equalizer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SwitchCompat switchEQState = view.findViewById(R.id.stateEqualizer);
        switchEQState.setOnClickListener(v -> {
            boolean checked = ((SwitchCompat) v).isChecked();

            PlaybackService.changeEQEnable(getContext(), checked);

        });
        switchEQState.setChecked(settingsManager.isEQEnable());


        EQBandControlView[] EQBandControlViews = new EQBandControlView[]{
                view.findViewById(R.id.bandControlLow),
                view.findViewById(R.id.bandControlMid),
                view.findViewById(R.id.bandControlHigh)
        };

        for (EQBandControlView EQBandControlView : EQBandControlViews) {
            EQBandControlView.setOnDealerViewListener(this);
            EQBandControlView.setOnBandLevelListener(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        settingsManager.setEQState(state);
    }

    @Override
    public void onAttachDealerView(DialerView view) {
        int band = ((EQBandControlView) view).getBand();
        switch (band) {
            case 0:
                view.setLevel(state.getLowValues(), true);
                break;
            case 1:
                view.setLevel(state.getMidValues(), true);
                break;
            case 2:
                view.setLevel(state.getHighValues(), true);
                break;
        }
    }

    @Override
    public void onBandLevelChanged(int band, float level, float[] values) {

        PlaybackService.changeEQBandLevel(getContext(), band, (int) level);

        switch (band) {
            case 0:
                state.setLowLevel(level);
                state.setLowValues(values);
                break;
            case 1:
                state.setMidLevel(level);
                state.setMidValues(values);
                break;
            case 2:
                state.setHighLevel(level);
                state.setHighValues(values);
                break;
        }
    }
}
