package com.fesskiev.player.ui.vk;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.R;

import java.util.ArrayList;
import java.util.List;


public class MusicVKFragment extends Fragment {

    public static MusicVKFragment newInstance() {
        return new MusicVKFragment();
    }

    private String[] tabTitles;
    private int[] imageResId;

    private ViewPagerAdapter adapter;
    private TabLayout tabLayout;
    private int currentPosition;
    private int prevPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tabTitles = new String[]{
                getString(R.string.vk_tab_title_user_music),
                getString(R.string.vk_tab_title_groups)
        };
        imageResId = new int[]{
                R.drawable.tab_wall_icon,
                R.drawable.tab_groups_icon
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music_vk, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(2);
        setupViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {


            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (ViewPager.SCROLL_STATE_IDLE == state) {
                    if (currentPosition != prevPosition) {
                        List<TextView> titleTexts = adapter.getTitleTextViews();
                        for (TextView textView : titleTexts) {
                            if (textView.getCurrentTextColor() == ContextCompat.getColor(getActivity(), R.color.accent)) {
                                textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.white_text));
                            } else {
                                textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.accent));
                            }
                        }
                        prevPosition = currentPosition;
                    }
                }
            }
        });

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        createTabs();

    }


//    private void updateTabsText() {
//        List<TextView> titleTexts = adapter.getTitleTextViews();
//        for (int j = 0; j < tabTitles.length; j++) {
//            TextView tv = titleTexts.get(j);
//            tv.setText(tabTitles[j]);
//        }
//    }


    private void createTabs() {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                if (currentPosition == i) {
                    tab.setCustomView(adapter.getTabView(imageResId[i], tabTitles[i],
                            ContextCompat.getColor(getActivity(), R.color.accent)));
                } else {
                    tab.setCustomView(adapter.getTabView(imageResId[i], tabTitles[i],
                            ContextCompat.getColor(getActivity(), R.color.white_text)));
                }

            }
        }
    }


    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getFragmentManager());
        adapter.addFragment(UserAudioFragment.newInstance());
        adapter.addFragment(GroupsFragment.newInstance());
        viewPager.setAdapter(adapter);
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> fragmentList = new ArrayList<>();
        private List<Fragment> registeredFragments = new ArrayList<>();
        private List<TextView> titleTextViews = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment) {
            fragmentList.add(fragment);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.add(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public List<Fragment> getRegisteredFragments() {
            return registeredFragments;
        }


        public List<TextView> getTitleTextViews() {
            return titleTextViews;
        }

        public View getTabView(int imageResId, String textTitle, int tabTextColor) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.custom_tab, null);
            TextView tv = (TextView) v.findViewById(R.id.titleTab);
            tv.setText(textTitle);
            tv.setTextColor(tabTextColor);
            titleTextViews.add(tv);
            ImageView img = (ImageView) v.findViewById(R.id.imageTab);
            img.setImageResource(imageResId);
            return v;
        }
    }


    public UserAudioFragment getUserAudioFragment() {
        List<Fragment> fragments = adapter.getRegisteredFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof UserAudioFragment) {
                return (UserAudioFragment) fragment;
            }
        }
        return null;
    }
}
