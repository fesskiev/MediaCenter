package com.fesskiev.mediacenter.widgets.dialogs;


import android.os.Bundle;
import android.support.annotation.Nullable;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.VideoFolder;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class VideoFolderDetailsDialog extends MediaFolderDetailsDialog {

    public static MediaFolderDetailsDialog newInstance(VideoFolder videoFolder) {
        MediaFolderDetailsDialog dialog = new VideoFolderDetailsDialog();
        Bundle args = new Bundle();
        args.putParcelable(DETAIL_MEDIA_FOLDER, videoFolder);
        dialog.setArguments(args);
        return dialog;
    }

    private VideoFolder videoFolder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        videoFolder = getArguments().getParcelable(DETAIL_MEDIA_FOLDER);
    }

    @Override
    public void fillFolderData() {

        folderName.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_name),
                videoFolder.getFolderName()));
        folderName.setSelected(true);

        folderPath.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_path),
                videoFolder.getPath()));
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
        subscription = repository.getVideoFiles(videoFolder.getId())
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(videoFiles -> {
                    for (VideoFile videoFile : videoFiles) {
                        String path = videoFile.framePath;
                        if (path != null) {
                            BitmapHelper.getInstance().loadVideoFileCover(path, cover);
                            break;
                        }
                    }
                    return Observable.just(videoFiles);
                })
                .subscribe(this::calculateValues);
    }

    private void calculateValues(List<VideoFile> videoFiles) {
        for (VideoFile videoFile : videoFiles) {
            folderSize += videoFile.size;
            folderLength += videoFile.length;
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
        subscription = repository.getVideoFiles(videoFolder.getId())
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(videoFiles -> updateHiddenVideoFolder(hidden))
                .doOnNext(videoFiles -> updateHiddenVideoFiles(videoFiles, hidden))
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
