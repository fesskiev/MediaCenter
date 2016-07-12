package com.fesskiev.player.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.ui.MainActivity;


public class AudioNotificationManager extends BroadcastReceiver {

    private static final String TAG = AudioNotificationManager.class.getName();

    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;

    public static final String ACTION_MEDIA_CONTROL_PLAY = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_PLAY";
    public static final String ACTION_MEDIA_CONTROL_PAUSE = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_PAUSE";
    public static final String ACTION_MEDIA_CONTROL_REWIND = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_REWIND";
    public static final String ACTION_MEDIA_CONTROL_FAST_FORWARD = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_FAST_FORWARD";
    public static final String ACTION_MEDIA_CONTROL_NEXT = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_NEXT";
    public static final String ACTION_MEDIA_CONTROL_PREVIOUS = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_PREVIOUS";
    public static final String ACTION_MEDIA_CONTROL_STOP = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_STOP";

    private Context context;
    private AudioPlayer audioPlayer;
    private PlaybackService playbackService;

    public AudioNotificationManager(Context context, PlaybackService playbackService) {
        this.context = context.getApplicationContext();
        this.playbackService = playbackService;
        audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        registerBroadcastReceiver();


        playbackService.startForeground(NOTIFICATION_ID,
                createNotification(audioPlayer.currentAudioFile));
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_MEDIA_CONTROL_PLAY);
        filter.addAction(ACTION_MEDIA_CONTROL_STOP);
        filter.addAction(ACTION_MEDIA_CONTROL_PAUSE);
        filter.addAction(ACTION_MEDIA_CONTROL_NEXT);
        filter.addAction(ACTION_MEDIA_CONTROL_PREVIOUS);
        filter.addAction(ACTION_MEDIA_CONTROL_FAST_FORWARD);
        filter.addAction(ACTION_MEDIA_CONTROL_REWIND);
        playbackService.registerReceiver(this, filter);
    }

    private void unregisterBroadcastReceiver() {
        playbackService.unregisterReceiver(this);
    }

    public void stopNotification() {
        NotificationManager notificationManager =
                (NotificationManager) context.
                        getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        playbackService.stopForeground(true);
        unregisterBroadcastReceiver();
    }

    private void buildNotification(final NotificationCompat.Action action) {

        final AudioFile audioFile = audioPlayer.currentAudioFile;
        if (audioFile != null) {
            BitmapHelper.loadBitmap(context, audioFile.getArtworkPath(),
                    new BitmapHelper.OnBitmapLoadListener() {
                        @Override
                        public void onLoaded(Bitmap bitmap) {
                            createNotification(action, audioFile, bitmap);
                        }

                        @Override
                        public void onFailed() {
                            createNotification(action, audioFile,
                                    BitmapHelper.getBitmapFromResource(context,
                                            R.drawable.no_cover_track_icon));
                        }
                    });
        }
    }

    private void createNotification(NotificationCompat.Action action, AudioFile audioFile, Bitmap bitmap) {
        NotificationCompat.Builder notificationBuilder
                = new NotificationCompat.Builder(context);
        notificationBuilder
                .setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 2, 4, 1, 3))
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setSmallIcon(R.drawable.icon_music)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(createContentIntent())
                .setUsesChronometer(false)
                .setContentTitle(audioFile.title)
                .setContentText(audioFile.artist)
                .setLargeIcon(bitmap);

        notificationBuilder.addAction(generateAction(R.drawable.icon_previous_media_control,
                "Previous", ACTION_MEDIA_CONTROL_PREVIOUS));
        notificationBuilder.addAction(generateAction(R.drawable.icon_rewind_media_control,
                "Rewind", ACTION_MEDIA_CONTROL_REWIND));
        notificationBuilder.addAction(action);
        notificationBuilder.addAction(generateAction(R.drawable.icon_fast_forward_media_control,
                "Fast Forward", ACTION_MEDIA_CONTROL_FAST_FORWARD));
        notificationBuilder.addAction(generateAction(R.drawable.icon_next_media_control,
                "Next", ACTION_MEDIA_CONTROL_NEXT));

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private Notification createNotification(AudioFile audioFile) {
        String artist, title;
        if (audioFile != null) {
            artist = audioFile.artist;
            title = audioFile.title;
        } else {
            artist = context.getString(R.string.playback_control_track_not_selected);
            title = context.getString(R.string.playback_control_track_not_selected);
        }
        NotificationCompat.Builder notificationBuilder
                = new NotificationCompat.Builder(context);
        notificationBuilder
                .setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 2, 4, 1, 3))
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setSmallIcon(R.drawable.icon_music)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(createContentIntent())
                .setUsesChronometer(false)
                .setContentTitle(artist)
                .setContentText(title);

        notificationBuilder.addAction(generateAction(R.drawable.icon_previous_media_control,
                "Previous", ACTION_MEDIA_CONTROL_PREVIOUS));
        notificationBuilder.addAction(generateAction(R.drawable.icon_rewind_media_control,
                "Rewind", ACTION_MEDIA_CONTROL_REWIND));
        notificationBuilder.addAction(generateAction(R.drawable.icon_play_media_control,
                "Play", ACTION_MEDIA_CONTROL_PLAY));
        notificationBuilder.addAction(generateAction(R.drawable.icon_fast_forward_media_control,
                "Fast Forward", ACTION_MEDIA_CONTROL_FAST_FORWARD));
        notificationBuilder.addAction(generateAction(R.drawable.icon_next_media_control,
                "Next", ACTION_MEDIA_CONTROL_NEXT));

        return notificationBuilder.build();
    }


    public void setPlayPauseState(boolean isPlaying) {
        if (isPlaying) {
            buildNotification(generateAction(R.drawable.icon_pause_media_control,
                    "Pause", ACTION_MEDIA_CONTROL_PAUSE));
        } else {
            buildNotification(generateAction(R.drawable.icon_play_media_control,
                    "Play", ACTION_MEDIA_CONTROL_PLAY));
        }
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(intentAction);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, 0);
        return new NotificationCompat.Action(icon, title, pendingIntent);
    }

    private PendingIntent createContentIntent() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, REQUEST_CODE, intent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "HANDLE INTENT :" + action);
        switch (action) {
            case ACTION_MEDIA_CONTROL_PLAY:
                play();
                break;
            case ACTION_MEDIA_CONTROL_PAUSE:
                pause();
                break;
            case ACTION_MEDIA_CONTROL_FAST_FORWARD:
                fastForward();
                break;
            case ACTION_MEDIA_CONTROL_REWIND:
                rewind();
                break;
            case ACTION_MEDIA_CONTROL_PREVIOUS:
                previous();
                break;
            case ACTION_MEDIA_CONTROL_NEXT:
                next();
                break;
            case ACTION_MEDIA_CONTROL_STOP:
                stop();
                break;
        }
    }

    private void stop() {

    }

    private void next() {
        if (audioPlayer.currentAudioFile != null && !audioPlayer.repeat) {
            audioPlayer.next();
            if (audioPlayer.currentAudioFile != null) {
                PlaybackService.createPlayer(context,
                        audioPlayer.currentAudioFile.getFilePath());
                PlaybackService.startPlayback(context);
            }
        }
    }

    private void previous() {
        if (audioPlayer.currentAudioFile != null && !audioPlayer.repeat) {
            audioPlayer.previous();
            PlaybackService.createPlayer(context,
                    audioPlayer.currentAudioFile.getFilePath());
            PlaybackService.startPlayback(context);
        }
    }

    private void rewind() {

    }

    private void fastForward() {

    }

    private void play() {
        PlaybackService.startPlayback(context);
    }

    private void pause() {
        PlaybackService.stopPlayback(context);
    }

}
