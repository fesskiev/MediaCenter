package com.fesskiev.mediacenter.widgets.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class VideoFileDetailsDialog extends DialogFragment {

    public interface OnVideoFileDetailsDialogListener {

        void onRefreshVideoFiles();
    }

    private static final String DETAIL_VIDEO_FILE = "com.fesskiev.player.DETAIL_VIDEO_FILE";

    public static VideoFileDetailsDialog newInstance(VideoFile videoFile) {
        VideoFileDetailsDialog dialog = new VideoFileDetailsDialog();
        Bundle args = new Bundle();
        args.putParcelable(DETAIL_VIDEO_FILE, videoFile);
        dialog.setArguments(args);
        return dialog;
    }

    private OnVideoFileDetailsDialogListener listener;

    private Subscription subscription;
    private DataRepository repository;

    private VideoFile videoFile;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomFragmentDialog);

        repository = MediaApplication.getInstance().getRepository();

        videoFile = getArguments().getParcelable(DETAIL_VIDEO_FILE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
          /*
         *  bug! http://stackoverflow.com/questions/32784009/styling-custom-dialog-fragment-not-working?noredirect=1&lq=1
         */
        return getActivity().getLayoutInflater().inflate(R.layout.dialog_video_file_details, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (videoFile != null) {

            ImageView cover = (ImageView) view.findViewById(R.id.fileCover);
            BitmapHelper.getInstance().loadVideoFileCover(videoFile.framePath, cover);

            TextView filerName = (TextView) view.findViewById(R.id.fileName);
            filerName.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.video_details_name),
                    videoFile.description));
            filerName.setSelected(true);

            TextView filePath = (TextView) view.findViewById(R.id.filePath);
            filePath.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_path),
                    videoFile.filePath.getAbsolutePath()));
            filePath.setSelected(true);

            TextView fileSize = (TextView) view.findViewById(R.id.fileSize);
            fileSize.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_size),
                    Utils.humanReadableByteCount(videoFile.size, false)));

            TextView fileLength = (TextView) view.findViewById(R.id.fileLength);
            fileLength.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_duration),
                    Utils.getVideoFileTimeFormat(videoFile.length)));

            TextView fileResolution = (TextView) view.findViewById(R.id.fileResolution);
            fileResolution.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.video_resolution),
                    videoFile.resolution));

            TextView fileTimestamp = (TextView) view.findViewById(R.id.fileTimestamp);
            fileTimestamp.setText(String.format("%1$s %2$s", getString(R.string.folder_details_timestamp),
                    new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date(videoFile.timestamp))));

            CheckBox hideFolder = (CheckBox) view.findViewById(R.id.hiddenVideoFileCheckBox);
            if (videoFile.isHidden) {
                hideFolder.setChecked(true);
            }
            hideFolder.setOnCheckedChangeListener((buttonView, isChecked) -> changeHiddenFleState(isChecked));
        }
    }

    private void changeHiddenFleState(boolean hide) {
        subscription = Observable.just(hide)
                .doOnNext(this::updateHiddenVideoFilesState)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> refreshCache());
    }

    private void refreshCache() {
        if (listener != null) {
            listener.onRefreshVideoFiles();
        }
    }

    private void updateHiddenVideoFilesState(boolean hidden) {
        videoFile.isHidden = hidden;
        repository.updateVideoFile(videoFile);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
    }

    public void setOnVideoFileDetailsDialogListener(OnVideoFileDetailsDialogListener l) {
        this.listener = l;
    }
}
