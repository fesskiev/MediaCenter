package com.fesskiev.mediacenter.vk;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.vk.Audio;
import com.fesskiev.mediacenter.data.source.remote.ErrorHelper;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.download.DownloadFile;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class UserAudioFragment extends RecyclerAudioFragment {


    public static UserAudioFragment newInstance() {
        return new UserAudioFragment();
    }

    private Subscription subscription;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fetchAudio(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
    }

    @Override
    public int getResourceId() {
        return R.layout.fragment_user_audio;
    }

    @Override
    public void fetchAudio(int offset) {
        showProgressBar();
        subscription = MediaApplication.getInstance().getRepository().getUserMusicFiles(offset)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(musicFilesResponse -> {
                    updateUserAudio(musicFilesResponse.getAudioFiles().getMusicFilesList());
                }, this::checkRequestError);

    }

    private void updateUserAudio(List<Audio> musicFilesList) {
        if (musicFilesList != null && !musicFilesList.isEmpty()) {
            audioAdapter.refresh(DownloadFile.getDownloadFiles(getActivity(),
                    audioAdapter, musicFilesList));
        } else {

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

                    }

                    @Override
                    public void hide(Snackbar snackbar) {

                    }
                });
        hideProgressBar();
        hideRefresh();
    }
}
