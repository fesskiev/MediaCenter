package com.fesskiev.player.ui.audio.tracklist;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.analytics.AnalyticsActivity;
import com.fesskiev.player.ui.audio.utils.CONTENT_TYPE;
import com.fesskiev.player.ui.audio.utils.Constants;
import com.fesskiev.player.utils.AnimationUtils;

public class TrackListActivity extends AnalyticsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);
        AnimationUtils.setupWindowAnimations(this);

        if (savedInstanceState == null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            if (toolbar != null) {
                String title = null;
                CONTENT_TYPE contentType =
                        (CONTENT_TYPE) getIntent().getSerializableExtra(Constants.EXTRA_CONTENT_TYPE);
                switch (contentType) {
                    case GENRE:
                    case ARTIST:
                        title = getIntent().getExtras().getString(Constants.EXTRA_CONTENT_TYPE_VALUE);
                        break;
                    case FOLDERS:
                        title = MediaApplication.getInstance().getAudioPlayer().currentAudioFolder.folderName;
                        break;
                    case PLAYLIST:
                        break;
                }

                toolbar.setTitle(title);
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);


                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content, TrackListFragment.newInstance(contentType, title),
                        TrackListFragment.class.getName());
                transaction.commit();

            }
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
}
