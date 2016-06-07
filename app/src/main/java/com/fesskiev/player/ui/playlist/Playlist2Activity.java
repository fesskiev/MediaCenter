package com.fesskiev.player.ui.playlist;

import android.content.ComponentName;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.fesskiev.player.R;
import com.fesskiev.player.ui.MainActivity;

public class Playlist2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        if (savedInstanceState == null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setTitle(getString(R.string.title_playlist_activity));
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        navigateUpToFromChild(Playlist2Activity.this,
                                IntentCompat.makeMainActivity(new ComponentName(Playlist2Activity.this,
                                        MainActivity.class)));
                    }
                });
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, Playlist2Fragment.newInstance(),
                    Playlist2Fragment.class.getName());
            transaction.commit();

        }
    }
}
