package com.fesskiev.mediacenter.ui.video;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.model.VideoFolder;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.players.VideoPlayer;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.ui.video.player.VideoExoPlayerActivity;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.dialogs.VideoFileDetailsDialog;
import com.fesskiev.mediacenter.widgets.item.VideoCardView;
import com.fesskiev.mediacenter.widgets.menu.ContextMenuManager;
import com.fesskiev.mediacenter.widgets.menu.VideoContextMenu;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class VideoFilesActivity extends AnalyticsActivity {

    public static VideoFilesActivity newInstance() {
        return new VideoFilesActivity();
    }

    private VideoFilesAdapter adapter;
    private Subscription subscription;
    private DataRepository repository;
    private VideoFolder videoFolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_files);

        EventBus.getDefault().register(this);

        repository = MediaApplication.getInstance().getRepository();

        videoFolder =
                getIntent().getExtras().getParcelable(VideoFoldersFragment.EXTRA_VIDEO_FOLDER);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (videoFolder != null) {
            toolbar.setTitle(videoFolder.folderName);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2,
                GridLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.foldersGridView);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new VideoFilesAdapter(this);
        recyclerView.setAdapter(adapter);
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


    public void fetchVideoFolderFiles(VideoFolder videoFolder) {
        subscription = repository.getVideoFiles(videoFolder.id)
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(Observable::from)
                .filter(file -> {
                    if (AppSettingsManager.getInstance().isShowHiddenFiles()) {
                        return true;
                    }
                    return !file.isHidden;
                })
                .toList()
                .subscribe(videoFiles -> adapter.refresh(videoFiles));
    }

    public void refreshVideoContent() {
        fetchVideoFolderFiles(videoFolder);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchVideoFolderFiles(videoFolder);
    }

    @Override
    public void onPause() {
        super.onPause();
        RxUtils.unsubscribe(subscription);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlaybackStateEvent(PlaybackService playbackState) {
        adapter.setPlaying(playbackState.isPlaying());
    }

    private static class VideoFilesAdapter extends RecyclerView.Adapter<VideoFilesAdapter.ViewHolder> {

        private WeakReference<Activity> activity;
        private List<VideoFile> videoFiles;
        private boolean isPlaying;

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
                        VideoPlayer videoPlayer = MediaApplication.getInstance().getVideoPlayer();
                        videoPlayer.setVideoFiles(videoFiles);
                        videoPlayer.setCurrentVideoFile(videoFile);

                        checkNeedStopAudioPlaying();
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

        private void checkNeedStopAudioPlaying() {
            if (isPlaying) {
                MediaApplication.getInstance().getAudioPlayer().pause();
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

                        @Override
                        public void onDetailsVideoFile() {
                            videoFileDetails(position);
                        }
                    });
        }

        private void videoFileDetails(int position) {
            Activity act = activity.get();
            if (act != null) {
                final VideoFile videoFile = videoFiles.get(position);
                if (videoFile != null) {
                    FragmentTransaction transaction =
                            ((FragmentActivity) act).getSupportFragmentManager().beginTransaction();
                    transaction.addToBackStack(null);
                    VideoFileDetailsDialog dialog = VideoFileDetailsDialog.newInstance(videoFile);
                    dialog.setOnVideoFileDetailsDialogListener(() -> refreshVideoFiles(act));
                    dialog.show(transaction, VideoFileDetailsDialog.class.getName());

                }
            }
        }

        private void refreshVideoFiles(Activity act) {
            ((VideoFilesActivity) act).refreshVideoContent();
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
                                            DataRepository repository = MediaApplication.getInstance().getRepository();
                                            return RxUtils.fromCallable(repository.deleteVideoFile(videoFile.getFilePath()));
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

                    if (videoFile.isHidden) {
                        holder.videoCard.setAlpha(0.35f);
                    } else {
                        holder.videoCard.setAlpha(1f);
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


        public void setPlaying(boolean playing) {
            isPlaying = playing;
        }
    }
}
