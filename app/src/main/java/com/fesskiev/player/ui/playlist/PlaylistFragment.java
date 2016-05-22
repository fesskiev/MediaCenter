package com.fesskiev.player.ui.playlist;


import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.ui.audio.utils.Constants;
import com.fesskiev.player.ui.player.AudioPlayerActivity;
import com.fesskiev.player.ui.player.HidingPlaybackFragment;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;


public class PlaylistFragment extends HidingPlaybackFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = PlaylistFragment.class.getSimpleName();

    public static PlaylistFragment newInstance() {
        return new PlaylistFragment();
    }

    private AudioPlayer audioPlayer;
    private AudioTracksAdapter adapter;
    private List<AudioFile> audioFiles;
    private CardView emptyPlaylistCard;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        audioFiles = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emptyPlaylistCard = (CardView) view.findViewById(R.id.emptyPlaylistCard);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false, 1000));
        adapter = new AudioTracksAdapter();
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.menu_clear_playlist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "clear click");
                DatabaseHelper.clearPlaylist(getContext());
            }
        });

        getActivity().getSupportLoaderManager().
                restartLoader(Constants.GET_AUDIO_PLAY_LIST_LOADER, null, this);
    }

    private void showEmptyCardPlaylist() {
        emptyPlaylistCard.setVisibility(View.VISIBLE);
    }

    private void hideEmptyCardPlaylist() {
        emptyPlaylistCard.setVisibility(View.GONE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case Constants.GET_AUDIO_PLAY_LIST_LOADER:
                return new CursorLoader(
                        getActivity(),
                        MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                        null,
                        MediaCenterProvider.TRACK_IN_TRACK_LIST + "=1",
                        null,
                        null

                );
            default:
                return null;

        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.wtf(TAG, "audio playlist size " + cursor.getCount());
        List<AudioFile> audioFiles = new ArrayList<>();
        if (cursor.getCount() > 0) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                AudioFile audioFile = new AudioFile(cursor);
                audioFiles.add(audioFile);
                Log.wtf(TAG, "audio playlist file " + audioFile.toString());
            }

            hideEmptyCardPlaylist();
        } else {
            showEmptyCardPlaylist();
        }
        adapter.refreshAdapter(audioFiles);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class AudioTracksAdapter extends RecyclerView.Adapter<AudioTracksAdapter.ViewHolder> {


        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView duration;
            TextView title;
            TextView filePath;
            ImageView cover;

            public ViewHolder(View v) {
                super(v);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPlayerActivity(getAdapterPosition(), cover);
                    }
                });

                duration = (TextView) v.findViewById(R.id.itemDuration);
                title = (TextView) v.findViewById(R.id.itemTitle);
                filePath = (TextView) v.findViewById(R.id.filePath);
                cover = (ImageView) v.findViewById(R.id.itemCover);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_audio_track, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            AudioFile audioFile = audioFiles.get(position);

            BitmapHelper.loadTrackListArtwork(getActivity(), null, audioFile, holder.cover);

            holder.duration.setText(Utils.getDurationString(audioFile.length));
            holder.title.setText(audioFile.title);
            holder.filePath.setText(audioFile.filePath.getName());
        }

        @Override
        public int getItemCount() {
            return audioFiles.size();
        }

        public void refreshAdapter(List<AudioFile> newAudioFiles) {
            audioFiles.clear();
            audioFiles.addAll(newAudioFiles);
            notifyDataSetChanged();
        }

        private void startPlayerActivity(int position, View cover) {
            AudioFile audioFile = audioFiles.get(position);
            if (audioFile != null) {
                audioPlayer.setCurrentAudioFile(audioFile);
                audioPlayer.position = position;

                AudioPlayerActivity.startPlayerActivity(getActivity(), true, cover);
            }
        }
    }
}
