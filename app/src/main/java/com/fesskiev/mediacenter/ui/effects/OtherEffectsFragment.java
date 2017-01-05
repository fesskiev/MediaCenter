package com.fesskiev.mediacenter.ui.effects;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.widgets.effects.DealerView;
import com.fesskiev.mediacenter.widgets.effects.EchoControlView;


public class OtherEffectsFragment extends Fragment implements EchoControlView.OnEchoControlListener,
        DealerView.OnDealerViewListener {

    public static OtherEffectsFragment newInstance() {
        return new OtherEffectsFragment();
    }

    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext().getApplicationContext();
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

        EchoControlView echoControlView = (EchoControlView) view.findViewById(R.id.echoView);
        echoControlView.setOnDealerViewListener(this);
        echoControlView.setOnEchoControlListener(this);

    }

    @Override
    public void onAttachDealerView(DealerView view) {

    }

    @Override
    public void onEchoControlChanged(float level, float[] values) {

        PlaybackService.changeEchoLevel(context, (int) level);
    }
}
