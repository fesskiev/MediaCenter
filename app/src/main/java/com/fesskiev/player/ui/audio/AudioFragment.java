package com.fesskiev.player.ui.audio;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.fesskiev.player.R;
import com.fesskiev.player.db.DatabaseHelper;
import com.fesskiev.player.services.FileSystemIntentService;
import com.fesskiev.player.ui.ViewPagerFragment;
import com.fesskiev.player.ui.audio.utils.Constants;
import com.fesskiev.player.utils.CacheManager;
import com.fesskiev.player.widgets.dialogs.FetchAudioFoldersDialog;

import java.util.List;


public class AudioFragment extends ViewPagerFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static AudioFragment newInstance(boolean isFetchAudio) {
        AudioFragment fragment = new AudioFragment();
        Bundle args = new Bundle();
        args.putBoolean(Constants.EXTRA_IS_FETCH_AUDIO, isFetchAudio);
        fragment.setArguments(args);
        return fragment;
    }

    private SwipeRefreshLayout swipeRefreshLayout;
    private FetchAudioFoldersDialog audioFoldersDialog;
    private boolean isFetchAudio;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isFetchAudio = getArguments().getBoolean(Constants.EXTRA_IS_FETCH_AUDIO);
        }
        setRetainInstance(true);
        registerAudioFolderBroadcastReceiver();
    }

    private void fetchAudioContent() {
        List<Fragment> fragments = getRegisteredFragments();
        for (Fragment fragment : fragments) {
            AudioContent audioContent = (AudioContent) fragment;
            audioContent.fetchAudioContent();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                swipeRefreshLayout.setEnabled(false);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        swipeRefreshLayout.setEnabled(true);
                        break;
                }
                return false;
            }
        });

        if (isFetchAudio) {
            FileSystemIntentService.startFileTreeService(getContext());
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    fetchAudioContent();
                }
            }, 1000);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterAudioFolderBroadcastReceiver();
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
                        isFetchAudio = true;

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
                    audioFoldersDialog = FetchAudioFoldersDialog.newInstance(getActivity());
                    audioFoldersDialog.show();
                    break;
                case FileSystemIntentService.ACTION_END_FETCH_AUDIO:
                    if (audioFoldersDialog != null) {
                        audioFoldersDialog.hide();
                    }

                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    if (isFetchAudio) {
                        fetchAudioContent();
                    }
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

    @Override
    public int getResourceId() {
        return R.layout.fragment_audio;
    }

    @Override
    public String[] getTitles() {
        return new String[]{
                getString(R.string.audio_tab_title_playlist),
                getString(R.string.audio_tab_title_folders),
                getString(R.string.audio_tab_title_artist),
                getString(R.string.audio_tab_title_genres),

        };
    }

    @Override
    public int[] getImagesIds() {
        return new int[]{
                R.drawable.tab_playlist_icon,
                R.drawable.tab_folder_icon,
                R.drawable.tab_artist_icon,
                R.drawable.tab_genre_icon
        };
    }

    @Override
    public Fragment[] getPagerFragments() {
        return new Fragment[]{
                AudioPlaylistFragment.newInstance(),
                AudioFoldersFragment.newInstance(),
                AudioArtistFragment.newInstance(),
                AudioGenresFragment.newInstance(),

        };
    }
}
