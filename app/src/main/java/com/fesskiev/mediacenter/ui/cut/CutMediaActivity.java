package com.fesskiev.mediacenter.ui.cut;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.MediaFile;
import com.fesskiev.mediacenter.ui.audio.player.AudioPlayerActivity;
import com.fesskiev.mediacenter.ui.chooser.FileSystemChooserActivity;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.ffmpeg.FFmpegHelper;
import com.fesskiev.mediacenter.widgets.MaterialProgressBar;
import com.fesskiev.mediacenter.widgets.seekbar.RangeSeekBar;

import java.io.File;


public class CutMediaActivity extends AnalyticsActivity {

    public static void startCutMediaActivity(Activity activity, int cutType) {
        Intent intent = new Intent(activity, CutMediaActivity.class);
        intent.putExtra(EXTRA_CUT_TYPE, cutType);
        activity.startActivity(intent);
    }

    private final static String EXTRA_CUT_TYPE = "com.fesskiev.mediacenter.EXTRA_CUT_TYPE";

    public final static int CUT_AUDIO = 0;
    public final static int CUT_VIDEO = 1;

    private final static int REQUEST_FOLDER = 0;

    private AppSettingsManager settingsManager;

    private TextView saveFolderPath;
    private TextInputEditText fileName;
    private MaterialProgressBar progressBar;

    private MediaFile mediaFile;

    private int cutType;

    private int startCut;
    private int endCut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut);

        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.activity_window_height, typedValue, true);
        float scaleValue = typedValue.getFloat();
        int height = (int) (getResources().getDisplayMetrics().heightPixels * scaleValue);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height);

        settingsManager = AppSettingsManager.getInstance();
        cutType = getIntent().getIntExtra(EXTRA_CUT_TYPE, -1);
        if (cutType != -1) {
            switch (cutType) {
                case CUT_AUDIO:
                    mediaFile = MediaApplication.getInstance().getAudioPlayer().getCurrentTrack();
                    break;
                case CUT_VIDEO:
                    mediaFile = MediaApplication.getInstance().getVideoPlayer().getCurrentVideoFile();
                    break;
            }
        }


        findViewById(R.id.cutFileFab).setOnClickListener(v -> processCutFile());

        findViewById(R.id.chooseCutSaveFolder).setOnClickListener(v -> selectSaveFolder());

        RangeSeekBar<Integer> rangeSeekBar = (RangeSeekBar) findViewById(R.id.rangeSeekBar);
        if (mediaFile != null) {
            rangeSeekBar.setRangeValues(0, (int) mediaFile.getLength());
        }
        rangeSeekBar.setCutType(cutType);
        rangeSeekBar.setOnRangeSeekBarChangeListener((bar, minValue, maxValue) -> {
            startCut = minValue;
            endCut = maxValue;
        });

        progressBar = (MaterialProgressBar) findViewById(R.id.progressBar);
        saveFolderPath = (TextView) findViewById(R.id.saveFolderPath);
        fileName = (TextInputEditText) findViewById(R.id.fileName);
        if (mediaFile != null) {
            fileName.setText(mediaFile.getFileName());
        }
        setSaveFolderPath(CacheManager.getCutFolderPath().getAbsolutePath());

    }

    private void selectSaveFolder() {
        Intent intent = new Intent(this, FileSystemChooserActivity.class);
        intent.putExtra(FileSystemChooserActivity.EXTRA_SELECT_TYPE, FileSystemChooserActivity.TYPE_FOLDER);
        startActivityForResult(intent, REQUEST_FOLDER);
    }

    private void processCutFile() {
        String fileNm = fileName.getText().toString();

        if (TextUtils.isEmpty(fileNm)) {
            showErrorFileNameSnackBar();
            return;
        }
        if (endCut == 0) {
            showErrorCutRangeShackBar();
            return;
        }

        String start = null;
        String end = null;
        switch (cutType) {
            case CUT_AUDIO:
                start = Utils.getDurationString(startCut);
                end = Utils.getDurationString(endCut);
                break;
            case CUT_VIDEO:
                start = Utils.getVideoFileTimeFormat(startCut);
                end = Utils.getVideoFileTimeFormat(endCut);
                break;
        }


        String trackPath = mediaFile.getFilePath();
        File savePath = new File(CacheManager.getCutFolderPath(), fileNm);

        FFmpegHelper.getInstance().cutMedia(trackPath, savePath.getAbsolutePath(), start, end, new FFmpegHelper.OnConvertProcessListener() {
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == FileSystemChooserActivity.RESULT_CODE_PATH_SELECTED) {
            String path = data.getStringExtra(FileSystemChooserActivity.RESULT_SELECTED_PATH);
            if (requestCode == REQUEST_FOLDER) {
                setSaveFolderPath(path);
                settingsManager.setCutFolderPath(path);
            }
        }
    }

    private void setSaveFolderPath(String path) {
        saveFolderPath.setText(path);
    }


    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }


    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void showErrorFileNameSnackBar() {
        Utils.showCustomSnackbar(findViewById(R.id.cutRoot), getApplicationContext(),
                getString(R.string.snackbar_cut_error_file_name), Snackbar.LENGTH_SHORT).show();
    }

    private void showErrorCutRangeShackBar() {
        Utils.showCustomSnackbar(findViewById(R.id.cutRoot), getApplicationContext(),
                getString(R.string.snackbar_cut_error_range), Snackbar.LENGTH_SHORT).show();
    }

    private void showErrorSnackbar() {
        Utils.showCustomSnackbar(findViewById(R.id.cutRoot), getApplicationContext(),
                getString(R.string.snackbar_cut_error), Snackbar.LENGTH_INDEFINITE).show();
    }

    private void showSuccessSnackbar() {
        Utils.showCustomSnackbar(findViewById(R.id.cutRoot), getApplicationContext(),
                getString(R.string.snackbar_cut_success), Snackbar.LENGTH_INDEFINITE).show();
    }

}
