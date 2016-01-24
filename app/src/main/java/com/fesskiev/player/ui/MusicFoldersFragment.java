package com.fesskiev.player.ui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.MusicApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.MusicFolder;
import com.fesskiev.player.model.MusicPlayer;
import com.fesskiev.player.services.FileTreeIntentService;
import com.fesskiev.player.ui.tracklist.TrackListActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MusicFoldersFragment extends Fragment {

    private static final String TAG = MusicFoldersFragment.class.getSimpleName();

    public static MusicFoldersFragment newInstance() {
        return new MusicFoldersFragment();
    }

    private GridViewAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerLocationBroadcastReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music_folders, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GridView gridView = (GridView) view.findViewById(R.id.foldersGridView);
        adapter = new GridViewAdapter();
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MusicPlayer musicPlayer = MusicApplication.getInstance().getMusicPlayer();
                MusicFolder musicFolder = musicPlayer.musicFolders.get(position);
                if (musicFolder != null) {
                    musicPlayer.currentMusicFolder = musicFolder;

                    startActivity(new Intent(getActivity(), TrackListActivity.class));
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterLocationBroadcastReceiver();
    }

    private void registerLocationBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FileTreeIntentService.ACTION_MUSIC_FOLDER);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(musicFolderReceiver,
                intentFilter);
    }

    private void unregisterLocationBroadcastReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(musicFolderReceiver);
    }

    private BroadcastReceiver musicFolderReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case FileTreeIntentService.ACTION_MUSIC_FOLDER:
                    List<MusicFolder> receiverMusicFolders =
                            MusicApplication.getInstance().getMusicPlayer().musicFolders;
                    if (receiverMusicFolders != null) {
                        adapter.refresh(receiverMusicFolders);
                    }
                    break;
            }
        }
    };


    private static class ViewHolder {
        public TextView albumName;
        public ImageView cover;
    }

    private class GridViewAdapter extends BaseAdapter {

        private List<MusicFolder> musicFolders;

        public GridViewAdapter() {
            this.musicFolders = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return musicFolders.size();
        }

        @Override
        public Object getItem(int position) {
            return musicFolders.get(position);
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
                convertView = inflater.inflate(R.layout.item_folder, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.albumName = (TextView) convertView.findViewById(R.id.albumName);
                viewHolder.cover = (ImageView) convertView.findViewById(R.id.folderCover);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (musicFolders.get(position).folderImages.size() > 0) {
                File coverFile = musicFolders.get(position).folderImages.get(0);
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

            viewHolder.albumName.setText(musicFolders.get(position).folderName);

            return convertView;
        }


        public void refresh(List<MusicFolder> receiverMusicFolders) {
            musicFolders.clear();
            musicFolders.addAll(receiverMusicFolders);
            notifyDataSetChanged();
        }
    }
}
