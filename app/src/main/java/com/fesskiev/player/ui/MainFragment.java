package com.fesskiev.player.ui;


import android.support.v4.app.Fragment;

import com.fesskiev.player.R;

public class MainFragment extends ViewPagerFragment{

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public String[] getTitles() {
        return new String[]{
                getString(R.string.main_tab_title_audio),
                getString(R.string.main_tab_title_media)
        };
    }

    @Override
    public int[] getImagesIds() {
        return new int[]{
                R.drawable.tab_wall_icon,
                R.drawable.tab_groups_icon
        };
    }

    @Override
    public Fragment[] getPagerFragments() {
        return new Fragment[]{
                MusicFoldersFragment.newInstance(),
                MediaFragment.newInstance()
        };
    }
}
