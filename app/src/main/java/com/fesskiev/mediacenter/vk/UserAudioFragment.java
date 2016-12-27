package com.fesskiev.mediacenter.vk;


import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.vk.data.source.DataRepository;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.download.DownloadFile;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class UserAudioFragment extends RecyclerAudioFragment {


    public static UserAudioFragment newInstance() {
        return new UserAudioFragment();
    }

    private Subscription subscription;

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
        DataRepository repository = DataRepository.getInstance();
        subscription = repository.getUserMusicFiles(offset)
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

    }
}
