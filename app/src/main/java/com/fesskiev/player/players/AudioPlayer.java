package com.fesskiev.player.players;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;


import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.data.model.AudioFile;
import com.fesskiev.player.data.model.MediaFile;
import com.fesskiev.player.data.source.DataRepository;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.ui.playback.Playable;
import com.fesskiev.player.utils.AppLog;
import com.fesskiev.player.utils.RxUtils;

import java.util.ArrayList;
import java.util.List;


import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AudioPlayer implements Playable {


    public interface OnAudioPlayerListener {

        void onCurrentTrackListChanged(List<AudioFile> audioFiles);

        void onCurrentTrackChanged(AudioFile audioFile);

        void onCurrentTrack(AudioFile audioFile);

        void onCurrentTrackList(List<AudioFile> audioFiles);

        void onPlaybackValuesChanged(int duration, int progress, int progressScale);

        void onPlaybackStateChanged(boolean playing);

    }

    private Context context;
    private List<OnAudioPlayerListener> audioPlayerListeners;

    private Subscription subscription;
    private DataRepository repository;

    private int position;
    private int volume;
    private int duration;
    private int progress;
    private int progressScale;
    private boolean isPlaying;
    private boolean mute;
    private boolean repeat;

    public AudioPlayer() {
        this.volume = 100;

        context = MediaApplication.getInstance().getApplicationContext();
        repository = MediaApplication.getInstance().getRepository();
        audioPlayerListeners = new ArrayList<>();

        registerPlaybackBroadcastReceiver();
    }

    public void addOnAudioPlayerListener(OnAudioPlayerListener listener) {
        if (!audioPlayerListeners.contains(listener)) {
            audioPlayerListeners.add(listener);
        }
    }

    public void clearOnAudioPlayerListener() {
        audioPlayerListeners.clear();
    }

    @Override
    public void open(MediaFile audioFile) {
        if (audioFile == null) {
            repository.getSelectedAudioFile()
                    .first()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(currentAudioFile -> {
                        PlaybackService.openFile(context, currentAudioFile.getFilePath());
                        PlaybackService.startPlayback(context);

                        notifyCurrentTrack(currentAudioFile);
                    });
        } else {
            PlaybackService.openFile(context, audioFile.getFilePath());
            PlaybackService.startPlayback(context);

            notifyCurrentTrack((AudioFile) audioFile);
        }
    }

    @Override
    public void play() {
        PlaybackService.startPlayback(context);
    }

    @Override
    public void pause() {
        PlaybackService.stopPlayback(context);
    }

    @Override
    public void next() {
        repository.getSelectedFolderAudioFiles()
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioFiles -> {
                    if (audioFiles == null) {
                        return;
                    }
                    if (position != audioFiles.size() - 1) {
                        incrementPosition();
                    }
                    AudioFile audioFile = audioFiles.get(position);
                    if (audioFile != null) {
                        repository.updateSelectedAudioFile(audioFile);

                        notifyCurrentTrackChanged(audioFile);

                        PlaybackService.openFile(context, audioFile.getFilePath());
                        PlaybackService.startPlayback(context);
                    }
                });
    }

    @Override
    public void previous() {
        repository.getSelectedFolderAudioFiles()
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioFiles -> {
                    if (audioFiles == null) {
                        return;
                    }
                    if (position > 0) {
                        decrementPosition();
                    }
                    AudioFile audioFile = audioFiles.get(position);
                    if (audioFile != null) {
                        repository.updateSelectedAudioFile(audioFile);

                        notifyCurrentTrackChanged(audioFile);

                        PlaybackService.openFile(context, audioFile.getFilePath());
                        PlaybackService.startPlayback(context);
                    }
                });
    }


    private void incrementPosition() {
        position++;
    }

    private void decrementPosition() {
        position--;
    }

    public void requestCurrentTrack() {
        subscription = repository.getSelectedAudioFile()
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioFile -> {

                    AppLog.ERROR("requestCurrentTrack");

                    notifyCurrentTrack(audioFile);

                });
    }

    public void requestCurrentTrackList() {
        subscription = repository.getSelectedFolderAudioFiles()
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioFiles -> {
                    AppLog.ERROR("requestCurrentTrackList");

                    notifyCurrentTrackList(audioFiles);
                });
    }


    public Observable<AudioFile> isTrackPlaying() {
        return repository.getSelectedAudioFile();
    }

    public void resetAudioPlayer() {
        isPlaying = false;
        RxUtils.unsubscribe(subscription);
        unregisterPlaybackBroadcastReceiver();
        clearOnAudioPlayerListener();
    }

    private void registerPlaybackBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlaybackService.ACTION_PLAYBACK_VALUES);
        filter.addAction(PlaybackService.ACTION_PLAYBACK_PLAYING_STATE);
        filter.addAction(PlaybackService.ACTION_TRACK_END);
        filter.addAction(PlaybackService.ACTION_HEADSET_PLUG_IN);
        filter.addAction(PlaybackService.ACTION_HEADSET_PLUG_OUT);
        LocalBroadcastManager.getInstance(context).registerReceiver(playbackReceiver, filter);
    }

    private void unregisterPlaybackBroadcastReceiver() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(playbackReceiver);
    }

    private BroadcastReceiver playbackReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PlaybackService.ACTION_PLAYBACK_VALUES:
                    duration = intent.getIntExtra(PlaybackService.PLAYBACK_EXTRA_DURATION, 0);
                    progress = intent.getIntExtra(PlaybackService.PLAYBACK_EXTRA_PROGRESS, 0);
                    progressScale = intent.getIntExtra(PlaybackService.PLAYBACK_EXTRA_PROGRESS_SCALE, 0);

                    notifyPlaybackValuesChanged(duration, progress, progressScale);
                    break;
                case PlaybackService.ACTION_PLAYBACK_PLAYING_STATE:
                    isPlaying = intent.getBooleanExtra(PlaybackService.PLAYBACK_EXTRA_PLAYING, false);

                    notifyPlaybackStateChanged(isPlaying);
                    break;
                case PlaybackService.ACTION_TRACK_END:
                    next();
                    break;
                case PlaybackService.ACTION_HEADSET_PLUG_IN:
                    //TODO settings start playback if plug in
//                    if (!isPlaying) {
//                        play();
//                    }
                    break;
                case PlaybackService.ACTION_HEADSET_PLUG_OUT:
                    pause();
                    break;

            }
        }
    };

    private void notifyCurrentTrackList(List<AudioFile> audioFiles) {
        for (OnAudioPlayerListener listener : audioPlayerListeners) {
            listener.onCurrentTrackList(audioFiles);
        }
    }


    private void notifyPlaybackStateChanged(boolean isPlaying) {
        for (OnAudioPlayerListener listener : audioPlayerListeners) {
            listener.onPlaybackStateChanged(isPlaying);
        }
    }

    private void notifyPlaybackValuesChanged(int duration, int progress, int progressScale) {
        for (OnAudioPlayerListener listener : audioPlayerListeners) {
            listener.onPlaybackValuesChanged(duration, progress, progressScale);
        }
    }

    private void notifyCurrentTrack(AudioFile audioFile) {
        for (OnAudioPlayerListener listener : audioPlayerListeners) {
            listener.onCurrentTrack(audioFile);
        }
    }

    private void notifyCurrentTrackChanged(AudioFile audioFile) {
        for (OnAudioPlayerListener listener : audioPlayerListeners) {
            listener.onCurrentTrackChanged(audioFile);
        }
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getProgressScale() {
        return progressScale;
    }

    public void setProgressScale(int progressScale) {
        this.progressScale = progressScale;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }
}
