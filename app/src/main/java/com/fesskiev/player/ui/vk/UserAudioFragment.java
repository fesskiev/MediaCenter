package com.fesskiev.player.ui.vk;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.fesskiev.player.R;
import com.fesskiev.player.model.vk.VKMusicFile;
import com.fesskiev.player.services.RESTService;
import com.fesskiev.player.utils.AppSettingsManager;
import com.fesskiev.player.utils.http.URLHelper;

import java.util.List;


public class UserAudioFragment extends AudioFragment {


    public static UserAudioFragment newInstance() {
        return new UserAudioFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(RESTService.ACTION_USER_AUDIO_RESULT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(audioReceiver,
                filter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(audioReceiver);
    }


    private BroadcastReceiver audioReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case RESTService.ACTION_USER_AUDIO_RESULT:
                    List<VKMusicFile> vkMusicFiles =
                            intent.getParcelableArrayListExtra(RESTService.EXTRA_AUDIO_RESULT);
                    if (vkMusicFiles != null) {
                        hideProgressBar();
                        audioAdapter.refresh(getDownloadVKMusicFiles(vkMusicFiles));
                    }
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBroadcastReceiver();
    }

    @Override
    public int getResourceId() {
        return R.layout.fragment_user_audio;
    }

    @Override
    public void fetchAudio(int offset) {
        showProgressBar();
        AppSettingsManager manager = AppSettingsManager.getInstance(getActivity());
        RESTService.fetchUserAudio(getActivity(),
                URLHelper.getUserAudioURL(manager.getAuthToken(), manager.getUserId(), 20, offset));
    }

}
