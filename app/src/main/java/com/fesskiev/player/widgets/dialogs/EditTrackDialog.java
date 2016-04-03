package com.fesskiev.player.widgets.dialogs;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.fesskiev.player.R;
import com.fesskiev.player.db.DatabaseHelper;
import com.fesskiev.player.model.AudioFile;

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

public class EditTrackDialog extends AlertDialog implements View.OnClickListener, TextWatcher {

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


    public EditTrackDialog(Context context, AudioFile audioFile, OnEditTrackChangedListener listener) {
        super(context);
        this.audioFile = audioFile;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_track);
        getWindow().
                clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        editArtist = (EditText) findViewById(R.id.editArtist);
        editArtist.addTextChangedListener(this);
        editTitle = (EditText) findViewById(R.id.editTitle);
        editTitle.addTextChangedListener(this);
        editAlbum = (EditText) findViewById(R.id.editAlbum);
        editAlbum.addTextChangedListener(this);
        editGenre = (EditText) findViewById(R.id.editGenre);
        editGenre.addTextChangedListener(this);

        findViewById(R.id.saveTrackInfoButton).setOnClickListener(this);

        setDialogFields();
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
        DatabaseHelper.updateAudioFile(getContext(), audioFile);
    }

    @Override
    public void onClick(View v) {
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

                listener.onEditTrackChanged(this.audioFile);
                hide();
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
}
