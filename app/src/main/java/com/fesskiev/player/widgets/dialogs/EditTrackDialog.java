package com.fesskiev.player.widgets.dialogs;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.fesskiev.player.R;
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

import java.io.File;
import java.io.IOException;

public class EditTrackDialog extends AlertDialog implements View.OnClickListener, TextWatcher {

    private AudioFile audioFile;
    private EditText editArtist;
    private EditText editTitle;
    private EditText editAlbum;
    private EditText editGenre;

    public EditTrackDialog(Context context, AudioFile audioFile) {
        super(context);
        this.audioFile = audioFile;
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

    private void setDialogFields(){
        editArtist.setText(audioFile.artist);
        editTitle.setText(audioFile.title);
        editAlbum.setText(audioFile.album);
        editGenre.setText(audioFile.genre);
    }

    @Override
    public void onClick(View v) {

        try {
            org.jaudiotagger.audio.AudioFile audioFile = AudioFileIO.read(new File(this.audioFile.filePath));
            Tag tag = audioFile.getTag();
            if(tag != null) {
                tag.setField(FieldKey.ARTIST, this.audioFile.artist);
                tag.setField(FieldKey.TITLE, this.audioFile.title);
                tag.setField(FieldKey.ALBUM, this.audioFile.album);
                tag.setField(FieldKey.GENRE, this.audioFile.genre);
                audioFile.commit();
            }
        } catch (CannotReadException | IOException | TagException |
                ReadOnlyFileException | InvalidAudioFrameException | CannotWriteException e) {
            e.printStackTrace();
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
