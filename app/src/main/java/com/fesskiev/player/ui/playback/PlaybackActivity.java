package com.fesskiev.player.ui.playback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import com.fesskiev.player.db.DatabaseHelper;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.ui.audio.player.AudioPlayerActivity;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.buttons.PlayPauseFloatingButton;
import com.fesskiev.player.widgets.recycleview.RecyclerItemTouchClickListener;

import java.util.ArrayList;
import java.util.List;

public class PlaybackActivity extends AnalyticsActivity {

    private static final String TAG = PlaybackActivity.class.getSimpleName();

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
    private boolean isShow;


    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        audioPlayer = MediaApplication.getInstance().getAudioPlayer();

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
                            audioPlayer.setCurrentAudioFile(audioFile);
                            audioFile.isSelected = true;
                            DatabaseHelper.updateSelectedAudioFile(audioFile);
                            PlaybackService.createPlayer(PlaybackActivity.this, audioFile.getFilePath());
                            PlaybackService.startPlayback(PlaybackActivity.this);
                        }
                    }

                    @Override
                    public void onItemLongPress(View childView, int position) {

                    }
                }));

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
        playPauseButton.setPlay(audioPlayer.isPlaying);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        if (bottomSheetBehavior != null) {

            peakView = findViewById(R.id.basicNavPlayerContainer);
            peakView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (audioPlayer.currentAudioFile != null) {
                        AudioPlayerActivity.startPlayerActivity(PlaybackActivity.this, false, cover);
                    }
                }
            });
            peakView.post(new Runnable() {
                @Override
                public void run() {
                    int marginTop = getResources().getDimensionPixelSize(R.dimen.bottom_sheet_margin_top);
                    height = peakView.getHeight() + marginTop;
                }
            });
        }

        registerPlaybackBroadcastReceiver();

        fetchCurrentAudioFile();
        fetchCurrentAudioFolder();
    }

    private void fetchCurrentAudioFile() {
        AudioFile audioFile = DatabaseHelper.getSelectedAudioFile();
        if (audioFile != null) {
            audioPlayer.setCurrentAudioFile(audioFile);
            PlaybackService.createPlayer(getApplicationContext(),
                    audioPlayer.currentAudioFile.getFilePath());
        } else {
            showEmptyTrackCard();
        }
    }

    private void fetchCurrentAudioFolder() {
        AudioFolder audioFolder = DatabaseHelper.getSelectedAudioFolder();
        if (audioFolder != null) {
            audioPlayer.currentAudioFolder = audioFolder;
            List<AudioFile> audioFiles =
                    DatabaseHelper.getSelectedFolderAudioFiles(audioFolder);
            if (audioFiles != null) {
                audioPlayer.setCurrentAudioFolderFiles(audioFiles);
            }
        } else {
            showEmptyFolderCard();
        }
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
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

    private void setMusicFolderInfo() {
        AudioFolder audioFolder = audioPlayer.currentAudioFolder;
        if (audioFolder != null) {
            adapter.refreshAdapter(audioFolder.audioFiles);
        }
    }

    private void setMusicFileInfo(AudioFile audioFile) {
        track.setText(audioFile.title);
        artist.setText(audioFile.artist);

        BitmapHelper.loadArtwork(this, audioPlayer.currentAudioFolder, audioFile, cover);

    }

    public void showPlayback() {
        if (!isShow) {
            bottomSheetBehavior.setPeekHeight(height);
            isShow = true;
            peakView.requestLayout();
        }
    }

    public void hidePlayback() {
        if (isShow) {
            bottomSheetBehavior.setPeekHeight(0);
            isShow = false;
            peakView.requestLayout();
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
        filter.addAction(PlaybackService.ACTION_SONG_END);
        filter.addAction(PlaybackService.ACTION_HEADSET_PLUG_IN);
        filter.addAction(PlaybackService.ACTION_HEADSET_PLUG_OUT);
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
                    int duration =
                            intent.getIntExtra(PlaybackService.PLAYBACK_EXTRA_DURATION, 0);
                    int progress =
                            intent.getIntExtra(PlaybackService.PLAYBACK_EXTRA_PROGRESS, 0);
                    int progressScale =
                            intent.getIntExtra(PlaybackService.PLAYBACK_EXTRA_PROGRESS_SCALE, 0);

                    audioPlayer.duration = duration;
                    audioPlayer.progress = progress;
                    audioPlayer.progressScale = progressScale;

                    durationText.setText(String.valueOf(Utils.getTimeFromMillisecondsString(progress)));
                    break;
                case PlaybackService.ACTION_PLAYBACK_PLAYING_STATE:
                    boolean isPlaying = intent.getBooleanExtra(PlaybackService.PLAYBACK_EXTRA_PLAYING, false);
                    Log.w(TAG, "playback activity is plying");
                    audioPlayer.isPlaying = isPlaying;
                    playPauseButton.setPlay(audioPlayer.isPlaying);
                    adapter.notifyDataSetChanged();
                    break;
                case PlaybackService.ACTION_SONG_END:
                    Log.w(TAG, "action song end");
                    audioPlayer.next();
                    PlaybackService.createPlayer(getApplicationContext(),
                            audioPlayer.currentAudioFile.getFilePath());
                    PlaybackService.startPlayback(getApplicationContext());
                    break;
                case PlaybackService.ACTION_HEADSET_PLUG_IN:
                    if (!audioPlayer.isPlaying) {
                        PlaybackService.startPlayback(getApplicationContext());
                    }
                    break;
                case PlaybackService.ACTION_HEADSET_PLUG_OUT:
                    if (audioPlayer.isPlaying) {
                        PlaybackService.stopPlayback(getApplicationContext());
                    }
                    break;
                case AudioPlayer.ACTION_CHANGE_CURRENT_AUDIO_FILE:
                    Log.w(TAG, "change current audio file");
                    setMusicFileInfo(audioPlayer.currentAudioFile);
                    adapter.notifyDataSetChanged();
                    hideEmptyTrackCard();
                    break;
                case AudioPlayer.ACTION_CHANGE_CURRENT_AUDIO_FOLDER:
                    Log.w(TAG, "change current audio folder");
                    setMusicFolderInfo();
                    hideEmptyFolderCard();
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
                if (audioPlayer.isTrackPlaying(audioFile)) {
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
            notifyDataSetChanged();
        }
    }
}
