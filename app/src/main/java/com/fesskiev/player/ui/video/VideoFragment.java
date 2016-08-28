package com.fesskiev.player.ui.video;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.VideoFile;
import com.fesskiev.player.model.VideoPlayer;
import com.fesskiev.player.services.FileSystemIntentService;
import com.fesskiev.player.ui.video.player.VideoExoPlayerActivity;
import com.fesskiev.player.utils.AppLog;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.RxUtils;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.buttons.VideoCardView;
import com.fesskiev.player.widgets.recycleview.GridDividerDecoration;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class VideoFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static VideoFragment newInstance() {
        return new VideoFragment();
    }

    private VideoFilesAdapter adapter;
    private CardView emptyAudioContent;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Subscription subscription;
    private VideoPlayer videoPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        videoPlayer = MediaApplication.getInstance().getVideoPlayer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2,
                GridLayoutManager.VERTICAL, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.foldersGridView);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(new GridDividerDecoration(getActivity()));
        adapter = new VideoFilesAdapter();
        recyclerView.setAdapter(adapter);

        emptyAudioContent = (CardView) view.findViewById(R.id.emptyAudioContentCard);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.primary_light));
        swipeRefreshLayout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));

    }

    @Override
    public void onResume() {
        super.onResume();
        fetchVideoContent();
    }

    protected void showEmptyContentCard() {
        emptyAudioContent.setVisibility(View.VISIBLE);
    }

    protected void hideEmptyContentCard() {
        emptyAudioContent.setVisibility(View.GONE);
    }

    public void fetchVideoContent() {
        RxUtils.unsubscribe(subscription);
        subscription = MediaApplication.getInstance().getMediaDataSource().getVideoFilesFromDB()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(videoFiles -> {
                    if (videoFiles != null && !videoFiles.isEmpty()) {
                        videoPlayer.videoFiles = videoFiles;
                        adapter.refresh(videoFiles);
                        hideEmptyContentCard();
                    } else {
                        showEmptyContentCard();
                    }
                    AppLog.INFO("onNext:video: " + (videoFiles == null ? "null" : videoFiles.size()));
                    RxUtils.unsubscribe(subscription);
                });
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void startExoPlayerActivity(VideoFile videoFile) {
        Intent intent = new Intent(getContext(),
                VideoExoPlayerActivity.class).setData(Uri.parse(videoFile.getFilePath()));
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle(getString(R.string.dialog_refresh_video_title));
        builder.setMessage(R.string.dialog_refresh_video_message);
        builder.setPositiveButton(R.string.dialog_refresh_video_ok,
                (dialog, which) -> {
                    RxUtils.unsubscribe(subscription);
                    subscription = RxUtils
                            .fromCallable(MediaApplication.getInstance().getMediaDataSource().resetVideoContentDatabase())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(aVoid -> FileSystemIntentService.startFetchVideo(getActivity()));
                });

        builder.setNegativeButton(R.string.dialog_refresh_video_cancel,
                (dialog, which) -> {
                    swipeRefreshLayout.setRefreshing(false);
                    dialog.cancel();
                });
        builder.setOnCancelListener(dialog -> swipeRefreshLayout.setRefreshing(false));
        builder.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        RxUtils.unsubscribe(subscription);
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
                        showPopupMenu(view, getAdapterPosition());
                    }

                    @Override
                    public void onPlayButtonCall() {
                        startVideoPlayer(getAdapterPosition());
                    }
                });

            }
        }

        private void startVideoPlayer(int position) {
            VideoFile videoFile = videoPlayer.videoFiles.get(position);
            if (videoFile != null) {
                if (videoFile.exists()) {
                    videoPlayer.currentVideoFile = videoFile;
                    startExoPlayerActivity(videoFile);
                } else {
                    Utils.showCustomSnackbar(getView(),
                            getContext(), getString(R.string.snackbar_file_not_exist),
                            Snackbar.LENGTH_LONG).show();
                }
            }
        }

        private void showPopupMenu(final View view, final int position) {
            final PopupMenu popupMenu = new PopupMenu(getActivity(), view);
            final Menu menu = popupMenu.getMenu();
            popupMenu.getMenuInflater().inflate(R.menu.menu_popup_item_video, menu);
            popupMenu.setOnMenuItemClickListener(item -> {

                switch (item.getItemId()) {
                    case R.id.delete:
                        deleteVideo(position);
                        break;
                    case R.id.add_playlist:
                        addToPlaylist(position);
                        break;
                }
                return true;
            });
            popupMenu.show();
        }

        private void deleteVideo(final int position) {
            final VideoFile videoFile = videoPlayer.videoFiles.get(position);
            if (videoFile != null) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
                builder.setTitle(getString(R.string.dialog_delete_file_title));
                builder.setMessage(R.string.dialog_delete_file_message);
                builder.setPositiveButton(R.string.dialog_delete_file_ok,
                        (dialog, which) -> {
                            if (videoFile.filePath.delete()) {
                                Utils.showCustomSnackbar(getView(),
                                        getContext(),
                                        getString(R.string.shackbar_delete_file),
                                        Snackbar.LENGTH_LONG).show();

                                MediaApplication.getInstance()
                                        .getMediaDataSource()
                                        .deleteVideoFile(videoFile.getFilePath());

                                adapter.removeItem(position);

                            }
                        });
                builder.setNegativeButton(R.string.dialog_delete_file_cancel,
                        (dialog, which) -> dialog.cancel());
                builder.show();
            }
        }

        private void addToPlaylist(int position) {
            VideoFile videoFile = videoPlayer.videoFiles.get(position);
            if (videoFile != null) {
                videoFile.inPlayList = true;
                MediaApplication.getInstance().getMediaDataSource().updateVideoFile(videoFile);
                Utils.showCustomSnackbar(getView(),
                        getContext().getApplicationContext(),
                        getString(R.string.add_to_playlist_text),
                        Snackbar.LENGTH_SHORT).show();
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
                holder.videoCard.setDescription(videoFile.description);

                if (videoFile.framePath != null) {
                    BitmapHelper.loadURIBitmap(getContext(),
                            videoFile.framePath, holder.videoCard.getFrameView());
                } else {
                    Glide.with(getContext()).
                            load(R.drawable.no_cover_track_icon).into(holder.videoCard.getFrameView());
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

        public void removeItem(int position) {
            videoFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());
        }
    }
}
