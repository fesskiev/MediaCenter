package com.fesskiev.mediacenter.ui.walkthrough;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.ui.MainActivity;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
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
    private boolean proUseerGranted;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            permissionGranted = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_walkthrough, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = (DisableSwipingViewPager) view.findViewById(R.id.viewpager);
        viewPager.setAdapter(new WalkthroughPagerAdapter(getFragmentManager()));

        InkPageIndicator pageIndicator = (InkPageIndicator) view.findViewById(R.id.indicator);
        pageIndicator.setViewPager(viewPager);

        enterAppButton = (Button) view.findViewById(R.id.enterAppButton);
        enterAppButton.setOnClickListener(v -> {

            AppSettingsManager.getInstance().setFirstStartApp();
            startMainActivity();
        });

        disableEnterButton();

        viewPager.setSwipingEnabled(permissionGranted);
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
        proUseerGranted = true;
        checkEnableEnterButton();
    }

    private void checkEnableEnterButton() {
        if (permissionGranted && fetchMediaGranted && proUseerGranted) {
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
}
