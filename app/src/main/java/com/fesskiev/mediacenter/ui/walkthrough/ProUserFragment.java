package com.fesskiev.mediacenter.ui.walkthrough;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.mediacenter.R;


public class ProUserFragment extends Fragment {

    public static ProUserFragment newInstance() {
        return new ProUserFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pro_user, container, false);
    }

}
