package com.fesskiev.mediacenter.ui.audio;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatImageView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.ui.MainActivity;
import com.fesskiev.mediacenter.widgets.dialogs.SimpleDialog;
import com.fesskiev.mediacenter.widgets.swipe.ScrollChildSwipeRefreshLayout;
import com.fesskiev.mediacenter.widgets.utils.DepthPageTransformer;

import java.util.ArrayList;
import java.util.List;

public class AudioFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static AudioFragment newInstance() {
        return new AudioFragment();
    }


    private TabLayout tabLayout;
    private ViewPagerAdapter adapter;
    private ScrollChildSwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager viewPager = view.findViewById(R.id.viewpager);
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
                    List<AppCompatImageView> titleImages = adapter.getTitleImageViews();
                    for (int i = 0; i < titleTexts.size(); i++) {
                        TextView textView = titleTexts.get(i);
                        AppCompatImageView imageView = titleImages.get(i);
                        if (currentPosition == i) {
                            textView.setTextColor(ContextCompat.
                                    getColor(getActivity(), R.color.yellow));
                            imageView.setSupportBackgroundTintList(ColorStateList.valueOf(ContextCompat.
                                    getColor(getActivity(), R.color.yellow)));
                        } else {
                            textView.setTextColor(ContextCompat.
                                    getColor(getActivity(), R.color.white_text));
                            imageView.setSupportBackgroundTintList(ColorStateList.valueOf(ContextCompat.
                                    getColor(getActivity(), R.color.white_text)));
                        }
                    }
                }
            }
        });

        tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        createTabs();

        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.primary_light));
        swipeRefreshLayout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));

        viewPager.setOnTouchListener((v, event) -> {
            swipeRefreshLayout.setEnabled(false);
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    swipeRefreshLayout.setEnabled(true);
                    break;
            }
            return false;
        });
    }

    private void createTabs() {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                if (i == 0) {
                    tab.setCustomView(adapter.getTabView(getImagesIds()[i], getTitles()[i],
                            ContextCompat.getColor(getActivity(), R.color.yellow)));
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

    public void refreshAudioContent() {
        swipeRefreshLayout.setRefreshing(false);

        List<Fragment> fragments = adapter.getRegisteredFragments();
        for (Fragment fragment : fragments) {
            ((AudioContent) fragment).fetch();
        }
    }

    public void clearAudioContent() {
        List<Fragment> fragments = adapter.getRegisteredFragments();
        for (Fragment fragment : fragments) {
            ((AudioContent) fragment).clear();
        }
    }

    @Override
    public void onRefresh() {
        makeRefreshDialog();
    }

    private void makeRefreshDialog() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        SimpleDialog dialog = SimpleDialog.newInstance(getString(R.string.dialog_refresh_folders_title),
                getString(R.string.dialog_refresh_folders_message), R.drawable.icon_refresh);
        dialog.show(transaction, SimpleDialog.class.getName());
        dialog.setPositiveListener(this::checkPermissionAndFetch);
        dialog.setNegativeListener(() -> swipeRefreshLayout.setRefreshing(false));
    }

    private void checkPermissionAndFetch() {
        swipeRefreshLayout.setRefreshing(false);
        ((MainActivity) getActivity()).checkPermissionAndFetchAudio();
    }

    private String[] getTitles() {
        return new String[]{
                getString(R.string.audio_tab_title_albums),
                getString(R.string.audio_tab_title_groups),

        };
    }

    private int[] getImagesIds() {
        return new int[]{
                R.drawable.icon_albums,
                R.drawable.icon_groups,
        };
    }

    private Fragment[] getPagerFragments() {
        return new Fragment[]{
                AudioFoldersFragment.newInstance(),
                AudioGroupsFragment.newInstance(),

        };
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> fragmentList = new ArrayList<>();
        private List<Fragment> registeredFragments = new ArrayList<>();
        private List<TextView> titleTextViews = new ArrayList<>();
        private List<AppCompatImageView> titleImageViews = new ArrayList<>();

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
            registeredFragments.add(fragment);

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

        public List<AppCompatImageView> getTitleImageViews() {
            return titleImageViews;
        }

        public View getTabView(int imageResId, String textTitle, int tabTextColor) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.custom_tab, null);
            TextView tv = v.findViewById(R.id.titleTab);
            tv.setText(textTitle);
            tv.setTextColor(tabTextColor);
            titleTextViews.add(tv);

            AppCompatImageView img = v.findViewById(R.id.imageTab);
            img.setBackgroundResource(imageResId);
            img.setSupportBackgroundTintList(ColorStateList.valueOf(tabTextColor));
            titleImageViews.add(img);
            return v;
        }
    }


    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

}
