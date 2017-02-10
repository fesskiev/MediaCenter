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
import com.fesskiev.mediacenter.data.model.effects.EchoState;
import com.fesskiev.mediacenter.data.model.effects.WhooshState;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.widgets.effects.DialerView;
import com.fesskiev.mediacenter.widgets.effects.EchoControlView;
import com.fesskiev.mediacenter.widgets.effects.WhooshControlView;


public class OtherEffectsFragment extends Fragment implements EchoControlView.OnEchoControlListener,
        WhooshControlView.OnWhooshControlListener,
        DialerView.OnDialerViewListener {

    public static OtherEffectsFragment newInstance() {
        return new OtherEffectsFragment();
    }

    private static final String WHOOSH_FREQUENCY = "Frequency";
    private static final String WHOOSH_MIX = "Mix";

    private Context context;
    private AppSettingsManager settingsManager;
    private WhooshState whooshState;
    private EchoState echoState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext().getApplicationContext();
        settingsManager = AppSettingsManager.getInstance();

        whooshState = settingsManager.getWhooshState();
        if (whooshState == null) {
            whooshState = new WhooshState();
        }

        echoState = settingsManager.getEchoState();
        if (echoState == null) {
            echoState = new EchoState();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_other_effects, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SwitchCompat switchEchoState = (SwitchCompat) view.findViewById(R.id.stateEcho);
        switchEchoState.setOnClickListener(v -> {
            boolean checked = ((SwitchCompat) v).isChecked();

            PlaybackService.changeEchoEnable(context, checked);

        });
        switchEchoState.setChecked(settingsManager.isEchoEnable());


        SwitchCompat switchWhooshState = (SwitchCompat) view.findViewById(R.id.stateWhoosh);
        switchWhooshState.setOnClickListener(v -> {
            boolean checked = ((SwitchCompat) v).isChecked();

            PlaybackService.changeWhooshEnable(context, checked);

        });
        switchWhooshState.setChecked(settingsManager.isWhooshEnable());


        WhooshControlView[] whooshControlViews = new WhooshControlView[]{
                (WhooshControlView) view.findViewById(R.id.whooshbMix),
                (WhooshControlView) view.findViewById(R.id.whooshFreq)
        };

        for (WhooshControlView whooshControlView : whooshControlViews) {
            whooshControlView.setOnDealerViewListener(this);
            whooshControlView.setOnWhooshControlListener(this);
        }

        EchoControlView echoControlView = (EchoControlView) view.findViewById(R.id.echoView);
        echoControlView.setOnDealerViewListener(this);
        echoControlView.setOnEchoControlListener(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        settingsManager.setWhooshState(whooshState);
        settingsManager.setEchoState(echoState);
    }

    @Override
    public void onAttachDealerView(DialerView view) {
        if (view instanceof EchoControlView) {
            setEchoLevel((EchoControlView) view);
        } else if (view instanceof WhooshControlView) {
            setWhooshLevel((WhooshControlView) view);
        }
    }

    private void setWhooshLevel(WhooshControlView view) {
        String name = view.getName();
        switch (name) {
            case WHOOSH_FREQUENCY:
                view.setLevel(whooshState.getFrequencyValues(), false);
                break;
            case WHOOSH_MIX:
                view.setLevel(whooshState.getMixValues(), false);
                break;

        }
    }

    private void setEchoLevel(EchoControlView view) {
        view.setLevel(echoState.getLevelValues(), false);
    }

    @Override
    public void onWhooshControlChanged(String name, float level, float[] values) {
        Log.d("whoosh", "whoosh name: " + name + " level: " + level);
        switch (name) {
            case WHOOSH_FREQUENCY:
                whooshState.setFrequencyValues(values);
                whooshState.setFrequency(level);
                break;
            case WHOOSH_MIX:
                whooshState.setMixValues(values);
                whooshState.setMix(level);
                break;

        }
        PlaybackService.changeWhooshLevel(context, whooshState);
    }

    @Override
    public void onEchoControlChanged(float level, float[] values) {

        echoState.setLevel(level);
        echoState.setLevelValues(values);

        PlaybackService.changeEchoLevel(context, echoState);
    }
}
