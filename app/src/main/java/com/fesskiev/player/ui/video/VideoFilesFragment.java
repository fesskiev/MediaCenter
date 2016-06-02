package com.fesskiev.player.ui.video;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.db.MediaCenterProvider;
import com.fesskiev.player.model.VideoFile;
import com.fesskiev.player.model.VideoPlayer;
import com.fesskiev.player.ui.GridFragment;
import com.fesskiev.player.ui.player.VideoExoPlayerActivity;
import com.fesskiev.player.ui.player.VideoPlayerActivity;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.widgets.recycleview.RecyclerItemTouchClickListener;

import java.util.ArrayList;
import java.util.List;


public class VideoFilesFragment extends GridFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = VideoFilesFragment.class.getSimpleName();

    public static VideoFilesFragment newInstance() {
        return new VideoFilesFragment();
    }

    public static final int GET_VIDEO_FILES_LOADER = 3001;

    private VideoPlayer videoPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoPlayer = MediaApplication.getInstance().getVideoPlayer();
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.addOnItemTouchListener(new RecyclerItemTouchClickListener(getActivity(),
                new RecyclerItemTouchClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View childView, int position) {
                        VideoFile videoFile = videoPlayer.videoFiles.get(position);
                        if (videoFile != null) {
                            videoPlayer.currentVideoFile = videoFile;
                            startActivity(new Intent(getActivity(), VideoExoPlayerActivity.class));
                        }
                    }

                    @Override
                    public void onItemLongPress(View childView, int position) {

                    }
                }));

        getActivity().getSupportLoaderManager().restartLoader(GET_VIDEO_FILES_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case GET_VIDEO_FILES_LOADER:
                return new CursorLoader(
                        getActivity(),
                        MediaCenterProvider.VIDEO_FILES_TABLE_CONTENT_URI,
                        null,
                        null,
                        null,
                        null

                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "cursor video files: " + cursor.getCount());
        if (cursor.getCount() > 0) {
            List<VideoFile> videoFiles = new ArrayList<>();
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                videoFiles.add(new VideoFile(cursor));
            }
            if (!videoFiles.isEmpty()) {
                videoPlayer.videoFiles = videoFiles;
                ((VideoFilesAdapter) adapter).refresh(videoFiles);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public RecyclerView.Adapter createAdapter() {
        return new VideoFilesAdapter();
    }


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
                holder.description.setText(videoFile.description);

                if (videoFile.framePath != null) {
                    BitmapHelper.loadURLBitmap(getContext(), videoFile.framePath, holder.frame);
                } else {
                    Glide.with(getContext()).load(R.drawable.no_cover_icon).into(holder.frame);
                }
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
