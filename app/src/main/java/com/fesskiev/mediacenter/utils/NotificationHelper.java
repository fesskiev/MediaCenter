package com.fesskiev.mediacenter.utils;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.ui.MainActivity;


public class NotificationHelper {

    private static NotificationHelper INSTANCE;
    private Notification notification;
    private Context context;

    private NotificationHelper() {
        this.context = MediaApplication.getInstance().getApplicationContext();
    }

    public static NotificationHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NotificationHelper();
        }
        return INSTANCE;
    }

    public static final int NOTIFICATION_ID = 412;
    public static final int NOTIFICATION_FETCH_ID = 411;

    private static final int REQUEST_CODE = 100;

    public static final String ACTION_MEDIA_CONTROL_PLAY = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_PLAY";
    public static final String ACTION_MEDIA_CONTROL_PAUSE = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_PAUSE";
    public static final String ACTION_MEDIA_CONTROL_NEXT = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_NEXT";
    public static final String ACTION_MEDIA_CONTROL_PREVIOUS = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_PREVIOUS";
    public static final String ACTION_CLOSE_APP = "com.fesskiev.player.action.ACTION_CLOSE_APP";

    private Bitmap bitmap;
    private boolean playing;

    public void updateNotification(AudioFile audioFile, Bitmap bitmap, boolean isPlaying) {
        this.playing = isPlaying;
        this.bitmap = bitmap;

        notification = buildNotification(audioFile);
        updateNotification(notification);
    }


    public void updatePlayingState(AudioFile audioFile, boolean isPlaying) {
        this.playing = isPlaying;
        notification = buildNotification(audioFile);
        updateNotification(notification);
    }

    private void updateNotification(Notification notification) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }


    private Notification buildNotification(AudioFile audioFile) {
        String artist, title;
        if (audioFile != null) {
            artist = audioFile.artist;
            title = audioFile.title;
        } else {
            artist = context.getString(R.string.playback_control_track_not_selected);
            title = context.getString(R.string.playback_control_track_not_selected);
        }
        RemoteViews notificationView = new RemoteViews(context.getPackageName(), R.layout.notification_layout);

        notificationView.setTextViewText(R.id.notificationArtist, artist);
        notificationView.setTextViewText(R.id.notificationTitle, title);
        notificationView.setImageViewBitmap(R.id.notificationCover, bitmap);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder
                .setCustomBigContentView(notificationView)
                .setSmallIcon(R.drawable.icon_notification_player)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(createContentIntent());

        notificationView.setOnClickPendingIntent(R.id.notificationNext, getPendingIntentAction(ACTION_MEDIA_CONTROL_NEXT));
        notificationView.setOnClickPendingIntent(R.id.notificationPrevious, getPendingIntentAction(ACTION_MEDIA_CONTROL_PREVIOUS));
        if (!isPlaying()) {
            notificationView.setImageViewResource(R.id.notificationPlayPause, R.drawable.icon_play_media_control);
            notificationView.setOnClickPendingIntent(R.id.notificationPlayPause, getPendingIntentAction(ACTION_MEDIA_CONTROL_PLAY));
        } else {
            notificationView.setImageViewResource(R.id.notificationPlayPause, R.drawable.icon_pause_media_control);
            notificationView.setOnClickPendingIntent(R.id.notificationPlayPause, getPendingIntentAction(ACTION_MEDIA_CONTROL_PAUSE));
        }

        notificationView.setOnClickPendingIntent(R.id.notificationClose, getPendingIntentAction(ACTION_CLOSE_APP));
        return notificationBuilder.build();
    }

    private PendingIntent createContentIntent() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getPendingIntentAction(String action) {
        Intent intent = new Intent(action);
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, 0);
    }

    public void stopNotification() {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }


    public Notification getNotification() {
        return notification;
    }

    public void createFetchNotification() {

        NotificationCompat.Builder notificationBuilder
                = new NotificationCompat.Builder(context);
        notificationBuilder
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setSmallIcon(R.drawable.icon_notification_fetch)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(createContentIntent())
                .setContentTitle("Fetch content start")
                .setContentText("Job schedule start fetching content")
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_FETCH_ID, notificationBuilder.build());

    }

    public void createMediaFoundNotification(String path, int id) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        NotificationCompat.Builder notificationBuilder
                = new NotificationCompat.Builder(context);
        notificationBuilder
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setSmallIcon(R.drawable.icon_notification_fetch)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(PendingIntent.getActivity(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setContentTitle(context.getString(R.string.notification_folder_found))
                .setContentText(path)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notificationBuilder.build());

    }

    public boolean isPlaying() {
        return playing;
    }
}
