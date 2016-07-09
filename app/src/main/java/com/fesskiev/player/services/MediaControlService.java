package com.fesskiev.player.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.ui.MainActivity;
import com.fesskiev.player.utils.BitmapHelper;


public class MediaControlService extends Service {

    private static final String TAG = MediaControlService.class.getName();

    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;

    public static final String ACTION_MEDIA_CONTROL_PLAY = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_PLAY";
    public static final String ACTION_MEDIA_CONTROL_PAUSE = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_PAUSE";
    public static final String ACTION_MEDIA_CONTROL_REWIND = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_REWIND";
    public static final String ACTION_MEDIA_CONTROL_FAST_FORWARD = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_FAST_FORWARD";
    public static final String ACTION_MEDIA_CONTROL_NEXT = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_NEXT";
    public static final String ACTION_MEDIA_CONTROL_PREVIOUS = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_PREVIOUS";
    public static final String ACTION_MEDIA_CONTROL_STOP = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_STOP";

    public static void startMediaPlayerService(Context context) {
        Intent intent = new Intent(context, MediaControlService.class);
        context.startService(intent);
    }

    public static void stopNotificationService(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.
                        getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        Intent intent = new Intent(context, MediaControlService.class);
        context.stopService(intent);
    }

    private MediaSessionCompat mediaSessionCompat;
    private MediaControllerCompat mediaControllerCompat;
    private AudioPlayer audioPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        registerPlaybackBroadcastReceiver();
    }

    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        String action = intent.getAction();
        Log.d(TAG, "HANDLE INTENT :" + action);
        switch (action) {
            case ACTION_MEDIA_CONTROL_PLAY:
                mediaControllerCompat.getTransportControls().play();
                break;
            case ACTION_MEDIA_CONTROL_PAUSE:
                mediaControllerCompat.getTransportControls().pause();
                break;
            case ACTION_MEDIA_CONTROL_FAST_FORWARD:
                mediaControllerCompat.getTransportControls().fastForward();
                break;
            case ACTION_MEDIA_CONTROL_REWIND:
                mediaControllerCompat.getTransportControls().rewind();
                break;
            case ACTION_MEDIA_CONTROL_PREVIOUS:
                mediaControllerCompat.getTransportControls().skipToPrevious();
                break;
            case ACTION_MEDIA_CONTROL_NEXT:
                mediaControllerCompat.getTransportControls().skipToNext();
                break;
            case ACTION_MEDIA_CONTROL_STOP:
                mediaControllerCompat.getTransportControls().stop();
                break;
        }
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MediaControlService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE, intent, 0);
        return new NotificationCompat.Action(icon, title, pendingIntent);
    }

    private PendingIntent createContentIntent() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(getApplicationContext(), REQUEST_CODE, intent, 0);
    }

    private void buildNotification(final NotificationCompat.Action action) {

        final AudioFile audioFile = audioPlayer.currentAudioFile;
        if (audioFile != null) {
            BitmapHelper.loadBitmap(getApplicationContext(), audioFile.getArtworkPath(),
                    new BitmapHelper.OnBitmapLoadListener() {
                        @Override
                        public void onLoaded(Bitmap bitmap) {
                            createNotification(action, audioFile, bitmap);
                        }

                        @Override
                        public void onFailed() {
                            createNotification(action, audioFile,
                                    BitmapHelper.getBitmapFromResource(getApplicationContext(),
                                            R.drawable.no_cover_track_icon));
                        }
                    });
        }
    }

    public void createNotification(NotificationCompat.Action action, AudioFile audioFile, Bitmap bitmap) {
        NotificationCompat.Builder notificationBuilder
                = new NotificationCompat.Builder(getApplicationContext());
        notificationBuilder
                .setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView( 0, 2, 4, 1, 3))
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary))
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
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void setPlayPauseState(boolean isPlaying) {
        if (isPlaying) {
            buildNotification(generateAction(R.drawable.icon_pause_media_control,
                    "Pause", ACTION_MEDIA_CONTROL_PAUSE));
        } else {
            buildNotification(generateAction(R.drawable.icon_play_media_control,
                    "Play", ACTION_MEDIA_CONTROL_PLAY));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        initMediaSessions();
        handleIntent(intent);

        return START_STICKY;
    }


    private void registerPlaybackBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlaybackService.ACTION_PLAYBACK_VALUES);
        filter.addAction(PlaybackService.ACTION_PLAYBACK_PLAYING_STATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(playbackReceiver, filter);
    }

    private void unregisterPlaybackBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playbackReceiver);
    }

    private void initMediaSessions() {
        mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), TAG);
        try {
            mediaControllerCompat = new MediaControllerCompat(getApplicationContext(),
                    mediaSessionCompat.getSessionToken());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mediaSessionCompat.setCallback(new MediaSessionCallback());
    }


    private BroadcastReceiver playbackReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PlaybackService.ACTION_PLAYBACK_PLAYING_STATE:
                    boolean isPlaying =
                            intent.getBooleanExtra(PlaybackService.PLAYBACK_EXTRA_PLAYING, false);
                    Log.e(TAG, "onReceive playing: " + isPlaying);
                    setPlayPauseState(isPlaying);
                    break;
            }
        }
    };

    private class MediaSessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlay() {
            super.onPlay();
            Log.e(TAG, "Callback onPlay");
            PlaybackService.startPlayback(getApplicationContext());
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.e(TAG, "Callback onPause");
            PlaybackService.stopPlayback(getApplicationContext());
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            Log.e(TAG, "Callback onSkipToNext");
            if (!audioPlayer.repeat) {
                audioPlayer.next();
                PlaybackService.createPlayer(getApplicationContext(),
                        audioPlayer.currentAudioFile.getFilePath());
                PlaybackService.startPlayback(getApplicationContext());

            }
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            Log.e(TAG, "Callback onSkipToPrevious");
            if (!audioPlayer.repeat) {
                audioPlayer.next();
                PlaybackService.createPlayer(getApplicationContext(),
                        audioPlayer.currentAudioFile.getFilePath());
                PlaybackService.startPlayback(getApplicationContext());
            }
        }

        @Override
        public void onFastForward() {
            super.onFastForward();
            Log.e(TAG, "Callback onFastForward");
        }

        @Override
        public void onRewind() {
            super.onRewind();
            Log.e(TAG, "Callback onRewind");
        }

        @Override
        public void onStop() {
            super.onStop();
            Log.e(TAG, "Callback onStop");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSessionCompat.release();
        unregisterPlaybackBroadcastReceiver();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
