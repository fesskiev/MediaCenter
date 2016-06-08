package com.fesskiev.player.ui.audio;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.ui.GridFragment;
import com.fesskiev.player.ui.audio.utils.CONTENT_TYPE;
import com.fesskiev.player.ui.audio.utils.Constants;
import com.fesskiev.player.ui.tracklist.TrackListActivity;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.widgets.recycleview.RecyclerItemTouchClickListener;
import com.fesskiev.player.widgets.recycleview.helper.ItemTouchHelperAdapter;
import com.fesskiev.player.widgets.recycleview.helper.ItemTouchHelperViewHolder;
import com.fesskiev.player.widgets.recycleview.helper.SimpleItemTouchHelperCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AudioFoldersFragment extends GridFragment implements AudioContent, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = AudioFoldersFragment.class.getSimpleName();


    public static AudioFoldersFragment newInstance() {
        return new AudioFoldersFragment();
    }


    private List<AudioFolder> audioFolders;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioFolders = new ArrayList<>();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback((ItemTouchHelperAdapter) adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.addOnItemTouchListener(new RecyclerItemTouchClickListener(getActivity(),
                new RecyclerItemTouchClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View childView, int position) {
                        AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();

                        AudioFolder audioFolder = audioFolders.get(position);
                        if (audioFolder != null) {
                            audioPlayer.currentAudioFolder = audioFolder;

                            Intent i = new Intent(getActivity(), TrackListActivity.class);
                            i.putExtra(Constants.EXTRA_CONTENT_TYPE, CONTENT_TYPE.FOLDERS);
                            startActivity(i);
                        }
                    }

                    @Override
                    public void onItemLongPress(View childView, int position) {

                    }
                }));


    }


    @Override
    public RecyclerView.Adapter createAdapter() {
        return new AudioFoldersAdapter();
    }

    @Override
    public void fetchAudioContent() {
        getActivity().getSupportLoaderManager().restartLoader(Constants.GET_AUDIO_FOLDERS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case Constants.GET_AUDIO_FOLDERS_LOADER:
                return new CursorLoader(
                        getActivity(),
                        MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                        null,
                        null,
                        null,
                        null

                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "cursor folders " + cursor.getCount());
        List<AudioFolder> audioFolders = new ArrayList<>();
        if (cursor.getCount() > 0) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                AudioFolder audioFolder = new AudioFolder(cursor);
                audioFolders.add(audioFolder);
            }

            if (!audioFolders.isEmpty()) {
                Collections.sort(audioFolders);
                MediaApplication.getInstance().getAudioPlayer().audioFolders = audioFolders;
                ((AudioFoldersAdapter) adapter).refresh(audioFolders);
            }
            hideEmptyContentCard();
        } else {
            showEmptyContentCard();
        }

        checkNeedShowPlayback(audioFolders);
        destroyLoader();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void destroyLoader() {
        getActivity().getSupportLoaderManager().destroyLoader(Constants.GET_AUDIO_FOLDERS_LOADER);
    }

    public class AudioFoldersAdapter extends
            RecyclerView.Adapter<AudioFoldersAdapter.ViewHolder> implements ItemTouchHelperAdapter {


        public class ViewHolder extends RecyclerView.ViewHolder implements
                ItemTouchHelperViewHolder {

            TextView albumName;
            ImageView cover;

            public ViewHolder(View v) {
                super(v);

                albumName = (TextView) v.findViewById(R.id.audioName);
                cover = (ImageView) v.findViewById(R.id.audioCover);

            }

            @Override
            public void onItemSelected() {
                itemView.setAlpha(0.5f);
            }

            @Override
            public void onItemClear(int position) {
                itemView.setAlpha(1.0f);
                updateAudioFolderIndex(position);
                notifyDataSetChanged();
            }
        }

        private void updateAudioFolderIndex(int position) {
            AudioFolder audioFolder = audioFolders.get(position);
            if (audioFolder != null) {
                audioFolder.index = position;
                DatabaseHelper.updateAudioFolderIndex(getActivity(), audioFolder);
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
                BitmapHelper.loadAudioFolderArtwork(getActivity(), audioFolder, holder.cover);

                holder.albumName.setText(audioFolder.folderName);
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
    }
}
