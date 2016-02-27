package com.fesskiev.player.ui.vk;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fesskiev.player.R;
import com.fesskiev.player.memory.MemoryLeakWatcherFragment;
import com.fesskiev.player.model.vk.VKMusicFile;
import com.fesskiev.player.utils.download.DownloadAudioFile;
import com.fesskiev.player.utils.download.DownloadManager;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.MaterialProgressBar;
import com.fesskiev.player.widgets.recycleview.EndlessScrollListener;
import com.fesskiev.player.widgets.recycleview.RecyclerItemTouchClickListener;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

public abstract class RecyclerAudioFragment extends MemoryLeakWatcherFragment {

    public abstract int getResourceId();

    public abstract void fetchAudio(int offset);

    protected AudioAdapter audioAdapter;
    private MaterialProgressBar progressBar;
    protected RecyclerView recyclerView;
    protected int audioOffset;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getResourceId(), container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        ScrollingLinearLayoutManager layoutManager = new ScrollingLinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false, 1000);
        recyclerView.setLayoutManager(layoutManager);
        audioAdapter = new AudioAdapter();
        recyclerView.setAdapter(audioAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemTouchClickListener(getActivity(),
                new RecyclerItemTouchClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View childView, int position) {
                List<DownloadAudioFile> downloadAudioFiles = audioAdapter.getDownloadAudioFiles();
                if (downloadAudioFiles != null) {
                    DownloadAudioFile downloadAudioFile = downloadAudioFiles.get(position);
                    if (downloadAudioFile != null) {
                        if (downloadAudioFile.getDownloadManager() == null) {
                            downloadFileDialog(downloadAudioFile, position);
                        } else {

                        }
                    }
                }
            }

            @Override
            public void onItemLongPress(View childView, int position) {

            }
        }));


        recyclerView.addOnScrollListener(new EndlessScrollListener(layoutManager) {
            @Override
            public void onEndlessScrolled() {
                audioOffset += 20;
                fetchAudio(audioOffset);
                showProgressBar();
            }
        });

        progressBar = (MaterialProgressBar) view.findViewById(R.id.progressBar);
    }

    private void downloadFileDialog(final DownloadAudioFile musicFile, final int position) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle(getString(R.string.dialog_download_title));
        builder.setMessage(R.string.dialog_download_message);
        builder.setPositiveButton(R.string.dialog_download_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        musicFile.downloadMusicFile(position);
                    }
                });
        builder.setNegativeButton(R.string.dialog_download_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    protected class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ViewHolder> {

        private List<DownloadAudioFile> downloadAudioFiles;

        public AudioAdapter() {
            this.downloadAudioFiles = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView artist;
            TextView title;
            TextView duration;
            ImageView downloadCompleteIcon;
            ProgressBar downloadProgress;
            TextView progressValue;
            View downloadContainer;

            public ViewHolder(View v) {
                super(v);

                artist = (TextView) v.findViewById(R.id.itemArtist);
                title = (TextView) v.findViewById(R.id.itemTitle);
                duration = (TextView) v.findViewById(R.id.itemTime);
                downloadCompleteIcon = (ImageView) v.findViewById(R.id.itemDownloadComplete);
                downloadProgress = (ProgressBar) v.findViewById(R.id.downloadProgressBar);
                progressValue = (TextView) v.findViewById(R.id.progressValue);
                downloadContainer = v.findViewById(R.id.downloadContainer);

            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_audio, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            DownloadAudioFile downloadAudioFile = downloadAudioFiles.get(position);
            if (downloadAudioFile != null) {

                holder.artist.setText(downloadAudioFile.getVkMusicFile().artist);
                holder.title.setText(downloadAudioFile.getVkMusicFile().title);
                holder.duration.setText(Utils.getTimeFromSecondsString(downloadAudioFile.getVkMusicFile().duration));

                if (downloadAudioFile.getDownloadManager() != null) {
                    DownloadManager downloadManager = downloadAudioFile.getDownloadManager();
                    switch (downloadManager.getStatus()) {
                        case DownloadManager.DOWNLOADING:
                            holder.downloadContainer.setVisibility(View.VISIBLE);
                            holder.downloadProgress.setProgress((int) downloadAudioFile.getDownloadManager().getProgress());
                            holder.progressValue.setText(String.valueOf((int) downloadAudioFile.getDownloadManager().getProgress()) + "%");
                            break;
                        case DownloadManager.COMPLETE:
                            holder.downloadContainer.setVisibility(View.GONE);
                            holder.downloadCompleteIcon.setVisibility(View.VISIBLE);
                            break;
                        case DownloadManager.PAUSED:
                            break;
                        case DownloadManager.CANCELLED:
                            break;
                        case DownloadManager.ERROR:
                            break;
                    }

                } else {
                    holder.downloadContainer.setVisibility(View.GONE);
                    holder.downloadCompleteIcon.setVisibility(View.GONE);
                }

            }
        }

        public void refresh(List<DownloadAudioFile> downloadAudioFiles) {
            this.downloadAudioFiles.addAll(downloadAudioFiles);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return downloadAudioFiles.size();
        }

        public List<DownloadAudioFile> getDownloadAudioFiles() {
            return downloadAudioFiles;
        }
    }



}
