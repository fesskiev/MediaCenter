package com.fesskiev.mediacenter.ui.audio.tracklist;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.ui.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.ui.audio.player.AudioPlayerActivity;
import com.fesskiev.mediacenter.ui.audio.CONTENT_TYPE;
import com.fesskiev.mediacenter.utils.AppAnimationUtils;
import com.fesskiev.mediacenter.utils.AppGuide;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.cards.TrackListCardView;
import com.fesskiev.mediacenter.widgets.dialogs.EditTrackDialog;
import com.fesskiev.mediacenter.widgets.dialogs.SimpleDialog;
import com.fesskiev.mediacenter.widgets.recycleview.HidingScrollListener;
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

public class TrackListActivity extends AnalyticsActivity implements View.OnClickListener {

    public static final String EXTRA_CONTENT_TYPE = "com.fesskiev.player.EXTRA_CONTENT_TYPE";
    public static final String EXTRA_CONTENT_TYPE_VALUE = "com.fesskiev.player.EXTRA_CONTENT_TYPE_VALUE";
    public static final String EXTRA_AUDIO_FOLDER = "com.fesskiev.player.EXTRA_AUDIO_FOLDER";

    @Inject
    AppAnimationUtils animationUtils;

    private AudioFolder audioFolder;

    private AppGuide appGuide;
    private FloatingActionMenu actionMenu;
    private RecyclerView recyclerView;
    private TrackListAdapter adapter;

    private CONTENT_TYPE contentType;
    private String contentValue;

    private TrackListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);
        MediaApplication.getInstance().getAppComponent().inject(this);

        contentType =
                (CONTENT_TYPE) getIntent().getSerializableExtra(EXTRA_CONTENT_TYPE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            String title = null;
            switch (contentType) {
                case GENRE:
                case ARTIST:
                    title = getIntent().getExtras().getString(EXTRA_CONTENT_TYPE_VALUE);
                    contentValue = title;
                    break;
                case FOLDERS:
                    audioFolder = getIntent().getExtras().getParcelable(EXTRA_AUDIO_FOLDER);
                    if (audioFolder != null) {
                        title = audioFolder.folderName;
                    }
                    break;
            }

            toolbar.setTitle(title);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        actionMenu = findViewById(R.id.menuSorting);
        actionMenu.setIconAnimated(true);

        FloatingActionButton[] sortButtons = new FloatingActionButton[]{
                findViewById(R.id.menuSortDuration),
                findViewById(R.id.menuSortFileSize),
                findViewById(R.id.menuSortTrackNumber),
                findViewById(R.id.menuSortTimestamp)
        };

        for (FloatingActionButton sortButton : sortButtons) {
            sortButton.setOnClickListener(this);
        }

        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false, 1000));
        adapter = new TrackListAdapter(this);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                hideViews();
            }

            @Override
            public void onShow() {
                showViews();
            }

            @Override
            public void onItemPosition(int position) {
                adapter.closeOpenCards();
            }
        });

        observeData();
    }

    private void observeData() {
        viewModel = ViewModelProviders.of(this).get(TrackListViewModel.class);
        viewModel.fetchContentByType(contentType, contentValue, audioFolder);
        viewModel.getTrackListLiveData().observe(this, audioFiles -> adapter.refreshAdapter(audioFiles));
        viewModel.getPlayingLiveData().observe(this, playing -> adapter.setPlaying(playing));
        viewModel.getCurrentTrackLiveData().observe(this, currentTrack -> adapter.setCurrentTrack(currentTrack));
        viewModel.getAddToPlayListAudioFileLiveData().observe(this, Void -> showAddAudioFileToPlayListSnackBar());
        viewModel.getNotExistsAudioFileLiveData().observe(this, Void -> showAudioFileNotExistsSnackBar());
        viewModel.getDeletedAudioFileLiveData().observe(this, position -> {
            adapter.removeItem(position);
            showAudioFileDeletedSnackBar();
        });
    }

    private void addToAudioFilePlayList(AudioFile audioFile) {
        viewModel.updateAudioFile(audioFile);
    }

    private void showEditAudioFileDialog(AudioFile audioFile, int position) {
        if (viewModel.checkAudioFileExist(audioFile)) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.addToBackStack(null);
            EditTrackDialog dialog = EditTrackDialog.newInstance(audioFile);
            dialog.show(transaction, EditTrackDialog.class.getName());
            dialog.setOnEditTrackChangedListener(new EditTrackDialog.OnEditTrackChangedListener() {
                @Override
                public void onEditTrackChanged(AudioFile audioFile) {
                    adapter.updateItem(position, audioFile);
                }

                @Override
                public void onEditTrackError() {

                }
            });
        }
    }

    private void deleteAudioFile(AudioFile audioFile, int position) {
        if (viewModel.checkAudioFileExist(audioFile)) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.addToBackStack(null);
            SimpleDialog dialog = SimpleDialog.newInstance(getString(R.string.dialog_delete_file_title),
                    getString(R.string.dialog_delete_file_message), R.drawable.icon_trash);
            dialog.show(transaction, SimpleDialog.class.getName());
            dialog.setPositiveListener(() -> viewModel.deleteAudioFile(audioFile, position));
        }
    }

    private void startAudioPlayerActivity(AudioFile audioFile, AudioFile currentTrack, List<AudioFile> audioFiles) {
        if (viewModel.checkAudioFileExist(audioFile)) {
            if (contentType == CONTENT_TYPE.FOLDERS) {
                viewModel.setCurrentTrackList(audioFolder, audioFiles);
            } else {
                viewModel.setCurrentTrackList(null, audioFiles);
            }
            if (currentTrack == null || !currentTrack.equals(audioFile)) {
                viewModel.setCurrentAudioFileAndPlay(audioFile);
            }
            AudioPlayerActivity.startPlayerActivity(TrackListActivity.this);
        }
    }

    private void showAudioFileDeletedSnackBar() {
        Utils.showCustomSnackbar(getCurrentFocus(),
                getApplicationContext(),
                getString(R.string.shackbar_delete_audio_file),
                Snackbar.LENGTH_LONG).addCallback(new Snackbar.Callback() {

            @Override
            public void onShown(Snackbar snackbar) {
                super.onShown(snackbar);
                closeMenu();
                animationUtils.translateMenu(actionMenu, -snackbar.getView().getHeight());
            }

            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                animationUtils.translateMenu(actionMenu, 0);
            }
        }).show();
    }

    private void showAudioFileNotExistsSnackBar() {
        Utils.showCustomSnackbar(getCurrentFocus(),
                getApplicationContext(), getString(R.string.snackbar_file_not_exist),
                Snackbar.LENGTH_LONG).addCallback(new Snackbar.Callback() {

            @Override
            public void onShown(Snackbar snackbar) {
                super.onShown(snackbar);
                closeMenu();
                animationUtils.translateMenu(actionMenu, -snackbar.getView().getHeight());
            }

            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                animationUtils.translateMenu(actionMenu, 0);
            }
        }).show();
    }

    private Observable<Bitmap> getTrackListArtwork(AudioFile audioFile) {
        return viewModel.getTrackListArtwork(audioFile);
    }

    private void showAddAudioFileToPlayListSnackBar() {
        Utils.showCustomSnackbar(getCurrentFocus(),
                getApplicationContext(),
                getString(R.string.add_to_playlist_text),
                Snackbar.LENGTH_SHORT).addCallback(new Snackbar.Callback() {

            @Override
            public void onShown(Snackbar snackbar) {
                super.onShown(snackbar);
                closeMenu();
                animationUtils.translateMenu(actionMenu, -snackbar.getView().getHeight());
            }

            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                animationUtils.translateMenu(actionMenu, 0);
                adapter.closeOpenCards();
            }
        }).show();
    }


    @Override
    protected void onStart() {
        super.onStart();
        recyclerView.postDelayed(this::makeGuideIfNeed, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (appGuide != null) {
            appGuide.clear();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menuSortDuration:
                viewModel.sortTracks(AudioPlayer.SORT_DURATION);
                break;
            case R.id.menuSortFileSize:
                viewModel.sortTracks(AudioPlayer.SORT_FILE_SIZE);
                break;
            case R.id.menuSortTrackNumber:
                viewModel.sortTracks(AudioPlayer.SORT_TRACK_NUMBER);
                break;
            case R.id.menuSortTimestamp:
                viewModel.sortTracks(AudioPlayer.SORT_TIMESTAMP);
                break;
        }
        actionMenu.close(true);
    }


    private void makeGuideIfNeed() {
        if (viewModel.isNeedTrackListActivityGuide()) {
            TrackListAdapter.ViewHolder viewHolder
                    = (TrackListAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(0);
            if (viewHolder != null) {
                TrackListCardView slidingCardView = (TrackListCardView) viewHolder.itemView;
                slidingCardView.open();

                final View addToPlaylist = slidingCardView.findViewById(R.id.addPlaylistButton);
                final View editButton = slidingCardView.findViewById(R.id.editButton);
                final View deleteButton = slidingCardView.findViewById(R.id.deleteButton);

                appGuide = new AppGuide(this, 4);
                appGuide.OnAppGuideListener(new AppGuide.OnAppGuideListener() {
                    @Override
                    public void next(int count) {
                        switch (count) {
                            case 1:
                                appGuide.makeGuide(deleteButton,
                                        getString(R.string.app_guide_delete_title),
                                        getString(R.string.app_guide_delete_desc));
                                break;
                            case 2:
                                appGuide.makeGuide(editButton,
                                        getString(R.string.app_guide_edit_title),
                                        getString(R.string.app_guide_edit_desc));
                                break;
                            case 3:
                                appGuide.makeGuide(actionMenu.getMenuIconView(),
                                        getString(R.string.app_guide_sorting_title),
                                        getString(R.string.app_guide_sorting_desc));
                                break;
                        }
                    }

                    @Override
                    public void watched() {
                        viewModel.setNeedTrackListActivityGuide(false);
                        adapter.closeOpenCards();
                    }
                });
                appGuide.makeGuide(addToPlaylist, getString(R.string.app_guide_add_playlist_title),
                        getString(R.string.app_guide_add_playlist_desc));
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }


    private void closeMenu() {
        if (actionMenu.isOpened()) {
            actionMenu.close(true);
        }
    }

    private void hideViews() {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) actionMenu.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        animationUtils.translate(actionMenu, actionMenu.getHeight()
                + fabBottomMargin);

    }

    private void showViews() {
        animationUtils.translate(actionMenu, 0);
    }


    private static class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.ViewHolder> {

        private WeakReference<TrackListActivity> activity;
        private List<AudioFile> audioFiles;
        private List<TrackListCardView> openCards;
        private AudioFile currentTrack;
        private boolean playing;

        public TrackListAdapter(TrackListActivity activity) {
            this.activity = new WeakReference<>(activity);
            this.audioFiles = new ArrayList<>();
            this.openCards = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView duration;
            TextView size;
            TextView title;
            TextView filePath;
            ImageView cover;
            ImageView playEq;

            public ViewHolder(final View v) {
                super(v);

                duration = v.findViewById(R.id.itemDuration);
                size = v.findViewById(R.id.itemSize);
                title = v.findViewById(R.id.itemTitle);
                filePath = v.findViewById(R.id.filePath);
                cover = v.findViewById(R.id.itemCover);
                playEq = v.findViewById(R.id.playEq);

                ((TrackListCardView) v).
                        setOnTrackListCardListener(new TrackListCardView.OnTrackListCardListener() {
                            @Override
                            public void onDeleteClick() {
                                deleteFile(getAdapterPosition());
                            }

                            @Override
                            public void onEditClick() {
                                showEditAudioFileDialog(getAdapterPosition());
                            }

                            @Override
                            public void onClick() {
                                startPlayerActivity(getAdapterPosition());
                            }

                            @Override
                            public void onPlayListClick() {
                                addAudioFileToPlaylist(getAdapterPosition());
                            }

                            @Override
                            public void onAnimateChanged(TrackListCardView cardView, boolean open) {
                                if (open) {
                                    openCards.add(cardView);
                                } else {
                                    openCards.remove(cardView);
                                }
                            }
                        });
            }

        }

        private void addAudioFileToPlaylist(int position) {
            TrackListActivity act = activity.get();
            if (act != null) {
                AudioFile audioFile = audioFiles.get(position);
                if (audioFile != null) {
                    act.addToAudioFilePlayList(audioFile);
                }
            }
        }

        private void startPlayerActivity(int position) {
            TrackListActivity act = activity.get();
            if (act != null) {
                AudioFile audioFile = audioFiles.get(position);
                if (audioFile != null) {
                    act.startAudioPlayerActivity(audioFile, currentTrack, audioFiles);
                }
            }
        }

        private void showEditAudioFileDialog(final int position) {
            TrackListActivity act = activity.get();
            if (act != null) {
                AudioFile audioFile = audioFiles.get(position);
                if (audioFile != null) {
                    act.showEditAudioFileDialog(audioFile, position);
                }
            }
        }

        private void deleteFile(final int position) {
            TrackListActivity act = activity.get();
            if (act != null) {
                AudioFile audioFile = audioFiles.get(position);
                if (audioFile != null) {
                    act.deleteAudioFile(audioFile, position);
                }
            }
        }

        @Override
        public TrackListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_track_list, parent, false);

            return new TrackListAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(TrackListAdapter.ViewHolder holder, int position) {

            AudioFile audioFile = audioFiles.get(position);
            if (audioFile != null) {

                holder.duration.setText(Utils.getDurationString(audioFile.length));
                holder.size.setText(Utils.humanReadableByteCount(audioFile.size, false));
                holder.title.setText(audioFile.title);
                holder.filePath.setText(audioFile.filePath.getName());

                TrackListActivity act = activity.get();
                if (act != null) {
                    act.getTrackListArtwork(audioFile)
                            .subscribe(bitmap -> holder.cover.setImageBitmap(bitmap));
                }
                if (currentTrack != null && currentTrack.equals(audioFile) && playing) {
                    holder.playEq.setVisibility(View.VISIBLE);

                    AnimationDrawable animation = (AnimationDrawable) ContextCompat.
                            getDrawable(holder.itemView.getContext(), R.drawable.ic_equalizer);
                    holder.playEq.setImageDrawable(animation);
                    if (animation != null) {
                        if (playing) {
                            animation.start();
                        } else {
                            animation.stop();
                        }
                    }
                } else {
                    holder.playEq.setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return audioFiles.size();
        }

        @Override
        public long getItemId(int position) {
            AudioFile audioFile = audioFiles.get(position);
            if (audioFile != null) {
                return audioFile.timestamp;
            }
            return super.getItemId(position);
        }

        public void refreshAdapter(List<AudioFile> receiverAudioFiles) {
            audioFiles.clear();
            audioFiles.addAll(receiverAudioFiles);
            notifyDataSetChanged();
        }

        public void removeItem(int position) {
            audioFiles.remove(position);
            notifyItemRemoved(position);
        }

        public void updateItem(int position, AudioFile audioFile) {
            audioFiles.set(position, audioFile);
            notifyItemChanged(position);
        }

        private void closeOpenCards() {
            if (!openCards.isEmpty()) {
                for (TrackListCardView cardView : openCards) {
                    if (cardView.isOpen()) {
                        cardView.close();
                    }
                }
                openCards.clear();
            }
        }

        public void setPlaying(boolean playing) {
            this.playing = playing;
            notifyDataSetChanged();
        }

        public void setCurrentTrack(AudioFile currentTrack) {
            this.currentTrack = currentTrack;
            notifyDataSetChanged();
        }
    }
}
