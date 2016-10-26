package com.fesskiev.player.ui.audio.tracklist;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.data.model.AudioFile;
import com.fesskiev.player.data.model.AudioFolder;
import com.fesskiev.player.data.model.AudioPlayer;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.ui.audio.player.AudioPlayerActivity;
import com.fesskiev.player.ui.audio.utils.CONTENT_TYPE;
import com.fesskiev.player.ui.audio.utils.Constants;
import com.fesskiev.player.utils.AppLog;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.RxUtils;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.cards.SlidingCardView;
import com.fesskiev.player.widgets.dialogs.EditTrackDialog;
import com.fesskiev.player.widgets.recycleview.HidingScrollListener;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class TrackListFragment extends Fragment {

    public static TrackListFragment newInstance(CONTENT_TYPE contentType, String contentValue) {
        TrackListFragment fragment = new TrackListFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.EXTRA_CONTENT_TYPE, contentType);
        args.putString(Constants.EXTRA_CONTENT_TYPE_VALUE, contentValue);
        fragment.setArguments(args);
        return fragment;
    }

    private Subscription subscription;
    private TrackListAdapter adapter;
    private AudioPlayer audioPlayer;
    private List<SlidingCardView> openCards;
    private CONTENT_TYPE contentType;
    private String contentValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            contentType = (CONTENT_TYPE)
                    getArguments().getSerializable(Constants.EXTRA_CONTENT_TYPE);
            contentValue = getArguments().getString(Constants.EXTRA_CONTENT_TYPE_VALUE);
        }

        audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        openCards = new ArrayList<>();

        registerPlaybackBroadcastReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_track_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false, 1000));
        adapter = new TrackListAdapter();
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


        fetchContentByType();
    }

    @Override
    public void onResume() {
        super.onResume();
        notifyTrackStateChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterPlaybackBroadcastReceiver();
        RxUtils.unsubscribe(subscription);
    }


    private void notifyTrackStateChanged() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void registerPlaybackBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlaybackService.ACTION_PLAYBACK_PLAYING_STATE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(playbackReceiver, filter);
    }

    private void unregisterPlaybackBroadcastReceiver() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(playbackReceiver);
    }


    private BroadcastReceiver playbackReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PlaybackService.ACTION_PLAYBACK_PLAYING_STATE:
                    notifyTrackStateChanged();
                    break;
            }
        }
    };

    private void fetchContentByType() {
        AudioFolder audioFolder = audioPlayer.currentAudioFolder;
        Observable<List<AudioFile>> audioFilesObservable = null;

        switch (contentType) {
            case GENRE:
                audioFilesObservable = MediaApplication.getInstance().getRepository().getGenreTracks(contentValue);
                break;
            case FOLDERS:
                audioFilesObservable = MediaApplication.getInstance().getRepository().getFolderTracks(audioFolder.id);
                break;
            case ARTIST:
                audioFilesObservable = MediaApplication.getInstance().getRepository().getArtistTracks(contentValue);
                break;
        }

        if (audioFilesObservable != null) {
            subscription = audioFilesObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(audioFiles -> {
                        AppLog.INFO("onNext:track list: " + audioFiles.size());
                        if (audioFolder != null) {
                            audioPlayer.setCurrentAudioFolderFiles(audioFiles);
                            adapter.refreshAdapter(audioFiles);
                        }
                    });
        }
    }

    private void closeOpenCards() {
        if (!openCards.isEmpty()) {
            for (SlidingCardView cardView : openCards) {
                if (cardView.isOpen()) {
                    cardView.animateSlidingContainer(false);
                }
            }
        }
    }


    private class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.ViewHolder> {

        private List<AudioFile> audioFiles;

        public TrackListAdapter() {
            this.audioFiles = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView duration;
            TextView title;
            TextView filePath;
            ImageView cover;
            ImageView playEq;

            public ViewHolder(final View v) {
                super(v);

                duration = (TextView) v.findViewById(R.id.itemDuration);
                title = (TextView) v.findViewById(R.id.itemTitle);
                filePath = (TextView) v.findViewById(R.id.filePath);
                cover = (ImageView) v.findViewById(R.id.itemCover);
                playEq = (ImageView) v.findViewById(R.id.playEq);

                ((SlidingCardView) v).
                        setOnSlidingCardListener(new SlidingCardView.OnSlidingCardListener() {
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
                                startPlayerActivity(getAdapterPosition(), cover);
                            }

                            @Override
                            public void onPlaylistClick() {
                                addToPlaylist(getAdapterPosition());
                            }

                            @Override
                            public void onAnimateChanged(SlidingCardView cardView, boolean open) {
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
                MediaApplication.getInstance().getRepository().updateAudioFile(audioFile);
                Utils.showCustomSnackbar(getView(),
                        getContext().getApplicationContext(),
                        getString(R.string.add_to_playlist_text),
                        Snackbar.LENGTH_SHORT).show();
                closeOpenCards();
            }
        }

        private void startPlayerActivity(int position, View cover) {
            AudioFile audioFile = audioFiles.get(position);
            if (audioFile != null) {
                if (audioFile.exists()) {
                    if (audioPlayer.isTrackPlaying(audioFile)) {
                        AudioPlayerActivity.startPlayerActivity(getActivity(), false, cover);
                    } else {
                        audioFile.isSelected = true;
                        MediaApplication.getInstance().getRepository().updateSelectedAudioFile(audioFile);

                        audioPlayer.setCurrentAudioFile(audioFile);
                        audioPlayer.position = position;
                        AudioPlayerActivity.startPlayerActivity(getActivity(), true, cover);
                    }
                } else {
                    Utils.showCustomSnackbar(getView(),
                            getContext(), getString(R.string.snackbar_file_not_exist),
                            Snackbar.LENGTH_LONG).show();
                }
            }
        }

        private void showEditDialog(final int position) {
            AudioFile audioFile = audioFiles.get(position);
            if (audioFile != null) {
                EditTrackDialog editTrackDialog = new EditTrackDialog(getActivity(), audioFile,
                        new EditTrackDialog.OnEditTrackChangedListener() {
                            @Override
                            public void onEditTrackChanged(AudioFile audioFile) {
                                adapter.updateItem(position, audioFile);
                            }

                            @Override
                            public void onEditTrackError() {
                            }
                        });
                editTrackDialog.show();
            }
        }

        private void deleteFile(final int position) {
            final AudioFile audioFile = audioFiles.get(position);
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
            builder.setTitle(getString(R.string.dialog_delete_file_title));
            builder.setMessage(R.string.dialog_delete_file_message);
            builder.setPositiveButton(R.string.dialog_delete_file_ok,
                    (dialog, which) -> {
                        if (audioFile.filePath.delete()) {
                            Utils.showCustomSnackbar(getView(),
                                    getContext(),
                                    getString(R.string.shackbar_delete_file),
                                    Snackbar.LENGTH_LONG).show();

                            MediaApplication.getInstance()
                                    .getRepository()
                                    .deleteAudioFile(audioFile.getFilePath());

                            adapter.removeItem(position);

                        }
                    });
            builder.setNegativeButton(R.string.dialog_delete_file_cancel,
                    (dialog, which) -> dialog.cancel());
            builder.show();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_track, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            AudioFile audioFile = audioFiles.get(position);

            BitmapHelper.getInstance().loadTrackListArtwork(audioFile, holder.cover);

            holder.duration.setText(Utils.getDurationString(audioFile.length));
            holder.title.setText(audioFile.title);
            holder.filePath.setText(audioFile.filePath.getName());

            if (audioPlayer.isTrackPlaying(audioFile)) {
                holder.playEq.setVisibility(View.VISIBLE);
                final AnimationDrawable animation = (AnimationDrawable) ContextCompat.
                        getDrawable(getContext().getApplicationContext(), R.drawable.ic_equalizer);
                holder.playEq.setImageDrawable(animation);
                if (animation != null) {
                    if (audioPlayer.isPlaying) {
                        holder.playEq.post(animation::start);
                    } else {
                        animation.stop();
                    }
                }
            } else {
                holder.playEq.setVisibility(View.INVISIBLE);
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
    }
}
