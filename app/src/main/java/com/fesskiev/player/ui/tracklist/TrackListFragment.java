package com.fesskiev.player.ui.tracklist;


import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import com.fesskiev.player.db.DatabaseHelper;
import com.fesskiev.player.db.MediaCenterProvider;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.ui.player.AudioPlayerActivity;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.cards.SlidingCardView;
import com.fesskiev.player.widgets.dialogs.EditTrackDialog;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;


public class TrackListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = TrackListFragment.class.getSimpleName();

    private static final int GET_AUDIO_FILES_LOADER = 1002;

    private TrackListAdapter trackListAdapter;
    private AudioFolder audioFolder;
    private AudioPlayer audioPlayer;
    private List<AudioFile> audioFiles;

    public static TrackListFragment newInstance() {
        return new TrackListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        audioFolder = audioPlayer.currentAudioFolder;
        audioFiles = new ArrayList<>();
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
        trackListAdapter = new TrackListAdapter();
        recyclerView.setAdapter(trackListAdapter);

        getActivity().getSupportLoaderManager().initLoader(GET_AUDIO_FILES_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case GET_AUDIO_FILES_LOADER:
                return new CursorLoader(
                        getActivity(),
                        MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                        null,
                        MediaCenterProvider.ID + "=" + "'" + audioFolder.id + "'",
                        null,
                        MediaCenterProvider.TRACK_NUMBER + " ASC"

                );
            default:
                return null;

        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() > 0) {
            List<AudioFile> audioFiles = new ArrayList<>();
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                AudioFile audioFile = new AudioFile(cursor);
                audioFiles.add(audioFile);
            }
            audioFolder.audioFiles = audioFiles;
            audioPlayer.sendBroadcastChangeAudioFolder();
            trackListAdapter.refreshAdapter(audioFiles);
        }
        cursor.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    private class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView duration;
            TextView title;
            TextView filePath;
            ImageView cover;

            public ViewHolder(final View v) {
                super(v);

                duration = (TextView) v.findViewById(R.id.itemDuration);
                title = (TextView) v.findViewById(R.id.itemTitle);
                filePath = (TextView) v.findViewById(R.id.filePath);
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
            AudioFile audioFile = audioFiles.get(position);
            if (audioFile != null) {
                audioPlayer.setCurrentAudioFile(audioFile);
                audioPlayer.position = position;

                AudioPlayerActivity.startPlayerActivity(getActivity(), true, cover);
            }
        }

        private void showEditDialog(final int position) {
            AudioFile audioFile = audioFiles.get(position);
            if (audioFile != null) {
                EditTrackDialog editTrackDialog = new EditTrackDialog(getActivity(), audioFile,
                        new EditTrackDialog.OnEditTrackChangedListener() {
                            @Override
                            public void onEditTrackChanged(AudioFile audioFile) {
                                trackListAdapter.updateItem(position, audioFile);
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
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (audioFile.filePath.delete()) {
                                Snackbar.make(getView(),
                                        getString(R.string.shackbar_delete_file), Snackbar.LENGTH_LONG).show();

                                DatabaseHelper.deleteAudioFile(getContext(),
                                        audioFile.filePath.getAbsolutePath());

                                trackListAdapter.removeItem(position);

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

            BitmapHelper.loadTrackListArtwork(getActivity(), audioFolder, audioFile, holder.cover);

            holder.duration.setText(Utils.getDurationString(audioFile.length));
            holder.title.setText(audioFile.title);
            holder.filePath.setText(audioFile.filePath.getName());
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
