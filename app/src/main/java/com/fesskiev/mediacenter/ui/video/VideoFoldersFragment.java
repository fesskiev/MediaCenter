package com.fesskiev.mediacenter.ui.video;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.RelativeLayout;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.VideoFolder;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.admob.AdMobHelper;
import com.fesskiev.mediacenter.widgets.dialogs.MediaFolderDetailsDialog;
import com.fesskiev.mediacenter.widgets.dialogs.VideoFolderDetailsDialog;
import com.fesskiev.mediacenter.widgets.item.VideoFolderCardView;
import com.fesskiev.mediacenter.widgets.menu.FolderContextMenu;
import com.fesskiev.mediacenter.widgets.menu.ContextMenuManager;
import com.fesskiev.mediacenter.widgets.recycleview.GridDividerDecoration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class VideoFoldersFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static VideoFoldersFragment newInstance() {
        return new VideoFoldersFragment();
    }

    public static final String EXTRA_VIDEO_FOLDER = "com.fesskiev.player.extra.EXTRA_VIDEO_FOLDER";

    private VideoFoldersAdapter adapter;
    private CardView emptyVideoContent;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Subscription subscription;
    private DataRepository repository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repository = MediaApplication.getInstance().getRepository();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_folders, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2,
                GridLayoutManager.VERTICAL, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.foldersGridView);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(new GridDividerDecoration(getActivity()));
        adapter = new VideoFoldersAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        emptyVideoContent = (CardView) view.findViewById(R.id.emptyVideoContentCard);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.primary_light));
        swipeRefreshLayout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));

        if (!AppSettingsManager.getInstance().isUserPro()) {
            AdMobHelper.getInstance().createAdView((RelativeLayout) view.findViewById(R.id.adViewContainer), AdMobHelper.KEY_VIDEO_BANNER);
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        fetchVideoFolders();
    }

    private void fetchVideoFolders() {
        RxUtils.unsubscribe(subscription);
        subscription = repository.getVideoFolders()
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(Observable::from)
                .filter(folder -> {
                    if (AppSettingsManager.getInstance().isShowHiddenFiles()) {
                        return true;
                    }
                    return !folder.isHidden;
                })
                .toList()
                .subscribe(videoFolders -> {
                    if (videoFolders != null) {
                        if (!videoFolders.isEmpty()) {
                            hideEmptyContentCard();
                        } else {
                            showEmptyContentCard();
                        }
                        adapter.refresh(videoFolders);
                    } else {
                        showEmptyContentCard();
                    }
                    AppLog.INFO("onNext:video folders: " + (videoFolders == null ? "null" : videoFolders.size()));
                });
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
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

    public void refreshVideoContent() {
        swipeRefreshLayout.setRefreshing(false);

        repository.getMemorySource().setCacheVideoFoldersDirty(true);

        fetchVideoFolders();
    }

    public void clearVideoContent() {
        adapter.clearAdapter();
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    private static class VideoFoldersAdapter extends RecyclerView.Adapter<VideoFoldersAdapter.ViewHolder> {

        private WeakReference<Activity> activity;
        private List<VideoFolder> videoFolders;


        public VideoFoldersAdapter(Activity activity) {
            this.activity = new WeakReference<>(activity);
            this.videoFolders = new ArrayList<>();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {

            VideoFolderCardView folderCard;

            public ViewHolder(View v) {
                super(v);

                folderCard = (VideoFolderCardView) v.findViewById(R.id.videoFolderCardView);
                folderCard.setOnVideoFolderCardViewListener(new VideoFolderCardView.OnVideoFolderCardViewListener() {
                    @Override
                    public void onPopupMenuButtonCall(View view) {
                        showAudioContextMenu(view, getAdapterPosition());
                    }

                    @Override
                    public void onOpenVideoListCall(View view) {
                        startVideoFilesActivity(getAdapterPosition());
                    }
                });
            }
        }

        private void showAudioContextMenu(View view, int position) {
            ContextMenuManager.getInstance().toggleAudioContextMenu(view,
                    new FolderContextMenu.OnFolderContextMenuListener() {
                        @Override
                        public void onDeleteFolder() {
                            deleteVideoFolder(position);
                        }


                        @Override
                        public void onDetailsFolder() {
                            showDetailsVideoFolder(position);
                        }
                    });
        }

        private void deleteVideoFolder(int position) {
            Activity act = activity.get();
            if (act != null) {
                VideoFolder videoFolder = videoFolders.get(position);
                if (videoFolder != null) {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(act, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle(act.getString(R.string.dialog_delete_file_title));
                    builder.setMessage(R.string.dialog_delete_folder_message);
                    builder.setPositiveButton(R.string.dialog_delete_file_ok,
                            (dialog, which) -> Observable.just(CacheManager.deleteDirectory(videoFolder.folderPath))
                                    .first()
                                    .subscribeOn(Schedulers.io())
                                    .flatMap(result -> {
                                        if (result) {
                                            DataRepository repository = MediaApplication.getInstance().getRepository();
                                            repository.getMemorySource().setCacheVideoFoldersDirty(true);
                                            return RxUtils.fromCallable(repository.deleteVideoFolder(videoFolder));
                                        }
                                        return Observable.empty();
                                    })
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(integer -> {
                                        removeFolder(position);
                                        Utils.showCustomSnackbar(act.getCurrentFocus(),
                                                act,
                                                act.getString(R.string.shackbar_delete_folder),
                                                Snackbar.LENGTH_LONG)
                                                .show();

                                    }));
                    builder.setNegativeButton(R.string.dialog_delete_file_cancel,
                            (dialog, which) -> dialog.cancel());
                    builder.show();
                }
            }
        }

        private void showDetailsVideoFolder(int position) {
            Activity act = activity.get();
            if (act != null) {
                VideoFolder videoFolder = videoFolders.get(position);
                if (videoFolder != null) {
                    FragmentTransaction transaction =
                            ((FragmentActivity) act).getSupportFragmentManager().beginTransaction();
                    transaction.addToBackStack(null);
                    MediaFolderDetailsDialog dialog = VideoFolderDetailsDialog.newInstance(videoFolder);
                    dialog.setOnMediaFolderDetailsDialogListener(() -> refreshVideoContent(act));
                    dialog.show(transaction, VideoFolderDetailsDialog.class.getName());
                }
            }
        }

        private void refreshVideoContent(Activity act) {
            VideoFoldersFragment videoFoldersFragment = (VideoFoldersFragment) ((FragmentActivity) act)
                    .getSupportFragmentManager().findFragmentByTag(VideoFoldersFragment.class.getName());
            if (videoFoldersFragment != null) {
                videoFoldersFragment.refreshVideoContent();
            }
        }


        private void startVideoFilesActivity(int position) {
            final VideoFolder videoFolder = videoFolders.get(position);
            if (videoFolder != null) {
                Activity act = activity.get();
                if (act != null) {
                    Intent i = new Intent(act, VideoFilesActivity.class);
                    i.putExtra(EXTRA_VIDEO_FOLDER, videoFolder);
                    act.startActivity(i);
                }
            }
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_video_folder, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Activity act = activity.get();
            if (act != null) {

                final VideoFolder videoFolder = videoFolders.get(position);
                if (videoFolder != null) {

                    holder.folderCard.setDescription(videoFolder.folderName);

                    MediaApplication.getInstance().getRepository().getVideoFilesFrame(videoFolder.id)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .take(4)
                            .subscribe(paths -> holder.folderCard.setFrameViewPaths(paths));

                    if (videoFolder.isHidden) {
                        holder.folderCard.setAlpha(0.35f);
                    } else {
                        holder.folderCard.setAlpha(1f);
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return videoFolders.size();
        }

        public void refresh(List<VideoFolder> receiveVideoFolders) {
            videoFolders.clear();
            videoFolders.addAll(receiveVideoFolders);
            notifyDataSetChanged();
        }

        public void removeFolder(int position) {
            videoFolders.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());
        }

        public void clearAdapter() {
            videoFolders.clear();
            notifyDataSetChanged();
        }
    }

    private void showEmptyContentCard() {
        emptyVideoContent.setVisibility(View.VISIBLE);
    }

    private void hideEmptyContentCard() {
        emptyVideoContent.setVisibility(View.GONE);
    }

}
