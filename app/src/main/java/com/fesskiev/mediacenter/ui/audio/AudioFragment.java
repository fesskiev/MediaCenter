package com.fesskiev.mediacenter.ui.audio;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.ui.ViewPagerFragment;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.widgets.dialogs.SimpleDialog;

import java.util.List;

import rx.Subscription;
import rx.schedulers.Schedulers;


public class AudioFragment extends ViewPagerFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static AudioFragment newInstance() {
        return new AudioFragment();
    }

    private SwipeRefreshLayout swipeRefreshLayout;
    private Subscription subscription;
    private DataRepository repository;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = MediaApplication.getInstance().getRepository();

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.primary_light));
        swipeRefreshLayout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));

        viewPager.setOnTouchListener((v, event) -> {
            swipeRefreshLayout.setEnabled(false);
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    swipeRefreshLayout.setEnabled(true);
                    break;
            }
            return false;
        });
    }

    public void refreshAudioContent() {
        swipeRefreshLayout.setRefreshing(false);

        repository.getMemorySource().setCacheArtistsDirty(true);
        repository.getMemorySource().setCacheGenresDirty(true);
        repository.getMemorySource().setCacheFoldersDirty(true);

        List<Fragment> fragments = getRegisteredFragments();
        for (Fragment fragment : fragments) {
            ((AudioContent) fragment).fetch();
        }
    }

    public void clearAudioContent() {
        List<Fragment> fragments = getRegisteredFragments();
        for (Fragment fragment : fragments) {
            ((AudioContent) fragment).clear();
        }
    }


    @Override
    public void onRefresh() {
        makeRefreshDialog();
    }

    private void makeRefreshDialog() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        SimpleDialog dialog = SimpleDialog.newInstance(getString(R.string.dialog_refresh_folders_title),
                getString(R.string.dialog_refresh_folders_message), R.drawable.icon_refresh);
        dialog.show(transaction, SimpleDialog.class.getName());
        dialog.setPositiveListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
            subscription = RxUtils
                    .fromCallable(repository.resetAudioContentDatabase())
                    .subscribeOn(Schedulers.io())
                    .doOnNext(integer -> CacheManager.clearAudioImagesCache())
                    .subscribe(integer -> FileSystemService.startFetchAudio(getActivity()));
        });
        dialog.setNegativeListener(() -> swipeRefreshLayout.setRefreshing(false));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }


    @Override
    public int getResourceId() {
        return R.layout.fragment_audio;
    }

    @Override
    public String[] getTitles() {
        return new String[]{
                getString(R.string.audio_tab_title_albums),
                getString(R.string.audio_tab_title_groups),

        };
    }

    @Override
    public int[] getImagesIds() {
        return new int[]{
                R.drawable.icon_albums,
                R.drawable.icon_groups,
        };
    }

    @Override
    public Fragment[] getPagerFragments() {
        return new Fragment[]{
                AudioFoldersFragment.newInstance(),
                AudioGroupsFragment.newInstance(),

        };
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

}
