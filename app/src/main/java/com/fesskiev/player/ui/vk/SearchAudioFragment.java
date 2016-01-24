package com.fesskiev.player.ui.vk;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.fesskiev.player.R;
import com.fesskiev.player.model.vk.VKMusicFile;
import com.fesskiev.player.services.RESTService;
import com.fesskiev.player.utils.AppSettingsManager;
import com.fesskiev.player.utils.http.URLHelper;
import com.fesskiev.player.widgets.utils.HidingScrollListener;

import java.util.List;


public class SearchAudioFragment extends AudioFragment implements TextWatcher, View.OnClickListener {

    public static SearchAudioFragment newInstance() {
        return new SearchAudioFragment();
    }

    private FloatingActionButton searchButton;
    private TextInputLayout requestLayout;
    private String requestString;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBroadcastReceiver();
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requestLayout = (TextInputLayout) view.findViewById(R.id.textInputRequestAudio);
        EditText requestEdit = (EditText) view.findViewById(R.id.requestAudio);
        requestEdit.addTextChangedListener(this);


        searchButton = (FloatingActionButton) view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);

        recyclerView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                hideViews();
            }

            @Override
            public void onShow() {
                showViews();
            }
        });

    }


    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(RESTService.ACTION_SEARCH_AUDIO_RESULT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(audioReceiver,
                filter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(audioReceiver);
    }

    private BroadcastReceiver audioReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case RESTService.ACTION_SEARCH_AUDIO_RESULT:
                    List<VKMusicFile> vkMusicFiles =
                            intent.getParcelableArrayListExtra(RESTService.EXTRA_AUDIO_RESULT);
                    if (vkMusicFiles != null) {
                        hideProgressBar();
                        downloadVkMusicFiles = getDownloadVKMusicFiles(vkMusicFiles);
                        audioAdapter.refresh(downloadVkMusicFiles);
                    }
                    break;
            }
        }
    };


    @Override
    public void onClick(View v) {
        if (requestString == null || TextUtils.isEmpty(requestString)) {
            requestLayout.setError(getString(R.string.request_error));
            return;
        }
        AppSettingsManager manager = AppSettingsManager.getInstance(getActivity());
        RESTService.fetchSearchAudio(getActivity(),
                URLHelper.getSearchAudioURL(manager.getAuthToken(), requestString, 20, 0));

        showProgressBar();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        requestString = s.toString();
    }

    @Override
    public int getResourceId() {
        return R.layout.fragment_search_audio;
    }

    @Override
    public void fetchAudio() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBroadcastReceiver();
    }

    private void hideViews() {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) searchButton.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        searchButton.animate().translationY(searchButton.getHeight()
                + fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    private void showViews() {
        searchButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }
}
