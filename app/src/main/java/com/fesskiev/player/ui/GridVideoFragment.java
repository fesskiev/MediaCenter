package com.fesskiev.player.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.memory.MemoryLeakWatcherFragment;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.widgets.gridview.HidingScrollListener;

public abstract class GridVideoFragment extends MemoryLeakWatcherFragment implements SwipeRefreshLayout.OnRefreshListener {

    public abstract BaseAdapter getAdapter();


    protected SwipeRefreshLayout swipeRefreshLayout;
    protected BaseAdapter adapter;
    protected GridView gridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_folders, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        gridView = (GridView) view.findViewById(R.id.foldersGridView);
        adapter = getAdapter();
        gridView.setAdapter(adapter);

        gridView.setOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                hidePlaybackControl();
            }

            @Override
            public void onShow() {
                showPlaybackControl();
            }
        });
    }

    private void hidePlaybackControl() {
        AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        if (audioPlayer.isPlaying) {
            ((MainActivity) getActivity()).hidePlaybackControl();
        }
    }

    private void showPlaybackControl() {
        AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        if (audioPlayer.isPlaying) {
            ((MainActivity) getActivity()).showPlaybackControl();
        }
    }
}
