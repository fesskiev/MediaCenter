package com.fesskiev.mediacenter.widgets.dialogs;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.utils.BitmapHelper;
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

public class AudioFolderDetailsDialog extends MediaFolderDetailsDialog {

    public static MediaFolderDetailsDialog newInstance(AudioFolder audioFolder) {
        MediaFolderDetailsDialog dialog = new AudioFolderDetailsDialog();
        Bundle args = new Bundle();
        args.putParcelable(DETAIL_MEDIA_FOLDER, audioFolder);
        dialog.setArguments(args);
        return dialog;
    }

    private AudioFolder audioFolder;
    private String folderNameChanged;
    private String fromDir;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        audioFolder = getArguments().getParcelable(DETAIL_MEDIA_FOLDER);
    }

    @Override
    public void fillFolderData() {

        BitmapHelper.getInstance().loadAudioFolderArtwork(audioFolder, cover);

        saveFolderNameButton.setOnClickListener(v -> saveAudioFolderName());

        folderName.setText(audioFolder.getFolderName());

        folderPath.setText(String.format(Locale.US, "%1$s %2$s", getString(R.string.folder_details_path), audioFolder.getPath()));
        folderPath.setSelected(true);

        folderTimestamp.setText(String.format("%1$s %2$s", getString(R.string.folder_details_timestamp),
                new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date(audioFolder.getTimestamp()))));

        if (audioFolder.isHidden()) {
            hideFolder.setChecked(true);
        }
        hideFolder.setOnCheckedChangeListener((buttonView, isChecked) -> changeHiddenFolderState(isChecked));
    }

    @Override
    public void fetchFolderFiles() {
        subscription = repository.getAudioTracks(audioFolder.getId())
                .subscribeOn(Schedulers.io())
                .firstOrError()
                .toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::calculateValues);
    }

    @Override
    public void folderNameChanged(String name) {
        folderNameChanged = name;
        if (folderNameChanged.equals(audioFolder.folderName)) {
            saveFolderNameButton.setVisibility(View.INVISIBLE);
        } else {
            saveFolderNameButton.setVisibility(View.VISIBLE);
        }
    }

    private void saveAudioFolderName() {
        if (!TextUtils.isEmpty(folderNameChanged)) {
            subscription = Observable.just(folderNameChanged)
                    .subscribeOn(Schedulers.io())
                    .flatMap(toDir -> Observable.just(renameFolder(toDir)))
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(this::updateFolderPath)
                    .subscribeOn(Schedulers.io())
                    .flatMap(toDir -> repository.getAudioTracks(audioFolder.getId())
                            .firstOrError()
                            .toObservable()
                            .doOnNext(audioFiles -> renameFiles(audioFiles, toDir))
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext(audioFiles -> refreshCache()))
                    .doOnNext(audioFiles -> audioPlayer.updateCurrentTrackAndTrackList())
                    .subscribe(audioFiles -> {

                    }, Throwable::printStackTrace);
        }
    }

    private void updateFolderPath(String folderName) {
        folderPath.setText(String.format(Locale.US, "%1$s/%2$s",
                audioFolder.folderPath.getParent(), folderName));
    }

    private String renameFolder(String toDir) {
        File toPath = new File(audioFolder.folderPath.getParent() + "/" + toDir);
        try {
            FileUtils.moveDirectory(audioFolder.folderPath, toPath);

            fromDir = audioFolder.folderPath.getName();

            audioFolder.folderPath = toPath;
            audioFolder.folderName = toPath.getName();
            File folderImage = audioFolder.folderImage;
            if (folderImage != null) {
                audioFolder.folderImage = new File(folderImage.getAbsolutePath().replace(fromDir, toDir));
            }
            repository.updateAudioFolder(audioFolder);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return toDir;
    }

    private void renameFiles(List<AudioFile> audioFiles, String toDir) {
        for (AudioFile audioFile : audioFiles) {

            audioFile.filePath = new File(audioFile.getFilePath().replace(fromDir, toDir));

            String artworkPath = audioFile.artworkPath;
            if (artworkPath != null) {
                audioFile.artworkPath = artworkPath.replace(fromDir, toDir);
            }
            String folderArtworkPath = audioFile.folderArtworkPath;
            if (folderArtworkPath != null) {
                audioFile.folderArtworkPath = folderArtworkPath.replace(fromDir, toDir);
            }
            repository.updateAudioFile(audioFile);
        }
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

    private void changeHiddenFolderState(boolean hidden) {
        subscription = repository.getAudioTracks(audioFolder.getId())
                .subscribeOn(Schedulers.io())
                .firstOrError()
                .toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(audioFiles -> updateHiddenAudioFolder(hidden))
                .doOnNext(audioFiles -> updateHiddenAudioFiles(audioFiles, hidden))
                .subscribe(audioFiles -> refreshCache());
    }

    private void updateHiddenAudioFiles(List<AudioFile> audioFiles, boolean hidden) {
        for (AudioFile audioFile : audioFiles) {
            audioFile.isHidden = hidden;
            repository.updateAudioFile(audioFile);
        }
    }

    private void updateHiddenAudioFolder(boolean hidden) {
        audioFolder.isHidden = hidden;
        repository.updateAudioFolder(audioFolder);
    }


}
