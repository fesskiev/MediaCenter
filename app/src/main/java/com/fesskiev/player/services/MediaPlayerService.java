package com.fesskiev.player.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.fesskiev.player.R;


public class MediaPlayerService extends Service {

    private static final String TAG = MediaPlayerService.class.getName();

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    public static void startMediaPlayerService(Context context) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(MediaPlayerService.ACTION_PLAY);
        context.startService(intent);
    }

    private NotificationManagerCompat notificationManagerCompat;
    private MediaSessionCompat mediaSessionCompat;
    private MediaControllerCompat mediaControllerCompat;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());

    }

    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        String action = intent.getAction();
        Log.d(TAG, "HANDLE INTENT :" + action);
        switch (action) {
            case ACTION_PLAY:
                mediaControllerCompat.getTransportControls().play();
                break;
            case ACTION_PAUSE:
                mediaControllerCompat.getTransportControls().pause();
                break;
            case ACTION_FAST_FORWARD:
                mediaControllerCompat.getTransportControls().fastForward();
                break;
            case ACTION_REWIND:
                mediaControllerCompat.getTransportControls().rewind();
                break;
            case ACTION_PREVIOUS:
                mediaControllerCompat.getTransportControls().skipToPrevious();
                break;
            case ACTION_NEXT:
                mediaControllerCompat.getTransportControls().skipToNext();
                break;
            case ACTION_STOP:
                mediaControllerCompat.getTransportControls().stop();
                break;
        }
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new NotificationCompat.Action(icon, title, pendingIntent);
    }

    private void buildNotification(NotificationCompat.Action action) {

        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);

        NotificationCompat.Builder notificationBuilder
                = new NotificationCompat.Builder(getApplicationContext());
        notificationBuilder
                .setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2, 3, 4))
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary))
                .setSmallIcon(R.drawable.icon_music)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setUsesChronometer(true)
                .setContentTitle("Media Title")
                .setContentText("Media Artist");
//                .setLargeIcon(art);


        notificationBuilder.addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS));
        notificationBuilder.addAction(generateAction(android.R.drawable.ic_media_rew, "Rewind", ACTION_REWIND));
        notificationBuilder.addAction(action);
        notificationBuilder.addAction(generateAction(android.R.drawable.ic_media_ff, "Fast Foward", ACTION_FAST_FORWARD));
        notificationBuilder.addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        initMediaSessions();
        handleIntent(intent);

        return START_NOT_STICKY;
    }

    private void initMediaSessions() {

        mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), TAG);

        try {
            mediaControllerCompat = new MediaControllerCompat(getApplicationContext(),
                    mediaSessionCompat.getSessionToken());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
                                           @Override
                                           public void onPlay() {
                                               super.onPlay();
                                               Log.e(TAG, "Callback onPlay");
                                               buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
                                           }

                                           @Override
                                           public void onPause() {
                                               super.onPause();
                                               Log.e(TAG, "Callback onPause");
                                               buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
                                           }

                                           @Override
                                           public void onSkipToNext() {
                                               super.onSkipToNext();
                                               Log.e(TAG, "Callback onSkipToNext");
                                               buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
                                           }

                                           @Override
                                           public void onSkipToPrevious() {
                                               super.onSkipToPrevious();
                                               Log.e(TAG, "Callback onSkipToPrevious");
                                               buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
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
                                               NotificationManager notificationManager =
                                                       (NotificationManager) getApplicationContext().
                                                               getSystemService(Context.NOTIFICATION_SERVICE);
                                               notificationManager.cancel(1);
                                               Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
                                               stopService(intent);
                                           }

                                           @Override
                                           public void onSeekTo(long pos) {
                                               super.onSeekTo(pos);
                                           }
                                       }
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSessionCompat.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
