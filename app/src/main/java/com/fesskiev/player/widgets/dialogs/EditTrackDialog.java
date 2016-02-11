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
import com.fesskiev.player.model.MusicFile;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;

public class EditTrackDialog extends AlertDialog implements View.OnClickListener, TextWatcher {

    private AudioFile audioFile;
    private EditText editArtist;
    private EditText editTitle;
    private EditText editAlbum;
    private EditText editGenre;


    public EditTrackDialog(Context context) {
        super(context);
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

    }

    public void setMusicFile(MusicFile musicFile) throws Exception {
        audioFile = AudioFileIO.read(new File(musicFile.filePath));
        Tag tag = audioFile.getTag();
        tag.setField(FieldKey.ARTIST, "Kings of Leon");
        audioFile.commit();
    }

    @Override
    public void onClick(View v) {

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

            } else if (s == editTitle.getEditableText()) {

            } else if (s == editAlbum.getEditableText()) {

            } else if (s == editGenre.getEditableText()) {

            }
        }
    }
}
