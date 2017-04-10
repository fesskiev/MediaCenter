package com.fesskiev.mediacenter.ui.playback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
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

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.ui.audio.player.AudioPlayerActivity;
import com.fesskiev.mediacenter.utils.AnimationUtils;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.NotificationHelper;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.converter.AudioConverterHelper;
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

    private NotificationHelper notificationHelper;
    private AudioPlayer audioPlayer;
    private AudioFile currentTrack;

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
    private Bitmap lastCover;
    private int height;

    private boolean startForeground;
    private boolean isShow = true;

    private boolean lastLoadSuccess;
    private boolean lastLoadError;
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

        notificationHelper = NotificationHelper.getInstance(getApplicationContext());

        audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        audioPlayer.getCurrentTrackAndTrackList();

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
        playPauseButton.setPlay(false);

        View bottomSheet = findViewById(R.id.bottom_sheet);
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
            peakView.setOnClickListener(v -> AudioPlayerActivity.startPlayerActivity(PlaybackActivity.this));
            peakView.post(() -> {
                int marginTop = getResources().getDimensionPixelSize(R.dimen.bottom_sheet_margin_top);
                height = peakView.getHeight() + marginTop;
                bottomSheetBehavior.setPeekHeight(height);
            });
        }

        showEmptyFolderCard();
        showEmptyTrackCard();

        registerNotificationReceiver();

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

        notificationHelper.updateNotification(currentTrack, lastCover, lastPositionSeconds, lastPlaying);

        adapter.notifyDataSetChanged();
    }

    private void registerNotificationReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(NotificationHelper.ACTION_MEDIA_CONTROL_PLAY);
        filter.addAction(NotificationHelper.ACTION_MEDIA_CONTROL_PAUSE);
        filter.addAction(NotificationHelper.ACTION_MEDIA_CONTROL_NEXT);
        filter.addAction(NotificationHelper.ACTION_MEDIA_CONTROL_PREVIOUS);
        registerReceiver(notificationReceiver, filter);

    }

    private void unregisterNotificationReceiver() {
        unregisterReceiver(notificationReceiver);
    }

    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (currentTrack != null) {
                final String action = intent.getAction();
                switch (action) {
                    case NotificationHelper.ACTION_MEDIA_CONTROL_PLAY:
                        audioPlayer.play();
                        break;
                    case NotificationHelper.ACTION_MEDIA_CONTROL_PAUSE:
                        audioPlayer.pause();
                        break;
                    case NotificationHelper.ACTION_MEDIA_CONTROL_PREVIOUS:
                        audioPlayer.previous();
                        break;
                    case NotificationHelper.ACTION_MEDIA_CONTROL_NEXT:
                        audioPlayer.next();
                        break;
                }
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unregisterNotificationReceiver();

        notificationHelper.stopNotification();

        if (startForeground) {
            PlaybackService.stopPlaybackForegroundService(getApplicationContext());
            PlaybackService.destroyPlayer(getApplicationContext());
            startForeground = false;
        }
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlaybackStateEvent(PlaybackService playbackState) {

        boolean isLoadSuccess = playbackState.isLoadSuccess();
        if (lastLoadSuccess != isLoadSuccess) {
            lastLoadSuccess = isLoadSuccess;
            if (!lastLoadSuccess) {
                playPauseButton.startLoading();
            } else {
                playPauseButton.finishLoading();
            }
        }

        boolean isLoadError = playbackState.isLoadError();
        if (lastLoadError != isLoadError) {
            lastLoadError = isLoadError;
            if (lastLoadError) {
                if (AudioConverterHelper.isAudioFileFLAC(audioPlayer.getCurrentTrack())) {
                    tryConvertAudioFile();
                }
            }
        }

        boolean playing = playbackState.isPlaying();
        if (lastPlaying != playing) {
            lastPlaying = playing;
            playPauseButton.setPlay(playing);

            notificationHelper.updateNotification(currentTrack, lastCover, lastPositionSeconds, lastPlaying);
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

        lastLoadSuccess = false;
        playPauseButton.startLoading();

        setMusicFileInfo(currentTrack);
        hideEmptyTrackCard();

        adapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCurrentTrackListEvent(List<AudioFile> currentTrackList) {

        adapter.refreshAdapter(currentTrackList);
        hideEmptyFolderCard();
    }

    private void tryConvertAudioFile() {
        Log.e("error", "LOAD FILE ERROR!");
        AudioConverterHelper.getInstance().convertAudioIfNeed(audioPlayer.getCurrentTrack(),
                new AudioConverterHelper.OnConvertProcessListener() {

                    @Override
                    public void onStart() {
                        Log.e("error", "onStart() convert");
                    }

                    @Override
                    public void onSuccess(AudioFile audioFile) {
                        Log.e("error", "onSuccess convert");
                        audioPlayer.setCurrentAudioFileAndPlay(audioFile);
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e("error", "onFailure: " + error.getMessage());
                    }
                });
    }

    private void setMusicFileInfo(AudioFile audioFile) {
        track.setText(audioFile.title);
        artist.setText(audioFile.artist);

        audioPlayer.getCurrentAudioFolder()
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioFolder -> BitmapHelper.getInstance().loadArtwork(audioFile, audioFolder, cover,
                        new BitmapHelper.OnBitmapLoadListener() {
                            @Override
                            public void onLoaded(Bitmap bitmap) {

                                lastCover = bitmap;

                                notificationHelper.updateNotification(audioFile, lastCover, 0, lastPlaying);
                                startForegroundService();
                            }

                            @Override
                            public void onFailed() {
                                notificationHelper.updateNotification(audioFile, lastCover, 0, lastPlaying);
                                startForegroundService();
                            }
                        }));

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

        private List<AudioFile> audioFiles;

        public TrackListAdapter() {
            audioFiles = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView playEq;
            TextView title;
            TextView duration;
            TextView filePath;

            public ViewHolder(View v) {
                super(v);
                v.setOnClickListener(view -> {
                    List<AudioFile> audioFiles = adapter.getAudioFiles();
                    AudioFile audioFile = audioFiles.get(getAdapterPosition());
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
            AudioFile audioFile = audioFiles.get(position);
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

        public void clearAdapter() {
            audioFiles.clear();
            notifyDataSetChanged();
        }
    }

    public void clearPlayback() {
        currentTrack = null;
        adapter.clearAdapter();
        track.setText("");
        artist.setText("");
        durationText.setText("");
        cover.setImageResource(0);
        showEmptyTrackCard();
        showEmptyFolderCard();

        lastPlaying = false;
        playPauseButton.setPlay(false);

        notificationHelper.updateNotification(currentTrack, lastCover, lastPositionSeconds, lastPlaying);

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

}
