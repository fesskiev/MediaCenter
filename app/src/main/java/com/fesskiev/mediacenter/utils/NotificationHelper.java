package com.fesskiev.mediacenter.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.ui.MainActivity;


public class NotificationHelper {

    private static NotificationHelper INSTANCE;

    private static final String CONTROL_CHANNEL = "notification_channel_control";
    private static final String MEDIA_CHANNEL = "notification_channel_media";

    public static final String ACTION_SKIP_BUTTON = "com.fesskiev.player.action.SKIP_BUTTON";
    public static final String ACTION_ADD_BUTTON = "com.fesskiev.player.action.ADD_BUTTON";

    public static final String EXTRA_ACTION_BUTTON = "com.fesskiev.player.extra.ACTION_BUTTON";

    public static final int NOTIFICATION_ID = 412;
    public static final int NOTIFICATION_FETCH_ID = 411;
    public static final int NOTIFICATION_MEDIA_ID = 413;

    private static final int REQUEST_CODE = 100;

    public static final String ACTION_MEDIA_CONTROL_PLAY = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_PLAY";
    public static final String ACTION_MEDIA_CONTROL_PAUSE = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_PAUSE";
    public static final String ACTION_MEDIA_CONTROL_NEXT = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_NEXT";
    public static final String ACTION_MEDIA_CONTROL_PREVIOUS = "com.fesskiev.player.action.ACTION_MEDIA_CONTROL_PREVIOUS";
    public static final String ACTION_CLOSE_APP = "com.fesskiev.player.action.ACTION_CLOSE_APP";

    private Context context;
    private Bitmap bitmap;
    private boolean playing;

    private NotificationManager notificationManager;
    private Notification notification;

    private NotificationHelper() {
        this.context = MediaApplication.getInstance().getApplicationContext();
        this.notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Utils.isOreo()) {
            NotificationChannel channelControl = new NotificationChannel(CONTROL_CHANNEL,
                    context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            channelControl.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            channelControl.setSound(null, null);
            channelControl.enableVibration(false);
            channelControl.enableLights(false);
            channelControl.setShowBadge(false);
            notificationManager.createNotificationChannel(channelControl);

            NotificationChannel channelMedia = new NotificationChannel(MEDIA_CHANNEL,
                    context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            channelMedia.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            channelMedia.enableVibration(false);
            channelMedia.enableLights(false);
            channelMedia.setShowBadge(true);
            notificationManager.createNotificationChannel(channelMedia);
        }
    }

    public static NotificationHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NotificationHelper();
        }
        return INSTANCE;
    }


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
        RemoteViews notificationBigView = new RemoteViews(context.getPackageName(), R.layout.notification_big_layout);
        RemoteViews notificationView = new RemoteViews(context.getPackageName(), R.layout.notification_layout);

        notificationBigView.setTextViewText(R.id.notificationArtist, artist);
        notificationBigView.setTextViewText(R.id.notificationTitle, title);
        notificationBigView.setImageViewBitmap(R.id.notificationCover, bitmap);

        notificationView.setTextViewText(R.id.notificationArtist, artist);
        notificationView.setTextViewText(R.id.notificationTitle, title);
        notificationView.setImageViewBitmap(R.id.notificationCover, bitmap);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CONTROL_CHANNEL);

        notificationBuilder
                .setCustomBigContentView(notificationBigView)
                .setCustomContentView(notificationView)
                .setSmallIcon(R.drawable.icon_notification_player)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setContentIntent(createContentIntent());

        notificationBigView.setOnClickPendingIntent(R.id.notificationNext, getPendingIntentAction(ACTION_MEDIA_CONTROL_NEXT));
        notificationBigView.setOnClickPendingIntent(R.id.notificationPrevious, getPendingIntentAction(ACTION_MEDIA_CONTROL_PREVIOUS));
        if (!isPlaying()) {
            notificationBigView.setImageViewResource(R.id.notificationPlayPause, R.drawable.icon_play_media_control);
            notificationBigView.setOnClickPendingIntent(R.id.notificationPlayPause, getPendingIntentAction(ACTION_MEDIA_CONTROL_PLAY));
        } else {
            notificationBigView.setImageViewResource(R.id.notificationPlayPause, R.drawable.icon_pause_media_control);
            notificationBigView.setOnClickPendingIntent(R.id.notificationPlayPause, getPendingIntentAction(ACTION_MEDIA_CONTROL_PAUSE));
        }
        notificationBigView.setOnClickPendingIntent(R.id.notificationClose, getPendingIntentAction(ACTION_CLOSE_APP));


        notificationView.setOnClickPendingIntent(R.id.notificationNext, getPendingIntentAction(ACTION_MEDIA_CONTROL_NEXT));
        notificationView.setOnClickPendingIntent(R.id.notificationPrevious, getPendingIntentAction(ACTION_MEDIA_CONTROL_PREVIOUS));
        if (!isPlaying()) {
            notificationView.setImageViewResource(R.id.notificationPlayPause, R.drawable.icon_play_media_control);
            notificationView.setOnClickPendingIntent(R.id.notificationPlayPause, getPendingIntentAction(ACTION_MEDIA_CONTROL_PLAY));
        } else {
            notificationView.setImageViewResource(R.id.notificationPlayPause, R.drawable.icon_pause_media_control);
            notificationView.setOnClickPendingIntent(R.id.notificationPlayPause, getPendingIntentAction(ACTION_MEDIA_CONTROL_PAUSE));
        }


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

    private PendingIntent getPendingIntentAction(String action, String extra) {
        Intent intent = new Intent(action);
        intent.putExtra(EXTRA_ACTION_BUTTON, extra);
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, 0);
    }

    public void stopNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public static void removeNotificationAndCloseNotificationBar(Context context){
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_MEDIA_ID);
            closeNotificationBar(context);
        }
    }
    private static void closeNotificationBar(Context context){
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }

    public Notification getNotification() {
        return notification;
    }

    public void createFetchNotification() {

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, MEDIA_CHANNEL);
        notificationBuilder
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setSmallIcon(R.drawable.icon_notification_player)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(createContentIntent())
                .setContentTitle("Fetch content start")
                .setContentText("Job schedule start fetching content")
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true);

        notificationManager.notify(NOTIFICATION_FETCH_ID, notificationBuilder.build());

    }

    public void createMediaFoundNotification(String path, int id) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        RemoteViews notificationBigView = new RemoteViews(context.getPackageName(), R.layout.notification_fetch_big_lyout);
        RemoteViews notificationView = new RemoteViews(context.getPackageName(), R.layout.notification_fetch_layout);

        notificationBigView.setTextViewText(R.id.notificationTitle, path);
        notificationBigView.setTextViewText(R.id.notificationText, path);

        notificationView.setTextViewText(R.id.notificationTitle, path);
        notificationView.setTextViewText(R.id.notificationText, path);

        notificationBigView.setOnClickPendingIntent(R.id.skipButton,
                getPendingIntentAction(ACTION_SKIP_BUTTON, path));
        notificationBigView.setOnClickPendingIntent(R.id.addButton,
                getPendingIntentAction(ACTION_ADD_BUTTON, path));


        NotificationCompat.Builder notificationBuilder
                = new NotificationCompat.Builder(context, MEDIA_CHANNEL);
        notificationBuilder
                .setCustomBigContentView(notificationBigView)
                .setCustomContentView(notificationView)
                .setSmallIcon(R.drawable.icon_notification_player)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(PendingIntent.getActivity(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true);


        notificationManager.notify(id, notificationBuilder.build());
    }

    public boolean isPlaying() {
        return playing;
    }
}
