package com.fesskiev.mediacenter.ui.cut;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.seekbar.RangeSeekBar;


public class CutMediaActivity extends AnalyticsActivity {

    private AudioFile currentTrack;
    private int startCut;
    private int endCut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut);

        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.activity_window_height, typedValue, true);
        float scaleValue = typedValue.getFloat();

        int height = (int) (getResources().getDisplayMetrics().heightPixels * scaleValue);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height);

        currentTrack = MediaApplication.getInstance().getAudioPlayer().getCurrentTrack();

        RangeSeekBar<Integer> rangeSeekBar = (RangeSeekBar) findViewById(R.id.rangeSeekBar);
        rangeSeekBar.setRangeValues(0, (int) currentTrack.length);

        rangeSeekBar.setOnRangeSeekBarChangeListener((bar, minValue, maxValue) -> {
            startCut = minValue;
            endCut = maxValue;
        });

        findViewById(R.id.cutFileFab).setOnClickListener(v -> processCutFile());
    }

    private void processCutFile() {
        if (endCut != 0) {
            String start = Utils.getDurationString(startCut);
            String end = Utils.getDurationString(endCut);
        }
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }

}
