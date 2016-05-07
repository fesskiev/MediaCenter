package com.fesskiev.player.ui.vk;


import android.support.v4.app.Fragment;

import com.fesskiev.player.R;
import com.fesskiev.player.ui.ViewPagerFragment;


public class MusicVKFragment extends ViewPagerFragment {

    public static MusicVKFragment newInstance() {
        return new MusicVKFragment();
    }

    @Override
    public int getResourceId() {
        return R.layout.fragment_music_vk;
    }

    @Override
    public String[] getTitles() {
        return new String[]{
                getString(R.string.vk_tab_title_user_music),
                getString(R.string.vk_tab_title_groups),
                getString(R.string.vk_tab_title_search)
        };
    }

    @Override
    public int[] getImagesIds() {
        return new int[]{
                R.drawable.tab_wall_icon,
                R.drawable.tab_groups_icon,
                R.drawable.tab_search_icon
        };
    }

    @Override
    public Fragment[] getPagerFragments() {
        return new Fragment[]{
                UserAudioFragment.newInstance(),
                GroupsFragment.newInstance(),
                SearchAudioFragment.newInstance()
        };
    }

}
