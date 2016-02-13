package com.fesskiev.player.ui.vk;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.fesskiev.player.R;
import com.fesskiev.player.model.vk.Group;
import com.fesskiev.player.model.vk.VKMusicFile;
import com.fesskiev.player.services.RESTService;
import com.fesskiev.player.utils.AppSettingsManager;
import com.fesskiev.player.utils.http.URLHelper;

import java.util.List;


public class GroupAudioFragment extends RecyclerAudioFragment {

    private static final String GROUP_BUNDLE = "com.fesskiev.player.GROUP_BUNDLE";

    private Group group;
    private AppSettingsManager appSettingsManager;

    public static GroupAudioFragment newInstance(Group group) {
        GroupAudioFragment groupAudioFragment = new GroupAudioFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(GROUP_BUNDLE, group);
        groupAudioFragment.setArguments(bundle);
        return groupAudioFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appSettingsManager = AppSettingsManager.getInstance(getActivity());

        if (getArguments() != null) {
            group = getArguments().getParcelable(GROUP_BUNDLE);
        }
        registerBroadcastReceiver();
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchAudio(audioOffset);
        showProgressBar();
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(RESTService.ACTION_GROUP_AUDIO_RESULT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(groupAudioReceiver,
                filter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(groupAudioReceiver);
    }

    private BroadcastReceiver groupAudioReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case RESTService.ACTION_GROUP_AUDIO_RESULT:
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
    public int getResourceId() {
        return R.layout.fragment_group_audio;
    }

    @Override
    public void fetchAudio(int offset) {
        RESTService.fetchGroupAudio(getActivity(),
                URLHelper.getGroupAudioURL(appSettingsManager.getAuthToken(),
                        group.gid, 20, offset));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBroadcastReceiver();
    }
}
