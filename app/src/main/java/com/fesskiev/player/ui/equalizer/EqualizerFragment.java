package com.fesskiev.player.ui.equalizer;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.fesskiev.player.R;
import com.fesskiev.player.SuperPoweredSDKWrapper;
import com.fesskiev.player.utils.AppLog;
import com.fesskiev.player.utils.AppSettingsManager;

import java.util.ArrayList;
import java.util.List;


public class EqualizerFragment extends Fragment {

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
        EQState.setOnCheckedChangeListener((compoundButton, checked) -> {
            superPoweredSDKWrapper.enableEQ(checked);
        });


        superPoweredSDKWrapper.enableEQ(true);
//        superPoweredSDKWrapper.setEQBands(band, value);

    }



}
