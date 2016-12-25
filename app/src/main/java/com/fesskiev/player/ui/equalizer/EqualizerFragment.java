package com.fesskiev.player.ui.equalizer;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.player.R;
import com.fesskiev.player.data.model.EQState;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.utils.AppSettingsManager;
import com.fesskiev.player.widgets.eq.BandControlView;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;


public class EqualizerFragment extends Fragment implements BandControlView.OnBandLevelListener {

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_equalizer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.saveEQStateButton).setOnClickListener(v -> {

            settingsManager.setEQState(state);
            EventBus.getDefault().post(state);

            getActivity().finish();
        });

        SwitchCompat switchEQState = (SwitchCompat) view.findViewById(R.id.stateEqualizer);
        switchEQState.setOnClickListener(v -> {
            boolean checked = ((SwitchCompat) v).isChecked();

            PlaybackService.changeEQEnable(getContext(), checked);

            settingsManager.setEQState(state);
            EventBus.getDefault().post(state);

        });
        switchEQState.setChecked(settingsManager.isEQOn());


        BandControlView[] bandControlViews = new BandControlView[]{
                (BandControlView) view.findViewById(R.id.bandControlLow),
                (BandControlView) view.findViewById(R.id.bandControlMid),
                (BandControlView) view.findViewById(R.id.bandControlHigh)
        };

        for (BandControlView bandControlView : bandControlViews) {
            bandControlView.setOnBandLevelListener(this);
        }


        new Handler().postDelayed(() -> setEQState(bandControlViews), 1000);
    }

    private void setEQState(BandControlView[] bandControlViews) {
        state = settingsManager.getEQState();
        if (state != null) {
            for (int i = 0; i < bandControlViews.length; i++) {
                switch (i) {
                    case 0:
                        bandControlViews[i].setLevel(state.getLowValues());
                        break;
                    case 1:
                        bandControlViews[i].setLevel(state.getMidValues());
                        break;
                    case 2:
                        bandControlViews[i].setLevel(state.getHighValues());
                        break;
                }
            }
        } else {
            state = new EQState();
        }
        if (settingsManager.isEQOn()) {
            PlaybackService.changeEQEnable(context, true);
        } else {
            PlaybackService.changeEQEnable(context, false);
        }
    }

    @Override
    public void onBandLevelChanged(int band, float level, float range, float[] values) {
//        Log.d("test", " band, " + band + " level: " + level + " degrees: " + Arrays.toString(values));

        PlaybackService.changeEQBandLevel(context, band, (int) level);

        switch (band) {
            case 0:
                state.setLowBand(range);
                state.setLowValues(values);
                break;
            case 1:
                state.setMidBand(range);
                state.setMidValues(values);
                break;
            case 2:
                state.setHighBand(range);
                state.setHighValues(values);
                break;
        }
    }
}
