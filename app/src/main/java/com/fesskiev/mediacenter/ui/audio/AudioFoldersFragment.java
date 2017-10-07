package com.fesskiev.mediacenter.ui.audio;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.ui.audio.tracklist.TrackListActivity;
import com.fesskiev.mediacenter.ui.playback.HidingPlaybackFragment;
import com.fesskiev.mediacenter.ui.playback.PlaybackActivity;
import com.fesskiev.mediacenter.ui.search.AlbumSearchActivity;
import com.fesskiev.mediacenter.utils.AppAnimationUtils;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.dialogs.AudioFolderDetailsDialog;
import com.fesskiev.mediacenter.widgets.dialogs.MediaFolderDetailsDialog;
import com.fesskiev.mediacenter.widgets.dialogs.SimpleDialog;
import com.fesskiev.mediacenter.widgets.item.AudioCardView;
import com.fesskiev.mediacenter.widgets.menu.FolderContextMenu;
import com.fesskiev.mediacenter.widgets.menu.ContextMenuManager;
import com.fesskiev.mediacenter.widgets.recycleview.HidingScrollListener;
import com.fesskiev.mediacenter.widgets.recycleview.ItemOffsetDecoration;
import com.fesskiev.mediacenter.widgets.recycleview.helper.ItemTouchHelperAdapter;
import com.fesskiev.mediacenter.widgets.recycleview.helper.ItemTouchHelperViewHolder;
import com.fesskiev.mediacenter.widgets.recycleview.helper.SimpleItemTouchHelperCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;;
import io.reactivex.android.schedulers.AndroidSchedulers;;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.fesskiev.mediacenter.ui.audio.tracklist.TrackListActivity.EXTRA_AUDIO_FOLDER;
import static com.fesskiev.mediacenter.ui.audio.tracklist.TrackListActivity.EXTRA_CONTENT_TYPE;


public class AudioFoldersFragment extends HidingPlaybackFragment implements AudioContent {

    public static AudioFoldersFragment newInstance() {
        return new AudioFoldersFragment();
    }

    private Disposable subscription;
    private DataRepository repository;

    private AudioFoldersAdapter adapter;
    private RecyclerView recyclerView;
    private CardView emptyAudioContent;
    private boolean layoutAnimate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = MediaApplication.getInstance().getRepository();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_folders, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);

        final int spacing = getResources().getDimensionPixelOffset(R.dimen.default_spacing_small);

        recyclerView = view.findViewById(R.id.foldersGridView);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new AudioFoldersAdapter(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new ItemOffsetDecoration(spacing));

        recyclerView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                hidePlaybackControl();
            }

            @Override
            public void onShow() {
                showPlaybackControl();
            }

            @Override
            public void onItemPosition(int position) {

            }
        });


        emptyAudioContent = view.findViewById(R.id.emptyAudioContentCard);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        fetch();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (repository.getMemorySource().isCacheFoldersDirty()) {
            fetch();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        RxUtils.unsubscribe(subscription);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        repository.getMemorySource().setCacheFoldersDirty(true);
    }


    @Override
    public void fetch() {
        subscription = repository.getAudioFolders()
                .firstOrError()
                .toObservable()
                .subscribeOn(Schedulers.io())
                .flatMap(Observable::fromIterable)
                .filter(folder -> AppSettingsManager.getInstance().isShowHiddenFiles() || !folder.isHidden)
                .toList()
                .toObservable()
                .flatMap(audioFolders -> {
                    if (audioFolders != null) {
                        AppLog.INFO("onNext:folders: " + audioFolders.size());
                        if (!audioFolders.isEmpty()) {
                            Collections.sort(audioFolders);
                        }
                    }
                    return audioFolders != null ? Observable.just(audioFolders) : Observable.empty();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioFolders -> {
                    if (!audioFolders.isEmpty()) {
                        hideEmptyContentCard();
                    } else {
                        showEmptyContentCard();
                    }
                    adapter.refresh(audioFolders);
                    animateLayout();
                    checkNeedShowPlayback(audioFolders);
                });
    }

    @Override
    public void clear() {
        adapter.clearAdapter();
    }

    protected void showEmptyContentCard() {
        emptyAudioContent.setVisibility(View.VISIBLE);
    }

    protected void hideEmptyContentCard() {
        emptyAudioContent.setVisibility(View.GONE);
    }

    protected void animateLayout() {
        if (!layoutAnimate) {
            AppAnimationUtils.getInstance().loadGridRecyclerItemAnimation(recyclerView);
            recyclerView.scheduleLayoutAnimation();
            layoutAnimate = true;
        }
    }

    private static class AudioFoldersAdapter extends RecyclerView.Adapter<AudioFoldersAdapter.ViewHolder>
            implements ItemTouchHelperAdapter {

        private WeakReference<Activity> activity;
        private List<AudioFolder> audioFolders;

        public AudioFoldersAdapter(Activity activity) {
            this.activity = new WeakReference<>(activity);
            audioFolders = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

            AudioCardView audioCardView;

            public ViewHolder(View v) {
                super(v);

                audioCardView = v.findViewById(R.id.audioCardView);
                audioCardView.setOnAudioCardViewListener(new AudioCardView.OnAudioCardViewListener() {

                    @Override
                    public void onPopupMenuButtonCall(View view) {
                        showAudioContextMenu(view, getAdapterPosition());
                    }

                    @Override
                    public void onOpenTrackListCall() {
                        AudioFolder audioFolder = audioFolders.get(getAdapterPosition());
                        if (audioFolder != null) {
                            Activity act = activity.get();
                            if (act != null) {
                                Intent i = new Intent(act, TrackListActivity.class);
                                i.putExtra(EXTRA_CONTENT_TYPE, CONTENT_TYPE.FOLDERS);
                                i.putExtra(EXTRA_AUDIO_FOLDER, audioFolder);
                                act.startActivity(i);
                            }
                        }
                    }
                });
            }

            private void showAudioContextMenu(View view, int position) {
                ContextMenuManager.getInstance().toggleFolderContextMenu(view,
                        new FolderContextMenu.OnFolderContextMenuListener() {
                            @Override
                            public void onDeleteFolder() {
                                deleteAudioFolder(position);
                            }

                            @Override
                            public void onDetailsFolder() {
                                showDetailsAudioFolder(position);
                            }

                            @Override
                            public void onSearchAlbum() {
                                startSearchAlbumActivity(position);
                            }

                        }, true);
            }


            @Override
            public void onItemSelected() {
                itemView.setAlpha(0.5f);
                changeSwipeRefreshState(false);
            }


            @Override
            public void onItemClear(int position) {
                itemView.setAlpha(1.0f);
                changeSwipeRefreshState(true);
                updateAudioFoldersIndexes();
            }

        }

        private void changeSwipeRefreshState(boolean enable) {
            Activity act = activity.get();
            if (act != null) {
                AudioFragment audioFragment = (AudioFragment) ((FragmentActivity) act).getSupportFragmentManager().
                        findFragmentByTag(AudioFragment.class.getName());
                if (audioFragment != null) {
                    audioFragment.getSwipeRefreshLayout().setEnabled(enable);
                }
            }
        }

        private void startSearchAlbumActivity(int position) {
            Activity act = activity.get();
            if (act != null) {
                AudioFolder audioFolder = audioFolders.get(position);
                if (audioFolder != null) {
                    AlbumSearchActivity.startSearchDataActivity(act, audioFolder);
                }
            }
        }

        private void showDetailsAudioFolder(int position) {
            Activity act = activity.get();
            if (act != null) {
                AudioFolder audioFolder = audioFolders.get(position);
                if (audioFolder != null) {
                    FragmentTransaction transaction =
                            ((FragmentActivity) act).getSupportFragmentManager().beginTransaction();
                    transaction.addToBackStack(null);
                    MediaFolderDetailsDialog dialog = AudioFolderDetailsDialog.newInstance(audioFolder);
                    dialog.setOnMediaFolderDetailsDialogListener(() -> refreshAudioContent(act));
                    dialog.show(transaction, AudioFolderDetailsDialog.class.getName());
                }
            }
        }

        private void refreshAudioContent(Activity act) {
            AudioFragment audioFragment = (AudioFragment) ((FragmentActivity) act).getSupportFragmentManager().
                    findFragmentByTag(AudioFragment.class.getName());
            if (audioFragment != null) {
                audioFragment.refreshAudioContent();
            }
        }


        private void deleteAudioFolder(int position) {
            Activity act = activity.get();
            if (act != null) {
                AudioFolder audioFolder = audioFolders.get(position);
                if (audioFolder != null) {
                    FragmentTransaction transaction =
                            ((FragmentActivity) act).getSupportFragmentManager().beginTransaction();
                    transaction.addToBackStack(null);
                    SimpleDialog dialog = SimpleDialog.newInstance(act.getString(R.string.dialog_delete_file_title),
                            act.getString(R.string.dialog_delete_folder_message), R.drawable.icon_trash);
                    dialog.show(transaction, SimpleDialog.class.getName());
                    dialog.setPositiveListener(() ->
                            Observable.just(CacheManager.deleteDirectoryWithFiles(audioFolder.folderPath))
                                    .firstOrError()
                                    .toObservable()
                                    .subscribeOn(Schedulers.io())
                                    .flatMap(result -> {
                                        DataRepository repository = MediaApplication.getInstance().getRepository();
                                        repository.getMemorySource().setCacheFoldersDirty(true);
                                        return RxUtils.fromCallable(repository.deleteAudioFolder(audioFolder));
                                    })
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(integer -> {
                                        if (MediaApplication.getInstance().getAudioPlayer()
                                                .isDeletedFolderSelect(audioFolder)) {
                                            ((PlaybackActivity) act).clearPlayback();
                                        }
                                        removeFolder(position);
                                        refreshAudioContent(act);
                                        Utils.showCustomSnackbar(act.getCurrentFocus(),
                                                act,
                                                act.getString(R.string.shackbar_delete_folder),
                                                Snackbar.LENGTH_LONG)
                                                .show();

                                    }));
                }
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_audio, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            Collections.swap(audioFolders, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onItemDismiss(int position) {

        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            AudioFolder audioFolder = audioFolders.get(position);
            if (audioFolder != null) {
                holder.audioCardView.setAlbumName(audioFolder.folderName);

                BitmapHelper.getInstance().loadAudioFolderArtwork(audioFolder,
                        holder.audioCardView.getCoverView());

                holder.audioCardView.needMenuVisible(true);

                if (audioFolder.isSelected) {
                    holder.audioCardView.addSelectedFolder();
                } else {
                    holder.audioCardView.removeSelectedFolder();
                }
                if (audioFolder.isHidden) {
                    holder.audioCardView.setAlpha(0.35f);
                } else {
                    holder.audioCardView.setAlpha(1f);
                }

                if (audioFolder.color == null) {
                    BitmapHelper.getInstance().getAudioFolderPalette(audioFolder)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(paletteColor -> setPalette(paletteColor, audioFolder, holder));
                } else {
                    holder.audioCardView.setFooterBackgroundColor(audioFolder.color.getVibrantLight());
                    holder.audioCardView.setAlbumTextColor(audioFolder.color.getMutedLight());
                }
            }
        }

        private void setPalette(BitmapHelper.PaletteColor paletteColor,
                                AudioFolder audioFolder, ViewHolder holder) {
            audioFolder.color = paletteColor;
            holder.audioCardView.setFooterBackgroundColor(audioFolder.color.getVibrantLight());
            holder.audioCardView.setAlbumTextColor(audioFolder.color.getMutedLight());
        }

        @Override
        public int getItemCount() {
            return audioFolders.size();
        }

        public void refresh(List<AudioFolder> receiverAudioFolders) {
            audioFolders.clear();
            audioFolders.addAll(receiverAudioFolders);
            notifyDataSetChanged();
        }

        public void clearAdapter() {
            audioFolders.clear();
            notifyDataSetChanged();
        }

        public void removeFolder(int position) {
            audioFolders.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());
        }

        public void updateAudioFoldersIndexes() {
            RxUtils.fromCallable(MediaApplication.getInstance().getRepository()
                    .updateAudioFoldersIndex(audioFolders))
                    .firstOrError()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(integer -> notifyDataSetChanged());
        }
    }
}
