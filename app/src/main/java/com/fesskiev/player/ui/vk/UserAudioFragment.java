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
import com.fesskiev.player.model.vk.VKMusicFile;
import com.fesskiev.player.services.RESTService;
import com.fesskiev.player.utils.AppSettingsManager;
import com.fesskiev.player.utils.Download;
import com.fesskiev.player.utils.http.URLHelper;
import com.fesskiev.player.widgets.MaterialProgressBar;
import com.fesskiev.player.widgets.recycleview.OnItemClickListener;
import com.fesskiev.player.widgets.recycleview.RecycleItemClickListener;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;
import com.fesskiev.player.widgets.utils.HidingScrollListener;

import java.util.ArrayList;
import java.util.List;


public class UserAudioFragment extends Fragment {

    private static final String TAG = MusicVKFragment.class.getSimpleName();

    public static UserAudioFragment newInstance() {
        return new UserAudioFragment();
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
        return inflater.inflate(R.layout.fragment_user_audio, container, false);
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
                                if (downloadVkMusicFile.download == null) {
                                    Log.d(TAG, "download file");
                                    downloadVkMusicFile.downloadMusicFile(position);
                                } else {
//                                    Intent intent = new Intent(getActivity(), PlayerActivity.class);
//                                    intent.putExtra(Constants.EXTRA_FOLDER_POSITION, );
//                                    intent.putExtra(Constants.EXTRA_FILE_POSITION, );
//                                    startActivity(intent);
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

                if(vkMusicFile.download != null ){
                    Download download = vkMusicFile.download;
                    switch (download.getStatus()){
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
            this.downloadVkMusicFiles.clear();
            this.downloadVkMusicFiles.addAll(downloadVkMusicFiles);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return downloadVkMusicFiles.size();
        }
    }

    private class DownloadVkMusicFile implements Download.OnDownloadListener {

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
