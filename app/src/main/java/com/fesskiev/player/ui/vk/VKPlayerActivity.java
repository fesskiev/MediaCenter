package com.fesskiev.player.ui.vk;

import android.content.ComponentName;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.fesskiev.player.R;
import com.fesskiev.player.model.vk.VKMusicFile;

public class VKPlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vkplayer);
        if (savedInstanceState == null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setTitle(getString(R.string.title_player_vk_activity));
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        navigateUpToFromChild(VKPlayerActivity.this,
                                IntentCompat.makeMainActivity(new ComponentName(VKPlayerActivity.this,
                                        MusicVKActivity.class)));
                    }
                });
            }

            VKMusicFile vkMusicFile = getIntent().getExtras().getParcelable(MusicVKFragment.SELECTED_MUSIC_FILE);
            String filePath = getIntent().getExtras().getString(MusicVKFragment.DOWNLOAD_FILE_PATH);


            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, VKPlayerFragment.newInstance(vkMusicFile, filePath),
                    VKPlayerFragment.class.getName());
            transaction.commit();


        }
    }
}
