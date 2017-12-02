package com.fesskiev.mediacenter.ui.video;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.ui.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.VideoFolder;
import com.fesskiev.mediacenter.ui.video.player.VideoExoPlayerActivity;
import com.fesskiev.mediacenter.utils.AppAnimationUtils;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.dialogs.SimpleDialog;
import com.fesskiev.mediacenter.widgets.dialogs.VideoFileDetailsDialog;
import com.fesskiev.mediacenter.widgets.item.VideoCardView;
import com.fesskiev.mediacenter.widgets.menu.ContextMenuManager;
import com.fesskiev.mediacenter.widgets.menu.VideoContextMenu;
import com.fesskiev.mediacenter.widgets.recycleview.ItemOffsetDecoration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class VideoFilesActivity extends AnalyticsActivity {

    public static VideoFilesActivity newInstance() {
        return new VideoFilesActivity();
    }

    private VideoFilesAdapter adapter;
    private RecyclerView recyclerView;
    private CardView emptyVideoContent;

    private VideoFolder videoFolder;

    private boolean layoutAnimate;

    private VideoFilesViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_files);
        videoFolder = getIntent().getExtras().getParcelable(VideoFoldersFragment.EXTRA_VIDEO_FOLDER);
        emptyVideoContent = findViewById(R.id.emptyVideoContentCard);
        setupToolbar();
        setupRecyclerView();
        observeData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (videoFolder != null) {
            toolbar.setTitle(videoFolder.folderName);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        final int spacing = getResources().getDimensionPixelOffset(R.dimen.default_spacing_small);
        recyclerView = findViewById(R.id.foldersGridView);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new VideoFilesAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new ItemOffsetDecoration(spacing));
    }

    private void observeData() {
        viewModel = ViewModelProviders.of(this).get(VideoFilesViewModel.class);
        viewModel.getVideoFilesLiveData().observe(this, this::refreshAdapter);
        viewModel.getDeletedFileLiveData().observe(this, position -> {
            adapter.removeItem(position);
            showDeletedVideoFileSnackBar();
        });
        viewModel.getNotExistFileLiveData().observe(this, Void -> showVideoFileNotExistSnackBar());
        viewModel.getEmptyFilesLiveData().observe(this, Void -> showEmptyContentCard());
    }

    private void refreshAdapter(List<VideoFile> videoFiles) {
        adapter.refresh(videoFiles);
        hideEmptyContentCard();
        animateLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.fetchVideoFolderFiles(videoFolder);
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("layoutAnimate", layoutAnimate);
        outState.putParcelable("videoFolder", videoFolder);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        layoutAnimate = savedInstanceState.getBoolean("layoutAnimate");
        videoFolder = savedInstanceState.getParcelable("videoFolder");
    }

    private void showAddToPlayListSnackBar() {
        Utils.showCustomSnackbar(getCurrentFocus(), getApplicationContext(),
                getString(R.string.add_to_playlist_video_text), Snackbar.LENGTH_SHORT).show();
    }

    private void showDeletedVideoFileSnackBar() {
        Utils.showCustomSnackbar(getCurrentFocus(), getApplicationContext(),
                getString(R.string.shackbar_delete_file), Snackbar.LENGTH_LONG).show();
    }

    private void showVideoFileNotExistSnackBar() {
        Utils.showCustomSnackbar(getCurrentFocus(), getApplicationContext(),
                getString(R.string.snackbar_file_not_exist), Snackbar.LENGTH_LONG).show();
    }

    private void animateLayout() {
        if (!layoutAnimate) {
            AppAnimationUtils.getInstance().loadGridRecyclerItemAnimation(recyclerView);
            recyclerView.scheduleLayoutAnimation();
            layoutAnimate = true;
        }
    }

    public void addVideoFileToPlayList(VideoFile videoFile) {
        if (viewModel.checkVideoFileExist(videoFile)) {
            viewModel.addVideoFileToPlayList(videoFile);
            showAddToPlayListSnackBar();
        }
    }

    public void deleteVideoFile(VideoFile videoFile, int position) {
        if (viewModel.checkVideoFileExist(videoFile)) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.addToBackStack(null);
            SimpleDialog dialog = SimpleDialog.newInstance(getString(R.string.dialog_delete_file_title),
                    getString(R.string.dialog_delete_file_message), R.drawable.icon_trash);
            dialog.show(transaction, SimpleDialog.class.getName());
            dialog.setPositiveListener(() -> viewModel.deleteVideoFile(videoFile, position));
        }
    }

    public void videoFileDetails(VideoFile videoFile) {
        if (viewModel.checkVideoFileExist(videoFile)) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.addToBackStack(null);
            VideoFileDetailsDialog dialog = VideoFileDetailsDialog.newInstance(videoFile);
            dialog.setOnVideoFileDetailsDialogListener(() -> viewModel.fetchVideoFolderFiles(videoFolder));
            dialog.show(transaction, VideoFileDetailsDialog.class.getName());
        }
    }

    public void startExoPlayerActivity(VideoFile videoFile) {
        if (viewModel.checkVideoFileExist(videoFile)) {
            Intent intent = new Intent(this, VideoExoPlayerActivity.class);
            intent.putExtra(VideoExoPlayerActivity.URI_EXTRA, videoFile.getFilePath());
            intent.putExtra(VideoExoPlayerActivity.VIDEO_NAME_EXTRA, videoFile.getFileName());
            intent.setAction(VideoExoPlayerActivity.ACTION_VIEW_URI);
            startActivity(intent);
        }
    }

    public Observable<Bitmap> getVideoFileFrame(String path) {
        return viewModel.getVideoFileFrame(path);
    }

    private void showEmptyContentCard() {
        emptyVideoContent.setVisibility(View.VISIBLE);
    }

    private void hideEmptyContentCard() {
        emptyVideoContent.setVisibility(View.GONE);
    }


    private static class VideoFilesAdapter extends RecyclerView.Adapter<VideoFilesAdapter.ViewHolder> {

        private WeakReference<VideoFilesActivity> activity;
        private List<VideoFile> videoFiles;

        public VideoFilesAdapter(VideoFilesActivity activity) {
            this.activity = new WeakReference<>(activity);
            this.videoFiles = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            VideoCardView videoCard;

            public ViewHolder(View v) {
                super(v);

                videoCard = v.findViewById(R.id.videoCardView);
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
            VideoFilesActivity act = activity.get();
            if (act != null) {
                VideoFile videoFile = videoFiles.get(position);
                if (videoFile != null) {
                    act.startExoPlayerActivity(videoFile);
                }
            }
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

                        @Override
                        public void onDetailsVideoFile() {
                            videoFileDetails(position);
                        }
                    });
        }

        private void videoFileDetails(int position) {
            VideoFilesActivity act = activity.get();
            if (act != null) {
                VideoFile videoFile = videoFiles.get(position);
                if (videoFile != null) {
                    act.videoFileDetails(videoFile);
                }
            }
        }

        private void deleteVideo(final int position) {
            VideoFilesActivity act = activity.get();
            if (act != null) {
                VideoFile videoFile = videoFiles.get(position);
                if (videoFile != null) {
                    act.deleteVideoFile(videoFile, position);
                }
            }
        }

        private void addToPlaylist(int position) {
            VideoFilesActivity act = activity.get();
            if (act != null) {
                VideoFile videoFile = videoFiles.get(position);
                if (videoFile != null) {
                    act.addVideoFileToPlayList(videoFile);
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
            final VideoFile videoFile = videoFiles.get(position);
            if (videoFile != null) {
                holder.videoCard.setDescription(videoFile.description);

                ImageView frameView = holder.videoCard.getFrameView();
                String framePath = videoFile.framePath;
                VideoFilesActivity act = activity.get();
                if (framePath != null && act != null) {
                    act.getVideoFileFrame(framePath).subscribe(frameView::setImageBitmap);
                } else {
                    frameView.setImageResource(0);
                    frameView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(),
                            R.color.search_background));
                }
                if (videoFile.isHidden) {
                    holder.videoCard.setAlpha(0.35f);
                } else {
                    holder.videoCard.setAlpha(1f);
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
