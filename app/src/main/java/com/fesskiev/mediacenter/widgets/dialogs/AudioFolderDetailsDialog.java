package com.fesskiev.mediacenter.widgets.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class AudioFolderDetailsDialog extends DialogFragment {

    public interface OnAudioFolderDetailsDialogListener {

        void onRefreshFolders();
    }

    private static final String DETAIL_AUDIO_FOLDER = "com.fesskiev.player.DETAIL_AUDIO_FOLDER";

    public static AudioFolderDetailsDialog newInstance(AudioFolder audioFolder) {
        AudioFolderDetailsDialog dialog = new AudioFolderDetailsDialog();
        Bundle args = new Bundle();
        args.putParcelable(DETAIL_AUDIO_FOLDER, audioFolder);
        dialog.setArguments(args);
        return dialog;
    }

    private OnAudioFolderDetailsDialogListener listener;

    private TextView folderSizeText;
    private TextView folderLengthText;
    private TextView folderTrackCountText;

    private Subscription subscription;
    private DataRepository repository;

    private AudioFolder audioFolder;

    private long folderSize = 0L;
    private long folderLength = 0L;
    private int folderTrackCount = 0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomFragmentDialog);

        audioFolder = getArguments().getParcelable(DETAIL_AUDIO_FOLDER);
        repository = MediaApplication.getInstance().getRepository();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        /*
         *  bug! http://stackoverflow.com/questions/32784009/styling-custom-dialog-fragment-not-working?noredirect=1&lq=1
         */
        return getActivity().getLayoutInflater().inflate(R.layout.dialog_audio_folder_details, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (audioFolder != null) {
            ImageView cover = (ImageView) view.findViewById(R.id.folderCover);
            BitmapHelper.getInstance().loadAudioFolderArtwork(audioFolder, cover);

            TextView folderName = (TextView) view.findViewById(R.id.folderName);
            folderName.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_name),
                    audioFolder.folderName));
            folderName.setSelected(true);

            TextView folderPath = (TextView) view.findViewById(R.id.folderPath);
            folderPath.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_path),
                    audioFolder.folderPath.getAbsolutePath()));
            folderPath.setSelected(true);

            folderSizeText = (TextView) view.findViewById(R.id.folderSize);
            folderLengthText = (TextView) view.findViewById(R.id.folderLength);
            folderTrackCountText = (TextView) view.findViewById(R.id.folderTrackCount);

            TextView folderTimestamp = (TextView) view.findViewById(R.id.folderTimestamp);
            folderTimestamp.setText(String.format("%1$s %2$s", getString(R.string.folder_details_timestamp),
                    new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date(audioFolder.timestamp))));

            CheckBox hideFolder = (CheckBox) view.findViewById(R.id.hiddenFolderCheckBox);
            if (audioFolder.isHidden) {
                hideFolder.setChecked(true);
            }
            hideFolder.setOnCheckedChangeListener((buttonView, isChecked) -> changeHiddenFolderState(isChecked));

        }
        fetchTrackList();
    }

    private void changeHiddenFolderState(boolean hidden) {
        subscription = repository.getFolderTracks(audioFolder.id)
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(audioFiles -> updateHiddenAudioFolder(hidden))
                .doOnNext(audioFiles -> updateHiddenAudioFiles(audioFiles, hidden))
                .subscribe(audioFiles -> refreshCache());
    }

    private void updateHiddenAudioFiles(List<AudioFile> audioFiles, boolean hidden) {
        for (AudioFile audioFile : audioFiles) {
            audioFile.isHidden = hidden;
            repository.updateHiddenAudioFile(audioFile);
        }
    }

    private void updateHiddenAudioFolder(boolean hidden) {
        audioFolder.isHidden = hidden;
        repository.updateHiddenAudioFolder(audioFolder);
    }

    private void refreshCache() {
        if (listener != null) {
            listener.onRefreshFolders();
        }
    }

    private void fetchTrackList() {
        subscription = repository.getFolderTracks(audioFolder.id)
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::calculateValues);
    }

    private void calculateValues(List<AudioFile> audioFiles) {
        for (AudioFile audioFile : audioFiles) {
            folderSize += audioFile.size;
            folderLength += audioFile.length;
            folderTrackCount += 1;
        }

        folderSizeText.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_size),
                Utils.humanReadableByteCount(folderSize, false)));

        folderLengthText.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_duration),
                Utils.getDurationString(folderLength)));

        folderTrackCountText.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_count),
                folderTrackCount));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
    }

    public void setOnAudioFolderDetailsDialogListener(OnAudioFolderDetailsDialogListener l) {
        this.listener = l;
    }
}
