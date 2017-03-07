package com.fesskiev.mediacenter.widgets.controls;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.widgets.buttons.PlayPauseButton;


public class AudioControlView extends FrameLayout {

    public interface OnAudioControlListener {

        void onPlayStateChanged();

        void onVolumeStateChanged(int volume, boolean change);

        void onSeekStateChanged(int seek, boolean change);
    }

    private OnAudioControlListener listener;
    private AudioVolumeSeekView volumeSeekView;
    private PlayPauseButton playPauseButton;

    public AudioControlView(Context context) {
        super(context);
        init(context);
    }

    public AudioControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AudioControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.audio_control_layout, this, true);

        volumeSeekView = (AudioVolumeSeekView) view.findViewById(R.id.audioVolumeControl);
        volumeSeekView.setListener(new AudioVolumeSeekView.OnAudioVolumeSeekListener() {
            @Override
            public void changeVolumeStart(int volume) {
                if (listener != null) {
                    listener.onVolumeStateChanged(volume, true);
                }
            }

            @Override
            public void changeVolumeFinish() {
                if (listener != null) {
                    listener.onVolumeStateChanged(0, false);
                }
            }

            @Override
            public void changeSeekStart(int seek) {
                if (listener != null) {
                    listener.onSeekStateChanged(seek, true);
                }
            }

            @Override
            public void changeSeekFinish() {
                if (listener != null) {
                    listener.onSeekStateChanged(0, false);
                }
            }
        });

        playPauseButton = (PlayPauseButton) view.findViewById(R.id.playPauseButton);
        playPauseButton.setColor(ContextCompat.getColor(context, R.color.primary));
        playPauseButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlayStateChanged();
            }
        });
    }

    public void setEnableChangeVolume(boolean enable) {
        volumeSeekView.setEnableChangeVolume(enable);
    }

    public void setVolumeValue(float value) {
        volumeSeekView.setVolumeValue(value);
    }

    public void setSeekValue(int value) {
        volumeSeekView.setSeekValue(value);
    }


    public void setOnAudioControlListener(OnAudioControlListener listener) {
        this.listener = listener;
    }

    public void setPlay(boolean isPlaying) {
        playPauseButton.setPlay(isPlaying);
    }

    public void startConvertState() {
        playPauseButton.startConvertState();
    }


    public void stopConvertState() {
        playPauseButton.stopConvertState();
    }

}
