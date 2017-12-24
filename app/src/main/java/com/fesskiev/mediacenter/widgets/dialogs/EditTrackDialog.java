package com.fesskiev.mediacenter.widgets.dialogs;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.utils.RxUtils;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.id3.ID3v23Tag;

import java.io.IOException;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class EditTrackDialog extends DialogFragment implements View.OnClickListener, TextWatcher {

    protected static final String AUDIO_FILE = "com.fesskiev.player.AUDIO_FILE";

    public static EditTrackDialog newInstance(AudioFile audioFile) {
        EditTrackDialog dialog = new EditTrackDialog();
        Bundle args = new Bundle();
        args.putParcelable(AUDIO_FILE, audioFile);
        dialog.setArguments(args);
        return dialog;
    }

    public interface OnEditTrackChangedListener {

        void onEditTrackChanged(AudioFile audioFile);

        void onEditTrackError();
    }

    private OnEditTrackChangedListener listener;
    private AudioFile audioFile;
    private EditText editArtist;
    private EditText editTitle;
    private EditText editAlbum;
    private EditText editGenre;

    @Inject
    DataRepository repository;

    protected Disposable disposable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomFragmentDialog);
        MediaApplication.getInstance().getAppComponent().inject(this);

        audioFile = getArguments().getParcelable(AUDIO_FILE);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
         /*
         *  bug! http://stackoverflow.com/questions/32784009/styling-custom-dialog-fragment-not-working?noredirect=1&lq=1
         */
        return getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_track, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editArtist = view.findViewById(R.id.editArtist);
        editArtist.addTextChangedListener(this);
        editTitle = view.findViewById(R.id.editTitle);
        editTitle.addTextChangedListener(this);
        editAlbum = view.findViewById(R.id.editAlbum);
        editAlbum.addTextChangedListener(this);
        editGenre = view.findViewById(R.id.editGenre);
        editGenre.addTextChangedListener(this);

        view.findViewById(R.id.saveTrackInfoButton).setOnClickListener(this);

        setDialogFields();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(disposable);
    }

    private void setDialogFields() {
        if (!audioFile.artist.equals(getContext().getString(R.string.empty_music_file_artist))) {
            editArtist.setText(audioFile.artist);
        }
        if (!audioFile.title.equals(getContext().getString(R.string.empty_music_file_title))) {
            editTitle.setText(audioFile.title);
        }
        if (!audioFile.album.equals(getContext().getString(R.string.empty_music_file_album))) {
            editAlbum.setText(audioFile.album);
        }
        if (!audioFile.genre.equals(getContext().getString(R.string.empty_music_file_genre))) {
            editGenre.setText(audioFile.genre);
        }
    }

    private void updateAudioFileDatabase(AudioFile audioFile) {
        disposable = repository.updateAudioFile(audioFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    listener.onEditTrackChanged(this.audioFile);
                    dismiss();
                });
    }

    @Override
    public void onClick(View v) {
        editTrackTagger();
    }

    private void editTrackTagger() {
        try {
            TagOptionSingleton.getInstance().setAndroid(true);
            org.jaudiotagger.audio.AudioFile audioFile = AudioFileIO.read(this.audioFile.filePath);
            audioFile.setTag(new ID3v23Tag());
            Tag tag = audioFile.getTag();
            if (tag != null) {
                tag.setField(FieldKey.ARTIST, this.audioFile.artist);
                tag.setField(FieldKey.TITLE, this.audioFile.title);
                tag.setField(FieldKey.ALBUM, this.audioFile.album);
                tag.setField(FieldKey.GENRE, this.audioFile.genre);
                audioFile.commit();

                updateAudioFileDatabase(this.audioFile);
            } else {
                listener.onEditTrackError();
            }
        } catch (CannotReadException | IOException | TagException |
                ReadOnlyFileException | InvalidAudioFrameException | CannotWriteException e) {
            e.printStackTrace();
            listener.onEditTrackError();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String value = s.toString();
        if (!TextUtils.isEmpty(value)) {
            if (s == editArtist.getEditableText()) {
                audioFile.artist = value;
            } else if (s == editTitle.getEditableText()) {
                audioFile.title = value;
            } else if (s == editAlbum.getEditableText()) {
                audioFile.album = value;
            } else if (s == editGenre.getEditableText()) {
                audioFile.genre = value;
            }
        }
    }

    public void setOnEditTrackChangedListener(OnEditTrackChangedListener listener) {
        this.listener = listener;
    }
}
