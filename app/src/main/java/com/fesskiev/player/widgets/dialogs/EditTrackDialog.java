package com.fesskiev.player.widgets.dialogs;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.fesskiev.player.R;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;

public class EditTrackDialog extends AlertDialog {

    private AudioFile audioFile;

    public EditTrackDialog(Context context) {
        super(context);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_track);
    }

    public void readFile(String filePath) throws Exception{
        audioFile = AudioFileIO.read(new File(filePath));
        Tag tag = audioFile.getTag();
        tag.setField(FieldKey.ARTIST, "Kings of Leon");
        audioFile.commit();
    }
}
