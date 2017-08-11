package com.fesskiev.mediacenter.utils;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fesskiev.common.data.MapAudioFile;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static com.fesskiev.common.Constants.START_ACTIVITY_PATH;
import static com.fesskiev.common.Constants.TRACK_LIST_KEY;
import static com.fesskiev.common.Constants.TRACK_LIST_PATH;

public class WearHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener,
        MessageApi.MessageListener,
        CapabilityApi.CapabilityListener {

    private GoogleApiClient googleApiClient;

    public WearHelper(Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void connect() {
        googleApiClient.connect();
    }

    public void disconnect() {
        if ((googleApiClient != null) && googleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(googleApiClient, this);
            Wearable.MessageApi.removeListener(googleApiClient, this);
            Wearable.CapabilityApi.removeListener(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    public void startWearApp() {
        new Thread(() -> {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                Wearable.MessageApi.sendMessage(
                        googleApiClient, node, START_ACTIVITY_PATH, new byte[0]);

            }
        }).start();
    }

    public void updateTrackList(List<AudioFile> currentTrackList) {

        PutDataMapRequest dataMapRequest = PutDataMapRequest.create(TRACK_LIST_PATH);
        ArrayList<DataMap> dataMaps = new ArrayList<>();
        for (AudioFile audioFile : currentTrackList) {
            DataMap dataMap = new DataMap();

            MapAudioFile mapAudioFile = MapAudioFile.MapAudioFileBuilder.buildMapAudioFile()
                    .withAlbum(audioFile.album)
                    .withBitrate(audioFile.bitrate)
                    .withSampleRate(audioFile.sampleRate)
                    .withTitle(audioFile.title)
                    .withSize(audioFile.size)
                    .withTimestamp(audioFile.timestamp)
                    .withId(audioFile.id)
                    .withGenre(audioFile.genre)
                    .withArtist(audioFile.artist)
                    .withLength(audioFile.length)
                    .withTrackNumber(audioFile.trackNumber)
                    .build();
            mapAudioFile.toDataMap(dataMap);

            dataMaps.add(dataMap);
        }
        dataMapRequest.getDataMap().putDataMapArrayList(TRACK_LIST_KEY , dataMaps);

        PutDataRequest request = dataMapRequest.asPutDataRequest();
        request.setUrgent();
        Wearable.DataApi.putDataItem(googleApiClient, request);
    }

    public void updateTrack(AudioFile currentTrack) {

    }

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(googleApiClient).await();

        for (Node node : nodes.getNodes()) {
            Log.w("test", "node: " + node.toString());
            results.add(node.getId());
        }

        return results;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startWearApp();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

    }
}
