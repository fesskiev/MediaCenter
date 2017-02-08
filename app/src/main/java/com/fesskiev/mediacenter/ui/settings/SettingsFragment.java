package com.fesskiev.mediacenter.ui.settings;


import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.utils.AppSettingsManager;


public class SettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {


    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    private AppSettingsManager appSettingsManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appSettingsManager = AppSettingsManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SwitchCompat[] switches = new SwitchCompat[]{
                (SwitchCompat) view.findViewById(R.id.play_headset_plug_in_switch),
                (SwitchCompat) view.findViewById(R.id.search_media_switch),
                (SwitchCompat) view.findViewById(R.id.download_wifi_switch)
        };

        for (SwitchCompat switchCompat : switches) {
            switchCompat.setOnCheckedChangeListener(this);
        }

        setSettingsState(switches);
    }

    private void setSettingsState(SwitchCompat[] switches) {
        for (SwitchCompat switchCompat : switches) {
            switch (switchCompat.getId()) {
                case R.id.play_headset_plug_in_switch:
                    switchCompat.setChecked(appSettingsManager.isPlayPlugInHeadset());
                    break;
                case R.id.search_media_switch:
                    switchCompat.setChecked(appSettingsManager.isBackgroundSearch());
                    break;
                case R.id.download_wifi_switch:
                    switchCompat.setChecked(appSettingsManager.isDownloadWiFiOnly());
                    break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton switchCompat, boolean isChecked) {
        switch (switchCompat.getId()) {
            case R.id.play_headset_plug_in_switch:
                appSettingsManager.setPlayPlugInHeadset(isChecked);
                break;
            case R.id.search_media_switch:
                appSettingsManager.setBackgroundSearch(isChecked);
                updateBackgroundSearch(isChecked);
                break;
            case R.id.download_wifi_switch:
                appSettingsManager.setDownloadWiFiOnly(isChecked);
                break;
        }
    }

    private void updateBackgroundSearch(boolean isChecked) {
        if (isChecked) {
            Log.wtf("job", "START JOB");

            JobInfo job = new JobInfo.Builder(0, new ComponentName(getActivity(), FileSystemService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setRequiresCharging(true)
                    .setPeriodic(2000)
                    .build();

            JobScheduler tm = (JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);
            tm.schedule(job);
        } else {

        }
    }
}
