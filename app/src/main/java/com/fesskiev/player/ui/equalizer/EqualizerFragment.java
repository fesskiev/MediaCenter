package com.fesskiev.player.ui.equalizer;

import android.os.Bundle;
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

import com.fesskiev.player.MusicApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.widgets.seekbar.VerticalSeekBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class EqualizerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = EqualizerFragment.class.getSimpleName();

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
                MusicApplication.setEnableEQ(isChecked);
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

        getPresetsName();
        setPresetItems();
    }

    private void getBandsLevel(){
        int[] bandRange = MusicApplication.getBandLevelRange();
        Log.d(TAG, "band range, min: " + bandRange[0] + " max: " + bandRange[1]);

        int minRangeScale = bandRange[0] + 1500;
        int maxRangeScale = bandRange[1] + 1500;
        Log.d(TAG, "band range scale, min: " + minRangeScale + " max: " + maxRangeScale);


        int bandsNumber = MusicApplication.getNumberOfBands();
        for (int i = 0; i < bandsNumber; i++) {
            int bandLevel = MusicApplication.getBandLevel(i);
            Log.d(TAG, "band number: " + i + " level: " + bandLevel);

            int scaleBandLevel = (bandLevel + 1500) / 100;
            Log.d(TAG, "scale band level: " + scaleBandLevel);

            bandsLevel[i].setProgress((int)scaleBandLevel);
        }

    }

    private void getPresetsName(){
        int presetNumber = MusicApplication.getNumberOfPreset();
        for (int j = 0; j < presetNumber; j++) {
            String presetName = MusicApplication.getPresetName(j);
            Log.d(TAG, "preset name: " + presetName);
            presetList.add(presetName);
        }
    }

    private void createEQState() {
        int bandsNumber = MusicApplication.getNumberOfBands();
        int presetNumber = MusicApplication.getNumberOfPreset();
        Log.d(TAG, "bands: " + bandsNumber + " presets: " + presetNumber);
        int[] bandRange = MusicApplication.getBandLevelRange();
        Log.d(TAG, "band range, min: " + bandRange[0] + " max: " + bandRange[1]);

        for (int i = 0; i < bandsNumber; i++) {
            int bandLevel = MusicApplication.getBandLevel(i);
            int[] freqRange = MusicApplication.getBandFrequencyRange(i);
            int centralFreq = MusicApplication.getCenterFrequency(i);
            Log.d(TAG, "band level: " + bandLevel
                    + " band freq range: " + Arrays.toString(freqRange)
                    + " central freq: " + centralFreq);
        }

        for (int j = 0; j < presetNumber; j++) {
            String presetName = MusicApplication.getPresetName(j);
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
                MusicApplication.usePreset(position);
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
