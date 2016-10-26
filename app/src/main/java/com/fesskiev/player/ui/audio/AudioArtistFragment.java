package com.fesskiev.player.ui.audio;


import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.data.model.Artist;
import com.fesskiev.player.ui.GridFragment;
import com.fesskiev.player.ui.audio.utils.CONTENT_TYPE;
import com.fesskiev.player.ui.audio.utils.Constants;
import com.fesskiev.player.ui.audio.tracklist.TrackListActivity;
import com.fesskiev.player.utils.AppLog;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.RxUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class AudioArtistFragment extends GridFragment {

    public static AudioArtistFragment newInstance() {
        return new AudioArtistFragment();
    }

    private Subscription subscription;


    @Override
    public void onStart() {
        super.onStart();
        fetchArtists();
    }

    public void fetchArtists() {
        subscription = MediaApplication.getInstance().getRepository().getArtists()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(artists -> {
                    if (artists != null) {
                        AppLog.INFO("onNext:artists: " + artists.size());
                        if (!artists.isEmpty()) {
                            ((AudioArtistsAdapter) adapter).refresh(artists);
                            hideEmptyContentCard();
                        } else {
                            showEmptyContentCard();
                        }
                        RxUtils.unsubscribe(subscription);
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
        return new AudioArtistsAdapter(getActivity());
    }

    private static class AudioArtistsAdapter extends RecyclerView.Adapter<AudioArtistsAdapter.ViewHolder> {

        private WeakReference<Activity> activity;
        private List<Artist> artists;

        public AudioArtistsAdapter(Activity activity) {
            this.artists = new ArrayList<>();
            this.activity = new WeakReference<>(activity);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView genreName;
            ImageView cover;

            public ViewHolder(View v) {
                super(v);

                genreName = (TextView) v.findViewById(R.id.audioName);
                cover = (ImageView) v.findViewById(R.id.audioCover);

                v.setOnClickListener(view -> {
                    Artist artist = artists.get(getAdapterPosition());
                    if (artist != null) {
                        Activity act = activity.get();
                        if (act != null) {
                            Intent i = new Intent(act, TrackListActivity.class);
                            i.putExtra(Constants.EXTRA_CONTENT_TYPE, CONTENT_TYPE.ARTIST);
                            i.putExtra(Constants.EXTRA_CONTENT_TYPE_VALUE, artist.name);
                            act.startActivity(i);
                        }
                    }
                });
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
            Artist artist = artists.get(position);
            if (artist != null) {

                holder.genreName.setText(artist.name);
                BitmapHelper.getInstance().loadAudioArtistsFolderArtwork(artist, holder.cover);
            }
        }

        @Override
        public int getItemCount() {
            if (artists != null) {
                return artists.size();
            }
            return 0;
        }

        public void refresh(List<Artist> receiveArtists) {
            artists.clear();
            artists.addAll(receiveArtists);
            notifyDataSetChanged();
        }
    }
}
