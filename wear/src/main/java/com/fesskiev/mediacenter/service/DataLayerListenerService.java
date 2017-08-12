package com.fesskiev.mediacenter.service;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fesskiev.common.data.MapAudioFile;
import com.fesskiev.mediacenter.ui.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
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
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.fesskiev.common.Constants.COVER;
import static com.fesskiev.common.Constants.TRACK_LIST_KEY;
import static com.fesskiev.common.Constants.TRACK_LIST_PATH;
import static com.fesskiev.common.Constants.START_ACTIVITY_PATH;

public class DataLayerListenerService extends WearableListenerService {

    private static final String TAG = "DataLayerService";

    public static final String ACTION_SEND_MESSAGE = "com.fesskiev.player.wear.action.ACTION_SEND_MESSAGE";

    public static final String ACTION_TRACK_LIST = "com.fesskiev.player.wear.ACTION_TRACK_LIST";

    public static final String EXTRA_TRACK_LIST = "com.fesskiev.player.wear.EXTRA_TRACK_LIST";
    public static final String EXTRA_MESSAGE_PATH = "com.fesskiev.player.wear.EXTRA_MESSAGE_PATH";

    public static void sendMessage(Context context, String type) {
        Intent intent = new Intent(context, DataLayerListenerService.class);
        intent.setAction(ACTION_SEND_MESSAGE);
        intent.putExtra(EXTRA_MESSAGE_PATH, type);
        context.startService(intent);
    }

    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case ACTION_SEND_MESSAGE:
                    String path = intent.getStringExtra(EXTRA_MESSAGE_PATH);
                    sendMessageToHandSet(path);
                    break;

            }
        }
        return START_STICKY;
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        if (!googleApiClient.isConnected()) {
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

                    ArrayList<MapAudioFile> audioFiles = new ArrayList<>();
                    for (DataMap dataMap : dataMaps) {
                        MapAudioFile audioFile = MapAudioFile.toMapAudioFile(dataMap);

                        Asset coverAsset = dataMap.getAsset(COVER);

                        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                                googleApiClient, coverAsset).await().getInputStream();
                        if (assetInputStream != null) {
                            audioFile.cover = BitmapFactory.decodeStream(assetInputStream);
                        }

                        audioFiles.add(audioFile);
                    }
                    sendTrackListBroadcast(audioFiles);

                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
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


    private void sendTrackListBroadcast(ArrayList<MapAudioFile> audioFiles) {
        Intent intent = new Intent(ACTION_TRACK_LIST);
        intent.putParcelableArrayListExtra(EXTRA_TRACK_LIST, audioFiles);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMessageToHandSet(String path){
        if (!googleApiClient.isConnected()) {
            return;
        }
        new Thread(() -> {
            List<String> nodeIds = getNodes();
            for(String nodeId : nodeIds) {
                Wearable.MessageApi.sendMessage(googleApiClient, nodeId, path,
                        new byte[0]);
            }
        }).start();
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
}