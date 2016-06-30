package com.fesskiev.player.ui.audio;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fesskiev.player.R;
import com.fesskiev.player.db.DatabaseHelper;
import com.fesskiev.player.model.Artist;
import com.fesskiev.player.ui.GridFragment;
import com.fesskiev.player.ui.audio.utils.CONTENT_TYPE;
import com.fesskiev.player.ui.audio.utils.Constants;
import com.fesskiev.player.ui.audio.tracklist.TrackListActivity;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.RxUtils;
import com.fesskiev.player.widgets.recycleview.RecyclerItemTouchClickListener;

import java.util.Set;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class AudioArtistFragment extends GridFragment implements AudioContent {

    private static final String TAG = AudioArtistFragment.class.getSimpleName();

    public static AudioArtistFragment newInstance() {
        return new AudioArtistFragment();
    }

    private Object[] artists;
    private Subscription subscription;

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
        subscription = RxUtils.fromCallableObservable(DatabaseHelper.getArtists())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Set<Artist>>() {
                    @Override
                    public void onCompleted() {
                        Log.wtf(TAG, "onCompleted:artists:");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.wtf(TAG, "onError:artists:");
                    }

                    @Override
                    public void onNext(Set<Artist> artists) {
                        Log.wtf(TAG, "onNext:artists: " + artists.size());
                        if (!artists.isEmpty()) {
                            ((AudioArtistsAdapter) adapter).refresh(artists);
                            hideEmptyContentCard();
                        } else {
                            showEmptyContentCard();
                        }

                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
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
