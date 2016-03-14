package com.fesskiev.player.ui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.services.FileTreeIntentService;
import com.fesskiev.player.ui.tracklist.TrackListActivity;
import com.fesskiev.player.widgets.recycleview.RecyclerItemTouchClickListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class AudioFoldersFragment extends GridVideoFragment {

    private static final String TAG = AudioFoldersFragment.class.getSimpleName();

    public static AudioFoldersFragment newInstance() {
        return new AudioFoldersFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerAudioFolderBroadcastReceiver();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.addOnItemTouchListener(new RecyclerItemTouchClickListener(getActivity(),
                new RecyclerItemTouchClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View childView, int position) {
                AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();
                AudioFolder audioFolder = audioPlayer.audioFolders.get(position);
                if (audioFolder != null) {
                    audioPlayer.currentAudioFolder = audioFolder;

                    startActivity(new Intent(getActivity(), TrackListActivity.class));
                }
            }

            @Override
            public void onItemLongPress(View childView, int position) {

            }
        }));
    }


    @Override
    public RecyclerView.Adapter getAdapter() {
        return new AudioFoldersAdapter();
    }


    @Override
    public void onRefresh() {
        FileTreeIntentService.startFileTreeService(getActivity());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterAudioFolderBroadcastReceiver();
    }

    private void registerAudioFolderBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FileTreeIntentService.ACTION_AUDIO_FOLDER);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(audioFolderReceiver,
                intentFilter);
    }

    private void unregisterAudioFolderBroadcastReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(audioFolderReceiver);
    }

    private BroadcastReceiver audioFolderReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case FileTreeIntentService.ACTION_AUDIO_FOLDER:
                    List<AudioFolder> receiverAudioFolders =
                            MediaApplication.getInstance().getAudioPlayer().audioFolders;
                    if (receiverAudioFolders != null) {
                        ((AudioFoldersAdapter) adapter).refresh(receiverAudioFolders);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    break;
            }
        }
    };


    private class AudioFoldersAdapter extends RecyclerView.Adapter<AudioFoldersAdapter.ViewHolder> {

        private List<AudioFolder> audioFolders;

        public AudioFoldersAdapter() {
            this.audioFolders = new ArrayList<>();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView albumName;
            ImageView cover;

            public ViewHolder(View v) {
                super(v);

               albumName = (TextView) v.findViewById(R.id.albumName);
               cover = (ImageView) v.findViewById(R.id.folderCover);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_audio_folder, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            AudioFolder audioFolder = audioFolders.get(position);
            if (audioFolder != null) {
                if (audioFolder.folderImages.size() > 0) {
                    File coverFile = audioFolder.folderImages.get(0);
                    if (coverFile != null) {
                        MediaApplication.getInstance().getPicasso()
                                .load(coverFile)
                                .fit()
                                .into(holder.cover);
                    }
                } else {
                    MediaApplication.getInstance().getPicasso()
                            .load(R.drawable.no_cover_icon)
                            .fit()
                            .into(holder.cover);
                }

                holder.albumName.setText(audioFolder.folderName);
            }
        }

        @Override
        public int getItemCount() {
            return audioFolders.size();
        }

        public void refresh(List<AudioFolder> receiverAudioFolders) {
            audioFolders.clear();
            audioFolders.addAll(receiverAudioFolders);
            notifyDataSetChanged();
        }
    }
}
