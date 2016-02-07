package com.fesskiev.player.ui.tracklist;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.MusicApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.MusicFile;
import com.fesskiev.player.model.MusicFolder;
import com.fesskiev.player.model.MusicPlayer;
import com.fesskiev.player.services.FetchAudioInfoIntentService;
import com.fesskiev.player.ui.player.PlayerActivity;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.recycleview.OnItemClickListener;
import com.fesskiev.player.widgets.recycleview.RecycleItemClickListener;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TrackListFragment extends Fragment {

    private static final String TAG = TrackListFragment.class.getSimpleName();

    private Bitmap coverImageBitmap;
    private MusicFilesAdapter musicFilesAdapter;
    private MusicFolder musicFolder;

    public static TrackListFragment newInstance() {
        return new TrackListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        musicFolder =
                MusicApplication.getInstance().getMusicPlayer().currentMusicFolder;
        List<File> folderImages = musicFolder.folderImages;
        if (folderImages != null && folderImages.size() > 0) {

            coverImageBitmap = Utils.getResizedBitmap(100, 100,
                    folderImages.get(0).getAbsolutePath());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_song_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false, 1000));
        musicFilesAdapter = new MusicFilesAdapter();
        recyclerView.setAdapter(musicFilesAdapter);
        recyclerView.addOnItemTouchListener(new RecycleItemClickListener(getActivity(), new OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                MusicFile musicFile = musicFolder.musicFilesDescription.get(position);
                if (musicFile != null) {
                    MusicPlayer musicPlayer = MusicApplication.getInstance().getMusicPlayer();
                    musicPlayer.currentMusicFile = musicFile;
                    musicPlayer.position = position;

                    startActivity(new Intent(getActivity(), PlayerActivity.class));
                }
            }
        }));


        if (musicFolder.musicFilesDescription.size() == 0) {
            FetchAudioInfoIntentService.startFetchAudioInfo(getActivity());
        } else {
            List<MusicFile> receiverMusicFiles = MusicApplication.getInstance().
                    getMusicPlayer().currentMusicFolder.musicFilesDescription;
            if (receiverMusicFiles != null) {
                musicFilesAdapter.refreshAdapter(receiverMusicFiles);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerMusicFilesReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterMusicFilesReceiver();
    }

    private void registerMusicFilesReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FetchAudioInfoIntentService.ACTION_MUSIC_FILES_RESULT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(musicFilesReceiver,
                intentFilter);
    }

    private void unregisterMusicFilesReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(musicFilesReceiver);
    }

    private BroadcastReceiver musicFilesReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case FetchAudioInfoIntentService.ACTION_MUSIC_FILES_RESULT:
//                    Log.d(TAG, "receive music files!");

                    List<MusicFile> receiverMusicFiles = MusicApplication.getInstance().
                            getMusicPlayer().currentMusicFolder.musicFilesDescription;
                    if (receiverMusicFiles != null) {
                        musicFilesAdapter.refreshAdapter(receiverMusicFiles);
                    }
                    break;
            }
        }
    };

    private class MusicFilesAdapter extends RecyclerView.Adapter<MusicFilesAdapter.ViewHolder> {

        private List<MusicFile> musicFiles;

        public MusicFilesAdapter() {
            this.musicFiles = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView duration;
            TextView title;
            ImageView cover;

            public ViewHolder(View v) {
                super(v);

                duration = (TextView) v.findViewById(R.id.itemDuration);
                title = (TextView) v.findViewById(R.id.itemTitle);
                cover = (ImageView) v.findViewById(R.id.itemCover);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_track, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            MusicFile musicFile = musicFiles.get(position);

            if (coverImageBitmap != null) {
                holder.cover.setImageBitmap(coverImageBitmap);
            } else {
                Bitmap artwork = musicFile.getArtwork();
                if (artwork != null) {
                    holder.cover.setImageBitmap(artwork);
                } else {
                    Picasso.with(getActivity()).
                            load(R.drawable.no_cover_icon).
                            into(holder.cover);
                }
            }

            holder.duration.setText(Utils.getDurationString(musicFile.length));
            holder.title.setText(musicFile.title);
        }

        @Override
        public int getItemCount() {
            return musicFiles.size();
        }

        public void refreshAdapter(List<MusicFile> receiverMusicFiles) {
            musicFiles.clear();
            musicFiles.addAll(receiverMusicFiles);
//            Collections.sort(musicFiles);
            notifyDataSetChanged();
        }
    }
}
