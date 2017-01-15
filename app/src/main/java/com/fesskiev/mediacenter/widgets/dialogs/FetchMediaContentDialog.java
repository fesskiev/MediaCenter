package com.fesskiev.mediacenter.widgets.dialogs;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;


public class FetchMediaContentDialog extends AlertDialog {

    public static FetchMediaContentDialog newInstance(Context context) {
        return new FetchMediaContentDialog(context);
    }

    private TextView folderNameText;
    private TextView fileNameText;


    public FetchMediaContentDialog(Context context) {
        super(context);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_fetch_media_content);

        folderNameText = (TextView) findViewById(R.id.fetchFolderName);
        fileNameText = (TextView) findViewById(R.id.fetchFileName);

    }

    public void setFolderName(String folderName) {
        if (folderNameText != null) {
            folderNameText.setText(folderName);
        }
    }

    public void setFileName(String fileName) {
        if (fileNameText != null) {
            fileNameText.setText(fileName);
        }
    }
}
