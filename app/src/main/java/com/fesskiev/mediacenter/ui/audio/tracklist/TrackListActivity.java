package com.fesskiev.mediacenter.ui.audio.tracklist;

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
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.ui.audio.player.AudioPlayerActivity;
import com.fesskiev.mediacenter.ui.audio.CONTENT_TYPE;
import com.fesskiev.mediacenter.utils.AppAnimationUtils;
import com.fesskiev.mediacenter.utils.AppGuide;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.cards.TrackListCardView;
import com.fesskiev.mediacenter.widgets.dialogs.EditTrackDialog;
import com.fesskiev.mediacenter.widgets.dialogs.SimpleDialog;
import com.fesskiev.mediacenter.widgets.recycleview.HidingScrollListener;
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;;
import io.reactivex.android.schedulers.AndroidSchedulers;;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TrackListActivity extends AnalyticsActivity implements View.OnClickListener {

    public static final String EXTRA_CONTENT_TYPE = "com.fesskiev.player.EXTRA_CONTENT_TYPE";
    public static final String EXTRA_CONTENT_TYPE_VALUE = "com.fesskiev.player.EXTRA_CONTENT_TYPE_VALUE";
    public static final String EXTRA_AUDIO_FOLDER = "com.fesskiev.player.EXTRA_AUDIO_FOLDER";

    private FloatingActionMenu actionMenu;
    private AppGuide appGuide;

    private Disposable subscription;
    private DataRepository repository;

    private AppSettingsManager settingsManager;

    private RecyclerView recyclerView;
    private TrackListAdapter adapter;
    private AudioPlayer audioPlayer;
    private List<TrackListCardView> openCards;

    private AudioFolder audioFolder;
    private CONTENT_TYPE contentType;
    private String contentValue;

    private boolean lastPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);

        audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        repository = MediaApplication.getInstance().getRepository();
        settingsManager = AppSettingsManager.getInstance();
        openCards = new ArrayList<>();

        EventBus.getDefault().register(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            String title = null;
            contentType =
                    (CONTENT_TYPE) getIntent().getSerializableExtra(EXTRA_CONTENT_TYPE);
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
            adapter = new TrackListAdapter();
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
                    closeOpenCards();
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchContentByType();
    }

    @Override
    protected void onPause() {
        super.onPause();
        RxUtils.unsubscribe(subscription);
        if (appGuide != null) {
            appGuide.clear();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menuSortDuration:
                adapter.sortTracks(AudioPlayer.SORT_DURATION);
                break;
            case R.id.menuSortFileSize:
                adapter.sortTracks(AudioPlayer.SORT_FILE_SIZE);
                break;
            case R.id.menuSortTrackNumber:
                adapter.sortTracks(AudioPlayer.SORT_TRACK_NUMBER);
                break;
            case R.id.menuSortTimestamp:
                adapter.sortTracks(AudioPlayer.SORT_TIMESTAMP);
                break;
        }
    }

    private void fetchContentByType() {
        Observable<List<AudioFile>> audioFilesObservable = null;

        switch (contentType) {
            case FOLDERS:
                audioFilesObservable = repository.getAudioTracks(audioFolder.id);
                break;
            case ARTIST:
                audioFilesObservable = repository.getArtistTracks(contentValue);
                break;
            case GENRE:
                audioFilesObservable = repository.getGenreTracks(contentValue);
                break;
        }

        if (audioFilesObservable != null) {
            subscription = audioFilesObservable
                    .firstOrError()
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .flatMap(Observable::fromIterable)
                    .filter(audioFile -> AppSettingsManager.getInstance().isShowHiddenFiles() || !audioFile.isHidden)
                    .toList()
                    .toObservable()
                    .map(unsortedList -> audioPlayer.sortAudioFiles(settingsManager.getSortType(), unsortedList))
                    .doOnNext(sortedList -> audioPlayer.setSortingTrackList(sortedList))
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(audioFiles -> adapter.refreshAdapter(audioFiles))
                    .delay(1000, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(audioFiles -> makeGuideIfNeed());
        }
    }

    private void makeGuideIfNeed() {
        if (settingsManager.isNeedTrackListActivityGuide()) {
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
                        settingsManager.setNeedTrackListActivityGuide(false);
                        closeOpenCards();
                    }
                });
                appGuide.makeGuide(addToPlaylist, getString(R.string.app_guide_add_playlist_title),
                        getString(R.string.app_guide_add_playlist_desc));
            }
        }
    }

    private void notifyTrackStateChanged() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlaybackStateEvent(PlaybackService playbackState) {
        boolean playing = playbackState.isPlaying();
        if (lastPlaying != playing) {
            lastPlaying = playing;
            notifyTrackStateChanged();
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCurrentTrackEvent(AudioFile currentTrack) {
        notifyTrackStateChanged();
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

    private void closeMenu() {
        if (actionMenu.isOpened()) {
            actionMenu.close(true);
        }
    }

    private void hideViews() {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) actionMenu.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        AppAnimationUtils.getInstance().translate(actionMenu, actionMenu.getHeight()
                + fabBottomMargin);

    }

    private void showViews() {
        AppAnimationUtils.getInstance().translate(actionMenu, 0);
    }


    private class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.ViewHolder> {

        private List<AudioFile> audioFiles;

        public TrackListAdapter() {
            this.audioFiles = new ArrayList<>();
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
                                showEditDialog(getAdapterPosition());
                            }

                            @Override
                            public void onClick() {
                                startPlayerActivity(getAdapterPosition());
                            }

                            @Override
                            public void onPlaylistClick() {
                                addToPlaylist(getAdapterPosition());
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

        private void addToPlaylist(int position) {
            AudioFile audioFile = audioFiles.get(position);
            if (audioFile != null) {
                audioFile.inPlayList = true;
                repository.updateAudioFile(audioFile);
                Utils.showCustomSnackbar(getCurrentFocus(),
                        getApplicationContext(),
                        getString(R.string.add_to_playlist_text),
                        Snackbar.LENGTH_SHORT).addCallback(new Snackbar.Callback() {

                    @Override
                    public void onShown(Snackbar snackbar) {
                        super.onShown(snackbar);
                        closeMenu();
                        AppAnimationUtils.getInstance().translateMenu(actionMenu,
                                -snackbar.getView().getHeight());
                    }

                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        AppAnimationUtils.getInstance().translateMenu(actionMenu, 0);
                        closeOpenCards();
                    }
                }).show();
            }
        }

        private void startPlayerActivity(int position) {
            if (position != -1) {
                AudioFile audioFile = audioFiles.get(position);
                if (audioFile != null) {
                    if (audioFile.exists()) {
                        AudioFile selectedTrack = audioPlayer.getCurrentTrack();
                        if (contentType == CONTENT_TYPE.FOLDERS) {
                            MediaApplication.getInstance().getAudioPlayer().setCurrentTrackList(audioFolder, audioFiles);
                        } else {
                            MediaApplication.getInstance().getAudioPlayer().setCurrentTrackList(audioFiles);
                        }
                        if (selectedTrack == null || !selectedTrack.equals(audioFile)) {
                            audioPlayer.setCurrentAudioFileAndPlay(audioFile);
                        }
                        AudioPlayerActivity.startPlayerActivity(TrackListActivity.this);

                    } else {
                        Utils.showCustomSnackbar(getCurrentFocus(),
                                getApplicationContext(), getString(R.string.snackbar_file_not_exist),
                                Snackbar.LENGTH_LONG).addCallback(new Snackbar.Callback() {

                            @Override
                            public void onShown(Snackbar snackbar) {
                                super.onShown(snackbar);
                                closeMenu();
                                AppAnimationUtils.getInstance().translateMenu(actionMenu,
                                        -snackbar.getView().getHeight());
                            }

                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                super.onDismissed(snackbar, event);
                                AppAnimationUtils.getInstance().translateMenu(actionMenu, 0);
                            }
                        }).show();
                    }
                }
            }
        }

        private void showEditDialog(final int position) {
            AudioFile audioFile = audioFiles.get(position);
            if (audioFile != null) {
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

        private void deleteFile(final int position) {
            final AudioFile audioFile = audioFiles.get(position);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.addToBackStack(null);
            SimpleDialog dialog = SimpleDialog.newInstance(getString(R.string.dialog_delete_file_title),
                    getString(R.string.dialog_delete_file_message), R.drawable.icon_trash);
            dialog.show(transaction, SimpleDialog.class.getName());
            dialog.setPositiveListener(() -> {
                if (!audioFile.filePath.exists() || audioFile.filePath.delete()) {
                    Utils.showCustomSnackbar(getCurrentFocus(),
                            getApplicationContext(),
                            getString(R.string.shackbar_delete_audio_file),
                            Snackbar.LENGTH_LONG).addCallback(new Snackbar.Callback() {

                        @Override
                        public void onShown(Snackbar snackbar) {
                            super.onShown(snackbar);
                            closeMenu();
                            AppAnimationUtils.getInstance().translateMenu(actionMenu,
                                    -snackbar.getView().getHeight());
                        }

                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            super.onDismissed(snackbar, event);
                            AppAnimationUtils.getInstance().translateMenu(actionMenu, 0);
                        }
                    }).show();

                    repository.deleteAudioFile(audioFile.getFilePath());
                    adapter.removeItem(position);
                }
            });
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

                BitmapHelper.getInstance().loadTrackListArtwork(audioFile, holder.cover);

                AudioFile selectedTrack = audioPlayer.getCurrentTrack();
                if (selectedTrack != null && selectedTrack.equals(audioFile) && lastPlaying) {
                    holder.playEq.setVisibility(View.VISIBLE);

                    AnimationDrawable animation = (AnimationDrawable) ContextCompat.
                            getDrawable(getApplicationContext(), R.drawable.ic_equalizer);
                    holder.playEq.setImageDrawable(animation);
                    if (animation != null) {
                        if (lastPlaying) {
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

        public void sortTracks(int type) {
            subscription = Observable.just(audioFiles)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(unsortedList -> audioPlayer.sortAudioFiles(type, unsortedList))
                    .doOnNext(sortedList -> audioPlayer.setSortingTrackList(sortedList))
                    .doOnNext(sortedList -> settingsManager.setSortType(type))
                    .doOnNext(sortedList -> actionMenu.close(true))
                    .subscribe(this::refreshAdapter);
        }

    }
}
