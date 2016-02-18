package com.fesskiev.player.ui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.services.FileTreeIntentService;
import com.fesskiev.player.ui.tracklist.TrackListActivity;
import com.squareup.picasso.Picasso;

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

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();
                AudioFolder audioFolder = audioPlayer.audioFolders.get(position);
                if (audioFolder != null) {
                    audioPlayer.currentAudioFolder = audioFolder;

                    startActivity(new Intent(getActivity(), TrackListActivity.class));
                }
            }
        });
    }


    @Override
    public BaseAdapter getAdapter() {
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


    private static class ViewHolder {
        public TextView albumName;
        public ImageView cover;
    }

    private class AudioFoldersAdapter extends BaseAdapter {

        private List<AudioFolder> audioFolders;

        public AudioFoldersAdapter() {
            this.audioFolders = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return audioFolders.size();
        }

        @Override
        public Object getItem(int position) {
            return audioFolders.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_audio_folder, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.albumName = (TextView) convertView.findViewById(R.id.albumName);
                viewHolder.cover = (ImageView) convertView.findViewById(R.id.folderCover);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            AudioFolder audioFolder = audioFolders.get(position);
            if (audioFolder != null) {
                if (audioFolder.folderImages.size() > 0) {
                    File coverFile = audioFolder.folderImages.get(0);
                    if (coverFile != null) {
                        Picasso.with(getActivity()).
                                load(coverFile).
                                resize(256, 256).
                                into(viewHolder.cover);
                    }
                } else {
                    Picasso.with(getActivity()).
                            load(R.drawable.no_cover_icon).
                            into(viewHolder.cover);
                }

                viewHolder.albumName.setText(audioFolder.folderName);
            }

            return convertView;
        }


        public void refresh(List<AudioFolder> receiverAudioFolders) {
            audioFolders.clear();
            audioFolders.addAll(receiverAudioFolders);
            notifyDataSetChanged();
        }
    }
}
