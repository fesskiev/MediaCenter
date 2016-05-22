package com.fesskiev.player.ui;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.player.R;
import com.fesskiev.player.ui.player.HidingPlaybackFragment;
import com.fesskiev.player.widgets.recycleview.GridDividerDecoration;
import com.fesskiev.player.widgets.recycleview.HidingScrollListener;

public abstract class GridFragment extends HidingPlaybackFragment {

    public abstract RecyclerView.Adapter createAdapter();

    protected RecyclerView.Adapter adapter;
    protected RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_folders, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3,
                GridLayoutManager.VERTICAL, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.foldersGridView);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(new GridDividerDecoration(getActivity()));
        adapter = createAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                hidePlaybackControl();
            }

            @Override
            public void onShow() {
                showPlaybackControl();
            }

            @Override
            public void onItemPosition(int position) {

            }
        });
    }

    public RecyclerView.Adapter getAdapter() {
        return adapter;
    }
}
