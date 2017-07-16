package com.fesskiev.mediacenter.ui.audio;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.Genre;
import com.fesskiev.mediacenter.ui.GridFragment;
import com.fesskiev.mediacenter.ui.audio.tracklist.TrackListActivity;
import com.fesskiev.mediacenter.ui.audio.utils.CONTENT_TYPE;
import com.fesskiev.mediacenter.ui.audio.utils.Constants;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.widgets.item.AudioCardView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AudioGenresFragment extends GridFragment implements AudioContent {

    public static AudioGenresFragment newInstance() {
        return new AudioGenresFragment();
    }

    private Subscription subscription;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetch();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (MediaApplication.getInstance().getRepository().getMemorySource().isCacheGenresDirty()) {
            fetch();
        }
    }

    @Override
    public void fetch() {
        subscription = MediaApplication.getInstance().getRepository().getGenres()
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(genres -> {
                    if (genres != null) {
                        AppLog.INFO("onNext:genres: " + genres.size());
                        if (!genres.isEmpty()) {
                            hideEmptyContentCard();
                        } else {
                            showEmptyContentCard();
                        }
                        ((AudioGenresAdapter) adapter).refresh(genres);
                    }
                });
    }

    @Override
    public void clear() {
        ((AudioGenresAdapter) adapter).clearAdapter();
    }


    @Override
    public void onPause() {
        super.onPause();
        RxUtils.unsubscribe(subscription);
    }

    @Override
    public RecyclerView.Adapter createAdapter() {
        return new AudioGenresAdapter(getActivity());
    }


    private static class AudioGenresAdapter extends RecyclerView.Adapter<AudioGenresAdapter.ViewHolder> {

        private WeakReference<Activity> activity;
        private List<Genre> genres;

        public AudioGenresAdapter(Activity activity) {
            this.genres = new ArrayList<>();
            this.activity = new WeakReference<>(activity);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            AudioCardView audioCardView;

            public ViewHolder(View v) {
                super(v);

                audioCardView = (AudioCardView) v.findViewById(R.id.audioCardView);
                audioCardView.setOnAudioCardViewListener(new AudioCardView.OnAudioCardViewListener() {

                    @Override
                    public void onPopupMenuButtonCall(View view) {

                    }

                    @Override
                    public void onOpenTrackListCall() {
                        Genre genre = genres.get(getAdapterPosition());
                        if (genre != null) {
                            Activity act = activity.get();
                            if (act != null) {
                                Intent i = new Intent(act, TrackListActivity.class);
                                i.putExtra(Constants.EXTRA_CONTENT_TYPE, CONTENT_TYPE.GENRE);
                                i.putExtra(Constants.EXTRA_CONTENT_TYPE_VALUE, genre.name);
                                act.startActivity(i);
                            }
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
            Genre genre = genres.get(position);
            if (genre != null) {

                holder.audioCardView.setAlbumName(genre.name);
                BitmapHelper.getInstance().loadAudioGenresFolderArtwork(genre,
                        holder.audioCardView.getCoverView());

                holder.audioCardView.needMenuVisible(false);
            }
        }

        @Override
        public int getItemCount() {
            if (genres != null) {
                return genres.size();
            }
            return 0;
        }

        public void refresh(List<Genre> receiveGenres) {
            genres.clear();
            genres.addAll(receiveGenres);
            notifyDataSetChanged();
        }

        public void clearAdapter() {
            genres.clear();
            notifyDataSetChanged();
        }
    }
}
