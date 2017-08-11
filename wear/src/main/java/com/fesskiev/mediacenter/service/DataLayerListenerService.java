package com.fesskiev.mediacenter.service;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fesskiev.common.data.MapAudioFile;
import com.fesskiev.mediacenter.ui.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.fesskiev.common.Constants.TRACK_LIST_KEY;
import static com.fesskiev.common.Constants.TRACK_LIST_PATH;
import static com.fesskiev.common.Constants.START_ACTIVITY_PATH;

public class DataLayerListenerService extends WearableListenerService {

    private static final String TAG = "DataLayerService";

    public static final String ACTION_TRACK_LIST = "com.fesskiev.player.wear.ACTION_TRACK_LIST";

    public static final String EXTRA_TRACK_LIST = "com.fesskiev.player.wear.EXTRA_TRACK_LIST";

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
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.w(TAG, "onDataChanged: " + dataEvents);
        if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
            ConnectionResult connectionResult = googleApiClient
                    .blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "DataLayerListenerService failed to connect to GoogleApiClient, "
                        + "error code: " + connectionResult.getErrorCode());
                return;
            }
        }

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
                        audioFiles.add(MapAudioFile.toMapAudioFile(dataMap));
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


    private void sendTrackListBroadcast(ArrayList<MapAudioFile> audioFiles){
        Intent intent = new Intent(ACTION_TRACK_LIST);
        intent.putParcelableArrayListExtra(EXTRA_TRACK_LIST, audioFiles);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}