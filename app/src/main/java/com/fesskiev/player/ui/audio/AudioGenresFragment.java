package com.fesskiev.player.ui.audio;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fesskiev.player.R;
import com.fesskiev.player.db.MediaCenterProvider;
import com.fesskiev.player.model.Genre;
import com.fesskiev.player.ui.GridFragment;
import com.fesskiev.player.ui.audio.utils.CONTENT_TYPE;
import com.fesskiev.player.ui.audio.utils.Constants;
import com.fesskiev.player.ui.tracklist.TrackListActivity;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.widgets.recycleview.RecyclerItemTouchClickListener;

import java.util.Set;
import java.util.TreeSet;

public class AudioGenresFragment extends GridFragment implements AudioContent, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = AudioGenresFragment.class.getSimpleName();

    public static AudioGenresFragment newInstance() {
        return new AudioGenresFragment();
    }

    private Object[] genres;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.addOnItemTouchListener(new RecyclerItemTouchClickListener(getActivity(),
                new RecyclerItemTouchClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View childView, int position) {

                        Genre genre = (Genre) genres[position];
                        if (genre != null) {

                            Intent i = new Intent(getActivity(), TrackListActivity.class);
                            i.putExtra(Constants.EXTRA_CONTENT_TYPE, CONTENT_TYPE.GENRE);
                            i.putExtra(Constants.EXTRA_CONTENT_TYPE_VALUE, genre.name);
                            startActivity(i);
                        }
                    }

                    @Override
                    public void onItemLongPress(View childView, int position) {

                    }
                }));
    }

    @Override
    public void fetchAudioContent() {
        getActivity().getSupportLoaderManager().restartLoader(Constants.GET_AUDIO_GENRES_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case Constants.GET_AUDIO_GENRES_LOADER:
                return new CursorLoader(
                        getActivity(),
                        MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                        new String[]{MediaCenterProvider.TRACK_GENRE, MediaCenterProvider.TRACK_COVER},
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
        if (cursor.getCount() > 0) {

            Set<Genre> genres = new TreeSet<>();

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                genres.add(new Genre(cursor));
            }

            if (!genres.isEmpty()) {
                ((AudioGenresAdapter) adapter).refresh(genres);
            }
            hideEmptyContentCard();
        } else {
            showEmptyContentCard();
        }
        destroyLoader();
    }

    private void destroyLoader() {
        getActivity().getSupportLoaderManager().destroyLoader(Constants.GET_AUDIO_GENRES_LOADER);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public RecyclerView.Adapter createAdapter() {
        return new AudioGenresAdapter();
    }


    public class AudioGenresAdapter extends RecyclerView.Adapter<AudioGenresAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView genreName;
            ImageView cover;

            public ViewHolder(View v) {
                super(v);

                genreName = (TextView) v.findViewById(R.id.audioName);
                cover = (ImageView) v.findViewById(R.id.audioCover);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_audio, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Genre genre = (Genre) genres[position];
            if (genre != null) {
                holder.genreName.setText(genre.name);

                if (genre.artworkPath != null) {
                    BitmapHelper.loadURLBitmap(getContext(), genre.artworkPath, holder.cover);
                } else {
                    Glide.with(getContext()).load(R.drawable.no_cover_icon).into(holder.cover);
                }
            }
        }

        @Override
        public int getItemCount() {
            if (genres != null) {
                return genres.length;
            }
            return 0;
        }

        public void refresh(Set<Genre> receiveGenres) {
            genres = receiveGenres.toArray();
            notifyDataSetChanged();
        }
    }
}
