package com.fesskiev.player.ui.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.buttons.PlayPauseFloatingButton;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlaybackActivity extends AppCompatActivity {

    private static final String TAG = PlaybackActivity.class.getSimpleName();

    private BottomSheetBehavior bottomSheetBehavior;
    private TrackListAdapter adapter;
    private PlayPauseFloatingButton playPauseButton;
    private TextView durationText;
    private TextView track;
    private TextView artist;
    private ImageView cover;
    private View peakView;
    private AudioPlayer audioPlayer;
    private int height;
    private boolean isShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerPlaybackBroadcastReceiver();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        audioPlayer = MediaApplication.getInstance().getAudioPlayer();

        track = (TextView) findViewById(R.id.track);
        artist = (TextView) findViewById(R.id.artist);
        cover = (ImageView) findViewById(R.id.cover);
        durationText = (TextView) findViewById(R.id.duration);


        RecyclerView trackListControl = (RecyclerView) findViewById(R.id.trackListControl);
        trackListControl.setLayoutManager(new ScrollingLinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false, 1000));
        adapter = new TrackListAdapter();
        trackListControl.setAdapter(adapter);

        AudioFile audioFile = MediaApplication.getInstance().getAudioPlayer().currentAudioFile;
        if (audioFile != null) {
            setMusicFileInfo(audioFile);
        }

        playPauseButton = (PlayPauseFloatingButton) findViewById(R.id.playPauseFAB);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioPlayer.isPlaying) {
                    PlaybackService.stopPlayback(PlaybackActivity.this);
                } else {
                    PlaybackService.startPlayback(PlaybackActivity.this);
                }
            }
        });

        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioPlayerActivity.startPlayerActivity(PlaybackActivity.this, false, cover);
            }
        });

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {

                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                }
            });

            peakView = findViewById(R.id.basicNavPlayerContainer);
            peakView.post(new Runnable() {
                @Override
                public void run() {
                    int marginTop = getResources().getDimensionPixelSize(R.dimen.bottom_sheet_margin_top);
                    height = peakView.getHeight() + marginTop;
                }
            });
        }
    }

    public void setMusicFileInfo(AudioFile audioFile) {
        track.setText(audioFile.title);
        artist.setText(audioFile.artist);

        Bitmap artwork = audioFile.getArtwork();
        if (artwork != null) {
            cover.setImageBitmap(artwork);
        } else {
            AudioFolder audioFolder = MediaApplication.getInstance().getAudioPlayer().currentAudioFolder;
            if (audioFolder != null && audioFolder.folderImages.size() > 0) {
                File coverFile = audioFolder.folderImages.get(0);
                if (coverFile != null) {
                    Picasso.with(this).load(coverFile).into(cover);
                }
            } else {
                Picasso.with(this).load(R.drawable.no_cover_icon).into(cover);
            }
        }
    }

    public void showPlayback() {
        if(!isShow) {
            bottomSheetBehavior.setPeekHeight(height);
            isShow = true;
//        peakView.requestLayout();
        }
    }

    public void hidePlayback() {
        if(isShow) {
            bottomSheetBehavior.setPeekHeight(0);
            isShow = false;
//        peakView.requestLayout();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterPlaybackBroadcastReceiver();
    }


    private void registerPlaybackBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlaybackService.ACTION_PLAYBACK_VALUES);
        filter.addAction(PlaybackService.ACTION_PLAYBACK_PLAYING_STATE);
        filter.addAction(AudioPlayer.ACTION_CHANGE_CURRENT_AUDIO_FILE);
        filter.addAction(AudioPlayer.ACTION_CHANGE_CURRENT_AUDIO_FOLDER);
        LocalBroadcastManager.getInstance(this).registerReceiver(playbackReceiver, filter);
    }

    private void unregisterPlaybackBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playbackReceiver);
    }


    private BroadcastReceiver playbackReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PlaybackService.ACTION_PLAYBACK_VALUES:
                    int progress =
                            intent.getIntExtra(PlaybackService.PLAYBACK_EXTRA_PROGRESS, 0);
                    durationText.setText(String.valueOf(Utils.getTimeFromMillisecondsString(progress)));
                    break;
                case PlaybackService.ACTION_PLAYBACK_PLAYING_STATE:
                    boolean isPlaying = intent.getBooleanExtra(PlaybackService.PLAYBACK_EXTRA_PLAYING, false);
                    Log.w(TAG, "playback activity is plying");
                    audioPlayer.isPlaying = isPlaying;
                    playPauseButton.setPlay(audioPlayer.isPlaying);
                    adapter.notifyDataSetChanged();
                    break;
                case AudioPlayer.ACTION_CHANGE_CURRENT_AUDIO_FILE:
                    Log.w(TAG, "change current audio file");
                    setMusicFileInfo(audioPlayer.currentAudioFile);
                    adapter.notifyDataSetChanged();
                    showPlayback();
                    break;
                case AudioPlayer.ACTION_CHANGE_CURRENT_AUDIO_FOLDER:
                    Log.w(TAG, "change current audio folder");
                    adapter.refreshAdapter(audioPlayer.currentAudioFolder.audioFilesDescription);
                    showPlayback();
                    break;
            }
        }
    };

    private class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.ViewHolder> {

        private List<AudioFile> audioFiles;

        public TrackListAdapter() {
            audioFiles = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView playEq;
            TextView title;
            TextView duration;

            public ViewHolder(View v) {
                super(v);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getAdapterPosition() == -1){
                            Snackbar.make(getCurrentFocus(), "pos is -1", Snackbar.LENGTH_SHORT);
                            return;
                        }

                        List<AudioFile> audioFiles = adapter.getAudioFiles();
                        AudioFile audioFile = audioFiles.get(getAdapterPosition());
                        if (audioFile != null) {
                            audioPlayer.setCurrentAudioFile(audioFile);
                            PlaybackService.createPlayer(PlaybackActivity.this, audioFile.filePath.getAbsolutePath());
                            PlaybackService.startPlayback(PlaybackActivity.this);
                        }
                    }
                });

                playEq = (ImageView) v.findViewById(R.id.playEq);
                title = (TextView) v.findViewById(R.id.title);
                duration = (TextView) v.findViewById(R.id.duration);

            }
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_track_playback, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            AudioFile audioFile = audioFiles.get(position);
            if (audioFile != null) {
                holder.title.setText(audioFile.title);
                holder.duration.setText(Utils.getDurationString(audioFile.length));
                if (audioPlayer.isPlaying && audioPlayer.currentAudioFile.equals(audioFile)) {
                    holder.playEq.setVisibility(View.VISIBLE);
                    AnimationDrawable animation = (AnimationDrawable) ContextCompat.
                            getDrawable(getApplicationContext(), R.drawable.ic_equalizer);
                    holder.playEq.setImageDrawable(animation);
                    if (animation != null) {
                        if (audioPlayer.isPlaying) {
                            animation.start();
                        } else {
                            animation.stop();
                        }
                    }
                } else {
                    holder.playEq.setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return audioFiles.size();
        }


        public List<AudioFile> getAudioFiles() {
            return audioFiles;
        }

        public void refreshAdapter(List<AudioFile> receiverAudioFiles) {
            audioFiles.clear();
            audioFiles.addAll(receiverAudioFiles);
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }
    }
}
