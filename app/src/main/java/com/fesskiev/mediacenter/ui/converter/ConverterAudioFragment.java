package com.fesskiev.mediacenter.ui.converter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.ui.chooser.FileSystemChooserActivity;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.ffmpeg.FFmpegHelper;
import com.fesskiev.mediacenter.utils.ffmpeg.AudioFormat;
import com.fesskiev.mediacenter.widgets.MaterialProgressBar;


public class ConverterAudioFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    public static ConverterAudioFragment newInstance() {
        return new ConverterAudioFragment();
    }

    private final static int REQUEST_FOLDER = 0;
    private final static int REQUEST_FILE = 1;

    private AudioPlayer audioPlayer;
    private AppSettingsManager settingsManager;

    private AudioFormat audioFormat;

    private MaterialProgressBar progressBar;
    private TextView saveFolderPath;
    private TextView convertFilePath;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        settingsManager = AppSettingsManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_converter_audio, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        saveFolderPath = (TextView) view.findViewById(R.id.saveFolderPath);
        convertFilePath = (TextView) view.findViewById(R.id.convertFilePath);

        progressBar = (MaterialProgressBar) view.findViewById(R.id.progressBar);
        ((RadioGroup) view.findViewById(R.id.radioGroupConvertFormat)).setOnCheckedChangeListener(this);

        view.findViewById(R.id.convertFileFab).setOnClickListener(v -> startConvertFile());
        view.findViewById(R.id.chooseConvertFile).setOnClickListener(v -> selectConvertFile());
        view.findViewById(R.id.chooseConvertSaveFolder).setOnClickListener(v -> selectSaveFolder());

        AudioFile audioFile = audioPlayer.getCurrentTrack();
        if (audioFile != null) {
            setConvertFilePath(audioFile.getFilePath());
        }
        setSaveFolderPath(settingsManager.geConvertFolderPath());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == FileSystemChooserActivity.RESULT_CODE_PATH_SELECTED) {
            String path = data.getStringExtra(FileSystemChooserActivity.RESULT_SELECTED_PATH);
            if (requestCode == REQUEST_FOLDER) {
                setSaveFolderPath(path);
                settingsManager.setConvertFolderPath(path);
            } else if (requestCode == REQUEST_FILE) {
                setConvertFilePath(path);
            }
        }

    }

    private void selectSaveFolder() {
        Intent intent = new Intent(getActivity(), FileSystemChooserActivity.class);
        intent.putExtra(FileSystemChooserActivity.EXTRA_SELECT_TYPE, FileSystemChooserActivity.TYPE_FOLDER);
        startActivityForResult(intent, REQUEST_FOLDER);
    }

    private void selectConvertFile() {
        Intent intent = new Intent(getActivity(), FileSystemChooserActivity.class);
        intent.putExtra(FileSystemChooserActivity.EXTRA_SELECT_TYPE, FileSystemChooserActivity.TYPE_FILE);
        startActivityForResult(intent, REQUEST_FILE);
    }

    private void setConvertFilePath(String path) {
        convertFilePath.setText(path);
    }

    private void setSaveFolderPath(String path) {
        saveFolderPath.setText(path);
    }

    private void startConvertFile() {
        if (audioFormat == null) {
            showSelectAudioFormatSnackbar();
            return;
        }

        String convertFile = convertFilePath.getText().toString();
        if(TextUtils.isEmpty(convertFile)){
            showEmptyFilePathSnackbar();
            return;
        }

        String saveFolder = saveFolderPath.getText().toString();

        FFmpegHelper.getInstance().convertAudio(convertFile, saveFolder,
                audioFormat,
                new FFmpegHelper.OnConvertProcessListener() {
                    @Override
                    public void onStart() {
                        showProgressBar();
                    }

                    @Override
                    public void onSuccess(AudioFile audioFile) {
                        hideProgressBar();
                        showSuccessSnackbar();
                    }

                    @Override
                    public void onFailure(Exception error) {
                        error.printStackTrace();
                        hideProgressBar();
                        showErrorSnackbar(error);
                    }
                });
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radioMP3:
                audioFormat = AudioFormat.MP3;
                break;
            case R.id.radioFLAC:
                audioFormat = AudioFormat.FLAC;
                break;
            case R.id.radioM4A:
                audioFormat = AudioFormat.M4A;
                break;
            case R.id.radioWAV:
                audioFormat = AudioFormat.WAV;
                break;
            case R.id.radioAAC:
                audioFormat = AudioFormat.AAC;
                break;
        }
    }


    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }


    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void showEmptyFilePathSnackbar() {
        Utils.showCustomSnackbar(getView(), getContext(),
                "You must select audio file for converting", Snackbar.LENGTH_SHORT).show();
    }

    private void showSelectAudioFormatSnackbar() {
        Utils.showCustomSnackbar(getView(), getContext(),
                "You must select audio format for converting", Snackbar.LENGTH_SHORT).show();
    }

    private void showErrorSnackbar(Exception error) {
        Utils.showCustomSnackbar(getView(), getContext(),
                "Error convert: " + error.getMessage(), Snackbar.LENGTH_INDEFINITE).show();
    }

    private void showSuccessSnackbar() {
        Utils.showCustomSnackbar(getView(), getContext(),
                "Convert Success!", Snackbar.LENGTH_INDEFINITE).show();
    }


}
