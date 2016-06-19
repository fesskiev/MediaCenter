package com.fesskiev.player.ui.audio;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.fesskiev.player.R;
import com.fesskiev.player.db.DatabaseHelper;
import com.fesskiev.player.services.FileSystemIntentService;
import com.fesskiev.player.ui.ViewPagerFragment;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.CacheManager;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class AudioFragment extends ViewPagerFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static AudioFragment newInstance() {
        return new AudioFragment();
    }

    private SwipeRefreshLayout swipeRefreshLayout;
    private Subscription subscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                swipeRefreshLayout.setEnabled(false);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        swipeRefreshLayout.setEnabled(true);
                        break;
                }
                return false;
            }
        });
    }

    public void fetchAudioContent() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        List<Fragment> fragments = getRegisteredFragments();
        for (Fragment fragment : fragments) {
            AudioContent audioContent = (AudioContent) fragment;
            audioContent.fetchAudioContent(getActivity());
        }
    }

    @Override
    public void onRefresh() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle(getString(R.string.dialog_refresh_folders_title));
        builder.setMessage(R.string.dialog_refresh_folders_message);
        builder.setPositiveButton(R.string.dialog_refresh_folders_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        subscription = getObservable()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(getObserver());

                    }
                });
        builder.setNegativeButton(R.string.dialog_refresh_folders_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        swipeRefreshLayout.setRefreshing(false);
                        dialog.cancel();
                    }
                });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        builder.show();
    }


    private Observable<Boolean> getObservable() {
        return Observable.just(true).map(new Func1<Boolean, Boolean>() {
            @Override
            public Boolean call(Boolean result) {
                CacheManager.clearImagesCache();
                BitmapHelper.saveDownloadFolderIcon(getActivity());
                DatabaseHelper.resetAudioContentDatabase(getActivity());
                return result;
            }
        });
    }

    private Observer<Boolean> getObserver() {
        return new Observer<Boolean>() {

            @Override
            public void onCompleted() {
                FileSystemIntentService.startFetchAudio(getActivity());
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Boolean bool) {

            }
        };
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
                getString(R.string.audio_tab_title_folders),
                getString(R.string.audio_tab_title_artist),
                getString(R.string.audio_tab_title_genres),

        };
    }

    @Override
    public int[] getImagesIds() {
        return new int[]{
                R.drawable.tab_folder_icon,
                R.drawable.tab_artist_icon,
                R.drawable.tab_genre_icon
        };
    }

    @Override
    public Fragment[] getPagerFragments() {
        return new Fragment[]{
                AudioFoldersFragment.newInstance(),
                AudioArtistFragment.newInstance(),
                AudioGenresFragment.newInstance(),

        };
    }

    @Override
    public OnInstantiateItemListener setOnInstantiateItemListener() {
        return new OnInstantiateItemListener() {
            @Override
            public void instantiateItem(int position) {
                if(position == LAST_ITEM_INSTANTIATE){
                    fetchAudioContent();
                }
            }
        };
    }
}
