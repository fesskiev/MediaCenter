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
import com.fesskiev.player.model.VideoFile;
import com.fesskiev.player.model.VideoPlayer;
import com.fesskiev.player.services.FileTreeIntentService;
import com.fesskiev.player.ui.player.VideoPlayerActivity;

import java.util.ArrayList;
import java.util.List;


public class VideoFilesFragment extends GridVideoFragment {

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

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VideoPlayer videoPlayer = MediaApplication.getInstance().getVideoPlayer();
                VideoFile videoFile = videoPlayer.videoFiles.get(position);
                if (videoFile != null) {
                    videoPlayer.currentVideoFile = videoFile;
                    startActivity(new Intent(getActivity(), VideoPlayerActivity.class));
                }
            }
        });

    }

    @Override
    public BaseAdapter getAdapter() {
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

    private static class ViewHolder {
        public TextView description;
        public ImageView frame;
    }

    private class VideoFilesAdapter extends BaseAdapter {

        List<VideoFile> videoFiles;

        public VideoFilesAdapter() {
            this.videoFiles = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return videoFiles.size();
        }

        @Override
        public Object getItem(int position) {
            return videoFiles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_video_file, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.description = (TextView) convertView.findViewById(R.id.fileDescription);
                viewHolder.frame = (ImageView) convertView.findViewById(R.id.frameView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final VideoFile videoFile = videoFiles.get(position);
            if (videoFile != null) {
                viewHolder.frame.setImageBitmap(videoFile.frame);
                viewHolder.description.setText(videoFile.description);
            }

            return convertView;
        }

        public void refresh(List<VideoFile> receiveVideoFiles) {
            videoFiles.clear();
            videoFiles.addAll(receiveVideoFiles);
            notifyDataSetChanged();
        }
    }
}
