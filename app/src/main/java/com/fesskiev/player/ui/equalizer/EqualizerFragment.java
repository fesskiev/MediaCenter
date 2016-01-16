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
import android.widget.SeekBar;
import android.widget.Spinner;

import com.fesskiev.player.R;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.widgets.VerticalSeekBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class EqualizerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = EqualizerFragment.class.getSimpleName();

    private PlaybackService playbackService;
    private Spinner presets;
    private VerticalSeekBar[] bandsLevel;
    private List<String> presetList;

    public static EqualizerFragment newInstance() {
        return new EqualizerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presetList = new ArrayList<>();

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

        presets = (Spinner) view.findViewById(R.id.presets);
        SwitchCompat stateEQSwitch = (SwitchCompat) view.findViewById(R.id.stateEqualizer);
        stateEQSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                playbackService.setEnableEQ(isChecked);
            }
        });

        bandsLevel = new VerticalSeekBar[]{
                (VerticalSeekBar) view.findViewById(R.id.firstBandLevel),
                (VerticalSeekBar) view.findViewById(R.id.secondBandLevel),
                (VerticalSeekBar) view.findViewById(R.id.thirdBandLevel),
                (VerticalSeekBar) view.findViewById(R.id.fourthBandLevel),
                (VerticalSeekBar) view.findViewById(R.id.fifthBandLevel)
        };

        for (VerticalSeekBar bandLevel : bandsLevel) {
            bandLevel.setOnSeekBarChangeListener(this);
        }

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

            getPresetsName();
            setPresetItems();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };

    private void getBandsLevel() {
        int[] bandRange = playbackService.getBandLevelRange();
        Log.d(TAG, "band range, min: " + bandRange[0] + " max: " + bandRange[1]);

        int minRangeScale = bandRange[0] + 1500;
        int maxRangeScale = bandRange[1] + 1500;
        Log.d(TAG, "band range scale, min: " + minRangeScale + " max: " + maxRangeScale);


        int bandsNumber = playbackService.getNumberOfBands();
        for (int i = 0; i < bandsNumber; i++) {
            int bandLevel = playbackService.getBandLevel(i);
            Log.d(TAG, "band number: " + i + " level: " + bandLevel);

            int scaleBandLevel = (bandLevel + 1500) / 100;
            Log.d(TAG, "scale band level: " + scaleBandLevel);

            bandsLevel[i].setProgress((int) scaleBandLevel);
        }

    }

    private void getPresetsName() {
        int presetNumber = playbackService.getNumberOfPreset();
        for (int j = 0; j < presetNumber; j++) {
            String presetName = playbackService.getPresetName(j);
            Log.d(TAG, "preset name: " + presetName);
            presetList.add(presetName);
        }
    }

    private void createEQState() {
        int bandsNumber = playbackService.getNumberOfBands();
        int presetNumber = playbackService.getNumberOfPreset();
        Log.d(TAG, "bands: " + bandsNumber + " presets: " + presetNumber);
        int[] bandRange = playbackService.getBandLevelRange();
        Log.d(TAG, "band range, min: " + bandRange[0] + " max: " + bandRange[1]);

        for (int i = 0; i < bandsNumber; i++) {
            int bandLevel = playbackService.getBandLevel(i);
            int[] freqRange = playbackService.getBandFrequencyRange(i);
            int centralFreq = playbackService.getCenterFrequency(i);
            Log.d(TAG, "band level: " + bandLevel
                    + " band freq range: " + Arrays.toString(freqRange)
                    + " central freq: " + centralFreq);
        }

        for (int j = 0; j < presetNumber; j++) {
            String presetName = playbackService.getPresetName(j);
            Log.d(TAG, "preset name: " + presetName);
            presetList.add(presetName);
        }
    }

    private void setPresetItems() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, presetList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        presets.setAdapter(adapter);
        presets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "position: " + position + " name: " + presetList.get(position));
                playbackService.usePreset(position);
                getBandsLevel();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.firstBandLevel:
                break;
            case R.id.secondBandLevel:
                break;
            case R.id.thirdBandLevel:
                break;
            case R.id.fourthBandLevel:
                break;
            case R.id.fifthBandLevel:
                break;
        }
    }
}
