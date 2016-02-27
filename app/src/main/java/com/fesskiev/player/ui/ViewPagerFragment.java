package com.fesskiev.player.ui;


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
import com.fesskiev.player.memory.MemoryLeakWatcherFragment;
import com.fesskiev.player.widgets.utils.DepthPageTransformer;
import com.fesskiev.player.widgets.utils.ZoomOutPageTransformer;

import java.util.ArrayList;
import java.util.List;

public abstract class ViewPagerFragment extends MemoryLeakWatcherFragment {

    public abstract String[] getTitles();

    public abstract int[] getImagesIds();

    public abstract Fragment[] getPagerFragments();

    private ViewPagerAdapter adapter;
    private TabLayout tabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music_vk, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setPageTransformer(true, new DepthPageTransformer());
        viewPager.setOffscreenPageLimit(getPagerFragments().length);
        setupViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            int currentPosition;

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
                    List<TextView> titleTexts = adapter.getTitleTextViews();
                    for (int i = 0; i < titleTexts.size(); i++) {
                        TextView textView = titleTexts.get(i);
                        if (currentPosition == i) {
                            textView.setTextColor(ContextCompat.
                                    getColor(getActivity(), R.color.accent));
                        } else {
                            textView.setTextColor(ContextCompat.
                                    getColor(getActivity(), R.color.white_text));
                        }
                    }
                }
            }
        });

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        createTabs();

    }


    private void createTabs() {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                if (i == 0) {
                    tab.setCustomView(adapter.getTabView(getImagesIds()[i], getTitles()[i],
                            ContextCompat.getColor(getActivity(), R.color.accent)));
                } else {
                    tab.setCustomView(adapter.getTabView(getImagesIds()[i], getTitles()[i],
                            ContextCompat.getColor(getActivity(), R.color.white_text)));
                }

            }
        }
    }


    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getFragmentManager());
        Fragment[] fragments = getPagerFragments();
        for (Fragment fragment : fragments) {
            adapter.addFragment(fragment);
        }
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


    public List<Fragment> getRegisteredFragments() {
        return adapter.getRegisteredFragments();
    }

}
