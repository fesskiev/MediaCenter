package com.fesskiev.player.ui.playlist;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.db.MediaDataSource;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.model.MediaFile;
import com.fesskiev.player.ui.audio.player.AudioPlayerActivity;
import com.fesskiev.player.utils.AppLog;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.RxUtils;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;


public class PlayListFragment extends Fragment {

    public static PlayListFragment newInstance() {
        return new PlayListFragment();
    }

    private Subscription subscription;
    private AudioTracksAdapter adapter;
    private CardView emptyPlaylistCard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emptyPlaylistCard = (CardView) view.findViewById(R.id.emptyPlaylistCard);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false, 1000));
        adapter = new AudioTracksAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.menu_clear_playlist).setOnClickListener(v -> {

            showEmptyCardPlaylist();

            MediaApplication.getInstance().getMediaDataSource().clearPlaylist();
            adapter.clearAdapter();
        });

        fetchPLayListFiles();
    }

    private void fetchPLayListFiles() {
        MediaDataSource dataSource = MediaApplication.getInstance().getMediaDataSource();
        subscription = Observable.concat(
                dataSource.getAudioFilePlaylistFromDB(),
                dataSource.getVideoFilePlaylistFromDB())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mediaFiles -> {
                    AppLog.DEBUG("size: " + mediaFiles.size());
                    if (!mediaFiles.isEmpty()) {
                        adapter.refreshAdapter(mediaFiles);
                        hideEmptyCardPlaylist();
                    } else {
                        showEmptyCardPlaylist();
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
    }


    private void showEmptyCardPlaylist() {
        emptyPlaylistCard.setVisibility(View.VISIBLE);
    }

    private void hideEmptyCardPlaylist() {
        emptyPlaylistCard.setVisibility(View.GONE);
    }


    private static class AudioTracksAdapter extends RecyclerView.Adapter<AudioTracksAdapter.ViewHolder> {

        private WeakReference<Activity> activity;
        private List<MediaFile> mediaFiles;

        public AudioTracksAdapter(Activity activity) {
            this.activity = new WeakReference<>(activity);
            this.mediaFiles = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView duration;
            TextView title;
            TextView filePath;
            ImageView cover;

            public ViewHolder(View v) {
                super(v);
                v.setOnClickListener(v1 -> startPlayerActivity(getAdapterPosition(), cover));

                duration = (TextView) v.findViewById(R.id.itemDuration);
                title = (TextView) v.findViewById(R.id.itemTitle);
                filePath = (TextView) v.findViewById(R.id.filePath);
                cover = (ImageView) v.findViewById(R.id.itemCover);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_audio_track, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            MediaFile mediaFile = mediaFiles.get(position);
            if (mediaFile != null) {
                Activity act = activity.get();
                if (act != null) {
                    BitmapHelper.loadTrackListArtwork(act, mediaFile, holder.cover);
                }

                holder.duration.setText(Utils.getDurationString(mediaFile.getLength()));
                holder.title.setText(mediaFile.getTitle());
                holder.filePath.setText(mediaFile.getFilePath());
            }
        }

        @Override
        public int getItemCount() {
            return mediaFiles.size();
        }

        public void refreshAdapter(List<MediaFile> newMediaFiles) {
            mediaFiles.addAll(newMediaFiles);
            notifyDataSetChanged();
        }

        public void clearAdapter() {
            mediaFiles.clear();
            notifyDataSetChanged();
        }

        private void startPlayerActivity(int position, View cover) {
            MediaFile mediaFile = mediaFiles.get(position);
            if (mediaFile != null) {
                switch (mediaFile.getMediaType()) {
                    case VIDEO:
                        break;
                    case AUDIO:
                        Activity act = activity.get();
                        if (act != null) {
                            AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();
                            audioPlayer.setCurrentAudioFile((AudioFile) mediaFile);
                            audioPlayer.position = position;
                            AudioPlayerActivity.startPlayerActivity(act, true, cover);
                        }
                        break;
                }
            }
        }
    }
}
