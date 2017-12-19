package com.fesskiev.mediacenter.ui.audio.tracklist;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.ui.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.recycleview.DividerItemDecoration;
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;


public class PlayerTrackListActivity extends AnalyticsActivity {

    private PlayerTrackListViewModel viewModel;
    private TrackListActivityAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_track_list);

        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.activity_window_height, typedValue, true);
        float scaleValue = typedValue.getFloat();

        int height = (int) (getResources().getDisplayMetrics().heightPixels * scaleValue);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height);


        RecyclerView recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false, 1000));
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                R.drawable.divider));
        adapter = new TrackListActivityAdapter();
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);

        observeData();
    }

    private void observeData() {
        viewModel = ViewModelProviders.of(this).get(PlayerTrackListViewModel.class);
        viewModel.getCurrentTrackListLiveData().observe(this, this::refreshAdapter);
    }

    private void refreshAdapter(List<AudioFile> audioFiles) {
        adapter.refreshAdapter(audioFiles);
    }

    private class TrackListActivityAdapter extends RecyclerView.Adapter<TrackListActivityAdapter.ViewHolder> {

        private List<AudioFile> audioFiles;

        public TrackListActivityAdapter() {
            audioFiles = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView trackNumber;
            TextView duration;
            TextView title;
            TextView filePath;

            public ViewHolder(View v) {
                super(v);
                v.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    if (position != -1) {
                        AudioFile audioFile = audioFiles.get(position);
                        if (audioFile != null) {
                            viewModel.setCurrentAudioFileAndPlay(audioFile);
                            finish();
                        }
                    }
                });

                trackNumber = v.findViewById(R.id.trackNumber);
                duration = v.findViewById(R.id.duration);
                title = v.findViewById(R.id.title);
                filePath = v.findViewById(R.id.filePath);
                filePath.setSelected(true);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_player_track_list, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            AudioFile audioFile = audioFiles.get(position);
            if (audioFile != null) {
                holder.trackNumber.setText(String.valueOf(audioFile.trackNumber));
                holder.duration.setText(Utils.getDurationString(audioFile.length));
                holder.title.setText(audioFile.title);
                holder.filePath.setText(audioFile.getFilePath());
            }
        }

        @Override
        public int getItemCount() {
            return audioFiles.size();
        }

        @Override
        public long getItemId(int position) {
            AudioFile audioFile = audioFiles.get(position);
            if (audioFile != null) {
                return audioFile.timestamp;
            }
            return super.getItemId(position);
        }
        
        public void refreshAdapter(List<AudioFile> audioFiles) {
            this.audioFiles.clear();
            this.audioFiles.addAll(audioFiles);
            notifyDataSetChanged();
        }
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }
}
