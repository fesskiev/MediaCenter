package com.fesskiev.mediacenter.ui.walkthrough;


import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.ui.MainActivity;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.InkPageIndicator;
import com.fesskiev.mediacenter.widgets.pager.DisableSwipingViewPager;


public class WalkthroughFragment extends Fragment {

    public static WalkthroughFragment newInstance() {
        return new WalkthroughFragment();
    }

    private DisableSwipingViewPager viewPager;
    private Fragment[] fragments;
    private Button enterAppButton;

    private boolean permissionGranted;
    private boolean fetchMediaGranted;
    private boolean proUserGranted;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_walkthrough, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startAnimation(view);

        viewPager = (DisableSwipingViewPager) view.findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);

        enterAppButton = (Button) view.findViewById(R.id.enterAppButton);
        enterAppButton.setOnClickListener(v -> {

            AppSettingsManager.getInstance().setFirstStartApp();
            startMainActivity();
        });

        disableEnterButton();

        if (Utils.isMarshmallow()) {
            fragments = new Fragment[]{
                    PermissionFragment.newInstance(),
                    FetchMediaFragment.newInstance(),
                    ProUserFragment.newInstance()
            };
        } else {
            fragments = new Fragment[]{
                    FetchMediaFragment.newInstance(),
                    ProUserFragment.newInstance()
            };
            BitmapHelper.getInstance().saveDownloadFolderIcon();
            permissionGranted = true;
        }

        viewPager.setAdapter(new WalkthroughPagerAdapter(getFragmentManager()));

        InkPageIndicator pageIndicator = (InkPageIndicator) view.findViewById(R.id.indicator);
        pageIndicator.setViewPager(viewPager);

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("fetchMediaGranted", fetchMediaGranted);
    }

    private void restoreState(Bundle savedInstanceState) {
        fetchMediaGranted = savedInstanceState.getBoolean("fetchMediaGranted");
        checkEnableEnterButton();
    }

    private void startAnimation(View view) {
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.walkRoot);
        AnimationDrawable animationDrawable = (AnimationDrawable) layout.getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();
    }

    private void disableEnterButton() {
        enterAppButton.setAlpha(0.2f);
        enterAppButton.setEnabled(false);
        enterAppButton.setClickable(false);
    }

    private void enableEnterButton() {
        enterAppButton.setAlpha(1.0f);
        enterAppButton.setEnabled(true);
        enterAppButton.setClickable(true);
    }

    public void permissionGranted() {
        permissionGranted = true;
        viewPager.setSwipingEnabled(true);
        checkEnableEnterButton();
    }

    public void fetchMediaGranted() {
        fetchMediaGranted = true;
        checkEnableEnterButton();
    }

    public void proUserGranted() {
        proUserGranted = true;
        checkEnableEnterButton();
    }

    private void checkEnableEnterButton() {
        if (permissionGranted && fetchMediaGranted && proUserGranted) {
            enableEnterButton();
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }


    private class WalkthroughPagerAdapter extends FragmentStatePagerAdapter {

        public WalkthroughPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

    }

    public FetchMediaFragment getFetchMediaFragment() {
        return (FetchMediaFragment) fragments[1];
    }
}
