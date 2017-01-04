package com.fesskiev.mediacenter.widgets.dialogs;


import android.content.Context;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.CountDownTimer;


public class FetchMediaContentDialog extends AlertDialog {


    public static FetchMediaContentDialog newInstance(Context context) {
        return new FetchMediaContentDialog(context);
    }

    private TextView folderNameText;
    private TextView fileNameText;
    private CountDownTimer countDownTimer;


    public FetchMediaContentDialog(Context context) {
        super(context);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_fetch_media_content);

        folderNameText = (TextView) findViewById(R.id.fetchFolderName);
        fileNameText = (TextView) findViewById(R.id.fetchFileName);

        ImageView timerView = (ImageView) findViewById(R.id.timer);
        ((Animatable) timerView.getDrawable()).start();

        countDownTimer = new CountDownTimer(3000);
        countDownTimer.setOnCountDownListener(() -> {
            ((Animatable) timerView.getDrawable()).start();
        });

        setCancelable(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        countDownTimer.stop();
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
