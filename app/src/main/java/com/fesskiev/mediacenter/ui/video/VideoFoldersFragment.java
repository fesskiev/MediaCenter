package com.fesskiev.mediacenter.ui.video;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.VideoFolder;
import com.fesskiev.mediacenter.ui.MainActivity;
import com.fesskiev.mediacenter.utils.AppAnimationUtils;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.dialogs.MediaFolderDetailsDialog;
import com.fesskiev.mediacenter.widgets.dialogs.SimpleDialog;
import com.fesskiev.mediacenter.widgets.dialogs.VideoFolderDetailsDialog;
import com.fesskiev.mediacenter.widgets.item.VideoFolderCardView;
import com.fesskiev.mediacenter.widgets.menu.ContextMenuManager;
import com.fesskiev.mediacenter.widgets.menu.FolderContextMenu;
import com.fesskiev.mediacenter.widgets.recycleview.ItemOffsetDecoration;
import com.fesskiev.mediacenter.widgets.recycleview.helper.ItemTouchHelperAdapter;
import com.fesskiev.mediacenter.widgets.recycleview.helper.ItemTouchHelperViewHolder;
import com.fesskiev.mediacenter.widgets.recycleview.helper.SimpleItemTouchHelperCallback;
import com.fesskiev.mediacenter.widgets.swipe.ScrollChildSwipeRefreshLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

public class VideoFoldersFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static VideoFoldersFragment newInstance() {
        return new VideoFoldersFragment();
    }

    public static final String EXTRA_VIDEO_FOLDER = "com.fesskiev.player.extra.EXTRA_VIDEO_FOLDER";

    private VideoFoldersAdapter adapter;
    private RecyclerView recyclerView;
    private CardView emptyVideoContent;
    private ScrollChildSwipeRefreshLayout swipeRefreshLayout;

    private boolean layoutAnimate;

    private VideoFoldersViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        observeData();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            layoutAnimate = savedInstanceState.getBoolean("layoutAnimate");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_folders, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);

        final int spacing = getResources().getDimensionPixelOffset(R.dimen.default_spacing_small);

        recyclerView = view.findViewById(R.id.foldersGridView);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new VideoFoldersAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new ItemOffsetDecoration(spacing));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        emptyVideoContent = view.findViewById(R.id.emptyVideoContentCard);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.primary_light));
        swipeRefreshLayout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        swipeRefreshLayout.setScrollUpChild(recyclerView);

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("layoutAnimate", layoutAnimate);
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchVideoFolders();
    }

    @Override
    public void onRefresh() {
        makeRefreshDialog();
    }

    private void fetchVideoFolders() {
        viewModel.fetchVideoFolders();
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void observeData() {
        viewModel = ViewModelProviders.of(this).get(VideoFoldersViewModel.class);
        viewModel.getVideoFoldersLiveData().observe(this, this::refreshAdapter);
        viewModel.getDeletedFolderLiveData().observe(this, position -> {
            adapter.removeFolder(position);
            showVideoFolderDeletedShackBar();
        });
        viewModel.getEmptyFoldersLiveData().observe(this, Void -> showEmptyContentCard());
        viewModel.getUpdatedFolderIndexesLiveData().observe(this, Void -> adapter.notifyDataSetChanged());
        viewModel.getNotExistFolderLiveData().observe(this, Void -> showVideoFolderNotExistSnackBar());
    }

    public void updateVideoFoldersIndexes(List<VideoFolder> videoFolders) {
        viewModel.updateVideoFoldersIndexes(videoFolders);
    }

    public Observable<List<Bitmap>> getVideoFilesFrame(VideoFolder videoFolder) {
        return viewModel.getVideoFilesFrame(videoFolder);
    }

    public void deleteVideoFolder(VideoFolder videoFolder, int position) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        SimpleDialog dialog = SimpleDialog.newInstance(getString(R.string.dialog_delete_file_title),
                getString(R.string.dialog_delete_folder_message), R.drawable.icon_trash);
        dialog.show(transaction, SimpleDialog.class.getName());
        dialog.setPositiveListener(() -> viewModel.deleteVideoFolder(videoFolder, position));
    }

    public void showVideoFolderDetails(VideoFolder videoFolder) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        MediaFolderDetailsDialog dialog = VideoFolderDetailsDialog.newInstance(videoFolder);
        dialog.setOnMediaFolderDetailsDialogListener(this::refreshVideoContent);
        dialog.show(transaction, VideoFolderDetailsDialog.class.getName());
    }

    public void startVideoFilesActivity(VideoFolder videoFolder) {
        if (viewModel.checkVideoFolderExist(videoFolder)) {
            Intent i = new Intent(getContext(), VideoFilesActivity.class);
            i.putExtra(EXTRA_VIDEO_FOLDER, videoFolder);
            startActivity(i);
        }
    }

    private void makeRefreshDialog() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        SimpleDialog dialog = SimpleDialog.newInstance(getString(R.string.dialog_refresh_video_title),
                getString(R.string.dialog_refresh_video_message), R.drawable.icon_refresh);
        dialog.show(transaction, SimpleDialog.class.getName());
        dialog.setPositiveListener(this::checkPermissionAndFetch);
        dialog.setNegativeListener(() -> swipeRefreshLayout.setRefreshing(false));
    }

    private void showVideoFolderDeletedShackBar() {
        Utils.showCustomSnackbar(getView(), getContext(), getString(R.string.shackbar_delete_folder),
                Snackbar.LENGTH_LONG).show();
    }

    private void showVideoFolderNotExistSnackBar() {
        Utils.showCustomSnackbar(getView(), getContext(),
                getString(R.string.snackbar_folder_not_exist), Snackbar.LENGTH_LONG).show();
    }

    private void refreshAdapter(List<VideoFolder> videoFolders) {
        adapter.refresh(videoFolders);
        animateLayout();
        hideEmptyContentCard();
        AppLog.INFO("onNext:video folders: " + (videoFolders == null ? "null" : videoFolders.size()));
    }


    private void checkPermissionAndFetch() {
        swipeRefreshLayout.setRefreshing(false);
        ((MainActivity) getActivity()).checkPermissionAndFetchVideo();
    }

    public void refreshVideoContent() {
        swipeRefreshLayout.setRefreshing(false);
        fetchVideoFolders();
    }

    public void clearVideoContent() {
        adapter.clearAdapter();
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    private void animateLayout() {
        if (!layoutAnimate) {
            AppAnimationUtils.getInstance().loadGridRecyclerItemAnimation(recyclerView);
            recyclerView.scheduleLayoutAnimation();
            layoutAnimate = true;
        }
    }

    private void showEmptyContentCard() {
        emptyVideoContent.setVisibility(View.VISIBLE);
    }

    private void hideEmptyContentCard() {
        emptyVideoContent.setVisibility(View.GONE);
    }


    private static class VideoFoldersAdapter extends RecyclerView.Adapter<VideoFoldersAdapter.ViewHolder> implements ItemTouchHelperAdapter {

        private WeakReference<VideoFoldersFragment> fragment;
        private List<VideoFolder> videoFolders;

        public VideoFoldersAdapter(VideoFoldersFragment fragment) {
            this.fragment = new WeakReference<>(fragment);
            this.videoFolders = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

            VideoFolderCardView folderCard;

            public ViewHolder(View v) {
                super(v);

                folderCard = v.findViewById(R.id.videoFolderCardView);
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

            @Override
            public void onItemSelected() {
                itemView.setAlpha(0.5f);
                changeSwipeRefreshState(false);
            }

            @Override
            public void onItemClear(int position) {
                itemView.setAlpha(1.0f);
                changeSwipeRefreshState(true);
                VideoFoldersFragment frg = fragment.get();
                if (frg != null) {
                    frg.updateVideoFoldersIndexes(videoFolders);
                }
            }

            private void changeSwipeRefreshState(boolean enable) {
                VideoFoldersFragment frg = fragment.get();
                if (frg != null) {
                    frg.getSwipeRefreshLayout().setEnabled(enable);
                }
            }
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            Collections.swap(videoFolders, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onItemDismiss(int position) {

        }

        private void showAudioContextMenu(View view, int position) {
            ContextMenuManager.getInstance().toggleFolderContextMenu(view,
                    new FolderContextMenu.OnFolderContextMenuListener() {
                        @Override
                        public void onDeleteFolder() {
                            deleteVideoFolder(position);
                        }

                        @Override
                        public void onDetailsFolder() {
                            showDetailsVideoFolder(position);
                        }

                        @Override
                        public void onSearchAlbum() {

                        }
                    }, false);
        }

        private void deleteVideoFolder(int position) {
            VideoFoldersFragment frg = fragment.get();
            if (frg != null) {
                VideoFolder videoFolder = videoFolders.get(position);
                if (videoFolder != null) {
                    frg.deleteVideoFolder(videoFolder, position);
                }
            }
        }

        private void showDetailsVideoFolder(int position) {
            VideoFoldersFragment frg = fragment.get();
            if (frg != null) {
                VideoFolder videoFolder = videoFolders.get(position);
                if (videoFolder != null) {
                    frg.showVideoFolderDetails(videoFolder);
                }
            }
        }

        private void startVideoFilesActivity(int position) {
            VideoFoldersFragment frg = fragment.get();
            if (frg != null) {
                VideoFolder videoFolder = videoFolders.get(position);
                if (videoFolder != null) {
                    frg.startVideoFilesActivity(videoFolder);
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
            final VideoFolder videoFolder = videoFolders.get(position);
            if (videoFolder != null) {
                holder.folderCard.setDescription(videoFolder.folderName);

                VideoFoldersFragment frg = fragment.get();
                if (frg != null) {
                    frg.getVideoFilesFrame(videoFolder).subscribe(bitmaps -> holder.folderCard.setFrameBitmaps(bitmaps));
                }
                if (videoFolder.isHidden) {
                    holder.folderCard.setAlpha(0.35f);
                } else {
                    holder.folderCard.setAlpha(1f);
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

        public void clearAdapter() {
            videoFolders.clear();
            notifyDataSetChanged();
        }

        public void removeFolder(int position) {
            videoFolders.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());
        }
    }
}
