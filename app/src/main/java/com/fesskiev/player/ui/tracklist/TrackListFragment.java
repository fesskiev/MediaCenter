package com.fesskiev.player.ui.tracklist;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.services.FetchAudioInfoIntentService;
import com.fesskiev.player.ui.player.AudioPlayerActivity;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.cards.SlidingCardView;
import com.fesskiev.player.widgets.dialogs.EditTrackDialog;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class TrackListFragment extends Fragment {

    private static final String TAG = TrackListFragment.class.getSimpleName();

    private MusicFilesAdapter musicFilesAdapter;
    private AudioFolder audioFolder;

    public static TrackListFragment newInstance() {
        return new TrackListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioFolder =
                MediaApplication.getInstance().getAudioPlayer().currentAudioFolder;
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
        musicFilesAdapter = new MusicFilesAdapter();
        recyclerView.setAdapter(musicFilesAdapter);

        if (audioFolder.audioFilesDescription.size() == 0) {
            FetchAudioInfoIntentService.startFetchAudioInfo(getActivity());
        } else {
            List<AudioFile> receiverAudioFiles = MediaApplication.getInstance().
                    getAudioPlayer().currentAudioFolder.audioFilesDescription;
            if (receiverAudioFiles != null) {
                musicFilesAdapter.refreshAdapter(receiverAudioFiles);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerMusicFilesReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterMusicFilesReceiver();
    }



    private void registerMusicFilesReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FetchAudioInfoIntentService.ACTION_MUSIC_FILES_RESULT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(musicFilesReceiver,
                intentFilter);
    }

    private void unregisterMusicFilesReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(musicFilesReceiver);
    }

    private BroadcastReceiver musicFilesReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case FetchAudioInfoIntentService.ACTION_MUSIC_FILES_RESULT:
//                    Log.d(TAG, "receive music files!");

                    List<AudioFile> receiverAudioFiles = MediaApplication.getInstance().
                            getAudioPlayer().currentAudioFolder.audioFilesDescription;
                    if (receiverAudioFiles != null) {
                        musicFilesAdapter.refreshAdapter(receiverAudioFiles);
                    }
                    break;
            }
        }
    };

    private class MusicFilesAdapter extends RecyclerView.Adapter<MusicFilesAdapter.ViewHolder> {

        private List<AudioFile> audioFiles;

        public MusicFilesAdapter() {
            this.audioFiles = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView duration;
            TextView title;
            ImageView cover;

            public ViewHolder(final View v) {
                super(v);

                duration = (TextView) v.findViewById(R.id.itemDuration);
                title = (TextView) v.findViewById(R.id.itemTitle);
                cover = (ImageView) v.findViewById(R.id.itemCover);

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
                        });
            }
        }

        private void startPlayerActivity(int position, View cover) {
            AudioFile audioFile = audioFolder.audioFilesDescription.get(position);
            if (audioFile != null) {
                AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();
                audioPlayer.currentAudioFile = audioFile;
                audioPlayer.position = position;


                AudioPlayerActivity.startPlayerActivity(getActivity(), true, cover);
            }
        }

        private void showEditDialog(int position) {
            AudioFile audioFile = audioFolder.audioFilesDescription.get(position);
            EditTrackDialog editTrackDialog = new EditTrackDialog(getActivity(), audioFile);
            editTrackDialog.show();
        }

        private void deleteFile(int position) {
            final AudioFile audioFile = audioFolder.audioFilesDescription.get(position);
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
            builder.setTitle(getString(R.string.dialog_delete_file_title));
            builder.setMessage(R.string.dialog_delete_file_message);
            builder.setPositiveButton(R.string.dialog_delete_file_ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (new File(audioFile.filePath).delete()) {
                                Snackbar.make(getView(),
                                        getString(R.string.shackbar_delete_file), Snackbar.LENGTH_LONG).show();
                                musicFilesAdapter.audioFiles.remove(audioFile);
                                musicFilesAdapter.notifyDataSetChanged();
                            }
                        }
                    });
            builder.setNegativeButton(R.string.dialog_delete_file_cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
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
                        Bitmap artwork = audioFile.getArtwork();
            if (artwork != null) {
                holder.cover.setImageBitmap(artwork);
            } else {
                if (audioFolder != null && audioFolder.folderImages.size() > 0) {
                    File coverFile = audioFolder.folderImages.get(0);
                    if (coverFile != null) {
                        MediaApplication.getInstance().getPicasso()
                                .load(coverFile)
                                .fit()
                                .into(holder.cover);
                    }
                } else {
                    MediaApplication.getInstance().getPicasso()
                            .load(R.drawable.no_cover_icon)
                            .fit()
                            .into(holder.cover);
                }
            }

            holder.duration.setText(Utils.getDurationString(audioFile.length));
            holder.title.setText(audioFile.title);
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
    }
}
