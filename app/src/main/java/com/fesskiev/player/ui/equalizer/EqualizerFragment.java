package com.fesskiev.player.ui.equalizer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.fesskiev.player.R;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.widgets.eq.EQBandView;
import com.fesskiev.player.widgets.spinner.CustomSpinnerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class EqualizerFragment extends Fragment {

    private static final String TAG = EqualizerFragment.class.getSimpleName();

    private List<String> presetList;
    private List<EQBandView> bandViews;
    private Spinner presets;
    private LinearLayout bandRoot;
    private PlaybackService playbackService;


    public static EqualizerFragment newInstance() {
        return new EqualizerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presetList = new ArrayList<>();
        bandViews = new ArrayList<>();

        getActivity().
                bindService(new Intent(getActivity(), PlaybackService.class),
                        connection, Context.BIND_AUTO_CREATE);
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
        EQState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                playbackService.setEnableEQ(checked);
            }
        });

        presets = (Spinner) view.findViewById(R.id.presets);
        bandRoot = (LinearLayout) view.findViewById(R.id.bandRoot);

    }

    public void onDestroy() {
        super.onDestroy();
        if (playbackService != null) {
            getActivity().unbindService(connection);
        }
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            PlaybackService.PlaybackServiceBinder binder
                    = (PlaybackService.PlaybackServiceBinder) service;
            playbackService = binder.getService();

            createEQState();
            setPresetItems();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };

    private void getBandsLevel() {
        int bandsNumber = playbackService.getNumberOfBands();
        for (int i = 0; i < bandsNumber; i++) {
            int bandLevel = playbackService.getBandLevel(i);

            Log.d(TAG, "band number: " + i + " level: " + bandLevel);

            bandViews.get(i).setBandLevel(bandLevel);
        }
    }


    private void createEQState() {
        int bandsNumber = playbackService.getNumberOfBands();
        int presetNumber = playbackService.getNumberOfPreset();
        Log.d(TAG, "bands: " + bandsNumber + " presets: " + presetNumber);
        int[] bandRange = playbackService.getBandLevelRange();
        Log.d(TAG, "band range, min: " + bandRange[0] + " max: " + bandRange[1]);

        for (int i = 0; i < bandsNumber; i++) {
            int[] freqRange = playbackService.getBandFrequencyRange(i);
            int centralFreq = playbackService.getCenterFrequency(i);

            Log.d(TAG, " band freq range: " + Arrays.toString(freqRange) + " central freq: " + centralFreq);

            EQBandView bandView = new EQBandView(getContext());
            bandView.setFrequencyText(String.valueOf(freqRange[0] / 1000));
            bandView.setBandNumber(i);
            bandView.setOnEQBandChangeListener(new EQBandView.OnEQBandChangeListener() {
                @Override
                public void onBandValueChanged(double value, int band) {
                    Log.d(TAG, "band: " + band + " value: " + value);
                    playbackService.setBandLevel(band, (int) value);
                }
            });

            bandViews.add(bandView);
            bandRoot.addView(bandView);
        }


        for (int j = 0; j < presetNumber; j++) {
            String presetName = playbackService.getPresetName(j);
            Log.d(TAG, "preset name: " + presetName);
            presetList.add(presetName);
        }
    }

    private void setPresetItems() {

        CustomSpinnerAdapter customSpinnerAdapter = new CustomSpinnerAdapter(getContext(),
                presetList, R.color.primary_dark, R.color.primary_dark);

        presets.setAdapter(customSpinnerAdapter);
        presets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "position: " + position + " name: " + presetList.get(position));
                playbackService.usePreset(position);
                getBandsLevel();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
