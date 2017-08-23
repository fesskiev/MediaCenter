package com.fesskiev.mediacenter.ui.playlist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.MediaFile;
import com.fesskiev.mediacenter.data.model.VideoFile;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.ui.audio.player.AudioPlayerActivity;
import com.fesskiev.mediacenter.ui.video.player.VideoExoPlayerActivity;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.cards.PlayListCardView;
import com.fesskiev.mediacenter.widgets.recycleview.HidingScrollListener;
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PlayListActivity extends AnalyticsActivity {

    private Subscription subscription;
    private DataRepository repository;

    private AudioTracksAdapter adapter;
    private CardView emptyPlaylistCard;
    private Menu menu;

    private List<PlayListCardView> openCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        repository = MediaApplication.getInstance().getRepository();
        openCards = new ArrayList<>();

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.title_playlist_activity));
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        emptyPlaylistCard = findViewById(R.id.emptyPlaylistCard);

        RecyclerView recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false, 1000));
        adapter = new AudioTracksAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {

            }

            @Override
            public void onShow() {

            }

            @Override
            public void onItemPosition(int position) {
                closeOpenCards();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_playlist, menu);
        View actionView = menu.findItem(R.id.action_clear_playlist).getActionView();
        actionView.setOnClickListener(v -> clearPlaylist());
        hideMenu();

        fetchPlayListFiles();
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
    }


    private void clearPlaylist() {
        repository.clearPlaylist();
        adapter.clearAdapter();
        showEmptyCardPlaylist();
        hideMenu();
    }

    private void closeOpenCards() {
        if (!openCards.isEmpty()) {
            for (PlayListCardView cardView : openCards) {
                if (cardView.isOpen()) {
                    cardView.close();
                }
            }
            openCards.clear();
        }
    }

    public void fetchPlayListFiles() {
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
                    adapter.refreshAdapter(mediaFiles);
                    if (!mediaFiles.isEmpty()) {
                        hideEmptyCardPlaylist();
                        showMenu();
                    } else {
                        showEmptyCardPlaylist();
                        hideMenu();
                    }
                });
    }

    private void hideMenu() {
        menu.findItem(R.id.action_clear_playlist).setVisible(false);
    }

    private void showMenu() {
        menu.findItem(R.id.action_clear_playlist).setVisible(true);
    }

    public DataRepository getRepository() {
        return repository;
    }

    public List<PlayListCardView> getOpenCards() {
        return openCards;
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

                cover = v.findViewById(R.id.itemCover);
                duration = v.findViewById(R.id.itemDuration);
                title = v.findViewById(R.id.itemTitle);
                filePath = v.findViewById(R.id.itemPath);
                filePath.setSelected(true);

                ((PlayListCardView) v).setOnPlayListCardListener(
                        new PlayListCardView.OnPlayListCardListener() {
                            @Override
                            public void onDeleteClick() {
                                removeFromPlaylist(getAdapterPosition());
                            }

                            @Override
                            public void onClick() {
                                startPlayerActivity(getAdapterPosition());
                            }

                            @Override
                            public void onAnimateChanged(PlayListCardView cardView, boolean open) {
                                PlayListActivity act = (PlayListActivity) activity.get();
                                if (act != null) {
                                    if (open) {
                                        act.getOpenCards().add(cardView);
                                    } else {
                                        act.getOpenCards().remove(cardView);
                                    }
                                }
                            }
                        });
            }
        }

        @Override
        public AudioTracksAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_play_list, parent, false);

            return new AudioTracksAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(AudioTracksAdapter.ViewHolder holder, int position) {

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
            mediaFiles.clear();
            mediaFiles.addAll(newMediaFiles);
            notifyDataSetChanged();
        }

        public void clearAdapter() {
            mediaFiles.clear();
            notifyDataSetChanged();
        }

        private void removeFromPlaylist(int position) {
            PlayListActivity act = (PlayListActivity) activity.get();
            if (act != null) {
                MediaFile mediaFile = mediaFiles.get(position);
                if (mediaFile != null) {
                    mediaFile.setToPlayList(false);
                    DataRepository repository = act.getRepository();
                    switch (mediaFile.getMediaType()) {
                        case VIDEO:
                            repository.updateVideoFile((VideoFile) mediaFile);
                            break;
                        case AUDIO:
                            repository.updateAudioFile((AudioFile) mediaFile);
                            break;
                    }
                    act.fetchPlayListFiles();
                }
            }
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
