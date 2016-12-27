package com.fesskiev.mediacenter.ui.equalizer;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.EQState;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.widgets.eq.BandControlView;

import org.greenrobot.eventbus.EventBus;


public class EqualizerFragment extends Fragment implements BandControlView.OnBandLevelListener,
        BandControlView.OnAttachStateListener {

    private Context context;
    private EQState state;
    private AppSettingsManager settingsManager;

    public static EqualizerFragment newInstance() {
        return new EqualizerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext().getApplicationContext();
        settingsManager = AppSettingsManager.getInstance(context);

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

        SwitchCompat switchEQState = (SwitchCompat) view.findViewById(R.id.stateEqualizer);

        switchEQState.setOnClickListener(v -> {
            boolean checked = ((SwitchCompat) v).isChecked();

            PlaybackService.changeEQEnable(context, checked);
            EventBus.getDefault().post(state);

        });
        switchEQState.setChecked(settingsManager.isEQEnable());


        BandControlView[] bandControlViews = new BandControlView[]{
                (BandControlView) view.findViewById(R.id.bandControlLow),
                (BandControlView) view.findViewById(R.id.bandControlMid),
                (BandControlView) view.findViewById(R.id.bandControlHigh)
        };

        for (BandControlView bandControlView : bandControlViews) {
            bandControlView.setAttachStateListener(this);
            bandControlView.setOnBandLevelListener(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        settingsManager.setEQState(state);
    }

    @Override
    public void onAttachBandControlView(BandControlView view) {
        int band = view.getBand();
        switch (band) {
            case 0:
                view.setLevel(state.getLowValues());
                break;
            case 1:
                view.setLevel(state.getMidValues());
                break;
            case 2:
                view.setLevel(state.getHighValues());
                break;
        }
    }

    @Override
    public void onBandLevelChanged(int band, float level, float range, float[] values) {
//        Log.d("test", " band, " + band + " level: " + level + " degrees: " + Arrays.toString(values));

        PlaybackService.changeEQBandLevel(context, band, (int) level);

        switch (band) {
            case 0:
                state.setLowLevel(level);
                state.setLowBand(range);
                state.setLowValues(values);
                break;
            case 1:
                state.setMidLevel(level);
                state.setMidBand(range);
                state.setMidValues(values);
                break;
            case 2:
                state.setHighLevel(level);
                state.setHighBand(range);
                state.setHighValues(values);
                break;
        }
    }
}
