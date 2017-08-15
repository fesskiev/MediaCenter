package com.fesskiev.mediacenter.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fesskiev.common.data.MapAudioFile;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

import static com.fesskiev.common.Constants.CHOOSE_TRACK;
import static com.fesskiev.common.Constants.COVER;
import static com.fesskiev.common.Constants.NEXT_PATH;
import static com.fesskiev.common.Constants.PAUSE_PATH;
import static com.fesskiev.common.Constants.PLAY_PATH;
import static com.fesskiev.common.Constants.PREVIOUS_PATH;
import static com.fesskiev.common.Constants.START_ACTIVITY_PATH;
import static com.fesskiev.common.Constants.TRACK_LIST_KEY;
import static com.fesskiev.common.Constants.TRACK_LIST_PATH;
import static com.fesskiev.common.Constants.VOLUME_DOWN;
import static com.fesskiev.common.Constants.VOLUME_OFF;
import static com.fesskiev.common.Constants.VOLUME_UP;

public class WearHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener,
        MessageApi.MessageListener {

    public interface OnWearControlListener {

        void onPrevious();

        void onNext();

        void onPause();

        void onPlay();

        void onVolumeUp();

        void onVolumeDown();

        void onVolumeOff();

        void onChooseTrack(String title);
    }

    private OnWearControlListener listener;

    private GoogleApiClient googleApiClient;
    private Subscription subscription;

    private AudioPlayer audioPlayer;

    public WearHelper(Context context, AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;

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
        if (googleApiClient != null && googleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(googleApiClient, this);
            Wearable.MessageApi.removeListener(googleApiClient, this);
            googleApiClient.disconnect();
        }
        RxUtils.unsubscribe(subscription);
    }

    public void startWearApp() {
        subscription = getNodes()
                .subscribeOn(Schedulers.io())
                .doOnNext(nodes -> {
                    for (String node : nodes) {
                        Wearable.MessageApi.sendMessage(
                                googleApiClient, node, START_ACTIVITY_PATH, new byte[0]);

                    }
                }).subscribe();

    }

    public void updateTrackList(List<AudioFile> currentTrackList) {
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            return;
        }
        subscription = Observable.just(currentTrackList)
                .subscribeOn(Schedulers.io())
                .flatMap(audioFiles -> {
                    ArrayList<DataMap> dataMaps = new ArrayList<>();

                    List<AudioFile> list = new ArrayList<>(currentTrackList);
                    for (AudioFile audioFile : list) {
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
                        mapAudioFile.toDataMap(dataMap).putAsset(COVER, toAsset(audioFile.getArtworkPath()));
                        dataMaps.add(dataMap);
                    }
                    return Observable.just(dataMaps);
                })
                .doOnNext(dataMaps -> {
                    PutDataMapRequest dataMapRequest = PutDataMapRequest.create(TRACK_LIST_PATH);
                    dataMapRequest.getDataMap().putDataMapArrayList(TRACK_LIST_KEY, dataMaps);

                    PutDataRequest request = dataMapRequest.asPutDataRequest();
                    request.setUrgent();
                    Wearable.DataApi.putDataItem(googleApiClient, request);
                })
                .subscribe();
    }

    public void updateTrack(AudioFile currentTrack) {

    }

    private Observable<List<String>> getNodes() {
        List<String> results = new ArrayList<>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return Observable.just(results);
    }

    private static Asset toAsset(String path) {
        if (path != null) {
            ByteArrayOutputStream byteStream = null;
            try {
                byteStream = new ByteArrayOutputStream();

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
                bitmap = Bitmap.createScaledBitmap(bitmap, 320, 320, true);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);

                return Asset.createFromBytes(byteStream.toByteArray());
            } finally {
                if (byteStream != null) {
                    try {
                        byteStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(googleApiClient, this);
        Wearable.MessageApi.addListener(googleApiClient, this);

        updateTrackList(audioPlayer.getCurrentTrackList());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.wtf("test", "event: " + messageEvent.getPath());
        if (listener != null) {
            switch (messageEvent.getPath()) {
                case PREVIOUS_PATH:
                    listener.onPrevious();
                    break;
                case NEXT_PATH:
                    listener.onNext();
                    break;
                case PAUSE_PATH:
                    listener.onPause();
                    break;
                case PLAY_PATH:
                    listener.onPlay();
                    break;
                case VOLUME_DOWN:
                    listener.onVolumeDown();
                    break;
                case VOLUME_UP:
                    listener.onVolumeUp();
                    break;
                case VOLUME_OFF:
                    listener.onVolumeOff();
                    break;
                case CHOOSE_TRACK:
                    listener.onChooseTrack(new String(messageEvent.getData()));
                    break;
            }
        }
    }

    public void setOnWearControlListener(OnWearControlListener listener) {
        this.listener = listener;
    }
}
