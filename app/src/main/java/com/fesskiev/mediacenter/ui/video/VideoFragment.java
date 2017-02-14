package com.fesskiev.mediacenter.ui.video;


import android.app.Activity;
import android.content.Intent;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.players.VideoPlayer;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.ui.video.player.VideoExoPlayerActivity;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.item.VideoCardView;
import com.fesskiev.mediacenter.widgets.menu.ContextMenuManager;
import com.fesskiev.mediacenter.widgets.menu.VideoContextMenu;
import com.fesskiev.mediacenter.widgets.recycleview.GridDividerDecoration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class VideoFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static VideoFragment newInstance() {
        return new VideoFragment();
    }

    private VideoFilesAdapter adapter;
    private CardView emptyVideoContent;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Subscription subscription;
    private DataRepository repository;
    private VideoPlayer videoPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        videoPlayer = MediaApplication.getInstance().getVideoPlayer();
        repository = MediaApplication.getInstance().getRepository();
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
        adapter = new VideoFilesAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        emptyVideoContent = (CardView) view.findViewById(R.id.emptyVideoContentCard);
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

    public void refreshVideoContent() {
        swipeRefreshLayout.setRefreshing(false);

        repository.getMemorySource().setCacheVideoFilesDirty(true);

        fetchVideoContent();
    }

    public void clearVideoContent() {
        adapter.clearAdapter();
    }

    public void fetchVideoContent() {
        RxUtils.unsubscribe(subscription);
        subscription = repository.getVideoFiles()
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(videoFiles -> {
                    if (videoFiles != null) {
                        if (!videoFiles.isEmpty()) {
                            hideEmptyContentCard();
                        } else {
                            showEmptyContentCard();
                        }
                        videoPlayer.setVideoFiles(videoFiles);
                        adapter.refresh(videoFiles);
                    } else {
                        showEmptyContentCard();
                    }
                    AppLog.INFO("onNext:video: " + (videoFiles == null ? "null" : videoFiles.size()));
                });
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    protected void showEmptyContentCard() {
        emptyVideoContent.setVisibility(View.VISIBLE);
    }

    protected void hideEmptyContentCard() {
        emptyVideoContent.setVisibility(View.GONE);
    }

    @Override
    public void onRefresh() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle(getString(R.string.dialog_refresh_video_title));
        builder.setMessage(R.string.dialog_refresh_video_message);
        builder.setPositiveButton(R.string.dialog_refresh_video_ok,
                (dialog, which) -> {
                    swipeRefreshLayout.setRefreshing(false);
                    RxUtils.unsubscribe(subscription);
                    subscription = RxUtils.fromCallable(repository.resetVideoContentDatabase())
                            .subscribeOn(Schedulers.io())
                            .doOnNext(integer -> CacheManager.clearVideoImagesCache())
                            .subscribeOn(Schedulers.io())
                            .subscribe(aVoid -> FileSystemService.startFetchVideo(getActivity()));
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

    private static class VideoFilesAdapter extends RecyclerView.Adapter<VideoFilesAdapter.ViewHolder> {

        private WeakReference<Activity> activity;
        private List<VideoFile> videoFiles;

        public VideoFilesAdapter(Activity activity) {
            this.activity = new WeakReference<>(activity);
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
                        showVideoContextMenu(view, getAdapterPosition());
                    }


                    @Override
                    public void onPlayButtonCall() {
                        startVideoPlayer(getAdapterPosition());
                    }
                });

            }
        }

        private void startVideoPlayer(int position) {
            Activity act = activity.get();
            if (act != null) {
                VideoFile videoFile = videoFiles.get(position);
                if (videoFile != null) {
                    if (videoFile.exists()) {
                        MediaApplication.getInstance().getVideoPlayer().setCurrentVideoFile(videoFile);
                        startExoPlayerActivity(act, videoFile);
                    } else {
                        Utils.showCustomSnackbar(act.getCurrentFocus(), act,
                                act.getString(R.string.snackbar_file_not_exist),
                                Snackbar.LENGTH_LONG)
                                .show();
                    }
                }
            }
        }

        private void startExoPlayerActivity(Activity act, VideoFile videoFile) {
            Intent intent = new Intent(act, VideoExoPlayerActivity.class);
            intent.putExtra(VideoExoPlayerActivity.URI_EXTRA, videoFile.getFilePath());
            intent.putExtra(VideoExoPlayerActivity.VIDEO_NAME_EXTRA, videoFile.getFileName());
            intent.setAction(VideoExoPlayerActivity.ACTION_VIEW_URI);
            act.startActivity(intent);
        }

        private void showVideoContextMenu(View view, int position) {
            ContextMenuManager.getInstance().toggleVideoContextMenu(view,
                    new VideoContextMenu.OnVideoContextMenuListener() {
                        @Override
                        public void onAddVideoToPlayList() {
                            addToPlaylist(position);
                        }

                        @Override
                        public void onDeleteVideo() {
                            deleteVideo(position);
                        }
                    });
        }

        private void deleteVideo(final int position) {
            Activity act = activity.get();
            if (act != null) {
                final VideoFile videoFile = videoFiles.get(position);
                if (videoFile != null) {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(act, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle(act.getString(R.string.dialog_delete_file_title));
                    builder.setMessage(R.string.dialog_delete_file_message);
                    builder.setPositiveButton(R.string.dialog_delete_file_ok,
                            (dialog, which) -> {
                                Observable.just(videoFile.filePath.delete())
                                        .first()
                                        .subscribeOn(Schedulers.io())
                                        .flatMap(result -> {
                                            if (result) {
                                                DataRepository repository = MediaApplication.getInstance().getRepository();
                                                repository.getMemorySource().setCacheVideoFilesDirty(true);
                                                return RxUtils.fromCallable(repository.deleteVideoFile(videoFile.getFilePath()));
                                            }
                                            return Observable.empty();
                                        })
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(integer -> {
                                            Utils.showCustomSnackbar(act.getCurrentFocus(),
                                                    act, act.getString(R.string.shackbar_delete_file),
                                                    Snackbar.LENGTH_LONG)
                                                    .show();
                                            removeItem(position);
                                        });
                            });
                    builder.setNegativeButton(R.string.dialog_delete_file_cancel,
                            (dialog, which) -> dialog.cancel());
                    builder.show();
                }
            }
        }

        private void addToPlaylist(int position) {
            Activity act = activity.get();
            if (act != null) {
                VideoFile videoFile = videoFiles.get(position);
                if (videoFile != null) {
                    videoFile.inPlayList = true;
                    MediaApplication.getInstance().getRepository().updateVideoFile(videoFile);
                    Utils.showCustomSnackbar(act.getCurrentFocus(), act,
                            act.getString(R.string.add_to_playlist_video_text), Snackbar.LENGTH_SHORT)
                            .show();
                }
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
            Activity act = activity.get();
            if (act != null) {
                final VideoFile videoFile = videoFiles.get(position);
                if (videoFile != null) {
                    holder.videoCard.setDescription(videoFile.description);

                    ImageView frameView = holder.videoCard.getFrameView();
                    String framePath = videoFile.framePath;
                    if (framePath != null) {
                        BitmapHelper.getInstance().loadVideoFileCover(framePath, frameView);
                    } else {
                        frameView.setImageResource(0);
                        frameView.setBackgroundColor(ContextCompat.getColor(act, R.color.search_background));
                    }
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

        public void clearAdapter() {
            videoFiles.clear();
            notifyDataSetChanged();
        }

    }
}
