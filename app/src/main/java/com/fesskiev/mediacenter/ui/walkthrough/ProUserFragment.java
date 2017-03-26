package com.fesskiev.mediacenter.ui.walkthrough;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fesskiev.mediacenter.R;


public class ProUserFragment extends Fragment implements View.OnClickListener  {

    public static ProUserFragment newInstance() {
        return new ProUserFragment();
    }

    private Button[] buttons;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pro_user, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttons = new Button[]{
                (Button) view.findViewById(R.id.buttonRemoveAdMob),
                (Button) view.findViewById(R.id.buttonSkipRemoveAdMob)
        };

        for (Button button : buttons) {
            button.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonRemoveAdMob:
                break;
            case R.id.buttonSkipRemoveAdMob:
                notifyProUserGranted();
                hideButtons();
                break;
        }
    }


    private void notifyProUserGranted() {
        WalkthroughFragment walkthroughFragment = (WalkthroughFragment) getFragmentManager().
                findFragmentByTag(WalkthroughFragment.class.getName());
        if (walkthroughFragment != null) {
            walkthroughFragment.proUserGranted();
        }
    }

    private void hideButtons() {
        for (Button button : buttons) {
            button.setVisibility(View.GONE);
        }
    }
}
