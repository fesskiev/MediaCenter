package com.fesskiev.player.ui.audio;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.player.R;
import com.fesskiev.player.db.MediaCenterProvider;
import com.fesskiev.player.model.AudioFile;

import java.util.ArrayList;
import java.util.List;


public class AudioTracksFragment extends Fragment implements AudioContent, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = AudioTracksFragment.class.getSimpleName();
    private static final int GET_AUDIO_FILES_LOADER = 1002;

    public static AudioTracksFragment newInstance() {
        return new AudioTracksFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tracks, container, false);
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

    private void destroyLoader(){
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
        }

        destroyLoader();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
