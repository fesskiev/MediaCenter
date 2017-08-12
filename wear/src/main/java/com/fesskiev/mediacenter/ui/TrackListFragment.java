package com.fesskiev.mediacenter.ui;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wear.widget.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.common.data.MapAudioFile;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.Utils;

import java.util.ArrayList;

import static com.fesskiev.mediacenter.service.DataLayerListenerService.ACTION_TRACK_LIST;
import static com.fesskiev.mediacenter.service.DataLayerListenerService.EXTRA_TRACK_LIST;

public class TrackListFragment extends Fragment {

    public static Fragment newInstance() {
        return new TrackListFragment();
    }

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

        WearableRecyclerView wearableRecyclerView = view.findViewById(R.id.recyclerView);
        wearableRecyclerView.setHasFixedSize(true);
        wearableRecyclerView.setLayoutManager(new WearableLinearLayoutManager(getActivity().getApplicationContext()));
        adapter = new TrackListAdapter();

        wearableRecyclerView.setAdapter(adapter);
        wearableRecyclerView.setEdgeItemsCenteringEnabled(true);
        wearableRecyclerView.setCircularScrollingGestureEnabled(true);
        wearableRecyclerView.setBezelFraction(0.5f);
        wearableRecyclerView.setScrollDegreesPerScreen(90);
    }

    @Override
    public void onStart() {
        super.onStart();
        registerTrackListReceiver();
    }


    @Override
    public void onPause() {
        super.onPause();
        unregisterTrackListReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void registerTrackListReceiver() {
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
                receiver, new IntentFilter(ACTION_TRACK_LIST));
    }

    private void unregisterTrackListReceiver() {
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<MapAudioFile> audioFiles = intent.getParcelableArrayListExtra(EXTRA_TRACK_LIST);
            if (audioFiles != null) {
                adapter.refreshAdapter(audioFiles);
            }
        }
    };

    public class TrackListAdapter extends WearableRecyclerView.Adapter<TrackListAdapter.ViewHolder> {

        private ArrayList<MapAudioFile> audioFiles;

        public TrackListAdapter() {
            audioFiles = new ArrayList<>();
        }

        public class ViewHolder extends WearableRecyclerView.ViewHolder {

            ImageView cover;
            TextView duration;
            TextView title;


            public ViewHolder(View view) {
                super(view);

                cover  = view.findViewById(R.id.cover);
                duration = view.findViewById(R.id.itemDuration);
                title = view.findViewById(R.id.itemTitle);

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
            MapAudioFile audioFile = audioFiles.get(position);
            if (audioFile != null) {
                Bitmap cover = audioFile.cover;
                if(cover != null){
                    viewHolder.cover.setImageBitmap(cover);
                }
                viewHolder.title.setText(audioFile.title);
                viewHolder.duration.setText(Utils.getDurationString(audioFile.length));
            }
        }

        @Override
        public int getItemCount() {
            return audioFiles.size();
        }

        public void refreshAdapter(ArrayList<MapAudioFile> audioFiles) {
            this.audioFiles.clear();
            this.audioFiles.addAll(audioFiles);
            notifyDataSetChanged();
        }
    }
}
