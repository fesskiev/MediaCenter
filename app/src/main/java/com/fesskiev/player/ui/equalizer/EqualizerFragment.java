package com.fesskiev.player.ui.equalizer;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.player.R;
import com.fesskiev.player.SuperPoweredSDKWrapper;
import com.fesskiev.player.utils.AppSettingsManager;
import com.fesskiev.player.widgets.eq.BandControlView;


public class EqualizerFragment extends Fragment implements BandControlView.OnBandLevelListener {

    private SuperPoweredSDKWrapper superPoweredSDKWrapper;
    private AppSettingsManager settingsManager;

    public static EqualizerFragment newInstance() {
        return new EqualizerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        superPoweredSDKWrapper = SuperPoweredSDKWrapper.getInstance();
        settingsManager = AppSettingsManager.getInstance(getContext().getApplicationContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_equalizer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SwitchCompat EQState = (SwitchCompat) view.findViewById(R.id.stateEqualizer);
        EQState.setOnCheckedChangeListener((compoundButton, checked) -> superPoweredSDKWrapper.enableEQ(checked));

        BandControlView[] bandControlViews = new BandControlView[]{
                (BandControlView) view.findViewById(R.id.bandControl1),
                (BandControlView) view.findViewById(R.id.bandControl2),
                (BandControlView) view.findViewById(R.id.bandControl3)
        };

        for (BandControlView bandControlView : bandControlViews) {
            bandControlView.setOnBandLevelListener(this);
        }

        if (settingsManager.isEQOn()) {
            superPoweredSDKWrapper.enableEQ(true);
        } else {
            superPoweredSDKWrapper.enableEQ(false);
        }
    }

    @Override
    public void onBandLevelChanged(int band, int level) {
        Log.d("test", " band, " + band + " level: " + level);

        superPoweredSDKWrapper.setEQBands(band, level);
    }
}
