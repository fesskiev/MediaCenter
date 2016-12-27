package com.fesskiev.mediacenter.ui.audio;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.ui.GridFragment;
import com.fesskiev.mediacenter.ui.audio.utils.CONTENT_TYPE;
import com.fesskiev.mediacenter.ui.audio.utils.Constants;
import com.fesskiev.mediacenter.ui.audio.tracklist.TrackListActivity;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.widgets.recycleview.helper.ItemTouchHelperAdapter;
import com.fesskiev.mediacenter.widgets.recycleview.helper.ItemTouchHelperViewHolder;
import com.fesskiev.mediacenter.widgets.recycleview.helper.SimpleItemTouchHelperCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class AudioFoldersFragment extends GridFragment {

    public static AudioFoldersFragment newInstance() {
        return new AudioFoldersFragment();
    }

    private Subscription subscription;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback((ItemTouchHelperAdapter) adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }


    @Override
    public RecyclerView.Adapter createAdapter() {
        return new AudioFoldersAdapter(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchAudioFolders();
    }


    public void fetchAudioFolders() {
        subscription = MediaApplication.getInstance().getRepository().getAudioFolders()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioFolders -> {
                    if (audioFolders != null) {
                        AppLog.INFO("onNext:folders: " + audioFolders.size());
                        if (!audioFolders.isEmpty()) {
                            Collections.sort(audioFolders);

                            ((AudioFoldersAdapter) adapter).refresh(audioFolders);
                            hideEmptyContentCard();
                        } else {
                            showEmptyContentCard();
                        }
                        checkNeedShowPlayback(audioFolders);
                        RxUtils.unsubscribe(subscription);
                    }
                });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
    }

    private static class AudioFoldersAdapter extends RecyclerView.Adapter<AudioFoldersAdapter.ViewHolder>
            implements ItemTouchHelperAdapter {

        private WeakReference<Activity> activity;
        private List<AudioFolder> audioFolders;

        public AudioFoldersAdapter(Activity activity) {
            this.activity = new WeakReference<>(activity);
            audioFolders = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

            TextView albumName;
            ImageView cover;

            public ViewHolder(View v) {
                super(v);

                albumName = (TextView) v.findViewById(R.id.audioName);
                cover = (ImageView) v.findViewById(R.id.audioCover);
                v.setOnClickListener(view -> {
                    AudioFolder audioFolder = audioFolders.get(getAdapterPosition());
                    if (audioFolder != null) {

                        MediaApplication.getInstance().getAudioPlayer().setCurrentTrackList(audioFolder);


                        Activity act = activity.get();
                        if (act != null) {
                            Intent i = new Intent(act, TrackListActivity.class);
                            i.putExtra(Constants.EXTRA_CONTENT_TYPE, CONTENT_TYPE.FOLDERS);
                            i.putExtra(Constants.EXTRA_CONTENT_TYPE_VALUE, audioFolder.id);
                            i.putExtra(Constants.EXTRA_AUDIO_FOLDER_TITLE_VALUE, audioFolder.folderName);
                            act.startActivity(i);
                        }
                    }
                });

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
                MediaApplication.getInstance().getRepository().updateAudioFolderIndex(audioFolder);
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
                holder.albumName.setText(audioFolder.folderName);
                BitmapHelper.getInstance().loadAudioFolderArtwork(audioFolder, holder.cover);

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
