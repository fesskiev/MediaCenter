package com.fesskiev.mediacenter.widgets.controls;


import android.content.Context;
import android.graphics.drawable.Animatable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.widgets.buttons.PlayPauseButton;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;

import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH;

public class VideoControlView extends FrameLayout {


    public interface OnVideoPlayerControlListener {

        void playPauseButtonClick(boolean isPlaying);

        void seekVideo(int progress);

        void nextVideo();

        void previousVideo();

        void resizeModeChanged(int mode);
    }

    private OnVideoPlayerControlListener listener;
    private PlayPauseButton playPauseButton;
    private SeekBar seekVideo;
    private TextView videoTimeCount;
    private TextView videoTimeTotal;
    private TextView resizeModeState;
    private boolean isPlaying;
    private int resizeMode;

    public VideoControlView(Context context) {
        super(context);
        init(context);
    }

    public VideoControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.video_player_control, this, true);

        videoTimeCount = (TextView) findViewById(R.id.videoTimeCount);
        videoTimeTotal = (TextView) findViewById(R.id.videoTimeTotal);

        resizeModeState = (TextView) findViewById(R.id.resizeModeState);

        view.findViewById(R.id.resizeModeButton).setOnClickListener(v -> changeResizeMode());

        ImageView nextVideo = (ImageView) findViewById(R.id.nextVideo);
        nextVideo.setOnClickListener(v -> {
            ((Animatable) nextVideo.getDrawable()).start();
            if (listener != null) {
                listener.nextVideo();
            }
        });

        ImageView previousVideo = (ImageView) findViewById(R.id.previousVideo);
        previousVideo.setOnClickListener(v -> {
            ((Animatable) previousVideo.getDrawable()).start();
            if (listener != null) {
                listener.previousVideo();
            }
        });
        playPauseButton = (PlayPauseButton) view.findViewById(R.id.playPauseButton);
        playPauseButton.setColor(ContextCompat.getColor(context, R.color.primary));
        playPauseButton.setOnClickListener(v -> {
            isPlaying = !isPlaying;
            playPauseButton.setPlay(isPlaying);
            if (listener != null) {
                listener.playPauseButtonClick(isPlaying);
            }

        });

        seekVideo = (SeekBar) findViewById(R.id.seekVideo);
        seekVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.progress = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (listener != null) {
                    listener.seekVideo(progress);
                }
            }
        });

    }

    private void changeResizeMode() {
        switch (resizeMode) {
            case RESIZE_MODE_FIT:
                resizeModeState.setText("FILL");
                resizeMode = RESIZE_MODE_FILL;
                break;
            case RESIZE_MODE_FILL:
                resizeModeState.setText("FIXED WIDTH");
                resizeMode = RESIZE_MODE_FIXED_WIDTH;
                break;
            case RESIZE_MODE_FIXED_HEIGHT:
                resizeModeState.setText("FIT");
                resizeMode = RESIZE_MODE_FIT;
                break;
            case RESIZE_MODE_FIXED_WIDTH:
                resizeModeState.setText("FIXED HEIGHT");
                resizeMode = RESIZE_MODE_FIXED_HEIGHT;
                break;

        }

        if (listener != null) {
            listener.resizeModeChanged(resizeMode);
        }
    }

    public void setOnVideoPlayerControlListener(OnVideoPlayerControlListener l) {
        this.listener = l;
    }

    public void setVideoTimeTotal(String time) {
        videoTimeTotal.setText(time);
    }

    public void setVideoTimeCount(String time) {
        videoTimeCount.setText(time);
    }

    public void setProgress(int progress) {
        seekVideo.setProgress(progress);
    }

    public void setPlay(boolean play) {
        this.isPlaying = play;
        playPauseButton.setPlay(play);
    }

    public void resetIndicators() {
        videoTimeTotal.setText("0:00");
        videoTimeCount.setText("0:00");
        seekVideo.setProgress(0);
    }

    public void setResizeModeState(@AspectRatioFrameLayout.ResizeMode int resizeMode) {
        switch (resizeMode) {
            case RESIZE_MODE_FIT:
                resizeModeState.setText("FIT");
                this.resizeMode = RESIZE_MODE_FIT;
                break;
            case RESIZE_MODE_FILL:
                resizeModeState.setText("FILL");
                this.resizeMode = RESIZE_MODE_FILL;
                break;
            case RESIZE_MODE_FIXED_HEIGHT:
                resizeModeState.setText("FIXED HEIGHT");
                this.resizeMode = RESIZE_MODE_FIXED_HEIGHT;
                break;
            case RESIZE_MODE_FIXED_WIDTH:
                resizeModeState.setText("FIXED WIDTH");
                this.resizeMode = RESIZE_MODE_FIXED_WIDTH;
                break;

        }
    }
}
