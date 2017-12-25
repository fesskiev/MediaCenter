package com.fesskiev.mediacenter.widgets.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.Observable;;
import io.reactivex.android.schedulers.AndroidSchedulers;;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class VideoFileDetailsDialog extends DialogFragment implements TextWatcher {

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

    private Disposable subscription;

    @Inject
    DataRepository repository;
    @Inject
    BitmapHelper bitmapHelper;

    private VideoFile videoFile;

    private ImageView cover;
    private EditText filerName;
    private TextView filePath;
    private TextView fileSize;
    private TextView fileLength;
    private TextView fileResolution;
    private TextView fileTimestamp;
    private CheckBox hideFolder;
    private Button saveFileNameButton;

    private String folderNameChanged;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomFragmentDialog);
        MediaApplication.getInstance().getAppComponent().inject(this);

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
            filerName = view.findViewById(R.id.fileName);
            cover = view.findViewById(R.id.fileCover);
            filePath = view.findViewById(R.id.filePath);
            fileSize = view.findViewById(R.id.fileSize);
            fileLength = view.findViewById(R.id.fileLength);
            fileResolution = view.findViewById(R.id.fileResolution);
            fileTimestamp = view.findViewById(R.id.fileTimestamp);
            hideFolder = view.findViewById(R.id.hiddenVideoFileCheckBox);
            hideFolder.setTypeface(ResourcesCompat.getFont(getContext(), R.font.ubuntu));
            hideFolder.setOnCheckedChangeListener((buttonView, isChecked) -> changeHiddenFleState(isChecked));

            view.findViewById(R.id.refreshVideoFile).setOnClickListener(v -> refreshVideoFile());

            saveFileNameButton = view.findViewById(R.id.saveFileNameButton);
            saveFileNameButton.setOnClickListener(view1 -> renameFile());

            updateDialog(videoFile);
        }
    }

    private void renameFile() {
        if (!TextUtils.isEmpty(folderNameChanged)) {
            subscription = Observable.just(folderNameChanged)
                    .subscribeOn(Schedulers.io())
                    .flatMap(this::renameFile)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(object -> updateFilePath())
                    .subscribe(audioFiles -> refreshCache(), Throwable::printStackTrace);
        }
    }

    private Observable<Object> renameFile(String toDir) {
        File toPath = new File(videoFile.filePath.getParent() + "/" + toDir);
        try {
            FileUtils.moveFile(videoFile.filePath, toPath);

            videoFile.filePath = toPath;
            videoFile.description = toPath.getName();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return repository.updateVideoFile(videoFile);
    }

    private void updateFilePath() {
        filerName.setText(videoFile.getFilePath());
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String value = s.toString();
        if (!TextUtils.isEmpty(value)) {
            folderNameChanged = value;
            if (folderNameChanged.equals(videoFile.description)) {
                saveFileNameButton.setVisibility(View.INVISIBLE);
            } else {
                saveFileNameButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void refreshVideoFile() {
        subscription = Observable.just(videoFile.fetchVideoData())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(this::updateDialog)
                .flatMap(videoFile -> repository.updateVideoFile(videoFile).subscribeOn(Schedulers.io()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> refreshCache());
    }

    private void updateDialog(VideoFile videoFile) {

        filerName.setText(videoFile.description);
        filerName.addTextChangedListener(this);
        filerName.setSelection(videoFile.getFileName().length());

        filePath.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_path),
                videoFile.filePath.getAbsolutePath()));
        filePath.setSelected(true);
        fileSize.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_size),
                Utils.humanReadableByteCount(videoFile.size, false)));
        fileLength.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_duration),
                Utils.getVideoFileTimeFormat(videoFile.duration)));
        fileResolution.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.video_resolution),
                videoFile.resolution));
        fileTimestamp.setText(String.format("%1$s %2$s", getString(R.string.folder_details_timestamp),
                new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date(videoFile.timestamp))));

        if (videoFile.isHidden) {
            hideFolder.setChecked(true);
        } else {
            hideFolder.setChecked(false);
        }
        subscription = bitmapHelper.loadVideoFileFrame(videoFile.framePath)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> cover.setImageBitmap(bitmap));
    }

    private void changeHiddenFleState(boolean hide) {
        subscription = Observable.just(hide)
                .subscribeOn(Schedulers.io())
                .flatMap(this::updateHiddenVideoFilesState)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> refreshCache(), Throwable::printStackTrace);
    }

    private void refreshCache() {
        if (listener != null) {
            listener.onRefreshVideoFiles();
        }
    }

    private Observable<Object> updateHiddenVideoFilesState(boolean hidden) {
        videoFile.isHidden = hidden;
        return repository.updateVideoFile(videoFile);
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
