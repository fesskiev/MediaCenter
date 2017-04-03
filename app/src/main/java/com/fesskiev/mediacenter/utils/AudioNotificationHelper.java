package com.fesskiev.mediacenter.utils;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.ui.MainActivity;


public class AudioNotificationHelper {

    private static AudioNotificationHelper audioNotificationHelper;
    private Notification notification;
    private Context context;

    private AudioNotificationHelper(Context context) {
        this.context = context;
    }

    public static AudioNotificationHelper getInstance(Context context) {
        if (audioNotificationHelper == null) {
            audioNotificationHelper = new AudioNotificationHelper(context);
        }
        return audioNotificationHelper;
    }

    public static final int NOTIFICATION_ID = 412;
    public static final int NOTIFICATION_FETCH_ID = 411;
    private static final int REQUEST_CODE = 100;

    public static final String ACTION_MEDIA_CONTROL_PLAY = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_PLAY";
    public static final String ACTION_MEDIA_CONTROL_PAUSE = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_PAUSE";
    public static final String ACTION_MEDIA_CONTROL_NEXT = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_NEXT";
    public static final String ACTION_MEDIA_CONTROL_PREVIOUS = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_PREVIOUS";

    public void updateNotification(AudioFile audioFile, Bitmap bitmap, int position, boolean isPlaying) {
        if (isPlaying) {
            notification = buildNotification(generateAction(R.drawable.icon_pause_media_control,
                    "Pause", ACTION_MEDIA_CONTROL_PAUSE), audioFile, bitmap, position * 1000, true);
        } else {
            notification = buildNotification(generateAction(R.drawable.icon_play_media_control,
                    "Play", ACTION_MEDIA_CONTROL_PLAY), audioFile, bitmap, position * 1000, false);
        }
        updateNotification(notification);
    }

    private void updateNotification(Notification notification) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }


    private Notification buildNotification(NotificationCompat.Action action,
                                           AudioFile audioFile,
                                           Bitmap bitmap,
                                           int position,
                                           boolean isPlaying) {
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
                        .setShowActionsInCompactView(0, 1, 2))
                .setLargeIcon(bitmap)
                .setColor(ContextCompat.getColor(context, R.color.primary_light))
                .setSmallIcon(R.drawable.icon_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(createContentIntent())
                .setContentTitle(artist)
                .setContentText(title);


        if (isPlaying) {
            notificationBuilder
                    .setWhen(System.currentTimeMillis() - position)
                    .setShowWhen(true)
                    .setUsesChronometer(true);
        } else {
            notificationBuilder
                    .setWhen(0)
                    .setShowWhen(false)
                    .setUsesChronometer(false);
        }

        notificationBuilder.addAction(generateAction(R.drawable.icon_previous_media_control,
                "Previous", ACTION_MEDIA_CONTROL_PREVIOUS));
        if (action != null) {
            notificationBuilder.addAction(action);
        } else {
            notificationBuilder.addAction(generateAction(R.drawable.icon_play_media_control,
                    "Play", ACTION_MEDIA_CONTROL_PLAY));
        }
        notificationBuilder.addAction(generateAction(R.drawable.icon_next_media_control,
                "Next", ACTION_MEDIA_CONTROL_NEXT));

        return notificationBuilder.build();
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
                .setColor(ContextCompat.getColor(context, R.color.primary_light))
                .setSmallIcon(R.drawable.icon_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(createContentIntent())
                .setContentTitle("Fetch content start")
                .setContentText("Job schedule start fetching content");

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_FETCH_ID, notificationBuilder.build());

    }
}
