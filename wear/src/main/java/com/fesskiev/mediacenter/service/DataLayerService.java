package com.fesskiev.mediacenter.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fesskiev.common.data.MapAudioFile;
import com.fesskiev.common.data.MapPlayback;
import com.fesskiev.mediacenter.ui.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.fesskiev.common.Constants.COVER;
import static com.fesskiev.common.Constants.PLAYBACK_KEY;
import static com.fesskiev.common.Constants.PLAYBACK_PATH;
import static com.fesskiev.common.Constants.PLAY_PATH;
import static com.fesskiev.common.Constants.TRACK_KEY;
import static com.fesskiev.common.Constants.TRACK_LIST_KEY;
import static com.fesskiev.common.Constants.TRACK_LIST_PATH;
import static com.fesskiev.common.Constants.START_ACTIVITY_PATH;
import static com.fesskiev.common.Constants.TRACK_PATH;

public class DataLayerService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener,
        MessageApi.MessageListener {

    private static final String TAG = "DataLayerService";

    public static final String ACTION_SEND_MESSAGE = "com.fesskiev.player.wear.action.ACTION_SEND_MESSAGE";

    public static final String ACTION_TRACK_LIST = "com.fesskiev.player.wear.ACTION_TRACK_LIST";
    public static final String ACTION_TRACK = "com.fesskiev.player.wear.ACTION_TRACK";
    public static final String ACTION_PLAYBACK = "com.fesskiev.player.wear.ACTION_PLAYBACK";

    public static final String EXTRA_TRACK_LIST = "com.fesskiev.player.wear.EXTRA_TRACK_LIST";
    public static final String EXTRA_TRACK = "com.fesskiev.player.wear.EXTRA_TRACK";
    public static final String EXTRA_PLAYBACK = "com.fesskiev.player.wear.EXTRA_PLAYBACK";
    public static final String EXTRA_MESSAGE_PATH = "com.fesskiev.player.wear.EXTRA_MESSAGE_PATH";
    public static final String EXTRA_MESSAGE_DATA = "com.fesskiev.player.wear.EXTRA_MESSAGE_DATA";

    public static void sendMessage(Context context, String type) {
        Intent intent = new Intent(context, DataLayerService.class);
        intent.setAction(ACTION_SEND_MESSAGE);
        intent.putExtra(EXTRA_MESSAGE_PATH, type);
        context.startService(intent);
    }

    public static void sendChooseTrackMessage(Context context, String type, String title) {
        Intent intent = new Intent(context, DataLayerService.class);
        intent.setAction(ACTION_SEND_MESSAGE);
        intent.putExtra(EXTRA_MESSAGE_PATH, type);
        intent.putExtra(EXTRA_MESSAGE_DATA, title);
        context.startService(intent);
    }

    private GoogleApiClient googleApiClient;
    private ServiceThread serviceThread;

    @Override
    public void onCreate() {
        super.onCreate();
        serviceThread = new ServiceThread();
        serviceThread.start();


        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    public void disconnect() {
        if ((googleApiClient != null) && googleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(googleApiClient, this);
            Wearable.MessageApi.removeListener(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_SEND_MESSAGE:
                        String path = intent.getStringExtra(EXTRA_MESSAGE_PATH);
                        String data = intent.getStringExtra(EXTRA_MESSAGE_DATA);
                        serviceThread.processSendMessage(path, data);
                        break;

                }
            }
        }
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
        serviceThread.quit();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(googleApiClient, this);
        Wearable.MessageApi.addListener(googleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.e(TAG, "onDataChanged");
        if (!googleApiClient.isConnected()) {
            Log.e(TAG, "onDataChanged not connected!");
            return;
        }
        processEvent(dataEvents);
    }

    private void processEvent(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();

                Uri uri = event.getDataItem().getUri();
                String path = uri.getPath();
                if (TRACK_LIST_PATH.equals(path)) {
                    List<DataMap> dataMaps = DataMapItem.fromDataItem(item).getDataMap()
                            .getDataMapArrayList(TRACK_LIST_KEY);

                    serviceThread.processTrackList(dataMaps);
                } else if (TRACK_PATH.equals(path)) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap()
                            .getDataMap(TRACK_KEY);
                    serviceThread.processTrack(dataMap);
                } else if (PLAYBACK_PATH.equals(path)) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap()
                            .getDataMap(PLAYBACK_KEY);
                    serviceThread.processPlayback(dataMap);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    private class ServiceThread extends HandlerThread {

        private final int SEND_MESSAGE = 0;
        private final int TRACK_LIST = 1;
        private final int TRACK = 2;
        private final int PLAYBACK = 3;

        private Handler handler;

        public ServiceThread() {
            super(ServiceThread.class.getName(), Process.THREAD_PRIORITY_BACKGROUND);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            handler = new Handler(getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case SEND_MESSAGE:
                            MessageObject message = (MessageObject) msg.obj;
                            sendMessageApi(message.path, message.data);
                            break;
                        case TRACK_LIST:
                            List<DataMap> dataMaps = (List<DataMap>) msg.obj;
                            fetchTrackList(dataMaps);
                            break;
                        case TRACK:
                            DataMap dataMap = (DataMap) msg.obj;
                            fetchTrack(dataMap);
                            break;
                        case PLAYBACK:
                            DataMap playbackMap = (DataMap) msg.obj;
                            fetchPlayback(playbackMap);
                            break;
                    }
                }

            };
        }

        private void fetchPlayback(DataMap playbackMap) {
            MapPlayback playback = MapPlayback.toMapPlayback(playbackMap);
            sendPlaybackBroadcast(playback);
        }

        private void fetchTrack(DataMap dataMap) {
            MapAudioFile audioFile = MapAudioFile.toMapAudioFile(dataMap);

            Asset coverAsset = dataMap.getAsset(COVER);
            if (coverAsset != null) {
                InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                        googleApiClient, coverAsset).await().getInputStream();
                if (assetInputStream != null) {
                    audioFile.cover = BitmapFactory.decodeStream(assetInputStream);
                }
            }
            sendTrackBroadcast(audioFile);
        }

        private void fetchTrackList(List<DataMap> dataMaps) {
            ArrayList<MapAudioFile> audioFiles = new ArrayList<>();
            for (DataMap dataMap : dataMaps) {
                MapAudioFile audioFile = MapAudioFile.toMapAudioFile(dataMap);

                Asset coverAsset = dataMap.getAsset(COVER);
                if (coverAsset != null) {
                    InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                            googleApiClient, coverAsset).await().getInputStream();
                    if (assetInputStream != null) {
                        audioFile.cover = BitmapFactory.decodeStream(assetInputStream);
                    }
                }
                audioFiles.add(audioFile);
            }
            sendTrackListBroadcast(audioFiles);
        }

        private void sendMessageApi(String path, String data) {
            if (!googleApiClient.isConnected()) {
                return;
            }
            List<String> nodeIds = getNodes();
            for (String nodeId : nodeIds) {
                Wearable.MessageApi.sendMessage(googleApiClient, nodeId, path,
                        data == null ? new byte[0] : data.getBytes());
            }
        }


        public void processSendMessage(String path, String data) {
            handler.sendMessage(Message.obtain(Message.obtain(handler, SEND_MESSAGE,
                    new MessageObject(path, data))));
        }

        public void processTrackList(List<DataMap> dataMaps) {
            handler.sendMessage(Message.obtain(Message.obtain(handler, TRACK_LIST, dataMaps)));
        }

        public void processTrack(DataMap dataMap) {
            handler.sendMessage(Message.obtain(Message.obtain(handler, TRACK, dataMap)));
        }

        public void processPlayback(DataMap dataMap) {
            handler.sendMessage(Message.obtain(Message.obtain(handler, PLAYBACK, dataMap)));
        }

        private class MessageObject {

            String path;
            String data;

            public MessageObject(String path, String data) {
                this.path = path;
                this.data = data;
            }
        }
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(START_ACTIVITY_PATH)) {
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }
    }

    private void sendPlaybackBroadcast(MapPlayback playback) {
        Intent intent = new Intent(ACTION_PLAYBACK);
        intent.putExtra(EXTRA_PLAYBACK, playback);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendTrackListBroadcast(ArrayList<MapAudioFile> audioFiles) {
        Intent intent = new Intent(ACTION_TRACK_LIST);
        intent.putParcelableArrayListExtra(EXTRA_TRACK_LIST, audioFiles);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendTrackBroadcast(MapAudioFile audioFile) {
        Intent intent = new Intent(ACTION_TRACK);
        intent.putExtra(EXTRA_TRACK, audioFile);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    private List<String> getNodes() {
        List<String> results = new ArrayList<>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}