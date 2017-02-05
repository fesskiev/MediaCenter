package com.fesskiev.mediacenter.vk;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.download.DownloadFile;
import com.fesskiev.mediacenter.utils.download.DownloadManager;
import com.fesskiev.mediacenter.widgets.MaterialProgressBar;
import com.fesskiev.mediacenter.widgets.recycleview.EndlessScrollListener;
import com.fesskiev.mediacenter.widgets.recycleview.RecyclerItemTouchClickListener;
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class RecyclerAudioFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public abstract int getResourceId();

    public abstract void fetchAudio(int offset);

    protected AudioAdapter audioAdapter;
    protected RecyclerView recyclerView;
    protected MaterialProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    protected int audioOffset;
    private int selectedPosition;
    private boolean listenerAttached;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getResourceId(), container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = (MaterialProgressBar) view.findViewById(R.id.progressBar);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.primary_light));
        swipeRefreshLayout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));

        recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        ScrollingLinearLayoutManager layoutManager = new ScrollingLinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false, 1000);
        recyclerView.setLayoutManager(layoutManager);
        audioAdapter = new AudioAdapter();
        recyclerView.setAdapter(audioAdapter);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        addTouchListener();

        recyclerView.addOnScrollListener(new EndlessScrollListener(layoutManager) {
            @Override
            public void onEndlessScrolled() {
                audioOffset += 20;
                fetchAudio(audioOffset);
                showProgressBar();
            }
        });
    }

    @Override
    public void onRefresh() {
        fetchAudio(audioOffset);
    }

    private RecyclerItemTouchClickListener recyclerItemTouchClickListener = new RecyclerItemTouchClickListener(getActivity(),
            new RecyclerItemTouchClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View childView, int position) {
                    selectedPosition = position;
                    List<DownloadFile> downloadFiles = audioAdapter.getDownloadFiles();
                    if (downloadFiles != null) {
                        DownloadFile downloadFile = downloadFiles.get(position);
                        if (downloadFile != null) {
                            if (downloadFile.getDownloadManager() == null) {
                                downloadFileDialog(downloadFile, position);
                            }
                        }
                    }
                }

                @Override
                public void onItemLongPress(View childView, int position) {

                }
            });

    private void removeTouchListener() {
        if (listenerAttached) {
            recyclerView.removeOnItemTouchListener(recyclerItemTouchClickListener);
            listenerAttached = false;
        }
    }

    private void addTouchListener() {
        if (!listenerAttached) {
            recyclerView.addOnItemTouchListener(recyclerItemTouchClickListener);
            listenerAttached = true;
        }
    }

    private void downloadFileDialog(final DownloadFile musicFile, final int position) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle(getString(R.string.dialog_download_title));
        builder.setMessage(R.string.dialog_download_message);
        builder.setPositiveButton(R.string.dialog_download_ok,
                (dialog, which) -> {
                    musicFile.downloadMusicFile(position);
                    removeTouchListener();
                });
        builder.setNegativeButton(R.string.dialog_download_cancel,
                (dialog, which) -> {
                    addTouchListener();
                    dialog.cancel();
                });
        builder.show();
    }

    public void showProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    public void showRefresh() {
        if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    public void hideRefresh() {
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }


    protected class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ViewHolder> {

        private List<DownloadFile> downloadFiles;

        public AudioAdapter() {
            this.downloadFiles = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            View downloadContainer;
            View itemContainer;
            TextView artist;
            TextView title;
            TextView duration;
            ProgressBar downloadProgress;
            TextView progressValue;
            ImageView startPauseDownload;
            ImageView cancelDownload;

            public ViewHolder(View v) {
                super(v);

                itemContainer = v.findViewById(R.id.itemContainer);
                downloadContainer = v.findViewById(R.id.downloadContainer);
                artist = (TextView) v.findViewById(R.id.itemArtist);
                title = (TextView) v.findViewById(R.id.itemTitle);
                duration = (TextView) v.findViewById(R.id.itemTime);
                downloadProgress = (ProgressBar) v.findViewById(R.id.downloadProgressBar);
                progressValue = (TextView) v.findViewById(R.id.progressValue);
                startPauseDownload = (ImageView) v.findViewById(R.id.startPauseDownloadButton);
                startPauseDownload.setOnClickListener(this);
                cancelDownload = (ImageView) v.findViewById(R.id.cancelDownloadButton);
                cancelDownload.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                DownloadFile downloadFile =
                        audioAdapter.getDownloadFiles().get(selectedPosition);
                switch (v.getId()) {
                    case R.id.startPauseDownloadButton:
                        if (downloadFile != null) {
                            DownloadManager downloadManager = downloadFile.getDownloadManager();
                            switch (downloadManager.getStatus()) {
                                case DownloadManager.PAUSED:
                                    downloadManager.resume();
                                    break;
                                case DownloadManager.DOWNLOADING:
                                    downloadManager.pause();
                                    break;
                            }
                            downloadFile.updateAdapter();
                        }
                        break;
                    case R.id.cancelDownloadButton:
                        if (downloadFile != null) {
                            DownloadManager downloadManager = downloadFile.getDownloadManager();
                            downloadManager.cancel();
                            if (downloadManager.removeFile()) {
                                Utils.showCustomSnackbar(getView(),
                                        getContext(),
                                        getString(R.string.shackbar_delete_file),
                                        Snackbar.LENGTH_SHORT).
                                        show();
                            }
                            addTouchListener();
                        }
                        break;
                }
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_vk_audio, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            DownloadFile downloadFile = downloadFiles.get(position);
            if (downloadFile != null) {

                holder.artist.setText(Html.fromHtml(downloadFile.getAudio().getArtist()));
                holder.title.setText(Html.fromHtml(downloadFile.getAudio().getTitle()));
                holder.duration.setText(Utils.getTimeFromSecondsString(downloadFile.getAudio().getDuration()));

                DownloadManager downloadManager = downloadFile.getDownloadManager();
                if (downloadManager != null) {
                    switch (downloadManager.getStatus()) {
                        case DownloadManager.DOWNLOADING:
                            holder.downloadContainer.setVisibility(View.VISIBLE);
                            holder.cancelDownload.setVisibility(View.GONE);
                            holder.startPauseDownload.setImageResource(R.drawable.pause_icon);
                            holder.downloadProgress.setProgress((int) downloadManager.getProgress());
                            holder.progressValue.setText(String.format(Locale.getDefault(), "%1$d %2$s",
                                    (int) downloadManager.getProgress(), "\u0025"));
                            break;
                        case DownloadManager.COMPLETE:
                            holder.downloadContainer.setVisibility(View.GONE);
                            holder.itemContainer.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.primary_light));
                            addTouchListener();
                            break;
                        case DownloadManager.PAUSED:
                            holder.startPauseDownload.setImageResource(R.drawable.download_icon);
                            holder.cancelDownload.setVisibility(View.VISIBLE);
                            break;
                        case DownloadManager.CANCELLED:
                            holder.downloadContainer.setVisibility(View.GONE);
                            break;
                        case DownloadManager.ERROR:
                            holder.downloadContainer.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.red));
                            holder.cancelDownload.setVisibility(View.VISIBLE);
                            holder.startPauseDownload.setVisibility(View.GONE);
                            break;
                    }

                } else {
                    holder.downloadContainer.setVisibility(View.GONE);
                    holder.itemContainer.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.white));
                }

            }
        }

        public void refresh(List<DownloadFile> downloadFiles) {
            this.downloadFiles.addAll(downloadFiles);
            notifyDataSetChanged();
        }

        public void clearAdapter() {
            this.downloadFiles.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return downloadFiles.size();
        }

        public List<DownloadFile> getDownloadFiles() {
            return downloadFiles;
        }
    }


}
