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
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fesskiev.player.MusicApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.VideoFile;
import com.fesskiev.player.services.FileTreeIntentService;

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
                            MusicApplication.getInstance().getVideoPlayer().videoFiles;
                    if (videoFiles != null) {
                        ((VideoFilesAdapter) adapter).refresh(videoFiles);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    break;
            }
        }
    };

    private static class ViewHolder {
        public TextView filePath;
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_video_file, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.filePath = (TextView) convertView.findViewById(R.id.videoFilePath);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            VideoFile videoFile = videoFiles.get(position);
            if (videoFile != null) {
                viewHolder.filePath.setText(videoFile.filePath);
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
