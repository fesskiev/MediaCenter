package com.fesskiev.mediacenter.ui.audio;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.ui.MainActivity;
import com.fesskiev.mediacenter.ui.audio.tracklist.TrackListActivity;
import com.fesskiev.mediacenter.ui.search.AlbumSearchActivity;
import com.fesskiev.mediacenter.utils.AppAnimationUtils;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.dialogs.AudioFolderDetailsDialog;
import com.fesskiev.mediacenter.widgets.dialogs.MediaFolderDetailsDialog;
import com.fesskiev.mediacenter.widgets.dialogs.SimpleDialog;
import com.fesskiev.mediacenter.widgets.item.AudioCardView;
import com.fesskiev.mediacenter.widgets.menu.ContextMenuManager;
import com.fesskiev.mediacenter.widgets.menu.FolderContextMenu;
import com.fesskiev.mediacenter.widgets.recycleview.ItemOffsetDecoration;
import com.fesskiev.mediacenter.widgets.recycleview.helper.ItemTouchHelperAdapter;
import com.fesskiev.mediacenter.widgets.recycleview.helper.ItemTouchHelperViewHolder;
import com.fesskiev.mediacenter.widgets.recycleview.helper.SimpleItemTouchHelperCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

import static com.fesskiev.mediacenter.ui.audio.tracklist.TrackListActivity.EXTRA_AUDIO_FOLDER;
import static com.fesskiev.mediacenter.ui.audio.tracklist.TrackListActivity.EXTRA_CONTENT_TYPE;


public class AudioFoldersFragment extends Fragment implements AudioContent {

    public static AudioFoldersFragment newInstance() {
        return new AudioFoldersFragment();
    }

    @Inject
    AppAnimationUtils animationUtils;

    private AudioFoldersAdapter adapter;
    private RecyclerView recyclerView;
    private CardView emptyAudioContent;

    private boolean layoutAnimate;

    private AudioFoldersViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MediaApplication.getInstance().getAppComponent().inject(this);
        observeData();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            layoutAnimate = savedInstanceState.getBoolean("layoutAnimate");
        }
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
        ((DefaultItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        adapter = new AudioFoldersAdapter(this);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new ItemOffsetDecoration(spacing));

        emptyAudioContent = view.findViewById(R.id.emptyAudioContentCard);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void observeData() {
        viewModel = ViewModelProviders.of(this).get(AudioFoldersViewModel.class);
        viewModel.getAudioFoldersLiveData().observe(this, this::refreshAdapter);
        viewModel.getEmptyFoldersLiveData().observe(this, Void -> showEmptyContentCard());
        viewModel.getDeletedFolderLiveData().observe(this, position -> {
            adapter.removeFolder(position);
            showAudioFolderDeletedShackBar();
        });
        viewModel.getClearPlaybackLiveData().observe(this, Void -> clearPlayback());
        viewModel.getUpdatedFolderIndexesLiveData().observe(this, Void -> adapter.notifyDataSetChanged());
        viewModel.getNotExistFolderLiveData().observe(this, Void -> showVideoFolderNotExistSnackBar());
    }


    @Override
    public void onResume() {
        super.onResume();
        fetch();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("layoutAnimate", layoutAnimate);
    }

    @Override
    public void fetch() {
        viewModel.getAudioFolders();
    }

    @Override
    public void clear() {
        adapter.clearAdapter();
    }

    private void showAudioFolderDeletedShackBar() {
        Utils.showCustomSnackbar(getView(),
                getContext(), getString(R.string.shackbar_delete_folder), Snackbar.LENGTH_LONG).show();
    }

    private void clearPlayback() {
        ((MainActivity) getActivity()).clearPlayback();
    }

    private void refreshAdapter(List<AudioFolder> audioFolders) {
        adapter.refresh(audioFolders);
        animateLayout();
        hideEmptyContentCard();
        AppLog.INFO("onNext:audio folders: " + (audioFolders == null ? "null" : audioFolders.size()));
    }

    private void showEmptyContentCard() {
        emptyAudioContent.setVisibility(View.VISIBLE);
    }

    private void hideEmptyContentCard() {
        emptyAudioContent.setVisibility(View.GONE);
    }

    private void animateLayout() {
        if (!layoutAnimate) {
            animationUtils.loadGridRecyclerItemAnimation(recyclerView);
            recyclerView.scheduleLayoutAnimation();
            layoutAnimate = true;
        }
    }

    public void startTrackListActivity(AudioFolder audioFolder) {
        if (viewModel.checkAudioFolderExist(audioFolder)) {
            Intent i = new Intent(getContext(), TrackListActivity.class);
            i.putExtra(EXTRA_CONTENT_TYPE, CONTENT_TYPE.FOLDERS);
            i.putExtra(EXTRA_AUDIO_FOLDER, audioFolder);
            startActivity(i);
        }
    }

    private void showVideoFolderNotExistSnackBar() {
        Utils.showCustomSnackbar(getView(), getContext(),
                getString(R.string.snackbar_folder_not_exist), Snackbar.LENGTH_LONG).show();
    }

    public void showDetailsAudioFolder(AudioFolder audioFolder) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        MediaFolderDetailsDialog dialog = AudioFolderDetailsDialog.newInstance(audioFolder);
        dialog.setOnMediaFolderDetailsDialogListener(this::refreshAudioContent);
        dialog.show(transaction, AudioFolderDetailsDialog.class.getName());
    }

    private void refreshAudioContent() {
        AudioFragment audioFragment = (AudioFragment) getActivity().getSupportFragmentManager().
                findFragmentByTag(AudioFragment.class.getName());
        if (audioFragment != null) {
            audioFragment.refreshAudioContent();
        }
    }

    public void enableSwipeRefreshLayout(boolean enable) {
        AudioFragment audioFragment = (AudioFragment) getActivity().getSupportFragmentManager().
                findFragmentByTag(AudioFragment.class.getName());
        if (audioFragment != null) {
            audioFragment.getSwipeRefreshLayout().setEnabled(enable);
        }
    }

    public Observable<Bitmap> getAudioFolderArtwork(AudioFolder audioFolder) {
        return viewModel.getAudioFolderArtwork(audioFolder);
    }

    public Observable<BitmapHelper.PaletteColor> getAudioFolderPalette(AudioFolder audioFolder) {
        return viewModel.getAudioFolderPalette(audioFolder);
    }

    public void deleteAudioFolder(AudioFolder audioFolder, int position) {
        if (viewModel.checkAudioFolderExist(audioFolder)) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.addToBackStack(null);
            SimpleDialog dialog = SimpleDialog.newInstance(getString(R.string.dialog_delete_file_title),
                    getString(R.string.dialog_delete_folder_message), R.drawable.icon_trash);
            dialog.show(transaction, SimpleDialog.class.getName());
            dialog.setPositiveListener(() -> viewModel.deleteAudioFolder(audioFolder, position));
        }
    }

    public void updateAudioFoldersIndexes(List<AudioFolder> audioFolders) {
        viewModel.updateAudioFolderIndexes(audioFolders);
    }

    private static class AudioFoldersAdapter extends RecyclerView.Adapter<AudioFoldersAdapter.ViewHolder>
            implements ItemTouchHelperAdapter {

        private WeakReference<AudioFoldersFragment> fragment;
        private List<AudioFolder> audioFolders;

        public AudioFoldersAdapter(AudioFoldersFragment fragment) {
            this.fragment = new WeakReference<>(fragment);
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
                        startAudioFilesActivity(getAdapterPosition());
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
                AudioFoldersFragment frg = fragment.get();
                if (frg != null) {
                    frg.updateAudioFoldersIndexes(audioFolders);
                }
            }

        }

        @Override
        public long getItemId(int position) {
            AudioFolder audioFolder = audioFolders.get(position);
            if (audioFolder != null) {
                return audioFolder.timestamp;
            }
            return super.getItemId(position);
        }

        private void startAudioFilesActivity(int position) {
            AudioFoldersFragment frg = fragment.get();
            if (frg != null) {
                AudioFolder audioFolder = audioFolders.get(position);
                if (audioFolder != null) {
                    frg.startTrackListActivity(audioFolder);
                }
            }
        }

        private void changeSwipeRefreshState(boolean enable) {
            AudioFoldersFragment frg = fragment.get();
            if (frg != null) {
                frg.enableSwipeRefreshLayout(enable);
            }
        }

        private void startSearchAlbumActivity(int position) {
            AudioFoldersFragment frg = fragment.get();
            if (frg != null) {
                AudioFolder audioFolder = audioFolders.get(position);
                if (audioFolder != null) {
                    AlbumSearchActivity.startSearchDataActivity(frg.getActivity(), audioFolder);
                }
            }
        }

        private void showDetailsAudioFolder(int position) {
            AudioFoldersFragment frg = fragment.get();
            if (frg != null) {
                AudioFolder audioFolder = audioFolders.get(position);
                if (audioFolder != null) {
                    frg.showDetailsAudioFolder(audioFolder);
                }
            }
        }

        private void deleteAudioFolder(int position) {
            AudioFoldersFragment frg = fragment.get();
            if (frg != null) {
                AudioFolder audioFolder = audioFolders.get(position);
                if (audioFolder != null) {
                    frg.deleteAudioFolder(audioFolder, position);
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

                AudioFoldersFragment frg = fragment.get();
                if (frg != null) {
                    frg.getAudioFolderArtwork(audioFolder)
                            .subscribe(bitmap -> holder.audioCardView.getCoverView().setImageBitmap(bitmap), Throwable::printStackTrace);
                    if (audioFolder.color == null) {
                        frg.getAudioFolderPalette(audioFolder)
                                .subscribe(paletteColor -> setPalette(paletteColor, audioFolder, holder));
                    } else {
                        holder.audioCardView.setFooterBackgroundColor(audioFolder.color.getVibrantLight());
                        holder.audioCardView.setAlbumTextColor(audioFolder.color.getMutedLight());
                    }
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
    }
}
