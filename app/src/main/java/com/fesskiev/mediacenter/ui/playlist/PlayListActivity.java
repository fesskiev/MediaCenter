package com.fesskiev.mediacenter.ui.playlist;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.ui.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.MediaFile;
import com.fesskiev.mediacenter.ui.audio.player.AudioPlayerActivity;
import com.fesskiev.mediacenter.ui.video.player.VideoExoPlayerActivity;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.cards.PlayListCardView;
import com.fesskiev.mediacenter.widgets.recycleview.HidingScrollListener;
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class PlayListActivity extends AnalyticsActivity {

    private AudioTracksAdapter adapter;
    private CardView emptyPlaylistCard;
    private Menu menu;

    private List<PlayListCardView> openCards;

    private PlayListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.title_playlist_activity));
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        emptyPlaylistCard = findViewById(R.id.emptyPlaylistCard);
        openCards = new ArrayList<>();
        setupRecyclerView();
        observeData();
    }

    private void setupRecyclerView() {
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

    private void observeData() {
        viewModel = ViewModelProviders.of(this).get(PlayListViewModel.class);
        viewModel.getPlayListFilesLiveData().observe(this, this::setPlayListFiles);
        viewModel.getEmptyPlaListLiveData().observe(this, Void -> setEmptyPlayList());
        viewModel.getNotExistsMediaFileLiveData().observe(this, Void -> showMediaFileNotExistsSnackBar());
    }

    private void setPlayListFiles(List<MediaFile> mediaFiles) {
        adapter.refreshAdapter(mediaFiles);
        hideEmptyCardPlaylist();
        showMenu();
    }

    private void setEmptyPlayList() {
        adapter.clearAdapter();
        showEmptyCardPlaylist();
        hideMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_playlist, menu);
        View actionView = menu.findItem(R.id.action_clear_playlist).getActionView();
        actionView.setOnClickListener(v -> viewModel.clearPlaylist());
        hideMenu();

        viewModel.fetchPlayListFiles();
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

    private Observable<Bitmap> getPlayListArtwork(MediaFile mediaFile) {
        return viewModel.getPlayListArtwork(mediaFile);
    }

    private void removeFromPlayList(MediaFile mediaFile) {
        viewModel.removeFromPlayList(mediaFile);
    }

    private void startPlayerActivity(MediaFile mediaFile) {
        if (viewModel.checkMediaFileExist(mediaFile)) {
            switch (mediaFile.getMediaType()) {
                case VIDEO:
                    Intent intent = new Intent(this, VideoExoPlayerActivity.class);
                    intent.putExtra(VideoExoPlayerActivity.URI_EXTRA, mediaFile.getFilePath());
                    intent.putExtra(VideoExoPlayerActivity.VIDEO_NAME_EXTRA, mediaFile.getFileName());
                    intent.setAction(VideoExoPlayerActivity.ACTION_VIEW_URI);
                    startActivity(intent);
                    break;
                case AUDIO:
                    viewModel.setCurrentAudioFileAndPlay((AudioFile) mediaFile);
                    AudioPlayerActivity.startPlayerActivity(this);
                    break;
            }
        }
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

    private void showMediaFileNotExistsSnackBar() {
        Utils.showCustomSnackbar(getCurrentFocus(), getApplicationContext(),
                getString(R.string.snackbar_file_not_exist), Snackbar.LENGTH_LONG).show();
    }

    public List<PlayListCardView> getOpenCards() {
        return openCards;
    }

    private void hideMenu() {
        menu.findItem(R.id.action_clear_playlist).setVisible(false);
    }

    private void showMenu() {
        menu.findItem(R.id.action_clear_playlist).setVisible(true);
    }

    private void showEmptyCardPlaylist() {
        emptyPlaylistCard.setVisibility(View.VISIBLE);
    }

    private void hideEmptyCardPlaylist() {
        emptyPlaylistCard.setVisibility(View.GONE);
    }

    private static class AudioTracksAdapter extends RecyclerView.Adapter<AudioTracksAdapter.ViewHolder> {

        private WeakReference<PlayListActivity> activity;
        private List<MediaFile> mediaFiles;

        public AudioTracksAdapter(PlayListActivity activity) {
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
                                removeFromPlayList(getAdapterPosition());
                            }

                            @Override
                            public void onClick() {
                                startPlayerActivity(getAdapterPosition());
                            }

                            @Override
                            public void onAnimateChanged(PlayListCardView cardView, boolean open) {
                                PlayListActivity act = activity.get();
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

        private void removeFromPlayList(int position) {
            PlayListActivity act = activity.get();
            if (act != null) {
                MediaFile mediaFile = mediaFiles.get(position);
                if (mediaFile != null) {
                    act.removeFromPlayList(mediaFile);
                }
            }
        }

        private void startPlayerActivity(int position) {
            PlayListActivity act = activity.get();
            if (act != null) {
                MediaFile mediaFile = mediaFiles.get(position);
                if (mediaFile != null) {
                    act.startPlayerActivity(mediaFile);
                }
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
                switch (mediaFile.getMediaType()) {
                    case VIDEO:
                        holder.duration.setText(Utils.getVideoFileTimeFormat(mediaFile.getDuration()));
                        break;
                    case AUDIO:
                        holder.duration.setText(Utils.getDurationString(mediaFile.getDuration()));
                        break;
                }
                holder.title.setText(mediaFile.getTitle());
                holder.filePath.setText(mediaFile.getFilePath());

                PlayListActivity act = activity.get();
                if (act != null) {
                    act.getPlayListArtwork(mediaFile).subscribe(bitmap -> holder.cover.setImageBitmap(bitmap));
                }
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
    }


}
