package com.fesskiev.player.ui.video;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.db.DatabaseHelper;
import com.fesskiev.player.db.MediaCenterProvider;
import com.fesskiev.player.model.VideoFile;
import com.fesskiev.player.model.VideoPlayer;
import com.fesskiev.player.services.FileSystemIntentService;
import com.fesskiev.player.ui.video.player.exo.VideoExoPlayerActivity;
import com.fesskiev.player.ui.video.utils.Constants;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.widgets.buttons.VideoCardView;
import com.fesskiev.player.widgets.recycleview.GridDividerDecoration;
import com.fesskiev.player.widgets.recycleview.RecyclerItemTouchClickListener;

import java.util.ArrayList;
import java.util.List;


public class VideoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = VideoFragment.class.getSimpleName();

    public static VideoFragment newInstance() {
        return new VideoFragment();
    }

    private VideoFilesAdapter adapter;
    private CardView emptyAudioContent;
    private SwipeRefreshLayout swipeRefreshLayout;
    private VideoPlayer videoPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoPlayer = MediaApplication.getInstance().getVideoPlayer();
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3,
                GridLayoutManager.VERTICAL, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.foldersGridView);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(new GridDividerDecoration(getActivity()));
        adapter = new VideoFilesAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemTouchClickListener(getActivity(),
                new RecyclerItemTouchClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View childView, int position) {
                        VideoFile videoFile = videoPlayer.videoFiles.get(position);
                        if (videoFile != null) {
                            videoPlayer.currentVideoFile = videoFile;
                            startExoPlayerActivity(videoFile);
                        }
                    }

                    @Override
                    public void onItemLongPress(View childView, int position) {

                    }
                }));
        emptyAudioContent = (CardView) view.findViewById(R.id.emptyAudioContentCard);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        fetchVideoContent();
    }

    protected void showEmptyContentCard() {
        emptyAudioContent.setVisibility(View.VISIBLE);
    }

    protected void hideEmptyContentCard() {
        emptyAudioContent.setVisibility(View.GONE);
    }

    public void fetchVideoContent() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        getActivity().
                getSupportLoaderManager().restartLoader(Constants.GET_VIDEO_FILES_LOADER, null, this);
    }

    private void startExoPlayerActivity(VideoFile videoFile) {
        Intent intent = new Intent(getContext(),
                VideoExoPlayerActivity.class).setData(Uri.parse(videoFile.filePath));
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle(getString(R.string.dialog_refresh_video_title));
        builder.setMessage(R.string.dialog_refresh_video_message);
        builder.setPositiveButton(R.string.dialog_refresh_video_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DatabaseHelper.resetVideoContentDatabase(getActivity());
                        FileSystemIntentService.startFetchVideo(getActivity());
                    }
                });
        builder.setNegativeButton(R.string.dialog_refresh_video_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        swipeRefreshLayout.setRefreshing(false);
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case Constants.GET_VIDEO_FILES_LOADER:
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
                adapter.refresh(videoFiles);
                hideEmptyContentCard();
            }
        } else {
            showEmptyContentCard();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    private class VideoFilesAdapter extends RecyclerView.Adapter<VideoFilesAdapter.ViewHolder> {

        List<VideoFile> videoFiles;

        public VideoFilesAdapter() {
            this.videoFiles = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            VideoCardView videoCard;

            public ViewHolder(View v) {
                super(v);

                videoCard = (VideoCardView) v.findViewById(R.id.videoCardView);
                videoCard.setOnVideoCardViewListener(new VideoCardView.OnVideoCardViewListener() {
                    @Override
                    public void onPopupMenuButtonCall(View view) {
                        showPopupMenu(view);
                    }
                });

            }
        }

        private void showPopupMenu(final View view) {
            final PopupMenu popupMenu = new PopupMenu(getActivity(), view);
            final Menu menu = popupMenu.getMenu();
            popupMenu.getMenuInflater().inflate(R.menu.menu_popup_item_video, menu);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.delete:
                            deleteVideo();
                            break;
                        case R.id.add_playlist:
                            addToPlaylist();
                            break;
                    }
                    return true;
                }
            });
            popupMenu.show();
        }

        private void deleteVideo() {

        }

        private void addToPlaylist() {

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
                holder.videoCard.setDescription(videoFile.description);

                if (videoFile.framePath != null) {
                    BitmapHelper.loadURLBitmap(getContext(),
                            videoFile.framePath, holder.videoCard.getFrameView());
                } else {
                    Glide.with(getContext()).
                            load(R.drawable.no_cover_icon).into(holder.videoCard.getFrameView());
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
