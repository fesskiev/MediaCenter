package com.fesskiev.mediacenter.ui.cue;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.ui.chooser.FileSystemChooserActivity;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.cue.CueParser;
import com.fesskiev.mediacenter.utils.cue.CueSheet;
import com.fesskiev.mediacenter.utils.cue.Index;
import com.fesskiev.mediacenter.utils.cue.Position;
import com.fesskiev.mediacenter.utils.cue.TrackData;
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CueActivity extends AnalyticsActivity {

    private CueAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cue);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false, 1000));
        adapter = new CueAdapter();
        recyclerView.setAdapter(adapter);

        startChooserActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == FileSystemChooserActivity.RESULT_CODE_PATH_SELECTED) {
            String cuePath = data.getStringExtra(FileSystemChooserActivity.RESULT_SELECTED_PATH);
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

            for (TrackData trackData : trackDatas) {
                AppLog.ERROR("***********");
                AppLog.ERROR("TrackData");
                AppLog.ERROR("Title: " + trackData.getTitle());
                AppLog.ERROR("Performer: " + trackData.getPerformer());

//                Position pre = trackData.getPostgap();
//                Position post = trackData.getPregap();
//                AppLog.ERROR("Pregap: " + (pre == null ? "null" : pre.toString()));
//                AppLog.ERROR("Postgap: " + (post== null ? "null" : post.toString()));

                List<Index> indexes = trackData.getIndices();
                for (Index index : indexes) {
                    Position position = index.getPosition();
                    AppLog.ERROR("pos: " + position.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }


    private class CueAdapter extends RecyclerView.Adapter<CueAdapter.ViewHolder> {

        private List<TrackData> trackDatas;

        CueAdapter() {
            trackDatas = new ArrayList<>();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(View v) {
                super(v);
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
