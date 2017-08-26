package com.fesskiev.mediacenter.ui.converter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.players.VideoPlayer;
import com.fesskiev.mediacenter.utils.ffmpeg.FFmpegHelper;
import com.fesskiev.mediacenter.utils.ffmpeg.Format;


public class ConverterVideoFragment extends ConverterFragment implements RadioGroup.OnCheckedChangeListener {

    public static ConverterVideoFragment newInstance() {
        return new ConverterVideoFragment();
    }


    private VideoPlayer videoPlayer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoPlayer = MediaApplication.getInstance().getVideoPlayer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_converter_video, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RadioGroup radioGroup = view.findViewById(R.id.radioGroupConvertFormat);
        radioGroup.setOnCheckedChangeListener(this);
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            View childAt = radioGroup.getChildAt(i);
            if (childAt instanceof RadioButton) {
                ((RadioButton) childAt).setTypeface(ResourcesCompat.getFont(getContext(), R.font.ubuntu));
            }
        }

        view.findViewById(R.id.convertFileFab).setOnClickListener(v -> startConvertFile());
        view.findViewById(R.id.chooseConvertFile).setOnClickListener(v -> selectConvertFile());
        view.findViewById(R.id.chooseConvertSaveFolder).setOnClickListener(v -> selectSaveFolder());

        VideoFile videoFile = videoPlayer.getCurrentVideoFile();
        if (videoFile != null) {
            setConvertFilePath(videoFile.getFilePath());
        }
        setSaveFolderPath(settingsManager.geConvertFolderPath());

    }

    private void startConvertFile() {
        if (format == null) {
            showSelectFormatSnackbar();
            return;
        }

        String convertFile = convertFilePath.getText().toString();
        if (TextUtils.isEmpty(convertFile)) {
            showEmptyFilePathSnackbar();
            return;
        }

        String saveFolder = saveFolderPath.getText().toString();

        FFmpegHelper.getInstance().convertVideoFile(convertFile, saveFolder, format,
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
                        showErrorSnackbar();
                    }
                });
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radioAVI:
                format = Format.AVI;
                break;
            case R.id.radioMOV:
                format = Format.MOV;
                break;
            case R.id.radioMP4:
                format = Format.MP4;
                break;
        }
    }
}
