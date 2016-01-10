package com.fesskiev.player.ui.vk;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fesskiev.player.R;
import com.fesskiev.player.model.VKMusicFile;
import com.fesskiev.player.services.FileTreeIntentService;
import com.fesskiev.player.services.RESTService;
import com.fesskiev.player.utils.AppSettingsManager;
import com.fesskiev.player.utils.DownloadFileHelper;
import com.fesskiev.player.utils.http.URLHelper;
import com.fesskiev.player.widgets.MaterialProgressBar;
import com.fesskiev.player.widgets.recycleview.OnItemClickListener;
import com.fesskiev.player.widgets.recycleview.RecycleItemClickListener;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;
import com.fesskiev.player.widgets.utils.HidingScrollListener;

import java.util.ArrayList;
import java.util.List;


public class MusicVKFragment extends Fragment {

    private static final String TAG = MusicVKFragment.class.getSimpleName();
    public static final String DOWNLOAD_FILE_PATH =
            "com.fesskiev.player.EXTRA_DOWNLOAD_FILE_PATH";
    public static final String SELECTED_MUSIC_FILE =
            "com.fesskiev.player.SELECTED_MUSIC_FILE";


    public static MusicVKFragment newInstance() {
        return new MusicVKFragment();
    }

    private AudioAdapter audioAdapter;
    private MaterialProgressBar progressBar;
    private List<DownloadVkMusicFile> downloadVkMusicFiles;
    private FloatingActionButton playPauseButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBroadcastReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music_vk, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false, 1000));
        audioAdapter = new AudioAdapter();
        recyclerView.setAdapter(audioAdapter);
        recyclerView.addOnItemTouchListener(new RecycleItemClickListener(getActivity(),
                new OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        if (downloadVkMusicFiles != null) {
                            DownloadVkMusicFile downloadVkMusicFile = downloadVkMusicFiles.get(position);
                            if (downloadVkMusicFile != null) {
                                if (!downloadVkMusicFile.isComplete()) {
                                    downloadVkMusicFile.downloadMusicFile(position);
                                } else {
                                    Intent intent = new Intent(getActivity(), VKPlayerActivity.class);
                                    intent.putExtra(DOWNLOAD_FILE_PATH, downloadVkMusicFile.filePath);
                                    intent.putExtra(SELECTED_MUSIC_FILE, downloadVkMusicFile.vkMusicFile);
                                    startActivity(intent);
                                }
                            }
                        }
                    }
                }));

        recyclerView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                hideViews();
            }

            @Override
            public void onShow() {
                showViews();
            }
        });

        playPauseButton = (FloatingActionButton) view.findViewById(R.id.playPauseButton);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        progressBar = (MaterialProgressBar) view.findViewById(R.id.progressBar);
        showProgressBar();

    }

    private void hideViews() {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) playPauseButton.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        playPauseButton.animate().translationY(playPauseButton.getHeight()
                + fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    private void showViews() {
        playPauseButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void fetchUserAudio() {
        AppSettingsManager manager = new AppSettingsManager(getActivity());
        RESTService.fetchAudio(getActivity(),
                URLHelper.getAudioURL(manager.getAuthToken(), manager.getUserId(), 20, 0));
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(RESTService.ACTION_AUDIO_RESULT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(audioReceiver,
                filter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(audioReceiver);
    }

    private BroadcastReceiver audioReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case RESTService.ACTION_AUDIO_RESULT:
                    List<VKMusicFile> vkMusicFiles =
                            intent.getParcelableArrayListExtra(RESTService.EXTRA_AUDIO_RESULT);
                    if (vkMusicFiles != null) {
                        hideProgressBar();
                        downloadVkMusicFiles = getDownloadVKMusicFiles(vkMusicFiles);
                        audioAdapter.refresh(downloadVkMusicFiles);
                    }
                    break;
            }
        }
    };

    private List<DownloadVkMusicFile> getDownloadVKMusicFiles(List<VKMusicFile> vkMusicFiles) {
        List<DownloadVkMusicFile> downloadVkMusicFiles = new ArrayList<>();
        for (VKMusicFile vkMusicFile : vkMusicFiles) {
            DownloadVkMusicFile downloadVkMusicFile = new DownloadVkMusicFile();
            downloadVkMusicFile.vkMusicFile = vkMusicFile;
            downloadVkMusicFiles.add(downloadVkMusicFile);
        }
        return downloadVkMusicFiles;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBroadcastReceiver();
    }

    private class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ViewHolder> {

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

            public ViewHolder(View v) {
                super(v);

                artist = (TextView) v.findViewById(R.id.itemArtist);
                title = (TextView) v.findViewById(R.id.itemTitle);
                downloadCompleteIcon = (ImageView) v.findViewById(R.id.itemDownloadComplete);
                downloadProgress = (ProgressBar) v.findViewById(R.id.downloadProgressBar);
                progressValue = (TextView) v.findViewById(R.id.progressValue);

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

            DownloadVkMusicFile downloadVkMusicFile = downloadVkMusicFiles.get(position);
            if (downloadVkMusicFile != null) {

                holder.artist.setText(downloadVkMusicFile.vkMusicFile.artist);
                holder.title.setText(downloadVkMusicFile.vkMusicFile.title);


                if (downloadVkMusicFile.downloadStart) {
                    holder.downloadProgress.setVisibility(View.VISIBLE);
                    holder.downloadProgress.setProgress((int) downloadVkMusicFile.progress);
                    holder.progressValue.setVisibility(View.VISIBLE);
                    holder.progressValue.setText(String.valueOf((int) downloadVkMusicFile.progress) + "%");
                } else {
                    holder.downloadProgress.setVisibility(View.INVISIBLE);
                    holder.progressValue.setVisibility(View.GONE);
                }

                if (downloadVkMusicFile.isComplete()) {
                    holder.downloadCompleteIcon.setVisibility(View.VISIBLE);
                } else {
                    holder.downloadCompleteIcon.setVisibility(View.INVISIBLE);
                }
            }
        }

        public void refresh(List<DownloadVkMusicFile> downloadVkMusicFiles) {
            this.downloadVkMusicFiles.clear();
            this.downloadVkMusicFiles.addAll(downloadVkMusicFiles);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return downloadVkMusicFiles.size();
        }
    }

    private class DownloadVkMusicFile implements DownloadFileHelper.OnFileDownloadListener {

        public VKMusicFile vkMusicFile;
        public String filePath;
        public int position;
        public double progress;
        public boolean downloadStart;

        public boolean isComplete() {
            return progress == 100;
        }

        @Override
        public void fileDownloadComplete(String filePath) {
            this.filePath = filePath;
            downloadStart = false;
        }

        @Override
        public void fileDownloadFailed() {

        }

        @Override
        public void fileDownloadProgress(final double progress) {
            this.progress = progress;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (((int) progress) % 10 == 0) {
                        audioAdapter.notifyItemChanged(position);
                    }
                }
            });
        }

        public void downloadMusicFile(int position) {
            this.position = position;
            downloadStart = true;

            String fileName = vkMusicFile.artist + "-" + vkMusicFile.title;
            new DownloadFileHelper(vkMusicFile.url, fileName).setOnFileDownloadListener(this);
        }
    }
}
