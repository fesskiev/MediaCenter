package com.fesskiev.mediacenter.ui.splash;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.ui.MainActivity;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;


public class SplashFragment extends Fragment {

    public static SplashFragment newInstance() {
        return new SplashFragment();
    }

    private static final int STARTUP_DELAY = 300;
    private static final int ANIM_ITEM_DURATION = 1000;
    private static final int ITEM_DELAY = 300;

    private DataRepository repository;
    private Subscription subscription;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repository = MediaApplication.getInstance().getRepository();
        FileSystemService.startFileSystemService(getContext().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        animate(view);
    }

    private void animate(View view) {
        ImageView appLogo = (ImageView) view.findViewById(R.id.appLogo);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.container);

        ViewCompat.animate(appLogo)
                .translationY(-Utils.dipToPixels(getContext().getApplicationContext(), 100))
                .setStartDelay(STARTUP_DELAY + 500)
                .setDuration(ANIM_ITEM_DURATION)
                .setInterpolator(new DecelerateInterpolator(1.2f))
                .setListener(new ViewPropertyAnimatorListener() {

                    private boolean ended = true;

                    @Override
                    public void onAnimationStart(View view) {

                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        if (ended) {
                            ended = false;
                            loadMediaFiles();
                        }
                    }

                    @Override
                    public void onAnimationCancel(View view) {

                    }
                }).start();

        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            ViewCompat.animate(v)
                    .alpha(1)
                    .setStartDelay((ITEM_DELAY * i) + 500)
                    .setDuration(1000)
                    .start();
        }
    }

    private void loadMediaFiles() {
        subscription = Observable.zip(
                repository.getAudioFolders(),
                repository.getGenres(),
                repository.getArtists(),
                (audioFolders, genres, artists) -> Observable.just(null))
                .subscribeOn(Schedulers.io())
                .subscribe(o -> startMainActivity());
    }

    private void startMainActivity() {
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
    }

}
