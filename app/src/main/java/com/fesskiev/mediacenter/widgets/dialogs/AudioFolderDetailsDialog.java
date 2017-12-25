package com.fesskiev.mediacenter.widgets.dialogs;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
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

import static com.fesskiev.mediacenter.services.FileSystemService.folderImageFilter;

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
        disposable = bitmapHelper.getAudioFolderArtwork(audioFolder)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> cover.setImageBitmap(bitmap));

        saveFolderNameButton.setOnClickListener(v -> saveAudioFolderName());

        folderName.setText(audioFolder.getFolderName());
        folderName.setSelection(audioFolder.getFolderName().length());

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
        disposable = repository.getAudioTracks(audioFolder.getId())
                .subscribeOn(Schedulers.io())
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
            disposable = Observable.just(folderNameChanged)
                    .subscribeOn(Schedulers.io())
                    .flatMap(this::renameFolder)
                    .flatMap(object -> repository.getAudioTracks(audioFolder.getId()))
                    .flatMap(Observable::fromIterable)
                    .flatMap(audioFiles -> renameFiles(audioFiles, folderNameChanged))
                    .toList()
                    .toObservable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(object -> updateFolderPath())
                    .doOnNext(audioFiles -> refreshCache())
                    .subscribe(audioFiles -> hideSaveButton(), Throwable::printStackTrace);
        }
    }

    private void updateFolderPath() {
        folderPath.setText(audioFolder.getPath());
    }

    private Observable<Object> renameFolder(String toDir) {
        File toPath = new File(audioFolder.folderPath.getParent() + "/" + toDir);
        try {
            FileUtils.moveDirectory(audioFolder.folderPath, toPath);

            fromDir = audioFolder.folderPath.getName();

            audioFolder.folderPath = toPath;
            audioFolder.folderName = toPath.getName();

            if (audioFolder.folderImage != null) {
                File[] filterImages = toPath.listFiles(folderImageFilter());
                if (filterImages != null && filterImages.length > 0) {
                    audioFolder.folderImage = filterImages[0];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return repository.updateAudioFolder(audioFolder);
    }

    private Observable<Object> renameFiles(AudioFile audioFile, String toDir) {

        audioFile.filePath = new File(audioFile.getFilePath().replace(fromDir, toDir));

        String artworkPath = audioFile.artworkPath;
        if (artworkPath != null) {
            audioFile.artworkPath = artworkPath.replace(fromDir, toDir);
        }

        if (audioFile.folderArtworkPath != null) {
            File[] filterImages = audioFile.filePath.getParentFile().listFiles(folderImageFilter());
            if (filterImages != null && filterImages.length > 0) {
                audioFile.folderArtworkPath = filterImages[0].getAbsolutePath();
            }
        }
        return repository.updateAudioFile(audioFile);
    }

    private void calculateValues(List<AudioFile> audioFiles) {
        for (AudioFile audioFile : audioFiles) {
            folderSize += audioFile.size;
            folderLength += audioFile.duration;
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
        disposable = updateHiddenAudioFolder(hidden)
                .subscribeOn(Schedulers.io())
                .flatMap(audioFiles -> repository.getAudioTracks(audioFolder.getId()))
                .flatMap(Observable::fromIterable)
                .flatMap(audioFile -> updateHiddenAudioFiles(audioFile, hidden))
                .toList()
                .toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioFiles -> refreshCache());
    }

    private Observable<Object> updateHiddenAudioFiles(AudioFile audioFile, boolean hidden) {
        audioFile.isHidden = hidden;
        return repository.updateAudioFile(audioFile);
    }

    private Observable<Object> updateHiddenAudioFolder(boolean hidden) {
        audioFolder.isHidden = hidden;
        return repository.updateAudioFolder(audioFolder);
    }
}
