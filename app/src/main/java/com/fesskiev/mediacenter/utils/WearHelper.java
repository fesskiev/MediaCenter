package com.fesskiev.mediacenter.utils;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fesskiev.common.data.MapAudioFile;
import com.fesskiev.common.data.MapPlayback;
import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
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
import com.google.android.wearable.intent.RemoteIntent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.fesskiev.common.Constants.CHOOSE_TRACK;
import static com.fesskiev.common.Constants.COVER;
import static com.fesskiev.common.Constants.NEXT_PATH;
import static com.fesskiev.common.Constants.PAUSE_PATH;
import static com.fesskiev.common.Constants.PLAYBACK_KEY;
import static com.fesskiev.common.Constants.PLAYBACK_PATH;
import static com.fesskiev.common.Constants.PLAY_PATH;
import static com.fesskiev.common.Constants.PREVIOUS_PATH;
import static com.fesskiev.common.Constants.REPEAT_OFF;
import static com.fesskiev.common.Constants.REPEAT_ON;
import static com.fesskiev.common.Constants.SHUTDOWN;
import static com.fesskiev.common.Constants.START_ACTIVITY_PATH;
import static com.fesskiev.common.Constants.SYNC_PATH;
import static com.fesskiev.common.Constants.TRACK_KEY;
import static com.fesskiev.common.Constants.TRACK_LIST_KEY;
import static com.fesskiev.common.Constants.TRACK_LIST_PATH;
import static com.fesskiev.common.Constants.TRACK_PATH;
import static com.fesskiev.common.Constants.VOLUME_DOWN_PATH;
import static com.fesskiev.common.Constants.VOLUME_OFF;
import static com.fesskiev.common.Constants.VOLUME_UP_PATH;

public class WearHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener,
        MessageApi.MessageListener,
        CapabilityApi.CapabilityListener {

    private static final String PLAY_STORE_APP_URI =
            "https://play.google.com/store/apps/details?id=com.fesskiev.mediacenter";

    private static final String CAPABILITY_WEAR_APP = "verify_remote_wear_app";

    private Context context;
    private Set<Node> wearNodesWithApp;
    private List<Node> allConnectedNodes;

    private boolean playing;
    private boolean looping;

    public interface OnWearControlListener {

        void onPrevious();

        void onNext();

        void onPause();

        void onPlay();

        void onVolumeUp();

        void onVolumeDown();

        void onVolumeOff();

        void onChooseTrack(String title);

        void onRepeatChanged(boolean repeat);

        void onShutdown();
    }

    public interface OnWearConnectionListener {

        void onNoDeviceConnected();

        void onWithoutApp();

        void onSomeDeviceWithApp();

        void onAllDeviceWithApp();
    }

    public interface OnPlayStoreResultListener {

        void onRequestSuccessful();

        void onRequestFailed();
    }

    private OnWearControlListener controlListener;
    private OnWearConnectionListener connectionListener;
    private OnPlayStoreResultListener playStoreResultListener;

    private GoogleApiClient googleApiClient;
    private CompositeDisposable subscription;

    private AudioPlayer audioPlayer;
    private PlaybackService service;
    private boolean available;


    public WearHelper(Context context) {
        this(context, null);
    }

    public WearHelper(Context context, PlaybackService service) {
        this.context = context;
        if (!isGooglePlayServicesAvailable()) {
            return;
        } else {
            available = true;
        }
        this.service = service;
        this.audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        this.subscription = new CompositeDisposable();

        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void connect() {
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    public void disconnect() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(googleApiClient, this);
            Wearable.MessageApi.removeListener(googleApiClient, this);
            Wearable.CapabilityApi.removeCapabilityListener(googleApiClient, this,
                    CAPABILITY_WEAR_APP);
            googleApiClient.disconnect();
        }

        if (subscription != null) {
            subscription.clear();
        }
    }

    public void startWearApp() {
        subscription.add(getNodes()
                .subscribeOn(Schedulers.io())
                .doOnNext(nodes -> {
                    for (String node : nodes) {
                        Wearable.MessageApi.sendMessage(
                                googleApiClient, node, START_ACTIVITY_PATH, new byte[0]);

                    }
                }).subscribe());

    }

    public void updatePlayingState() {
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            return;
        }
        this.playing = service.isPlaying();
        this.looping = service.isLooping();

        subscription.add(Observable.just(service)
                .subscribeOn(Schedulers.io())
                .flatMap(ser -> {
                    DataMap dataMap = new DataMap();

                    MapPlayback mapPlayback = MapPlayback.MapPlaybackBuilder.buildMapPlayback()
                            .withDuration(ser.getDuration())
                            .withPosition(ser.getPosition())
                            .withPositionPercent(ser.getPositionPercent())
                            .withVolume(ser.getVolume())
                            .withFocusedVolume(ser.getFocusedVolume())
                            .withDurationScale(ser.getDurationScale())
                            .withPlaying(ser.isPlaying())
                            .withLooping(ser.isLooping())
                            .build();
                    mapPlayback.toDataMap(dataMap);
                    dataMap.putLong("Time", System.currentTimeMillis());

                    return Observable.just(dataMap);
                }).doOnNext(dataMap -> {
                    PutDataMapRequest dataMapRequest = PutDataMapRequest.create(PLAYBACK_PATH);
                    dataMapRequest.getDataMap().putDataMap(PLAYBACK_KEY, dataMap);

                    PutDataRequest request = dataMapRequest.asPutDataRequest();
                    request.setUrgent();
                    Wearable.DataApi.putDataItem(googleApiClient, request);
                })
                .subscribe());
    }

    public void updateTrackList(List<AudioFile> currentTrackList) {
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            return;
        }
        subscription.add(Observable.just(currentTrackList)
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
                                .withId(audioFile.fileId)
                                .withGenre(audioFile.genre)
                                .withArtist(audioFile.artist)
                                .withLength(audioFile.length)
                                .withTrackNumber(audioFile.trackNumber)
                                .build();
                        dataMap.putLong("Time", System.currentTimeMillis());
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
                .subscribe());
    }

    public void updateTrack(AudioFile currentTrack) {
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            return;
        }
        subscription.add(Observable.just(currentTrack)
                .subscribeOn(Schedulers.io())
                .flatMap(audioFile -> {
                    DataMap dataMap = new DataMap();

                    MapAudioFile mapAudioFile = MapAudioFile.MapAudioFileBuilder.buildMapAudioFile()
                            .withAlbum(audioFile.album)
                            .withBitrate(audioFile.bitrate)
                            .withSampleRate(audioFile.sampleRate)
                            .withTitle(audioFile.title)
                            .withSize(audioFile.size)
                            .withTimestamp(audioFile.timestamp)
                            .withId(audioFile.fileId)
                            .withGenre(audioFile.genre)
                            .withArtist(audioFile.artist)
                            .withLength(audioFile.length)
                            .withTrackNumber(audioFile.trackNumber)
                            .build();
                    mapAudioFile.toDataMap(dataMap).putAsset(COVER, toAsset(audioFile.getArtworkPath()));
                    return Observable.just(dataMap);
                }).doOnNext(dataMap -> {
                    PutDataMapRequest dataMapRequest = PutDataMapRequest.create(TRACK_PATH);
                    dataMapRequest.getDataMap().putLong("Time", System.currentTimeMillis());
                    dataMapRequest.getDataMap().putDataMap(TRACK_KEY, dataMap);

                    PutDataRequest request = dataMapRequest.asPutDataRequest();
                    request.setUrgent();
                    Wearable.DataApi.putDataItem(googleApiClient, request);
                })
                .subscribe());
    }

    private Observable<List<String>> getNodes() {
        return Observable.create(subscriber -> {
            List<String> results = new ArrayList<>();
            NodeApi.GetConnectedNodesResult nodes =
                    Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
            for (Node node : nodes.getNodes()) {
                results.add(node.getId());
            }
            subscriber.onNext(results);
        });
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
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        wearNodesWithApp = capabilityInfo.getNodes();

        findAllWearDevices();
        verifyNode();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(googleApiClient, this);
        Wearable.MessageApi.addListener(googleApiClient, this);
        Wearable.CapabilityApi.addCapabilityListener(googleApiClient, this,
                CAPABILITY_WEAR_APP);

        findAllWearDevices();
        findWearDevicesWithApp();
    }

    private void findAllWearDevices() {
        PendingResult<NodeApi.GetConnectedNodesResult> pendingResult =
                Wearable.NodeApi.getConnectedNodes(googleApiClient);

        pendingResult.setResultCallback(getConnectedNodesResult -> {
            if (getConnectedNodesResult.getStatus().isSuccess()) {
                allConnectedNodes = getConnectedNodesResult.getNodes();
                verifyNode();
            }
        });
    }

    private void findWearDevicesWithApp() {
        PendingResult<CapabilityApi.GetCapabilityResult> pendingResult =
                Wearable.CapabilityApi.getCapability(
                        googleApiClient,
                        CAPABILITY_WEAR_APP,
                        CapabilityApi.FILTER_ALL);

        pendingResult.setResultCallback(getCapabilityResult -> {
            if (getCapabilityResult.getStatus().isSuccess()) {
                CapabilityInfo capabilityInfo = getCapabilityResult.getCapability();
                wearNodesWithApp = capabilityInfo.getNodes();
                verifyNode();
            }
        });
    }

    private void verifyNode() {
        if ((wearNodesWithApp == null) || (allConnectedNodes == null)) {
            return;
        }
        if (allConnectedNodes.isEmpty()) {
            if (connectionListener != null) {
                connectionListener.onNoDeviceConnected();
            }
        } else if (wearNodesWithApp.isEmpty()) {
            if (connectionListener != null) {
                connectionListener.onWithoutApp();
            }
        } else if (wearNodesWithApp.size() < allConnectedNodes.size()) {
            if (connectionListener != null) {
                connectionListener.onSomeDeviceWithApp();
            }
        } else {
            if (connectionListener != null) {
                connectionListener.onAllDeviceWithApp();
            }
        }
    }

    public void openPlayStoreOnWearDevicesWithoutApp() {
        ArrayList<Node> nodesWithoutApp = new ArrayList<>();

        for (Node node : allConnectedNodes) {
            if (!wearNodesWithApp.contains(node)) {
                nodesWithoutApp.add(node);
            }
        }

        if (!nodesWithoutApp.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(PLAY_STORE_APP_URI));

            for (Node node : nodesWithoutApp) {
                RemoteIntent.startRemoteActivity(context, intent,
                        resultReceiver,
                        node.getId());
            }
        }
    }

    private final ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == RemoteIntent.RESULT_OK) {
                if (playStoreResultListener != null) {
                    playStoreResultListener.onRequestSuccessful();
                }
            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                if (playStoreResultListener != null) {
                    playStoreResultListener.onRequestFailed();
                }
            }
        }
    };

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
        if (controlListener != null) {
            switch (messageEvent.getPath()) {
                case PREVIOUS_PATH:
                    controlListener.onPrevious();
                    break;
                case NEXT_PATH:
                    controlListener.onNext();
                    break;
                case PAUSE_PATH:
                    controlListener.onPause();
                    break;
                case PLAY_PATH:
                    controlListener.onPlay();
                    break;
                case VOLUME_DOWN_PATH:
                    controlListener.onVolumeDown();
                    break;
                case VOLUME_UP_PATH:
                    controlListener.onVolumeUp();
                    break;
                case VOLUME_OFF:
                    controlListener.onVolumeOff();
                    break;
                case CHOOSE_TRACK:
                    controlListener.onChooseTrack(new String(messageEvent.getData()));
                    break;
                case REPEAT_ON:
                    controlListener.onRepeatChanged(true);
                    break;
                case REPEAT_OFF:
                    controlListener.onRepeatChanged(false);
                    break;
                case SHUTDOWN:
                    controlListener.onShutdown();
                    break;
                case SYNC_PATH:
                    updatePlayingState();
                    updateTrack(audioPlayer.getCurrentTrack());
                    updateTrackList(audioPlayer.getCurrentTrackList());
                    break;
            }
        }
    }

    public void setOnWearControlListener(OnWearControlListener listener) {
        this.controlListener = listener;
    }

    public void setOnWearConnectionListener(OnWearConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    public void setPlayStoreResultListener(OnPlayStoreResultListener playStoreResultListener) {
        this.playStoreResultListener = playStoreResultListener;
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isLooping() {
        return looping;
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return status == ConnectionResult.SUCCESS;
    }

    public boolean isAvailable() {
        return available;
    }
}
