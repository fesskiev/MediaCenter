package com.fesskiev.player.ui.audio.tracklist;

import android.content.ComponentName;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.IntentCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.analytics.AnalyticsActivity;
import com.fesskiev.player.ui.MainActivity;
import com.fesskiev.player.ui.audio.utils.CONTENT_TYPE;
import com.fesskiev.player.ui.audio.utils.Constants;

public class TrackListActivity extends AnalyticsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);
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
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        navigateUpToFromChild(TrackListActivity.this,
                                IntentCompat.makeMainActivity(new ComponentName(TrackListActivity.this,
                                        MainActivity.class)));
                    }
                });

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content, TrackListFragment.newInstance(contentType, title),
                        TrackListFragment.class.getName());
                transaction.commit();

            }
        }
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }
}
