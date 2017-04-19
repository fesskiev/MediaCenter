package com.fesskiev.mediacenter.ui.walkthrough;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.utils.FetchMediaFilesManager;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.fetch.FetchContentView;


public class FetchMediaFragment extends Fragment implements View.OnClickListener {

    public static FetchMediaFragment newInstance() {
        return new FetchMediaFragment();
    }

    private FetchMediaFilesManager fetchMediaFilesManager;

    private boolean fetchMediaGranted;

    private TextView fetchText;
    private Button[] buttons;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileSystemService.startFileSystemService(getContext().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fetch_media, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fetchText = (TextView) view.findViewById(R.id.fetchText);

        buttons = new Button[]{
                (Button) view.findViewById(R.id.fetchMediaButton),
                (Button) view.findViewById(R.id.fetchMediaSkipButton)
        };

        for (Button button : buttons) {
            button.setOnClickListener(this);
        }


        FetchContentView fetchContentView = (FetchContentView) view.findViewById(R.id.fetchContentView);
        fetchMediaFilesManager = new FetchMediaFilesManager(fetchContentView);
        fetchMediaFilesManager.isNeedTimer(true);
        fetchMediaFilesManager.setTextWhite();
        fetchMediaFilesManager.setOnFetchMediaFilesListener(new FetchMediaFilesManager.OnFetchMediaFilesListener() {

            @Override
            public void onFetchMediaPrepare() {
                fetchText.setVisibility(View.GONE);
                hideButtons();
            }

            @Override
            public void onFetchAudioContentStart(boolean clear) {

            }

            @Override
            public void onFetchVideoContentStart(boolean clear) {

            }

            @Override
            public void onFetchMediaContentFinish() {
                fetchMediaFilesSuccess();
            }

            @Override
            public void onAudioFolderCreated() {

            }

            @Override
            public void onVideoFolderCreated() {

            }
        });

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("fetchState", fetchMediaFilesManager.isFetchStart());
        outState.putBoolean("fetchMediaGranted", fetchMediaGranted);
    }

    private void restoreState(Bundle savedInstanceState) {
        fetchMediaGranted = savedInstanceState.getBoolean("fetchMediaGranted");
        if (fetchMediaGranted) {
            hideButtons();
            fetchMediaFilesSuccess();
            return;
        }

        boolean fetchState = savedInstanceState.getBoolean("fetchState");
        fetchMediaFilesManager.setFetchStart(fetchState);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fetchMediaButton:
                fetchMediaFiles();
                break;
            case R.id.fetchMediaSkipButton:
                skipFetchMediaFiles();
                break;
        }
    }

    private void fetchMediaFiles() {
        FileSystemService.startFetchMedia(getContext().getApplicationContext());
    }

    private void skipFetchMediaFiles() {
        fetchMediaGranted = true;
        hideButtons();
        fetchMediaFilesSuccess();
    }


    private void fetchMediaFilesSuccess() {
        fetchMediaGranted = true;

        fetchText.setVisibility(View.VISIBLE);
        fetchText.setText(getString(R.string.search_media_files_success));
        fetchText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_walk_through_ok, 0, 0);
        fetchText.setCompoundDrawablePadding((int) Utils.dipToPixels(getContext().getApplicationContext(), 8));

        notifyFetchMediaGranted();
    }

    private void notifyFetchMediaGranted() {
        WalkthroughFragment walkthroughFragment = (WalkthroughFragment) getFragmentManager().
                findFragmentByTag(WalkthroughFragment.class.getName());
        if (walkthroughFragment != null) {
            walkthroughFragment.fetchMediaGranted();
        }
    }

    private void hideButtons() {
        for (Button button : buttons) {
            button.setVisibility(View.GONE);
        }
    }

    public FetchMediaFilesManager getFetchMediaFilesManager() {
        return fetchMediaFilesManager;
    }

    private void stopFetchFiles() {
        FileSystemService.shouldContinue = false;
    }

    public void stopFetchMedia() {
        if (fetchMediaFilesManager.isFetchStart()) {
            stopFetchFiles();
        }
        fetchMediaFilesManager.unregister();
    }
}
