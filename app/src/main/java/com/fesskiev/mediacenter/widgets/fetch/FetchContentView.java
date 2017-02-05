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


public class FetchContentView extends FrameLayout {

    private TextView titleNameText;
    private TextView descNameText;
    private TextView titleFetchText;
    private ImageView timerView;
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

        titleNameText = (TextView) view.findViewById(R.id.fetchFolderName);
        descNameText = (TextView) view.findViewById(R.id.fetchFileName);
        titleFetchText = (TextView) view.findViewById(R.id.fetchTitle);

        timerView = (ImageView) view.findViewById(R.id.timer);

        setVisibility(INVISIBLE);

    }

    public void setAudioFolderName(String folderName) {
        titleNameText.setText(folderName);
        descNameText.setText("");
    }

    public void setAudioFileName(String trackName) {
        descNameText.setText(trackName);
    }

    public void setVideoFileName(String videoFileName) {
        titleNameText.setText(videoFileName);
        descNameText.setText("");
    }

    public void setVisibleContent() {
        setVisibility(VISIBLE);
    }

    public void setInvisibleContent() {
        setVisibility(INVISIBLE);
    }

    public void setTextColor(int color) {
        titleNameText.setTextColor(color);
        descNameText.setTextColor(color);
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
}


