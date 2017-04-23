package com.fesskiev.mediacenter.ui.cue;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.services.PlaybackService;
import com.fesskiev.mediacenter.ui.chooser.FileSystemChooserActivity;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.cue.CueParser;
import com.fesskiev.mediacenter.utils.cue.CueSheet;
import com.fesskiev.mediacenter.utils.cue.Index;
import com.fesskiev.mediacenter.utils.cue.TrackData;
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CueActivity extends AnalyticsActivity {

    private CueAdapter adapter;
    private AppSettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cue);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.title_cue_activity));
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false, 1000));
        adapter = new CueAdapter();
        recyclerView.setAdapter(adapter);

        findViewById(R.id.addCueFileFab).setOnClickListener(v -> startChooserActivity());

        settingsManager = AppSettingsManager.getInstance();
        tryLoadCue();
    }

    private void tryLoadCue() {
        String cuePath = settingsManager.getCuePath();
        if (!cuePath.isEmpty()) {
            parseCueFile(cuePath);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == FileSystemChooserActivity.RESULT_CODE_PATH_SELECTED) {
            String cuePath = data.getStringExtra(FileSystemChooserActivity.RESULT_SELECTED_PATH);
            settingsManager.setCuePath(cuePath);
            parseCueFile(cuePath);
        } else if (requestCode == RESULT_CANCELED) {
            finish();
        }
    }

    private void startChooserActivity() {
        Intent intent = new Intent(this, FileSystemChooserActivity.class);
        intent.putExtra(FileSystemChooserActivity.EXTRA_SELECT_TYPE, FileSystemChooserActivity.TYPE_FILE);
        intent.putExtra(FileSystemChooserActivity.EXTRA_EXTENSION, FileSystemChooserActivity.EXTENSION_CUE);
        startActivityForResult(intent, 0);
    }


    private void parseCueFile(String path) {
        try {
            CueSheet cueSheet = CueParser.parse(new File(path));
            List<TrackData> trackDatas = cueSheet.getAllTrackData();
            if (trackDatas != null && !trackDatas.isEmpty()) {
                adapter.refresh(trackDatas);
            }

        } catch (IOException e) {
            e.printStackTrace();
            errorParseCue();
        }
    }

    private void errorParseCue() {
        settingsManager.setCuePath("");
        Utils.showCustomSnackbar(findViewById(R.id.cueRoot), getApplicationContext(),
                getString(R.string.snackbar_cue_parse_error),
                Snackbar.LENGTH_LONG).show();
    }

    private class CueAdapter extends RecyclerView.Adapter<CueAdapter.ViewHolder> {

        private List<TrackData> trackDatas;

        CueAdapter() {
            trackDatas = new ArrayList<>();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView index;
            TextView performer;
            TextView title;

            public ViewHolder(View v) {
                super(v);
                v.setOnClickListener(v1 -> seekToPosition(getAdapterPosition()));

                performer = (TextView) v.findViewById(R.id.itemPerformer);
                index = (TextView) v.findViewById(R.id.itemIndex);
                title = (TextView) v.findViewById(R.id.itemTitle);
            }
        }

        private void seekToPosition(int position) {
            TrackData trackData = trackDatas.get(position);
            if (trackData != null) {
                List<Index> indices = trackData.getIndices();
                if (indices != null && !indices.isEmpty()) {
                    PlaybackService.setPositionPlayback(getApplicationContext(), getTrackDataSecondsPosition(indices));
                }
            }
        }

        private int getTrackDataSecondsPosition(List<Index> indices) {
            if (indices.size() > 1) {
                Index index = indices.get(1);
                return index.getPosition().getTotalFrames() / 75;
            } else {
                Index index = indices.get(0);
                return index.getPosition().getTotalFrames() / 75;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cue, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            TrackData trackData = trackDatas.get(position);
            if (trackData != null) {
                String performer = trackData.getPerformer();
                if (performer != null) {
                    holder.performer.setText(performer);
                } else {
                    holder.performer.setText("");
                }
                String title = trackData.getTitle();
                if (title != null) {
                    holder.title.setText(title);
                } else {
                    holder.title.setText("");
                }

                List<Index> indices = trackData.getIndices();
                if (indices != null && !indices.isEmpty()) {
                    holder.index.setText(Utils.getTimeFromSecondsString(getTrackDataSecondsPosition(indices)));
                } else {
                    holder.index.setText("");
                }
            }
        }

        @Override
        public int getItemCount() {
            return trackDatas.size();
        }

        public void refresh(List<TrackData> trackDatas) {
            this.trackDatas.clear();
            this.trackDatas.addAll(trackDatas);
            notifyDataSetChanged();
        }
    }
}
