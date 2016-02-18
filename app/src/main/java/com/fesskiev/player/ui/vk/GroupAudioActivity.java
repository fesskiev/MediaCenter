package com.fesskiev.player.ui.vk;

import android.content.ComponentName;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.fesskiev.player.R;
import com.fesskiev.player.model.vk.Group;

public class GroupAudioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_audio);
        if (savedInstanceState == null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setTitle(getString(R.string.title_group_music_activity));
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        navigateUpToFromChild(GroupAudioActivity.this,
                                IntentCompat.makeMainActivity(new ComponentName(GroupAudioActivity.this,
                                        MusicVKActivity.class)));
                    }
                });

                Group group = getIntent().getExtras().getParcelable(GroupsFragment.GROUP_EXTRA);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content, GroupAudioFragment.newInstance(group),
                        GroupAudioFragment.class.getName());
                transaction.commit();

            }
        }

    }
}
