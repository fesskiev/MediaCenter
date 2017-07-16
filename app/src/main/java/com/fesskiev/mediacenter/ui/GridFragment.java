package com.fesskiev.mediacenter.ui;

import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.ui.audio.AudioFragment;
import com.fesskiev.mediacenter.ui.playback.HidingPlaybackFragment;
import com.fesskiev.mediacenter.widgets.recycleview.HidingScrollListener;

public abstract class GridFragment extends HidingPlaybackFragment {

    public abstract RecyclerView.Adapter createAdapter();

    protected RecyclerView.Adapter adapter;
    protected RecyclerView recyclerView;
    private CardView emptyAudioContent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_grid, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2,
                GridLayoutManager.VERTICAL, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.foldersGridView);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = createAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                hideTabLayout();
                hidePlaybackControl();
            }

            @Override
            public void onShow() {
                showTabLayout();
                showPlaybackControl();
            }

            @Override
            public void onItemPosition(int position) {

            }
        });

        emptyAudioContent = (CardView) view.findViewById(R.id.emptyAudioContentCard);
    }

    private void hideTabLayout() {
        AudioFragment audioFragment = (AudioFragment) getActivity().getSupportFragmentManager().
                findFragmentByTag(AudioFragment.class.getName());
        if (audioFragment != null) {
            audioFragment.hideTabs();
        }
    }

    private void showTabLayout() {
        AudioFragment audioFragment = (AudioFragment) getActivity().getSupportFragmentManager().
                findFragmentByTag(AudioFragment.class.getName());
        if (audioFragment != null) {
            audioFragment.showTabs();
        }
    }

    protected void showEmptyContentCard() {
        emptyAudioContent.setVisibility(View.VISIBLE);
    }

    protected void hideEmptyContentCard() {
        emptyAudioContent.setVisibility(View.GONE);
    }


    public RecyclerView.Adapter getAdapter() {
        return adapter;
    }
}
