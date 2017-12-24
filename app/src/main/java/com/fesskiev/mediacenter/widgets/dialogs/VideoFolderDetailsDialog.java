package com.fesskiev.mediacenter.widgets.dialogs;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.VideoFolder;
import com.fesskiev.mediacenter.utils.Utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;;
import io.reactivex.schedulers.Schedulers;

public class VideoFolderDetailsDialog extends MediaFolderDetailsDialog {

    public static MediaFolderDetailsDialog newInstance(VideoFolder videoFolder) {
        MediaFolderDetailsDialog dialog = new VideoFolderDetailsDialog();
        Bundle args = new Bundle();
        args.putParcelable(DETAIL_MEDIA_FOLDER, videoFolder);
        dialog.setArguments(args);
        return dialog;
    }

    private VideoFolder videoFolder;
    private String folderNameChanged;
    private String fromDir;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MediaApplication.getInstance().getAppComponent().inject(this);

        videoFolder = getArguments().getParcelable(DETAIL_MEDIA_FOLDER);
    }

    @Override
    public void fillFolderData() {

        saveFolderNameButton.setOnClickListener(v -> saveVideoFolderName());

        folderName.setText(videoFolder.getFolderName());
        folderName.setSelection(videoFolder.getFolderName().length());

        folderPath.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_path), videoFolder.getPath()));
        folderPath.setSelected(true);

        folderTimestamp.setText(String.format("%1$s %2$s", getString(R.string.folder_details_timestamp),
                new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date(videoFolder.getTimestamp()))));

        if (videoFolder.isHidden()) {
            hideFolder.setChecked(true);
        }
        hideFolder.setOnCheckedChangeListener((buttonView, isChecked) -> changeHiddenFolderState(isChecked));
    }

    @Override
    public void fetchFolderFiles() {
        disposable = repository.getVideoFiles(videoFolder.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(this::calculateValues)
                .subscribeOn(Schedulers.io())
                .flatMap(videoFiles -> {
                    for (VideoFile videoFile : videoFiles) {
                        String path = videoFile.framePath;
                        if (path != null) {
                            return bitmapHelper.loadVideoFileFrame(path);
                        }
                    }
                    return Observable.empty();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> cover.setImageBitmap(bitmap));
    }

    @Override
    public void folderNameChanged(String name) {
        folderNameChanged = name;
        if (folderNameChanged.equals(videoFolder.folderName)) {
            saveFolderNameButton.setVisibility(View.INVISIBLE);
        } else {
            saveFolderNameButton.setVisibility(View.VISIBLE);
        }
    }

    private void saveVideoFolderName() {
        if (!TextUtils.isEmpty(folderNameChanged)) {
            disposable = Observable.just(folderNameChanged)
                    .subscribeOn(Schedulers.io())
                    .flatMap(toDir -> Observable.just(renameFolder(toDir)))
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(this::updateFolderPath)
                    .subscribeOn(Schedulers.io())
                    .flatMap(toDir -> repository.getVideoFiles(videoFolder.getId())
                            .doOnNext(audioFiles -> renameFiles(audioFiles, toDir))
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext(audioFiles -> refreshCache()))
                    .subscribe(audioFiles -> hideSaveButton(), Throwable::printStackTrace);
        }
    }

    private String renameFolder(String toDir) {
        File toPath = new File(videoFolder.folderPath.getParent() + "/" + toDir);
        try {
            FileUtils.moveDirectory(videoFolder.folderPath, toPath);

            fromDir = videoFolder.folderPath.getName();

            videoFolder.folderPath = toPath;
            videoFolder.folderName = toPath.getName();

            repository.updateVideoFolder(videoFolder);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return toDir;
    }

    private void renameFiles(List<VideoFile> videoFiles, String toDir) {
        for (VideoFile videoFile : videoFiles) {
            videoFile.filePath = new File(videoFile.getFilePath().replace(fromDir, toDir));
            repository.updateVideoFile(videoFile);
        }
    }

    private void updateFolderPath(String folderName) {
        folderPath.setText(String.format(Locale.US, "%1$s/%2$s",
                videoFolder.folderPath.getParent(), folderName));
    }

    private void calculateValues(List<VideoFile> videoFiles) {
        for (VideoFile videoFile : videoFiles) {
            folderSize += videoFile.size;
            folderLength += videoFile.duration;
            folderTrackCount += 1;
        }

        folderSizeText.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_size),
                Utils.humanReadableByteCount(folderSize, false)));

        folderLengthText.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_duration),
                Utils.getVideoFileTimeFormat(folderLength)));

        folderTrackCountText.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_video_count),
                folderTrackCount));
    }

    private void changeHiddenFolderState(boolean hidden) {
        disposable = repository.getVideoFiles(videoFolder.getId())
                .subscribeOn(Schedulers.io())
                .doOnNext(videoFiles -> updateHiddenVideoFolder(hidden))
                .doOnNext(videoFiles -> updateHiddenVideoFiles(videoFiles, hidden))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(videoFiles -> refreshCache());
    }

    private void updateHiddenVideoFiles(List<VideoFile> videoFiles, boolean hidden) {
        for (VideoFile videoFile : videoFiles) {
            videoFile.isHidden = hidden;
            repository.updateVideoFile(videoFile);
        }
    }

    private void updateHiddenVideoFolder(boolean hidden) {
        videoFolder.isHidden = hidden;
        repository.updateVideoFolder(videoFolder);
    }
}
