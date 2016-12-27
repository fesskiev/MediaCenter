package com.fesskiev.mediacenter.ui.audio.tracklist;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;

import java.util.List;


public class PlayerTrackListActivity extends AnalyticsActivity {

    private AudioPlayer audioPlayer;
    private List<AudioFile> audioFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_track_list);

        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.activity_window_height, typedValue, true);
        float scaleValue = typedValue.getFloat();

        int height = (int) (getResources().getDisplayMetrics().heightPixels * scaleValue);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height);

        audioPlayer = MediaApplication.getInstance().getAudioPlayer();
        audioFiles = audioPlayer.getCurrentTrackList();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false, 1000));
        recyclerView.setAdapter(new TrackListActivityAdapter());

    }

    private class TrackListActivityAdapter extends RecyclerView.Adapter<TrackListActivityAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView trackNumber;
            TextView duration;
            TextView title;
            TextView filePath;

            public ViewHolder(View v) {
                super(v);
                v.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    if(position != -1){
                        AudioFile audioFile = audioFiles.get(position);
                        if (audioFile != null) {
                            audioPlayer.setCurrentAudioFileAndPlay(audioFile);
                            finish();
                        }
                    }
                });

                trackNumber = (TextView) v.findViewById(R.id.trackNumber);
                duration = (TextView) v.findViewById(R.id.duration);
                title = (TextView) v.findViewById(R.id.title);
                filePath = (TextView) v.findViewById(R.id.filePath);
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
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }
}
