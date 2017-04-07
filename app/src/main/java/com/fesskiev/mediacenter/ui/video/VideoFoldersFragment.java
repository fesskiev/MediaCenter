package com.fesskiev.mediacenter.ui.video;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.fesskiev.mediacenter.utils.admob.AdMobHelper;
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
    public void onPause() {
        super.onPause();
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

    private static class VideoFoldersAdapter extends RecyclerView.Adapter<VideoFoldersAdapter.ViewHolder> {

        private WeakReference<Activity> activity;
        private List<VideoFolder> videoFolders;


        public VideoFoldersAdapter(Activity activity) {
            this.activity = new WeakReference<>(activity);
            this.videoFolders = new ArrayList<>();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {


            public ViewHolder(View v) {
                super(v);

            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
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
    }

    private void showEmptyContentCard() {
        emptyVideoContent.setVisibility(View.VISIBLE);
    }

    private void hideEmptyContentCard() {
        emptyVideoContent.setVisibility(View.GONE);
    }

}
