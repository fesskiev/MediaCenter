package com.fesskiev.mediacenter.ui.playlist;


import android.app.Activity;
import android.content.Intent;
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

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.MediaFile;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.ui.audio.player.AudioPlayerActivity;
import com.fesskiev.mediacenter.ui.video.player.VideoExoPlayerActivity;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;
import com.github.clans.fab.FloatingActionMenu;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class PlayListFragment extends Fragment {

    public static PlayListFragment newInstance() {
        return new PlayListFragment();
    }

    private Subscription subscription;
    private AudioTracksAdapter adapter;
    private FloatingActionMenu actionMenu;
    private CardView emptyPlaylistCard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        actionMenu = (FloatingActionMenu) view.findViewById(R.id.menuPlaylist);
        actionMenu.setIconAnimated(true);
        emptyPlaylistCard = (CardView) view.findViewById(R.id.emptyPlaylistCard);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false, 1000));
        adapter = new AudioTracksAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.menuClearPlaylist).setOnClickListener(v -> {

            showEmptyCardPlaylist();

            MediaApplication.getInstance().getRepository().clearPlaylist();
            adapter.clearAdapter();
            actionMenu.hideMenu(true);
        });

        fetchPlayListFiles();
    }

    private void fetchPlayListFiles() {
        DataRepository repository = MediaApplication.getInstance().getRepository();
        subscription = Observable.zip(repository.getAudioFilePlaylist(),
                repository.getVideoFilePlaylist(),
                (audioFiles, videoFiles) -> {
                    List<MediaFile> mediaFiles = new ArrayList<>();
                    mediaFiles.addAll(audioFiles);
                    mediaFiles.addAll(videoFiles);
                    return mediaFiles;
                })
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mediaFiles -> {
                    AppLog.DEBUG("playlist size: " + mediaFiles.size());
                    if (!mediaFiles.isEmpty()) {
                        adapter.refreshAdapter(mediaFiles);
                        hideEmptyCardPlaylist();
                    } else {
                        showEmptyCardPlaylist();
                        actionMenu.hideMenu(true);
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
                v.setOnClickListener(v1 -> startPlayerActivity(getAdapterPosition()));

                cover = (ImageView) v.findViewById(R.id.itemCover);
                duration = (TextView) v.findViewById(R.id.itemDuration);
                title = (TextView) v.findViewById(R.id.itemTitle);
                filePath = (TextView) v.findViewById(R.id.itemPath);
                filePath.setSelected(true);

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

                BitmapHelper.getInstance().loadTrackListArtwork(mediaFile, holder.cover);

                switch (mediaFile.getMediaType()) {
                    case VIDEO:
                        holder.duration.setText(Utils.getVideoFileTimeFormat(mediaFile.getLength()));
                        break;
                    case AUDIO:
                        holder.duration.setText(Utils.getDurationString(mediaFile.getLength()));
                        break;
                }
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

        private void startPlayerActivity(int position) {
            Activity act = activity.get();
            if (act != null) {
                MediaFile mediaFile = mediaFiles.get(position);
                if (mediaFile != null) {
                    switch (mediaFile.getMediaType()) {
                        case VIDEO:
                            Intent intent = new Intent(act, VideoExoPlayerActivity.class);
                            intent.putExtra(VideoExoPlayerActivity.URI_EXTRA, mediaFile.getFilePath());
                            intent.putExtra(VideoExoPlayerActivity.VIDEO_NAME_EXTRA, mediaFile.getFileName());
                            intent.setAction(VideoExoPlayerActivity.ACTION_VIEW_URI);
                            act.startActivity(intent);
                            break;
                        case AUDIO:
                            AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();
                            audioPlayer.setCurrentAudioFileAndPlay((AudioFile) mediaFile);
                            AudioPlayerActivity.startPlayerActivity(act);
                            break;
                    }
                }
            }
        }
    }
}
