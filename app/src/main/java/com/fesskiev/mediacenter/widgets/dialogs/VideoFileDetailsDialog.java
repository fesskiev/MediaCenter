package com.fesskiev.mediacenter.widgets.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
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

    private ImageView cover;
    private TextView filerName;
    private TextView filePath;
    private TextView fileSize;
    private TextView fileLength;
    private TextView fileResolution;
    private TextView fileTimestamp;
    private CheckBox hideFolder;

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

            cover = (ImageView) view.findViewById(R.id.fileCover);
            filerName = (TextView) view.findViewById(R.id.fileName);
            filePath = (TextView) view.findViewById(R.id.filePath);
            fileSize = (TextView) view.findViewById(R.id.fileSize);
            fileLength = (TextView) view.findViewById(R.id.fileLength);
            fileResolution = (TextView) view.findViewById(R.id.fileResolution);
            fileTimestamp = (TextView) view.findViewById(R.id.fileTimestamp);
            hideFolder = (CheckBox) view.findViewById(R.id.hiddenVideoFileCheckBox);
            hideFolder.setTypeface(ResourcesCompat.getFont(getContext(), R.font.ubuntu));
            hideFolder.setOnCheckedChangeListener((buttonView, isChecked) -> changeHiddenFleState(isChecked));

            view.findViewById(R.id.refreshVideoFile).setOnClickListener(v -> refreshVideoFile());

            updateDialog(videoFile);
        }
    }

    private void refreshVideoFile() {
        subscription = Observable.just(videoFile.fetchVideoData())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(videoFile -> repository.updateVideoFile(videoFile))
                .doOnNext(this::updateDialog)
                .subscribe(aBoolean -> refreshCache());
    }

    private void updateDialog(VideoFile videoFile) {

        filerName.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.video_details_name),
                videoFile.description));
        filerName.setSelected(true);
        filePath.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_path),
                videoFile.filePath.getAbsolutePath()));
        filePath.setSelected(true);
        fileSize.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_size),
                Utils.humanReadableByteCount(videoFile.size, false)));
        fileLength.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_duration),
                Utils.getVideoFileTimeFormat(videoFile.length)));
        fileResolution.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.video_resolution),
                videoFile.resolution));
        fileTimestamp.setText(String.format("%1$s %2$s", getString(R.string.folder_details_timestamp),
                new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date(videoFile.timestamp))));

        if (videoFile.isHidden) {
            hideFolder.setChecked(true);
        } else {
            hideFolder.setChecked(false);
        }
        BitmapHelper.getInstance().loadVideoFileCover(videoFile.framePath, cover);
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
