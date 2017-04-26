package com.fesskiev.mediacenter.ui.processing;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.converter.AudioConverterHelper;
import com.fesskiev.mediacenter.utils.converter.AudioFormat;
import com.fesskiev.mediacenter.widgets.MaterialProgressBar;


public class ConverterFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    public static ConverterFragment newInstance() {
        return new ConverterFragment();
    }

    private AudioPlayer audioPlayer;

    private AudioFormat audioFormat;

    private MaterialProgressBar progressBar;
    private TextView saveFolderPath;
    private TextView convertFilePath;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioPlayer = MediaApplication.getInstance().getAudioPlayer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_converter, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        saveFolderPath = (TextView) view.findViewById(R.id.saveFolderPath);
        convertFilePath = (TextView) view.findViewById(R.id.convertFilePath);

        progressBar = (MaterialProgressBar) view.findViewById(R.id.progressBar);
        ((RadioGroup) view.findViewById(R.id.radioGroupConvertFormat)).setOnCheckedChangeListener(this);

        view.findViewById(R.id.convertFileFab).setOnClickListener(v -> startConvertFile());

        setConvertFilePath(audioPlayer.getCurrentTrack().getFilePath());
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
        AudioConverterHelper.getInstance().convertAudio(audioPlayer.getCurrentTrack(), audioFormat,
                new AudioConverterHelper.OnConvertProcessListener() {
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
