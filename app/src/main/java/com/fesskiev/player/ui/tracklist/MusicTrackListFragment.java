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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.MusicApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.MusicFile;
import com.fesskiev.player.model.MusicFolder;
import com.fesskiev.player.services.FetchMp3InfoIntentService;
import com.fesskiev.player.ui.MusicFoldersFragment;
import com.fesskiev.player.ui.player.PlayerActivity;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.recycleview.OnItemClickListener;
import com.fesskiev.player.widgets.recycleview.RecycleItemClickListener;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MusicTrackListFragment extends Fragment {

    private static final String TAG = MusicTrackListFragment.class.getSimpleName();
    public static final String MUSIC_FILE_PATH = "music_file_path";
    public static final String COVER_IMAGE_PATH = "cover_image_path";

    private MusicFolder musicFolder;
    private RecyclerView recyclerView;
    private Bitmap coverImageBitmap;
    private List<MusicFile> musicFiles;
    private MusicFilesAdapter musicFilesAdapter;
    private int position;

    public static MusicTrackListFragment newInstance(int position) {
        MusicTrackListFragment fragment = new MusicTrackListFragment();
        Bundle args = new Bundle();
        args.putInt(MusicFoldersFragment.FOLDER_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(MusicFoldersFragment.FOLDER_POSITION);
            musicFolder =
                    ((MusicApplication) getActivity().getApplication()).getMusicFolders().get(position);
            List<File> folderImages = musicFolder.folderImages;
            if (folderImages != null && folderImages.size() > 0) {
                coverImageBitmap = Utils.getResizedBitmap(100, 100,
                        folderImages.get(0).getAbsolutePath());
            }
        }

        musicFiles = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_song_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false, 1000));
        musicFilesAdapter = new MusicFilesAdapter(musicFiles);
        recyclerView.setAdapter(musicFilesAdapter);
        recyclerView.addOnItemTouchListener(new RecycleItemClickListener(getActivity(), new OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {

                File musicFile = musicFolder.musicFiles.get(position);
                if (musicFile != null) {
                    Intent intent = new Intent(getActivity(), PlayerActivity.class);
                    intent.putExtra(COVER_IMAGE_PATH,
                            musicFolder.folderImages.get(0).getAbsolutePath());
                    intent.putExtra(MUSIC_FILE_PATH, musicFile.getAbsolutePath());
                    startActivity(intent);
                }
            }
        }));
        FetchMp3InfoIntentService.startFetchMp3Info(getActivity(), position);
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
        intentFilter.addAction(FetchMp3InfoIntentService.ACTION_MUSIC_FILES);
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
                case FetchMp3InfoIntentService.ACTION_MUSIC_FILES:
                    Log.d(TAG, "receive music files!");

                    List<MusicFile> receiverMusicFiles = ((MusicApplication) getActivity().
                            getApplication()).getMusicFolders().
                            get(position).musicFilesDescription;
                    if(receiverMusicFiles != null){
                        musicFiles.clear();
                        musicFiles.addAll(receiverMusicFiles);
                        musicFilesAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    private class MusicFilesAdapter extends RecyclerView.Adapter<MusicFilesAdapter.ViewHolder> {

        private List<MusicFile> musicFiles;

        public MusicFilesAdapter(List<MusicFile> musicFiles) {
            this.musicFiles = musicFiles;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView artist;
            TextView title;
            ImageView cover;

            public ViewHolder(View v) {
                super(v);

                artist = (TextView) v.findViewById(R.id.itemArtist);
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
                // set default bitmap
            }

            holder.artist.setText(musicFile.artist);
            holder.title.setText(musicFile.title);
        }

        @Override
        public int getItemCount() {
            return musicFiles.size();
        }
    }
}
