package com.fesskiev.mediacenter.ui.converter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.ui.chooser.FileSystemChooserActivity;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.ffmpeg.Format;
import com.fesskiev.mediacenter.widgets.progress.MaterialProgressBar;

import javax.inject.Inject;


public class ConverterFragment extends Fragment {

    protected final static int REQUEST_FOLDER = 0;
    protected final static int REQUEST_FILE = 1;

    protected MaterialProgressBar progressBar;
    protected TextView saveFolderPath;
    protected TextView convertFilePath;

    protected Format format;

    @Inject
    AppSettingsManager settingsManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MediaApplication.getInstance().getAppComponent().inject(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        saveFolderPath = view.findViewById(R.id.saveFolderPath);
        convertFilePath = view.findViewById(R.id.convertFilePath);

        progressBar = view.findViewById(R.id.progressBar);
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

    protected void selectSaveFolder() {
        Intent intent = new Intent(getActivity(), FileSystemChooserActivity.class);
        intent.putExtra(FileSystemChooserActivity.EXTRA_SELECT_TYPE, FileSystemChooserActivity.TYPE_FOLDER);
        startActivityForResult(intent, REQUEST_FOLDER);
    }

    protected void selectConvertFile() {
        Intent intent = new Intent(getActivity(), FileSystemChooserActivity.class);
        intent.putExtra(FileSystemChooserActivity.EXTRA_SELECT_TYPE, FileSystemChooserActivity.TYPE_FILE);
        startActivityForResult(intent, REQUEST_FILE);
    }

    protected void setConvertFilePath(String path) {
        convertFilePath.setText(path);
    }

    protected void setSaveFolderPath(String path) {
        saveFolderPath.setText(path);
    }

    protected void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    protected void showSelectFormatSnackbar() {
        Utils.showCustomSnackbar(getView(), getContext(),
                getString(R.string.snackbar_converter_error_format), Snackbar.LENGTH_SHORT).show();
    }

    protected void showErrorSnackbar() {
        Utils.showCustomSnackbar(getView(), getContext(),
                getString(R.string.snackbar_converter_error_convert), Snackbar.LENGTH_INDEFINITE).show();
    }

    protected void showSuccessSnackbar() {
        Utils.showCustomSnackbar(getView(), getContext(),
                getString(R.string.snackbar_converter_success_convert), Snackbar.LENGTH_INDEFINITE).show();
    }

    protected void showEmptyFilePathSnackbar() {
        Utils.showCustomSnackbar(getView(), getContext(),
                getString(R.string.snackbar_converter_error_path), Snackbar.LENGTH_SHORT).show();
    }
}
