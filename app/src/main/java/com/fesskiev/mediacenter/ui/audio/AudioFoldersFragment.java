package com.fesskiev.mediacenter.ui.audio;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.ui.GridFragment;
import com.fesskiev.mediacenter.ui.audio.tracklist.TrackListActivity;
import com.fesskiev.mediacenter.ui.audio.utils.CONTENT_TYPE;
import com.fesskiev.mediacenter.ui.audio.utils.Constants;
import com.fesskiev.mediacenter.ui.playback.PlaybackActivity;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.dialogs.AudioFolderDetailsDialog;
import com.fesskiev.mediacenter.widgets.dialogs.MediaFolderDetailsDialog;
import com.fesskiev.mediacenter.widgets.item.AudioCardView;
import com.fesskiev.mediacenter.widgets.menu.FolderContextMenu;
import com.fesskiev.mediacenter.widgets.menu.ContextMenuManager;
import com.fesskiev.mediacenter.widgets.recycleview.helper.ItemTouchHelperAdapter;
import com.fesskiev.mediacenter.widgets.recycleview.helper.ItemTouchHelperViewHolder;
import com.fesskiev.mediacenter.widgets.recycleview.helper.SimpleItemTouchHelperCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class AudioFoldersFragment extends GridFragment implements AudioContent {

    public static AudioFoldersFragment newInstance() {
        return new AudioFoldersFragment();
    }

    private Subscription subscription;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback((ItemTouchHelperAdapter) adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        fetch();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (MediaApplication.getInstance().getRepository().getMemorySource().isCacheFoldersDirty()) {
            fetch();
        }
    }

    @Override
    public RecyclerView.Adapter createAdapter() {
        return new AudioFoldersAdapter(getActivity());
    }

    @Override
    public void fetch() {
        subscription = MediaApplication.getInstance().getRepository().getAudioFolders()
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(Observable::from)
                .filter(folder -> {
                    if (AppSettingsManager.getInstance().isShowHiddenFiles()) {
                        return true;
                    }
                    return !folder.isHidden;
                })
                .toList()
                .subscribe(audioFolders -> {
                    if (audioFolders != null) {
                        AppLog.INFO("onNext:folders: " + audioFolders.size());
                        if (!audioFolders.isEmpty()) {
                            Collections.sort(audioFolders);
                            hideEmptyContentCard();
                        } else {
                            showEmptyContentCard();
                        }
                        ((AudioFoldersAdapter) adapter).refresh(audioFolders);
                        checkNeedShowPlayback(audioFolders);
                    }
                });
    }

    @Override
    public void clear() {
        ((AudioFoldersAdapter) adapter).clearAdapter();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
        MediaApplication.getInstance().getRepository().getMemorySource().setCacheFoldersDirty(true);
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

                audioCardView = (AudioCardView) v.findViewById(R.id.audioCardView);
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
                                i.putExtra(Constants.EXTRA_CONTENT_TYPE, CONTENT_TYPE.FOLDERS);
                                i.putExtra(Constants.EXTRA_AUDIO_FOLDER, audioFolder);
                                act.startActivity(i);
                            }
                        }
                    }
                });
            }

            private void showAudioContextMenu(View view, int position) {
                ContextMenuManager.getInstance().toggleAudioContextMenu(view,
                        new FolderContextMenu.OnFolderContextMenuListener() {
                            @Override
                            public void onDeleteFolder() {
                                deleteAudioFolder(position);
                            }

                            @Override
                            public void onDetailsFolder() {
                                showDetailsAudioFolder(position);
                            }
                        });
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
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(act, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle(act.getString(R.string.dialog_delete_file_title));
                    builder.setMessage(R.string.dialog_delete_folder_message);
                    builder.setPositiveButton(R.string.dialog_delete_file_ok,
                            (dialog, which) -> {

                                Observable.just(CacheManager.deleteDirectory(audioFolder.folderPath))
                                        .first()
                                        .subscribeOn(Schedulers.io())
                                        .flatMap(result -> {
                                            if (result) {
                                                DataRepository repository = MediaApplication.getInstance().getRepository();
                                                repository.getMemorySource().setCacheFoldersDirty(true);
                                                return RxUtils.fromCallable(repository.deleteAudioFolder(audioFolder));
                                            }
                                            return Observable.empty();
                                        })
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(integer -> {
                                            if (MediaApplication.getInstance().getAudioPlayer()
                                                    .isDeletedFolderSelect(audioFolder)) {
                                                ((PlaybackActivity) act).clearPlayback();
                                            }
                                            removeFolder(position);
                                            Utils.showCustomSnackbar(act.getCurrentFocus(),
                                                    act,
                                                    act.getString(R.string.shackbar_delete_folder),
                                                    Snackbar.LENGTH_LONG)
                                                    .show();

                                        });
                            });
                    builder.setNegativeButton(R.string.dialog_delete_file_cancel,
                            (dialog, which) -> dialog.cancel());
                    builder.show();
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

                if (audioFolder.isHidden) {
                    holder.audioCardView.setAlpha(0.35f);
                } else {
                    holder.audioCardView.setAlpha(1f);
                }
            }
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
                    .first()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(integer -> notifyDataSetChanged());
        }
    }
}
