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
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.fesskiev.player.R;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.utils.AppSettingsManager;
import com.fesskiev.player.widgets.eq.EQBandView;
import com.fesskiev.player.widgets.spinner.CustomSpinnerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class EqualizerFragment extends Fragment {

    private static final String TAG = EqualizerFragment.class.getSimpleName();

    public static final int POSITION_NONE = 0;
    public static final int POSITION_CUSTOM_PRESET = 1;
    public static final int POSITION_PRESET = 2;
    public static final int OFFSET = 2;

    private AppSettingsManager settingsManager;
    private List<String> presetList;
    private List<EQBandView> bandViews;
    private Spinner presetsSpinner;
    private SwitchCompat EQState;
    private LinearLayout bandRoot;
    private PlaybackService playbackService;
    private boolean customPreset;


    public static EqualizerFragment newInstance() {
        return new EqualizerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsManager = AppSettingsManager.getInstance(getContext().getApplicationContext());
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

        EQState = (SwitchCompat) view.findViewById(R.id.stateEqualizer);
        EQState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                enableEQ(checked);

            }
        });


        presetsSpinner = (Spinner) view.findViewById(R.id.presets);
        bandRoot = (LinearLayout) view.findViewById(R.id.bandRoot);

    }


    public void onDestroy() {
        super.onDestroy();
        if (playbackService != null) {
            getActivity().unbindService(connection);
        }

        if (customPreset) {
            saveBandsProgress();
        }
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            PlaybackService.PlaybackServiceBinder binder
                    = (PlaybackService.PlaybackServiceBinder) service;
            playbackService = binder.getService();

            createEQBands();
            setPresetSpinnerItems();
            setBandsCenter();
            if (settingsManager.isEQOn()) {
                EQState.setChecked(true);
                enableBands(true);
                checkNeedPreset();

            } else {
                EQState.setChecked(false);
                enableBands(false);
                enablePresetsSpinner(false);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };

    private void checkNeedPreset() {
        switch (settingsManager.getEQPresetState()) {
            case POSITION_NONE:
                presetsSpinner.setSelection(POSITION_NONE, false);
                break;
            case POSITION_CUSTOM_PRESET:
                presetsSpinner.setSelection(POSITION_CUSTOM_PRESET, false);
                break;
            case POSITION_PRESET:
                presetsSpinner.setSelection(settingsManager.getEQPresetValue(), false);
                break;
            default:
                break;
        }
    }

    private void getBandsLevel() {
        int bandsNumber = playbackService.getNumberOfBands();
        for (int i = 0; i < bandsNumber; i++) {
            int bandLevel = playbackService.getBandLevel(i);

            Log.d(TAG, "band number: " + i + " level: " + bandLevel);

            bandViews.get(i).setBandLevel(bandLevel);
        }
    }


    private void createEQBands() {
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
                    customPreset = true;
                    Log.d(TAG, "band changed: " + band + " value: " + value);
                    playbackService.setBandLevel(band, (int) value);
                }
            });

            bandViews.add(bandView);
            bandRoot.addView(bandView);
        }

        presetList.add(POSITION_NONE, getString(R.string.preset_spinner_none));
        presetList.add(POSITION_CUSTOM_PRESET, getString(R.string.preset_spinner_custom));
        for (int j = 0; j < presetNumber; j++) {
            String presetName = playbackService.getPresetName(j);
            Log.d(TAG, "preset name: " + presetName);
            presetList.add(presetName);
        }
    }

    private void setPresetSpinnerItems() {

        CustomSpinnerAdapter customSpinnerAdapter = new CustomSpinnerAdapter(getContext(),
                presetList, R.color.primary_dark, R.color.primary_dark);

        presetsSpinner.setAdapter(customSpinnerAdapter);
        presetsSpinner.setSelection(0, false);
        presetsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "position: " + position + " name: " + presetList.get(position));
                switch (position) {
                    case POSITION_NONE:
                        setBandsCenter();
                        enableEQ(false);
                        EQState.setChecked(false);
                        settingsManager.setEQPresetState(POSITION_NONE);
                        break;
                    case POSITION_CUSTOM_PRESET:
                        setCustomPreset();
                        settingsManager.setEQPresetState(POSITION_CUSTOM_PRESET);
                        break;
                    default:
                        usePreset(position);
                        settingsManager.setEQPresetState(POSITION_PRESET);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void enableEQ(boolean enable) {
        playbackService.setEnableEQ(enable);
        settingsManager.setEQState(enable);
        enableBands(enable);
        enablePresetsSpinner(enable);
    }

    private void usePreset(int position) {
        customPreset = false;
        playbackService.usePreset(position - OFFSET);
        settingsManager.setEQPresetValue(position);
        getBandsLevel();
    }


    private void saveBandsProgress() {
        List<Double> levels = new ArrayList<>();
        for (EQBandView bandView : bandViews) {
            levels.add(bandView.getProgress());
        }
        settingsManager.setCustomBandsLevel(levels);
    }

    private void setCustomPreset() {
        List<Double> levels = settingsManager.getCustomBandsLevels();
        int bandsNumber = playbackService.getNumberOfBands();
        for (int i = 0; i < levels.size(); i++) {
            if (i <= bandsNumber) {
                double value = levels.get(i);
                Log.wtf(TAG, "custom band value: " + value);
                playbackService.setBandLevel(i, (int) value);
                bandViews.get(i).setBandLevel((int) value);
            }
        }
    }

    private void enableBands(boolean enable) {
        for (EQBandView bandView : bandViews) {
            bandView.enable(enable);
        }
    }

    private void setBandsCenter() {
        for (EQBandView bandView : bandViews) {
            bandView.setCenterProgress();
        }
    }

    private void enablePresetsSpinner(boolean enable) {
        presetsSpinner.setEnabled(enable);
    }

}
