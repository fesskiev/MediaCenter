package com.fesskiev.player.ui.audio;


import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.R;
import com.fesskiev.player.db.DatabaseHelper;
import com.fesskiev.player.model.Genre;
import com.fesskiev.player.ui.GridFragment;
import com.fesskiev.player.ui.audio.utils.CONTENT_TYPE;
import com.fesskiev.player.ui.audio.utils.Constants;
import com.fesskiev.player.ui.audio.tracklist.TrackListActivity;
import com.fesskiev.player.utils.AppLog;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.RxUtils;

import java.lang.ref.WeakReference;
import java.util.Set;


import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AudioGenresFragment extends GridFragment implements AudioContent {

    public static AudioGenresFragment newInstance() {
        return new AudioGenresFragment();
    }

    private Subscription subscription;

    @Override
    public void fetchAudioContent() {

        subscription = RxUtils.fromCallable(DatabaseHelper.getGenres())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(genres -> {
                    AppLog.INFO("onNext:genres: " + genres.size());
                    if (!genres.isEmpty()) {
                        ((AudioGenresAdapter) adapter).refresh(genres);
                        hideEmptyContentCard();
                    } else {
                        showEmptyContentCard();
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
        return new AudioGenresAdapter(getActivity());
    }


    private static class AudioGenresAdapter extends RecyclerView.Adapter<AudioGenresAdapter.ViewHolder> {

        private WeakReference<Activity> activity;
        private Object[] genres;

        public AudioGenresAdapter(Activity activity) {
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
                    Genre genre = (Genre) genres[getAdapterPosition()];
                    if (genre != null) {
                        Activity act = activity.get();
                        if (act != null) {
                            Intent i = new Intent(act, TrackListActivity.class);
                            i.putExtra(Constants.EXTRA_CONTENT_TYPE, CONTENT_TYPE.GENRE);
                            i.putExtra(Constants.EXTRA_CONTENT_TYPE_VALUE, genre.name);
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
            Genre genre = (Genre) genres[position];
            if (genre != null) {
                holder.genreName.setText(genre.name);
                Activity act = activity.get();
                if (act != null) {
                    if (genre.artworkPath != null) {
                        BitmapHelper.loadURIBitmap(act.getApplicationContext(),
                                genre.artworkPath, holder.cover);
                    } else {
                        BitmapHelper.loadNoCoverFolder(act.getApplicationContext(), holder.cover);
                    }
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
