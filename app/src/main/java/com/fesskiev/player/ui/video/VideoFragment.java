package com.fesskiev.player.ui.video;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.buttons.VideoCardView;
import com.fesskiev.player.widgets.recycleview.GridDividerDecoration;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class VideoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = VideoFragment.class.getSimpleName();

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
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        subscription = getObservable()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(getObserver());

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
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                swipeRefreshLayout.setRefreshing(false);
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

        destroyLoader();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void destroyLoader() {
        getActivity().getSupportLoaderManager().destroyLoader(Constants.GET_VIDEO_FILES_LOADER);
    }

    private Observable<Boolean> getObservable() {
        return Observable.just(true).map(new Func1<Boolean, Boolean>() {
            @Override
            public Boolean call(Boolean result) {
                DatabaseHelper.resetVideoContentDatabase(getActivity());
                return result;
            }
        });
    }

    private Observer<Boolean> getObserver() {
        return new Observer<Boolean>() {

            @Override
            public void onCompleted() {
                FileSystemIntentService.startFetchVideo(getActivity());

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Boolean bool) {

            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (subscription != null) {
            subscription.unsubscribe();
        }
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
                videoPlayer.currentVideoFile = videoFile;
                startExoPlayerActivity(videoFile);
            }
        }

        private void showPopupMenu(final View view, final int position) {
            final PopupMenu popupMenu = new PopupMenu(getActivity(), view);
            final Menu menu = popupMenu.getMenu();
            popupMenu.getMenuInflater().inflate(R.menu.menu_popup_item_video, menu);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.delete:
                            deleteVideo(position);
                            break;
                        case R.id.add_playlist:
                            addToPlaylist(position);
                            break;
                    }
                    return true;
                }
            });
            popupMenu.show();
        }

        private void deleteVideo(int position) {

        }

        private void addToPlaylist(int position) {
            VideoFile videoFile = videoPlayer.videoFiles.get(position);
            if (videoFile != null) {
                videoFile.inPlayList = true;
                DatabaseHelper.updateVideoFile(getContext(), videoFile);
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
