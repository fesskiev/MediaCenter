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
import com.fesskiev.player.model.VideoFile;
import com.fesskiev.player.model.VideoPlayer;
import com.fesskiev.player.services.FileTreeIntentService;
import com.fesskiev.player.ui.player.VideoPlayerActivity;
import com.fesskiev.player.widgets.recycleview.RecyclerItemTouchClickListener;

import java.util.ArrayList;
import java.util.List;


public class VideoFilesFragment extends GridFragment {

    public static VideoFilesFragment newInstance() {
        return new VideoFilesFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerVideoFolderBroadcastReceiver();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.addOnItemTouchListener(new RecyclerItemTouchClickListener(getActivity(),
                new RecyclerItemTouchClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View childView, int position) {
                VideoPlayer videoPlayer = MediaApplication.getInstance().getVideoPlayer();
                VideoFile videoFile = videoPlayer.videoFiles.get(position);
                if (videoFile != null) {
                    videoPlayer.currentVideoFile = videoFile;
                    startActivity(new Intent(getActivity(), VideoPlayerActivity.class));
                }
            }

            @Override
            public void onItemLongPress(View childView, int position) {

            }
        }));

    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return new VideoFilesAdapter();
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterVideoFolderBroadcastReceiver();
    }

    private void registerVideoFolderBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FileTreeIntentService.ACTION_VIDEO_FILE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(videoFilesReceiver,
                intentFilter);
    }

    private void unregisterVideoFolderBroadcastReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(videoFilesReceiver);
    }

    private BroadcastReceiver videoFilesReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case FileTreeIntentService.ACTION_VIDEO_FILE:
                    List<VideoFile> videoFiles =
                            MediaApplication.getInstance().getVideoPlayer().videoFiles;
                    if (videoFiles != null) {
                        ((VideoFilesAdapter) adapter).refresh(videoFiles);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    break;
            }
        }
    };


    private class VideoFilesAdapter extends RecyclerView.Adapter<VideoFilesAdapter.ViewHolder> {

        List<VideoFile> videoFiles;

        public VideoFilesAdapter() {
            this.videoFiles = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView description;
            ImageView frame;

            public ViewHolder(View v) {
                super(v);

                description = (TextView) v.findViewById(R.id.fileDescription);
                frame = (ImageView) v.findViewById(R.id.frameView);
            }
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_video_file, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final VideoFile videoFile = videoFiles.get(position);
            if (videoFile != null) {
                holder.frame.setImageBitmap(videoFile.frame);
                holder.description.setText(videoFile.description);
            }
        }

        @Override
        public int getItemCount() {
            return videoFiles.size();
        }

        public void refresh(List<VideoFile> receiveVideoFiles) {
            videoFiles.clear();
            videoFiles.addAll(receiveVideoFiles);
            notifyDataSetChanged();
        }
    }
}
