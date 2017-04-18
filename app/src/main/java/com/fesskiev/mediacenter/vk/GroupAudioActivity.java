package com.fesskiev.mediacenter.vk;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.vk.Group;

public class GroupAudioActivity extends AnalyticsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_audio);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            Group group = getIntent().getExtras().getParcelable(GroupsFragment.GROUP_EXTRA);
            if (group != null) {
                toolbar.setTitle(group.getName());

                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                if (savedInstanceState == null) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.content, GroupAudioFragment.newInstance(group),
                            GroupAudioFragment.class.getName());
                    transaction.commit();
                }
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
