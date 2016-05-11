package com.fesskiev.player.ui.audio;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.player.db.MediaCenterProvider;
import com.fesskiev.player.ui.GridFragment;

public class AudioGenresFragment extends GridFragment implements AudioContent, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = AudioGenresFragment.class.getSimpleName();
    private static final int GET_AUDIO_GENRES_LOADER = 1003;

    public static AudioGenresFragment newInstance() {
        return new AudioGenresFragment();
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void fetchAudioContent() {
        getActivity().getSupportLoaderManager().restartLoader(GET_AUDIO_GENRES_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case GET_AUDIO_GENRES_LOADER:
                return new CursorLoader(
                        getActivity(),
                        MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                        new String[]{MediaCenterProvider.TRACK_GENRE},
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
        Log.d(TAG, "cursor genres " + cursor.getCount());
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
           Log.d(TAG, "genre: " +
                   cursor.getString(cursor.getColumnIndex(MediaCenterProvider.TRACK_GENRE)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public RecyclerView.Adapter createAdapter() {
        return new AudioGenresAdapter();
    }


    public class AudioGenresAdapter extends
            RecyclerView.Adapter< AudioGenresAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(View itemView) {
                super(itemView);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

    }
}
