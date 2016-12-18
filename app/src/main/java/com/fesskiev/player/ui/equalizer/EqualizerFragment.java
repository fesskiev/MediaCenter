package com.fesskiev.player.ui.equalizer;


import android.content.Context;
import android.os.Bundle;
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
            getActivity().finish();
        });

        SwitchCompat EQState = (SwitchCompat) view.findViewById(R.id.stateEqualizer);
        EQState.setOnCheckedChangeListener((compoundButton, checked) ->
                PlaybackService.changeEQEnable(getContext(), checked));

        BandControlView[] bandControlViews = new BandControlView[]{
                (BandControlView) view.findViewById(R.id.bandControl1),
                (BandControlView) view.findViewById(R.id.bandControl2),
                (BandControlView) view.findViewById(R.id.bandControl3)
        };

        for (BandControlView bandControlView : bandControlViews) {
            bandControlView.setOnBandLevelListener(this);
        }

        if (settingsManager.isEQOn()) {
            PlaybackService.changeEQEnable(context, true);
        } else {
            PlaybackService.changeEQEnable(context, false);
        }

        setEQState(bandControlViews);
    }

    private void setEQState(BandControlView[] bandControlViews) {
        state = settingsManager.getEQState();
        if (state != null) {
            for (int i = 0; i < bandControlViews.length; i++) {
                switch (i) {
                    case 0:
                        bandControlViews[i].setLevel(state.getFirstBand());
                        break;
                    case 1:
                        bandControlViews[i].setLevel(state.getSecondBand());
                        break;
                    case 2:
                        bandControlViews[i].setLevel(state.getThirdBand());
                        break;
                }
            }
        } else {
            state = new EQState();
        }
    }

    @Override
    public void onBandLevelChanged(int band, int level) {
        Log.d("test", " band, " + band + " level: " + level);

        PlaybackService.changeEQBandLevel(context, band, level);
    }
}
