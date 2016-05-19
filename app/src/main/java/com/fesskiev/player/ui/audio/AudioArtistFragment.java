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
import com.fesskiev.player.model.Artist;
import com.fesskiev.player.model.Genre;
import com.fesskiev.player.ui.GridFragment;
import com.fesskiev.player.ui.audio.utils.CONTENT_TYPE;
import com.fesskiev.player.ui.audio.utils.Constants;
import com.fesskiev.player.ui.tracklist.TrackListActivity;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.widgets.recycleview.RecyclerItemTouchClickListener;

import java.util.Set;
import java.util.TreeSet;


public class AudioArtistFragment extends GridFragment implements AudioContent, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = AudioArtistFragment.class.getSimpleName();

    public static AudioArtistFragment newInstance() {
        return new AudioArtistFragment();
    }

    private Object[] artists;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.addOnItemTouchListener(new RecyclerItemTouchClickListener(getActivity(),
                new RecyclerItemTouchClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View childView, int position) {

                        Artist artist = (Artist) artists[position];
                        if (artist != null) {

                            Intent i = new Intent(getActivity(), TrackListActivity.class);
                            i.putExtra(Constants.EXTRA_CONTENT_TYPE, CONTENT_TYPE.ARTIST);
                            i.putExtra(Constants.EXTRA_CONTENT_TYPE_VALUE, artist.name);
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
        getActivity().getSupportLoaderManager().restartLoader(Constants.GET_AUDIO_ARTIST_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case Constants.GET_AUDIO_ARTIST_LOADER:
                return new CursorLoader(
                        getActivity(),
                        MediaCenterProvider.AUDIO_TRACKS_TABLE_CONTENT_URI,
                        new String[]{MediaCenterProvider.TRACK_ARTIST, MediaCenterProvider.TRACK_COVER},
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
        Log.d(TAG, "cursor artists " + cursor.getCount());
        if (cursor.getCount() > 0) {
            Set<Artist> artists = new TreeSet<>();

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                artists.add(new Artist(cursor));
            }

            if (!artists.isEmpty()) {
                ((AudioArtistsAdapter) adapter).refresh(artists);
            }
        }
        destroyLoader();
    }

    private void destroyLoader() {
        getActivity().getSupportLoaderManager().destroyLoader(Constants.GET_AUDIO_ARTIST_LOADER);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public RecyclerView.Adapter createAdapter() {
        return new AudioArtistsAdapter();
    }

    public class AudioArtistsAdapter extends RecyclerView.Adapter<AudioArtistsAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView genreName;
            ImageView cover;

            public ViewHolder(View v) {
                super(v);

                genreName = (TextView) v.findViewById(R.id.genreName);
                cover = (ImageView) v.findViewById(R.id.genreCover);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_genre, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Artist artist = (Artist) artists[position];
            if (artist != null) {
                holder.genreName.setText(artist.name);

                if (artist.artworkPath != null) {
                    BitmapHelper.loadURLBitmap(getContext(), artist.artworkPath, holder.cover);
                } else {
                    Glide.with(getContext()).load(R.drawable.no_cover_icon).into(holder.cover);
                }
            }
        }

        @Override
        public int getItemCount() {
            if (artists != null) {
                return artists.length;
            }
            return 0;
        }

        public void refresh(Set<Artist> receiveArtists) {
            artists = receiveArtists.toArray();
            notifyDataSetChanged();
        }
    }
}
