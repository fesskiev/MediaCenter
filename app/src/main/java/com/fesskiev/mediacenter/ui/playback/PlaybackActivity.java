package com.fesskiev.mediacenter.ui.playback;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.ui.audio.player.AudioPlayerActivity;
import com.fesskiev.mediacenter.utils.AnimationUtils;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.buttons.PlayPauseFloatingButton;
import com.fesskiev.mediacenter.widgets.nav.MediaNavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public abstract class PlaybackActivity extends AnalyticsActivity {

    public abstract MediaNavigationView getMediaNavigationView();

    public abstract void processFinishPlayback();

    protected AudioPlayer audioPlayer;

    private List<AudioFile> currentTrackList;
    private AudioFile currentTrack;

    protected View bottomSheet;
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

    protected boolean startForeground;
    private boolean isShow = true;

    private boolean lastLoadSuccess;
    private boolean lastPlaying;
    private int lastPositionSeconds;
    private boolean lastEnableEQ;
    private boolean lastEnableReverb;
    private boolean lastEnableWhoosh;
    private boolean lastEnableEcho;

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        EventBus.getDefault().register(this);

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

        playPauseButton = (PlayPauseFloatingButton) findViewById(R.id.playPauseFAB);
        playPauseButton.setOnClickListener(v -> {
            if (checkTrackSelected()) {
                if (lastPlaying) {
                    audioPlayer.pause();
                } else {
                    audioPlayer.play();
                }
                togglePlayPause();
            }
        });
        playPauseButton.setPlay(lastPlaying);

        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_SETTLING);
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
                if (checkTrackSelected()) {
                    AudioPlayerActivity.startPlayerActivity(PlaybackActivity.this);
                }
            });
            peakView.post(() -> {
                int marginTop = getResources().getDimensionPixelSize(R.dimen.bottom_sheet_margin_top);
                height = peakView.getHeight() + marginTop;
                bottomSheetBehavior.setPeekHeight(height);
            });
        }

        audioPlayer = MediaApplication.getInstance().getAudioPlayer();

        if (savedInstanceState == null) {
            currentTrackList = new ArrayList<>();
            showEmptyFolderCard();
            showEmptyTrackCard();
            audioPlayer.getCurrentTrackAndTrackList();
        } else {
            restorePlaybackState(savedInstanceState);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("startForeground", startForeground);
    }

    private void restorePlaybackState(Bundle savedInstanceState) {
        startForeground = savedInstanceState.getBoolean("startForeground");

        currentTrack = audioPlayer.getCurrentTrack();
        if (currentTrack != null) {
            hideEmptyTrackCard();
            setMusicFileInfo(currentTrack);
            startForegroundService();
        } else {
            showEmptyTrackCard();
        }

        currentTrackList = audioPlayer.getCurrentTrackList();
        if (currentTrackList == null) {
            currentTrackList = new ArrayList<>();
            showEmptyFolderCard();
        } else {
            hideEmptyFolderCard();
            adapter.refreshAdapter();
        }
    }

    private boolean checkTrackSelected() {
        if (currentTrack == null) {
            AnimationUtils.getInstance().errorAnimation(playPauseButton);
            return false;
        }
        return true;
    }

    private void togglePlayPause() {
        lastPlaying = !lastPlaying;
        playPauseButton.setPlay(lastPlaying);

        adapter.notifyDataSetChanged();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlaybackStateEvent(PlaybackService playbackState) {
        if (playbackState.isFinish()) {
            processFinishPlayback();
            return;
        }

        boolean isLoadSuccess = playbackState.isLoadSuccess();
        if (lastLoadSuccess != isLoadSuccess) {
            lastLoadSuccess = isLoadSuccess;
            if (!lastLoadSuccess) {
                playPauseButton.startLoading();
            } else {
                playPauseButton.finishLoading();
            }
        }

        boolean playing = playbackState.isPlaying();
        if (lastPlaying != playing) {
            lastPlaying = playing;
            playPauseButton.setPlay(playing);

            adapter.notifyDataSetChanged();

        }

        int positionSeconds = playbackState.getPosition();
        if (lastPositionSeconds != positionSeconds) {
            lastPositionSeconds = positionSeconds;
            durationText.setText(Utils.getPositionSecondsString(lastPositionSeconds));
        }

        boolean enableEq = playbackState.isEnableEQ();
        if (lastEnableEQ != enableEq) {
            lastEnableEQ = enableEq;
            AppSettingsManager.getInstance().setEQEnable(lastEnableEQ);
            getMediaNavigationView().setEQEnable(lastEnableEQ);
        }

        boolean enableReverb = playbackState.isEnableReverb();
        if (lastEnableReverb != enableReverb) {
            lastEnableReverb = enableReverb;
            AppSettingsManager.getInstance().setReverbEnable(lastEnableReverb);
            getMediaNavigationView().setReverbEnable(lastEnableReverb);
        }

        boolean enableWhoosh = playbackState.isEnableWhoosh();
        if (lastEnableWhoosh != enableWhoosh) {
            lastEnableWhoosh = enableWhoosh;
            AppSettingsManager.getInstance().setWhooshEnable(lastEnableWhoosh);
            getMediaNavigationView().setWhooshEnable(lastEnableWhoosh);
        }

        boolean enableEcho = playbackState.isEnableEcho();
        if (lastEnableEcho != enableEcho) {
            lastEnableEcho = enableEcho;
            AppSettingsManager.getInstance().setEchoEnable(lastEnableEcho);
            getMediaNavigationView().setEchoEnable(lastEnableEcho);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCurrentTrackEvent(AudioFile currentTrack) {
        this.currentTrack = currentTrack;
        if (currentTrack != null) {
            setMusicFileInfo(currentTrack);
            hideEmptyTrackCard();

            adapter.notifyDataSetChanged();
        }
        startForegroundService();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCurrentTrackListEvent(List<AudioFile> currentTrackList) {
        this.currentTrackList = currentTrackList;
        if (currentTrackList != null) {
            adapter.refreshAdapter();
            hideEmptyFolderCard();
        }
    }


    private void setMusicFileInfo(AudioFile audioFile) {
        track.setText(audioFile.title);
        artist.setText(audioFile.artist);
        clearDuration();

        audioPlayer.getCurrentAudioFolder()
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioFolder -> BitmapHelper.getInstance().loadTrackListArtwork(audioFile, audioFolder, cover));

    }

    private void startForegroundService() {
        if (!startForeground) {
            startForeground = true;
            PlaybackService.startPlaybackForegroundService(getApplicationContext());
        }
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


        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView playEq;
            TextView title;
            TextView duration;
            TextView filePath;

            public ViewHolder(View v) {
                super(v);
                v.setOnClickListener(view -> {
                    AudioFile audioFile = currentTrackList.get(getAdapterPosition());
                    if (audioFile != null) {
                        audioPlayer.setCurrentAudioFileAndPlay(audioFile);
                    }
                });

                playEq = (ImageView) v.findViewById(R.id.playEq);
                title = (TextView) v.findViewById(R.id.title);
                duration = (TextView) v.findViewById(R.id.duration);
                filePath = (TextView) v.findViewById(R.id.filePath);
                filePath.setSelected(true);

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
            AudioFile audioFile = currentTrackList.get(position);
            if (audioFile != null) {
                holder.title.setText(audioFile.title);
                holder.filePath.setText(audioFile.getFilePath());
                holder.duration.setText(Utils.getDurationString(audioFile.length));

                if (currentTrack != null) {
                    if (currentTrack.equals(audioFile) && lastPlaying) {
                        holder.playEq.setVisibility(View.VISIBLE);

                        AnimationDrawable animation = (AnimationDrawable) ContextCompat.
                                getDrawable(getApplicationContext(), R.drawable.ic_equalizer);
                        holder.playEq.setImageDrawable(animation);
                        if (animation != null) {
                            if (lastPlaying) {
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
        }

        @Override
        public int getItemCount() {
            return currentTrackList.size();
        }

        public void refreshAdapter() {
            notifyDataSetChanged();
        }

        public void clearAdapter() {
            currentTrackList.clear();
            notifyDataSetChanged();
        }
    }

    public void clearPlayback() {
        adapter.clearAdapter();
        showEmptyFolderCard();

        lastPlaying = false;
        playPauseButton.setPlay(false);

        adapter.notifyDataSetChanged();
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

    private void clearDuration() {
        durationText.setText("");
    }

}
