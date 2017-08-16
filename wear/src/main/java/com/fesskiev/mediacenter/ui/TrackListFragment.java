package com.fesskiev.mediacenter.ui;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wear.widget.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.common.data.MapAudioFile;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.service.DataLayerService;
import com.fesskiev.mediacenter.utils.Utils;

import java.util.ArrayList;

import static com.fesskiev.common.Constants.CHOOSE_TRACK;

public class TrackListFragment extends Fragment {

    public static TrackListFragment newInstance() {
        return new TrackListFragment();
    }

    private TrackListAdapter adapter;
    private MapAudioFile currentTrack;

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
        wearableRecyclerView.setBezelFraction(1.0f);
        wearableRecyclerView.setScrollDegreesPerScreen(180);

    }

    public void refreshAdapter(ArrayList<MapAudioFile> audioFiles) {
        adapter.refreshAdapter(audioFiles);
    }

    public void updateCurrentTrack(MapAudioFile audioFile) {
        this.currentTrack = audioFile;
        adapter.notifyDataSetChanged();
    }

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
                view.setOnClickListener(v -> handleItemClick(getAdapterPosition()));

                cover = view.findViewById(R.id.cover);
                duration = view.findViewById(R.id.itemDuration);
                title = view.findViewById(R.id.itemTitle);

            }
        }

        private void handleItemClick(int position) {
            MapAudioFile audioFile = audioFiles.get(position);
            if (audioFile != null) {
                DataLayerService.sendChooseTrackMessage(getActivity().getApplicationContext(),
                        CHOOSE_TRACK, audioFile.title);
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
                if (cover != null) {
                    RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory
                            .create(getResources(), cover);
                    drawable.setCircular(true);

                    viewHolder.cover.setImageDrawable(drawable);
                }
                viewHolder.title.setText(audioFile.title);
                viewHolder.duration.setText(Utils.getDurationString(audioFile.length));

                if(currentTrack != null && currentTrack.equals(audioFile)){

                }
            }
        }

        @Override
        public int getItemCount() {
            return audioFiles.size();
        }

        public void refreshAdapter(ArrayList<MapAudioFile> newAudioFiles) {
            audioFiles.clear();
            audioFiles.addAll(newAudioFiles);
            notifyDataSetChanged();
        }
    }
}
