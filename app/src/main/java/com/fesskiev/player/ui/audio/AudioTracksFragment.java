package com.fesskiev.player.ui.audio;


import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import com.fesskiev.player.db.MediaCenterProvider;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.ui.player.AudioPlayerActivity;
import com.fesskiev.player.ui.player.HidingPlaybackFragment;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.recycleview.HidingScrollListener;
import com.fesskiev.player.widgets.recycleview.RecyclerItemTouchClickListener;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;


public class AudioTracksFragment extends HidingPlaybackFragment implements AudioContent, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = AudioTracksFragment.class.getSimpleName();
    private static final int GET_AUDIO_FILES_LOADER = 1002;

    public static AudioTracksFragment newInstance() {
        return new AudioTracksFragment();
    }

    private List<AudioFile> audioFiles;
    private AudioTracksAdapter adapter;
    private AudioPlayer audioPlayer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        audioFiles = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tracks, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false, 1000));
        adapter = new AudioTracksAdapter();
        recyclerView.setAdapter(adapter);
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
    }

    @Override
    public void fetchAudioContent() {
        getActivity().getSupportLoaderManager().restartLoader(GET_AUDIO_FILES_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case GET_AUDIO_FILES_LOADER:
                return new CursorLoader(
                        getActivity(),
                        MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                        null,
                        null,
                        null,
                        MediaCenterProvider.TRACK_NUMBER + " ASC"

                );
            default:
                return null;

        }
    }

    private void destroyLoader() {
        getActivity().getSupportLoaderManager().destroyLoader(GET_AUDIO_FILES_LOADER);
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
            Log.d(TAG, "audio files size " + audioFiles.size());

            adapter.refreshAdapter(audioFiles);
        }

        destroyLoader();
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
