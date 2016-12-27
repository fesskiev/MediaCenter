package com.fesskiev.mediacenter.vk;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.vk.data.source.DataRepository;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.download.DownloadFile;
import com.fesskiev.mediacenter.widgets.recycleview.HidingScrollListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class SearchAudioFragment extends RecyclerAudioFragment implements TextWatcher, View.OnClickListener {

    public static SearchAudioFragment newInstance() {
        return new SearchAudioFragment();
    }

    private FloatingActionButton searchButton;
    private Subscription subscription;
    private TextInputLayout requestLayout;
    private String requestString;


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

            @Override
            public void onItemPosition(int position) {

            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
    }

    @Override
    public void onClick(View v) {
        if (requestString == null || TextUtils.isEmpty(requestString)) {
            requestLayout.setError(getString(R.string.request_error));
            return;
        }
        audioOffset += 20;
        fetchAudio(audioOffset);

        showProgressBar();
        Utils.hideKeyboard(getActivity());
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
        if (!TextUtils.isEmpty(requestString)) {
            requestLayout.setErrorEnabled(false);
        }
    }

    @Override
    public int getResourceId() {
        return R.layout.fragment_search_audio;
    }

    @Override
    public void fetchAudio(int offset) {
        if (requestString != null) {
            try {
                String encodeString = URLEncoder.encode(requestString, "UTF-8");

                showProgressBar();
                DataRepository repository = DataRepository.getInstance();
                subscription = repository.getSearchMusicFiles(requestString, offset)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(musicFilesResponse -> {
                            hideProgressBar();
                            if (musicFilesResponse != null) {
                                audioAdapter.refresh(DownloadFile.
                                        getDownloadFiles(getActivity(), audioAdapter,
                                                musicFilesResponse.getAudioFiles().getMusicFilesList()));
                            }
                        }, throwable -> {
                            hideProgressBar();
                            AppLog.ERROR(throwable.getMessage());
                        });

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
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
