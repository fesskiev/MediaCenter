package com.fesskiev.player.ui.audio;


import android.content.Intent;
import android.os.Bundle;
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
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.ui.GridFragment;
import com.fesskiev.player.ui.audio.utils.CONTENT_TYPE;
import com.fesskiev.player.ui.audio.utils.Constants;
import com.fesskiev.player.ui.audio.tracklist.TrackListActivity;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.RxUtils;
import com.fesskiev.player.widgets.recycleview.RecyclerItemTouchClickListener;
import com.fesskiev.player.widgets.recycleview.helper.ItemTouchHelperAdapter;
import com.fesskiev.player.widgets.recycleview.helper.ItemTouchHelperViewHolder;
import com.fesskiev.player.widgets.recycleview.helper.SimpleItemTouchHelperCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class AudioFoldersFragment extends GridFragment implements AudioContent {

    private static final String TAG = AudioFoldersFragment.class.getSimpleName();


    public static AudioFoldersFragment newInstance() {
        return new AudioFoldersFragment();
    }


    private List<AudioFolder> audioFolders;
    private Subscription subscription;

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
                            audioPlayer.currentAudioFolder.isSelected = true;
                            DatabaseHelper.updateSelectedAudioFolder(audioFolder);

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
        subscription = RxUtils.fromCallableObservable(DatabaseHelper.getAudioFolders())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<AudioFolder>>() {
                    @Override
                    public void onCompleted() {
                        Log.wtf(TAG, "onCompleted:folders:");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.wtf(TAG, "onError:folders:");
                    }

                    @Override
                    public void onNext(List<AudioFolder> audioFolders) {
                        Log.wtf(TAG, "onNext:folders: " + audioFolders.size());
                        if (!audioFolders.isEmpty()) {
                            Collections.sort(audioFolders);

                            AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();
                            audioPlayer.audioFolders = audioFolders;

                            ((AudioFoldersAdapter) adapter).refresh(audioFolders);
                            hideEmptyContentCard();
                        } else {
                            showEmptyContentCard();
                        }
                        checkNeedShowPlayback(audioFolders);
                    }
                });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
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
                DatabaseHelper.updateAudioFolderIndex(audioFolder);
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
