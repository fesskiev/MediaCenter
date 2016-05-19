package com.fesskiev.player.widgets.dialogs;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import com.fesskiev.player.R;


public class FetchAudioFoldersDialog extends AlertDialog {


    public static FetchAudioFoldersDialog newInstance(Context context) {
        return new FetchAudioFoldersDialog(context);
    }

    private TextView folderNameText;
    private TextView audioTrackText;


    public FetchAudioFoldersDialog(Context context) {
        super(context);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_fetch_audio_folder);

        folderNameText = (TextView) findViewById(R.id.fetchFolderName);
        audioTrackText = (TextView) findViewById(R.id.fetchAudioName);
        setCancelable(false);
    }

    public void setFolderName(String folderName) {
        if (folderNameText != null) {
            folderNameText.setText(folderName);
        }
    }

    public void setAudioTrackName(String trackName) {
        if (audioTrackText != null) {
            audioTrackText.setText(trackName);
        }
    }
}
