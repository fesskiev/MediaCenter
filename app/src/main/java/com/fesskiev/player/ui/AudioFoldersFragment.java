package com.fesskiev.player.ui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.db.DatabaseHelper;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.services.FileObserverService;
import com.fesskiev.player.services.FileSystemIntentService;
import com.fesskiev.player.ui.tracklist.TrackListActivity;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.CacheManager;
import com.fesskiev.player.widgets.dialogs.FetchAudioFoldersDialog;
import com.fesskiev.player.widgets.recycleview.RecyclerItemTouchClickListener;
import com.fesskiev.player.widgets.recycleview.helper.ItemTouchHelperAdapter;
import com.fesskiev.player.widgets.recycleview.helper.ItemTouchHelperViewHolder;
import com.fesskiev.player.widgets.recycleview.helper.SimpleItemTouchHelperCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AudioFoldersFragment extends GridFragment {

    public interface OnAttachFolderFragmentListener {
        void onAttachFolderFragment();
    }

    private static final String TAG = AudioFoldersFragment.class.getSimpleName();

    public static AudioFoldersFragment newInstance() {
        return new AudioFoldersFragment();
    }

    private OnAttachFolderFragmentListener attachFolderFragmentListener;
    private FetchAudioFoldersDialog audioFoldersDialog;
    private ItemTouchHelper itemTouchHelper;
    private List<AudioFolder> audioFolders;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        attachFolderFragmentListener = (OnAttachFolderFragmentListener) context;
        audioFolders = new ArrayList<>();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerAudioFolderBroadcastReceiver();

        if (attachFolderFragmentListener != null) {
            attachFolderFragmentListener.onAttachFolderFragment();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback((ItemTouchHelperAdapter) adapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.addOnItemTouchListener(new RecyclerItemTouchClickListener(getActivity(),
                new RecyclerItemTouchClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View childView, int position) {
                        AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();

                        AudioFolder audioFolder = audioFolders.get(position);
                        if (audioFolder != null) {
                            audioPlayer.currentAudioFolder = audioFolder;
                            startActivity(new Intent(getActivity(), TrackListActivity.class));
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
    public void onRefresh() {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle(getString(R.string.dialog_refresh_folders_title));
        builder.setMessage(R.string.dialog_refresh_folders_message);
        builder.setPositiveButton(R.string.dialog_refresh_folders_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        CacheManager.clearImagesCache();
                        DatabaseHelper.resetDatabase(getActivity());
                        FileSystemIntentService.startFileTreeService(getActivity());

                    }
                });
        builder.setNegativeButton(R.string.dialog_refresh_folders_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        swipeRefreshLayout.setRefreshing(false);
                        dialog.cancel();
                    }
                });
        builder.show();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterAudioFolderBroadcastReceiver();
    }

    private void registerAudioFolderBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FileSystemIntentService.ACTION_START_FETCH_AUDIO);
        intentFilter.addAction(FileSystemIntentService.ACTION_END_FETCH_AUDIO);
        intentFilter.addAction(FileSystemIntentService.ACTION_AUDIO_FOLDER_NAME);
        intentFilter.addAction(FileSystemIntentService.ACTION_AUDIO_TRACK_NAME);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(audioFolderReceiver,
                intentFilter);
    }

    private void unregisterAudioFolderBroadcastReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(audioFolderReceiver);
    }

    private BroadcastReceiver audioFolderReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case FileSystemIntentService.ACTION_START_FETCH_AUDIO:
                    Log.w(TAG, FileSystemIntentService.ACTION_START_FETCH_AUDIO);
                    audioFoldersDialog = new FetchAudioFoldersDialog(getActivity());
                    audioFoldersDialog.show();
                    break;
                case FileSystemIntentService.ACTION_END_FETCH_AUDIO:
                    if (audioFoldersDialog != null) {
                        audioFoldersDialog.hide();
                    }

                    List<AudioFolder> receiverAudioFolders =
                            MediaApplication.getInstance().getAudioPlayer().audioFolders;
                    if (receiverAudioFolders != null && !receiverAudioFolders.isEmpty()) {
                        ((AudioFoldersAdapter) adapter).refresh(receiverAudioFolders);
                    }
                    swipeRefreshLayout.setRefreshing(false);

                    FileObserverService.startFileObserverService(getActivity());

                    break;
                case FileSystemIntentService.ACTION_AUDIO_FOLDER_NAME:
                    String folderName =
                            intent.getStringExtra(FileSystemIntentService.EXTRA_AUDIO_FOLDER_NAME);
                    if (audioFoldersDialog != null) {
                        audioFoldersDialog.setFolderName(folderName);
                    }
                    break;
                case FileSystemIntentService.ACTION_AUDIO_TRACK_NAME:
                    String trackName =
                            intent.getStringExtra(FileSystemIntentService.EXTRA_AUDIO_TRACK_NAME);
                    if (audioFoldersDialog != null) {
                        audioFoldersDialog.setAudioTrackName(trackName);
                    }
                    break;
            }
        }
    };


    public class AudioFoldersAdapter extends RecyclerView.Adapter<AudioFoldersAdapter.ViewHolder> implements ItemTouchHelperAdapter {


        public class ViewHolder extends RecyclerView.ViewHolder implements
                ItemTouchHelperViewHolder {

            TextView albumName;
            ImageView cover;

            public ViewHolder(View v) {
                super(v);

                albumName = (TextView) v.findViewById(R.id.albumName);
                cover = (ImageView) v.findViewById(R.id.folderCover);

            }

            @Override
            public void onItemSelected() {
                itemView.setAlpha(0.5f);
            }

            @Override
            public void onItemClear() {
                itemView.setAlpha(1.0f);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_audio_folder, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            Log.d(TAG, "item move from: " + fromPosition + " to: " + toPosition);

            Collections.swap(audioFolders, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onItemDismiss(int position) {

        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            Log.w(TAG, "notify!");
                            notifyDataSetChanged();
                            break;
                        case MotionEvent.ACTION_DOWN:
                            Log.w(TAG, "move!");
                            itemTouchHelper.startDrag(holder);
                            return true;
                    }


                    return true;
                }
            });

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
