package com.fesskiev.player.ui.vk;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.fesskiev.player.model.vk.VKMusicFile;
import com.fesskiev.player.utils.Download;
import com.fesskiev.player.widgets.MaterialProgressBar;
import com.fesskiev.player.widgets.recycleview.EndlessScrollListener;
import com.fesskiev.player.widgets.recycleview.OnItemClickListener;
import com.fesskiev.player.widgets.recycleview.RecycleItemClickListener;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

public abstract class AudioFragment extends Fragment {

    public abstract int getResourceId();

    public abstract void fetchAudio(int offset);

    protected AudioAdapter audioAdapter;
    private MaterialProgressBar progressBar;
    protected RecyclerView recyclerView;
    protected int audioOffset;

    @Nullable
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
        recyclerView.addOnItemTouchListener(new RecycleItemClickListener(getActivity(),
                new OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        List<DownloadVkMusicFile> downloadVkMusicFiles = audioAdapter.getDownloadVkMusicFiles();
                        if (downloadVkMusicFiles != null) {
                            DownloadVkMusicFile downloadVkMusicFile = downloadVkMusicFiles.get(position);
                            if (downloadVkMusicFile != null) {
                                if (downloadVkMusicFile.download == null) {
                                    downloadFileDialog(downloadVkMusicFile, position);
                                } else {

                                }
                            }
                        }
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

    private void downloadFileDialog(final DownloadVkMusicFile musicFile, final int position) {
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


    protected List<DownloadVkMusicFile> getDownloadVKMusicFiles(List<VKMusicFile> vkMusicFiles) {
        List<DownloadVkMusicFile> downloadVkMusicFiles = new ArrayList<>();
        for (VKMusicFile vkMusicFile : vkMusicFiles) {
            DownloadVkMusicFile downloadVkMusicFile = new DownloadVkMusicFile();
            downloadVkMusicFile.vkMusicFile = vkMusicFile;
            downloadVkMusicFiles.add(downloadVkMusicFile);
        }
        return downloadVkMusicFiles;
    }


    protected class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ViewHolder> {

        private List<DownloadVkMusicFile> downloadVkMusicFiles;

        public AudioAdapter() {
            this.downloadVkMusicFiles = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView artist;
            TextView title;
            ImageView downloadCompleteIcon;
            ProgressBar downloadProgress;
            TextView progressValue;
            View downloadContainer;

            public ViewHolder(View v) {
                super(v);

                artist = (TextView) v.findViewById(R.id.itemArtist);
                title = (TextView) v.findViewById(R.id.itemTitle);
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

            DownloadVkMusicFile vkMusicFile = downloadVkMusicFiles.get(position);
            if (vkMusicFile != null) {

                holder.artist.setText(vkMusicFile.vkMusicFile.artist);
                holder.title.setText(vkMusicFile.vkMusicFile.title);

                if (vkMusicFile.download != null) {
                    Download download = vkMusicFile.download;
                    switch (download.getStatus()) {
                        case Download.DOWNLOADING:
                            holder.downloadContainer.setVisibility(View.VISIBLE);
                            holder.downloadProgress.setProgress((int) vkMusicFile.download.getProgress());
                            holder.progressValue.setText(String.valueOf((int) vkMusicFile.download.getProgress()) + "%");
                            break;
                        case Download.COMPLETE:
                            holder.downloadContainer.setVisibility(View.GONE);
                            holder.downloadCompleteIcon.setVisibility(View.VISIBLE);
                            break;
                        case Download.PAUSED:
                            break;
                        case Download.CANCELLED:
                            break;
                        case Download.ERROR:
                            break;
                    }

                } else {
                    holder.downloadContainer.setVisibility(View.GONE);
                    holder.downloadCompleteIcon.setVisibility(View.GONE);
                }

            }
        }

        public void refresh(List<DownloadVkMusicFile> downloadVkMusicFiles) {
            this.downloadVkMusicFiles.addAll(downloadVkMusicFiles);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return downloadVkMusicFiles.size();
        }

        public List<DownloadVkMusicFile> getDownloadVkMusicFiles() {
            return downloadVkMusicFiles;
        }
    }


    protected class DownloadVkMusicFile implements Download.OnDownloadListener {

        public VKMusicFile vkMusicFile;
        public Download download;
        public int position;

        @Override
        public void onStatusChanged() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    audioAdapter.notifyItemChanged(position);

                }
            });
        }

        @Override
        public void onProgress() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    audioAdapter.notifyItemChanged(position);

                }
            });
        }

        public void downloadMusicFile(int position) {
            this.position = position;
            String fileName = vkMusicFile.artist + "-" + vkMusicFile.title;
            download = new Download(vkMusicFile.url, fileName);
            download.setOnDownloadListener(this);
        }
    }
}
