package com.fesskiev.player.ui.playback;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
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
import com.fesskiev.player.analytics.AnalyticsActivity;
import com.fesskiev.player.data.model.AudioFile;
import com.fesskiev.player.players.AudioPlayer;
import com.fesskiev.player.ui.audio.player.AudioPlayerActivity;
import com.fesskiev.player.utils.AppLog;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.buttons.PlayPauseFloatingButton;
import com.fesskiev.player.widgets.recycleview.RecyclerItemTouchClickListener;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class PlaybackActivity extends AnalyticsActivity implements AudioPlayer.OnAudioPlayerListener {


    private AudioPlayer audioPlayer;
    private BottomSheetBehavior bottomSheetBehavior;
    private TrackListAdapter adapter;
    private PlayPauseFloatingButton playPauseButton;
    private TextView durationText;
    private TextView track;
    private TextView artist;
    private ImageView cover;
    private View emptyFolder;
    private View emptyTrack;
    private View peakView;
    private int height;
    private boolean isShow = true;

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        audioPlayer.addOnAudioPlayerListener(this);
        audioPlayer.requestCurrentTrack();
        audioPlayer.requestCurrentTrackList();

        track = (TextView) findViewById(R.id.track);
        artist = (TextView) findViewById(R.id.artist);
        cover = (ImageView) findViewById(R.id.cover);
        durationText = (TextView) findViewById(R.id.duration);

        emptyTrack = findViewById(R.id.emptyTrackCard);
        emptyFolder = findViewById(R.id.emptyFolderCard);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.trackListControl);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TrackListAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemTouchClickListener(this,
                new RecyclerItemTouchClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View childView, int position) {

                        List<AudioFile> audioFiles = adapter.getAudioFiles();
                        AudioFile audioFile = audioFiles.get(position);
                        if (audioFile != null) {
                            audioFile.isSelected = true;

                            audioPlayer.setCurrentAudioFile(audioFile);
                            audioPlayer.open(audioFile);
                            audioPlayer.play();
                        }
                    }

                    @Override
                    public void onItemLongPress(View childView, int position) {

                    }
                }));

        playPauseButton = (PlayPauseFloatingButton) findViewById(R.id.playPauseFAB);
        playPauseButton.setOnClickListener(v -> {
            if (audioPlayer.isPlaying()) {
                audioPlayer.pause();
            } else {
                audioPlayer.play();
            }
        });
        playPauseButton.setPlay(false);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(View bottomSheet, int newState) {
                    switch (newState) {
                        case BottomSheetBehavior.STATE_HIDDEN:
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            break;
                    }
                }

                @Override
                public void onSlide(View bottomSheet, float slideOffset) {

                }
            });

            peakView = findViewById(R.id.basicNavPlayerContainer);
            peakView.setOnClickListener(v -> {
                AudioPlayerActivity.startPlayerActivity(PlaybackActivity.this, false, cover);

            });
            peakView.post(() -> {
                int marginTop = getResources().getDimensionPixelSize(R.dimen.bottom_sheet_margin_top);
                height = peakView.getHeight() + marginTop;
                bottomSheetBehavior.setPeekHeight(height);
            });
        }

        showEmptyFolderCard();
        showEmptyTrackCard();
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }


    @Override
    public void onCurrentTrackChanged(AudioFile audioFile) {
        if (audioFile != null) {
            setMusicFileInfo(audioFile);
            adapter.notifyDataSetChanged();
            hideEmptyTrackCard();
        }
    }

    @Override
    public void onAudioTrackOpen(AudioFile audioFile) {
        if (audioFile != null) {
            setMusicFileInfo(audioFile);
            adapter.notifyDataSetChanged();
            hideEmptyTrackCard();
        }
    }


    boolean openTack;

    @Override
    public void onCurrentTrackRequest(AudioFile audioFile) {
        if (audioFile != null && !openTack) {
            audioPlayer.open(audioFile);
            openTack = true;
        }
    }

    @Override
    public void onCurrentTrackListRequest(List<AudioFile> audioFiles) {
        if (audioFiles != null) {
            adapter.refreshAdapter(audioFiles);
            hideEmptyFolderCard();
        }
    }

    @Override
    public void onPlaybackValuesChanged(int duration, int progress, int progressScale) {
        durationText.setText(String.valueOf(Utils.getTimeFromMillisecondsString(progress)));
    }

    @Override
    public void onPlaybackStateChanged(boolean playing) {
        playPauseButton.setPlay(playing);
        adapter.notifyDataSetChanged();
    }


    private void setMusicFileInfo(AudioFile audioFile) {
        track.setText(audioFile.title);
        artist.setText(audioFile.artist);

        BitmapHelper.getInstance().loadTrackListArtwork(audioFile, cover);

    }

    public void showPlayback() {
        if (!isShow) {
            bottomSheetBehavior.setPeekHeight(height);
            isShow = true;
        }
    }

    public void hidePlayback() {
        if (isShow) {
            bottomSheetBehavior.setPeekHeight(0);
            isShow = false;
        }
    }

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

                audioPlayer.isTrackPlaying()
                        .first()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(selectedTrack -> {
                            if (selectedTrack != null && selectedTrack.equals(audioFile) && audioPlayer.isPlaying()) {
                                holder.playEq.setVisibility(View.VISIBLE);

                                AnimationDrawable animation = (AnimationDrawable) ContextCompat.
                                        getDrawable(getApplicationContext(), R.drawable.ic_equalizer);
                                holder.playEq.setImageDrawable(animation);
                                if (animation != null) {
                                    if (audioPlayer.isPlaying()) {
                                        animation.start();
                                    } else {
                                        animation.stop();
                                    }
                                }
                            } else {
                                holder.playEq.setVisibility(View.INVISIBLE);
                            }

                        });
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
            notifyDataSetChanged();
        }
    }

    private void showEmptyFolderCard() {
        emptyFolder.setVisibility(View.VISIBLE);
    }

    private void showEmptyTrackCard() {
        emptyTrack.setVisibility(View.VISIBLE);
    }

    private void hideEmptyFolderCard() {
        emptyFolder.setVisibility(View.GONE);
    }

    private void hideEmptyTrackCard() {
        emptyTrack.setVisibility(View.GONE);
    }
}
