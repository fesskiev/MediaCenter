package com.fesskiev.mediacenter.ui.settings;


import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.ui.chooser.FileSystemChooserActivity;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.widgets.settings.MediaContentUpdateTimeView;


public class SettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {


    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    private AppSettingsManager appSettingsManager;
    private TextView recordSavePath;

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
                (SwitchCompat) view.findViewById(R.id.playHeadsetPlugInSwitch),
                //TODO hide download switch temp
                (SwitchCompat) view.findViewById(R.id.downloadWifiSwitch),
                (SwitchCompat) view.findViewById(R.id.encryptDataSwitch),
                (SwitchCompat) view.findViewById(R.id.showHiddenFilesSwitch),
                (SwitchCompat) view.findViewById(R.id.fullScreenSwitch),
                (SwitchCompat) view.findViewById(R.id.enableAppGuideSwitch)
        };

        Typeface tf = ResourcesCompat.getFont(getContext(), R.font.ubuntu);
        for (SwitchCompat switchCompat : switches) {
            switchCompat.setTypeface(tf);
        }

        recordSavePath = (TextView) view.findViewById(R.id.recordPathToSave);
        recordSavePath.setText(appSettingsManager.getRecordPath());

        view.findViewById(R.id.recordContainer).setOnClickListener(v -> startChooserActivity());

        MediaContentUpdateTimeView contentUpdateTimeView
                = (MediaContentUpdateTimeView) view.findViewById(R.id.mediaContentUpdateTime);
        contentUpdateTimeView.setOnMediaContentTimeUpdateListener(new MediaContentUpdateTimeView
                .OnMediaContentTimeUpdateListener() {
            @Override
            public void onUpdateByTime(int time) {
                startBackgroundJob(time);
            }

            @Override
            public void onCancelUpdateByTime() {
                stopBackgroundJob();
            }
        });

        ImageView timerView = (ImageView) view.findViewById(R.id.timerView);
        view.findViewById(R.id.searchFilesTitleContainer).setOnClickListener(v -> {
            ((Animatable) timerView.getDrawable()).start();
            contentUpdateTimeView.toggleWithAnimate();
        });

        setSettingsState(switches);
        for (SwitchCompat switchCompat : switches) {
            switchCompat.setOnCheckedChangeListener(this);
        }
    }

    private void startChooserActivity() {
        Intent intent = new Intent(getActivity(), FileSystemChooserActivity.class);
        intent.putExtra(FileSystemChooserActivity.EXTRA_SELECT_TYPE, FileSystemChooserActivity.TYPE_FOLDER);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == FileSystemChooserActivity.RESULT_CODE_PATH_SELECTED) {
            String recordPath = data.getStringExtra(FileSystemChooserActivity.RESULT_SELECTED_PATH);
            setRecordPath(recordPath);
        }
    }

    private void setRecordPath(String recordPath) {
        appSettingsManager.setRecordPath(recordPath);
        recordSavePath.setText(recordPath);
    }

    private void setSettingsState(SwitchCompat[] switches) {
        for (SwitchCompat switchCompat : switches) {
            switch (switchCompat.getId()) {
                case R.id.playHeadsetPlugInSwitch:
                    switchCompat.setChecked(appSettingsManager.isPlayPlugInHeadset());
                    break;
                case R.id.downloadWifiSwitch:
                    switchCompat.setChecked(appSettingsManager.isDownloadWiFiOnly());
                    break;
                case R.id.encryptDataSwitch:
                    switchCompat.setChecked(appSettingsManager.isNeedEncrypt());
                    break;
                case R.id.showHiddenFilesSwitch:
                    switchCompat.setChecked(appSettingsManager.isShowHiddenFiles());
                    break;
                case R.id.fullScreenSwitch:
                    switchCompat.setChecked(appSettingsManager.isFullScreenMode());
                    break;
                case R.id.enableAppGuideSwitch:
                    switchCompat.setChecked(appSettingsManager.isNeedGuide());
                    break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton switchCompat, boolean isChecked) {
        switch (switchCompat.getId()) {
            case R.id.fullScreenSwitch:
                appSettingsManager.setFullScreenMode(isChecked);
                break;
            case R.id.playHeadsetPlugInSwitch:
                appSettingsManager.setPlayPlugInHeadset(isChecked);
                break;
            case R.id.downloadWifiSwitch:
                appSettingsManager.setDownloadWiFiOnly(isChecked);
                break;
            case R.id.encryptDataSwitch:
                appSettingsManager.setEncrypt(isChecked);
                break;
            case R.id.showHiddenFilesSwitch:
                appSettingsManager.setShowHiddenFiles(isChecked);
                refreshRepository();
                break;
            case R.id.enableAppGuideSwitch:
                appSettingsManager.setNeedGuide(isChecked);
                break;
        }
    }

    private void refreshRepository() {
        DataRepository repository = MediaApplication.getInstance().getRepository();
        repository.getMemorySource().setCacheArtistsDirty(true);
        repository.getMemorySource().setCacheGenresDirty(true);
        repository.getMemorySource().setCacheFoldersDirty(true);
    }

    private void startBackgroundJob(int periodic) {
        FileSystemService.scheduleJob(getContext().getApplicationContext(), periodic);
    }

    private void stopBackgroundJob() {
        FileSystemService.cancelJob(getContext().getApplicationContext());
    }
}
