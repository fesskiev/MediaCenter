package com.fesskiev.mediacenter.ui.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.AudioFolder;


public class SearchDataActivity extends AnalyticsActivity {

    public static void startSearchDataActivity(Activity activity, AudioFolder audioFolder) {
        Intent intent = new Intent(activity, SearchDataActivity.class);
        intent.putExtra(EXTRA_AUDIO_FOLDER, audioFolder);
        activity.startActivity(intent);
    }

    private final static String EXTRA_AUDIO_FOLDER = "com.fesskiev.mediacenter.EXTRA_AUDIO_FOLDER";

    private AudioFolder audioFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (savedInstanceState != null) {
            audioFolder = savedInstanceState.getParcelable(EXTRA_AUDIO_FOLDER);
        } else {
            audioFolder = getIntent().getExtras().getParcelable(EXTRA_AUDIO_FOLDER);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_AUDIO_FOLDER, audioFolder);
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }
}
