package com.fesskiev.mediacenterwear;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.wearable.view.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TrackListFragment extends Fragment {

    private WearableRecyclerView wearableRecyclerView;
    private TrackListAdapter adapter;


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
        wearableRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
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
