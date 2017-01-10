package com.fesskiev.mediacenter.vk;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.vk.Audio;
import com.fesskiev.mediacenter.data.source.remote.ErrorHelper;
import com.fesskiev.mediacenter.utils.AnimationUtils;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.download.DownloadFile;
import com.fesskiev.mediacenter.widgets.recycleview.HidingScrollListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

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
    private float bottomViewPadding;


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bottomViewPadding = -Utils.dipToPixels(getContext(), 56f);

        requestLayout = (TextInputLayout) view.findViewById(R.id.textInputRequestAudio);
        EditText requestEdit = (EditText) view.findViewById(R.id.requestAudio);
        requestEdit.addTextChangedListener(this);


        searchButton = (FloatingActionButton) view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);

        showViews();

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

        fetchAudio(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
    }

    @Override
    public void onRefresh() {
        fetchAudio(audioOffset);
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
                subscription = MediaApplication.getInstance().getRepository().getSearchMusicFiles(requestString, offset)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(musicFilesResponse -> {
                            updateSearchAudio(musicFilesResponse.getAudioFiles().getMusicFilesList());
                        }, this::checkRequestError);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            hideRefresh();
        }
    }

    private void updateSearchAudio(List<Audio> musicFilesList) {
        if (musicFilesList != null) {
            audioAdapter.refresh(DownloadFile.getDownloadFiles(getActivity(), audioAdapter, musicFilesList));
        }
        hideProgressBar();
        hideRefresh();
    }

    private void checkRequestError(Throwable throwable) {
        ErrorHelper.getInstance().createErrorSnackBar(getActivity(), throwable,
                new ErrorHelper.OnErrorHandlerListener() {
                    @Override
                    public void tryRequestAgain() {
                        fetchAudio(audioOffset);
                    }

                    @Override
                    public void show(Snackbar snackbar) {
                        AnimationUtils.getInstance().translate(searchButton, -snackbar.getView().getHeight());
                    }

                    @Override
                    public void hide(Snackbar snackbar) {
                        AnimationUtils.getInstance().translate(searchButton, bottomViewPadding);
                    }
                });
        hideProgressBar();
        hideRefresh();
    }

    private void hideViews() {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) searchButton.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        AnimationUtils.getInstance().translate(searchButton, searchButton.getHeight()
                + fabBottomMargin);

    }

    private void showViews() {
        AnimationUtils.getInstance().translate(searchButton, bottomViewPadding);
    }
}
