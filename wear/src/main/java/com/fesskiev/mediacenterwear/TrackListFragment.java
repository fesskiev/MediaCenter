package com.fesskiev.mediacenterwear;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.wearable.view.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.common.data.CAudioFile;

import java.util.ArrayList;
import java.util.List;

public class TrackListFragment extends Fragment {

    public static Fragment newInstance() {
        return new TrackListFragment();
    }

    private WearableRecyclerView wearableRecyclerView;
    private TrackListAdapter adapter;
    private List<CAudioFile> audioFiles;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioFiles = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_track_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        wearableRecyclerView = (WearableRecyclerView) view.findViewById(R.id.recyclerView);
        wearableRecyclerView.setHasFixedSize(true);
        wearableRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false));
        adapter = new TrackListAdapter();
        wearableRecyclerView.setAdapter(adapter);
        wearableRecyclerView.setCenterEdgeItems(true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public class TrackListAdapter extends WearableRecyclerView.Adapter<TrackListAdapter.ViewHolder> {


        public class ViewHolder extends WearableRecyclerView.ViewHolder {


            public ViewHolder(View view) {
                super(view);

            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_track_list, viewGroup, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }
}
