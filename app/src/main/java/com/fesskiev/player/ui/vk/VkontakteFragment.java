package com.fesskiev.player.ui.vk;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;

import com.fesskiev.player.R;
import com.fesskiev.player.ui.ViewPagerFragment;

import java.util.List;


public class VkontakteFragment extends ViewPagerFragment {

    public static VkontakteFragment newInstance() {
        return new VkontakteFragment();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitleTextSize(12);
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

    @Override
    public OnInstantiateItemListener setOnInstantiateItemListener() {
        return position -> {
            if (position == LAST_ITEM_INSTANTIATE) {
                new Handler().postDelayed(this::requestVKFiles, 500);
            }
        };
    }

    private void requestVKFiles() {
        VkontakteFragment vkontakteFragment = (VkontakteFragment)
                getActivity().getSupportFragmentManager().
                        findFragmentByTag(VkontakteFragment.class.getName());
        if (vkontakteFragment != null) {
            List<Fragment> registeredFragments = vkontakteFragment.getRegisteredFragments();
            if (registeredFragments != null) {
                for (Fragment fragment : registeredFragments) {
                    if (fragment instanceof RecyclerAudioFragment) {
                        ((RecyclerAudioFragment) fragment).fetchAudio(0);
                    } else if (fragment instanceof GroupsFragment) {
                        ((GroupsFragment) fragment).fetchGroups();
                    }
                }
            }
        }
    }
}
