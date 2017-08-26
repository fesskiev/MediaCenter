package com.fesskiev.mediacenter.widgets.fetch;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.CountDownTimer;
import com.fesskiev.mediacenter.widgets.progress.NumberProgressBar;


public class FetchContentView extends FrameLayout {

    private TextView folderNameText;
    private TextView fileNameText;
    private TextView titleFetchText;
    private ImageView timerView;
    private NumberProgressBar progressBar;
    private CountDownTimer countDownTimer;

    public FetchContentView(Context context) {
        super(context);
        init(context);
    }

    public FetchContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FetchContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_fetch_content, this, true);

        folderNameText = view.findViewById(R.id.fetchFolderName);
        fileNameText = view.findViewById(R.id.fetchFileName);
        titleFetchText = view.findViewById(R.id.fetchTitle);

        progressBar = view.findViewById(R.id.fetchProgressBar);

        timerView = view.findViewById(R.id.timer);

        setVisibility(INVISIBLE);

    }

    public void setFolderName(String folderName) {
        folderNameText.setText(folderName);
        fileNameText.setText("");
    }

    public void setFileName(String trackName) {
        fileNameText.setText(trackName);
    }

    public void setDefaultState(String folderName, String trackName) {
        folderNameText.setText(folderName);
        fileNameText.setText(trackName);
    }

    public void clear() {
        folderNameText.setText("");
        fileNameText.setText("");
    }

    public void setVisibleContent() {
        setVisibility(VISIBLE);
    }

    public void setInvisibleContent() {
        setVisibility(INVISIBLE);
    }

    public void setTextColor(int color) {
        folderNameText.setTextColor(color);
        fileNameText.setTextColor(color);
        titleFetchText.setTextColor(color);
    }

    public void showTimer() {
        timerView.setVisibility(View.VISIBLE);
        ((Animatable) timerView.getDrawable()).start();

        countDownTimer = new CountDownTimer(3000);
        countDownTimer.setOnCountDownListener(() -> ((Animatable) timerView.getDrawable()).start());
    }

    public void hideTimer() {
        timerView.setVisibility(View.INVISIBLE);
        ((Animatable) timerView.getDrawable()).stop();
        if (countDownTimer != null) {
            countDownTimer.stop();
        }
    }

    public void setProgress(float percent) {
        progressBar.setProgress((int) percent);
    }
}


