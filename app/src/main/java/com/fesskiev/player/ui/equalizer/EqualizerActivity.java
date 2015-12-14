package com.fesskiev.player.ui.equalizer;

import android.content.ComponentName;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.fesskiev.player.R;
import com.fesskiev.player.ui.player.PlayerActivity;
import com.fesskiev.player.ui.tracklist.TrackListFragment;

public class EqualizerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);
        if (savedInstanceState == null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setTitle(getString(R.string.title_eq_activity));
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        navigateUpToFromChild(EqualizerActivity.this,
                                IntentCompat.makeMainActivity(new ComponentName(EqualizerActivity.this,
                                        PlayerActivity.class)));
                    }
                });
            }


            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, EqualizerFragment.newInstance(),
                    EqualizerFragment.class.getName());
            transaction.commit();

        }

    }
}
